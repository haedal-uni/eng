package com.eng.service;

import com.eng.domain.Word;
import com.eng.dto.MeaningDto;
import com.eng.repository.MeanRepository;
import com.eng.repository.WordRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class SearchService {
    private final WordRepository wordRepository;
    private final MeanRepository meanRepository;
    private final String method = "getWordRedis:";

    @Resource(name="addWordRedisTemplate")
    private final RedisTemplate<String, List<String>> redisTemplate;

    public void addWordRedis() {
        List<Word> all = wordRepository.findAll();
        for(Word word : all){
            List<MeaningDto> meaning = meanRepository.findByWordApplicableMean(word.getId());
            List<String> meaningList = meaning.stream()
                            .map(MeaningDto::getMeaning)
                    .toList();
            redisTemplate.opsForValue().set(method + word.getWord(), meaningList);
        }
    }

    public List<Map<String, List<String>>> getWordRedis(String x) {
        RedisConnection connection = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection();

        // startsWith 조회
        Set<String> startKeySet = new HashSet<>();
        try (Cursor<byte[]> cursor = ((RedisKeyCommands) connection).scan(
                ScanOptions.scanOptions().match(method + x + "*").count(30).build())) {
            while (cursor.hasNext()) {
                startKeySet.add(new String(cursor.next()));
                if (startKeySet.size() >= 5) break;
            }
        }

        // endsWith 조회
        Set<String> endKeySet = new HashSet<>();
        try (Cursor<byte[]> cursor = ((RedisKeyCommands) connection).scan(
                ScanOptions.scanOptions().match(method + "*" + x).count(30).build())) {
            while (cursor.hasNext()) {
                endKeySet.add(new String(cursor.next()));
                if (endKeySet.size() >= 3) break;
            }
        }

        // start와 end 결과값 가져오기
        Map<String, List<String>> start = new HashMap<>();
        Map<String, List<String>> end = new HashMap<>();

        for (String key : startKeySet) {
            List<String> values = redisTemplate.opsForValue().get(key);
            if (values != null) {
                start.put(key.replace(method, ""), values); // 네임스페이스 제거
            }
        }

        for (String key : endKeySet) {
            List<String> values = redisTemplate.opsForValue().get(key);
            if (values != null) {
                end.put(key.replace(method, ""), values); // 네임스페이스 제거
            }
        }
        return Arrays.asList(start, end); // List<Map>으로 반환
    }
}