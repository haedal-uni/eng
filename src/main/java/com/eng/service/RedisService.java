package com.eng.service;

import com.eng.dto.StudyDto;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    @Resource(name = "maxPageRedisTemplate")
    private final RedisTemplate<String, Integer> numTemplate;

    @Resource(name="studyWordsRedisTemplate")
    private final RedisTemplate<String, List<StudyDto>> redisTemplate;

    public void addStudyList(String username, List<StudyDto> dto){
        LocalTime midnight = LocalTime.MIDNIGHT;
        Duration ttl = Duration.between(LocalDateTime.now(), LocalDateTime.now().toLocalDate().atTime(midnight).plusDays(1));
        redisTemplate.opsForValue().set(username + "list", dto);
        redisTemplate.expire(username + "list", ttl.getSeconds(), TimeUnit.SECONDS);
    }

    public List<StudyDto> getStudyList(String username){
        return redisTemplate.opsForValue().get(username + "list");
    }

    public void addMaxPage(String username, int maxPage){
        LocalTime midnight = LocalTime.MIDNIGHT;
        Duration ttl = Duration.between(LocalDateTime.now(), LocalDateTime.now().toLocalDate().atTime(midnight).plusDays(1));
        numTemplate.opsForValue().set(username+"page", maxPage);
        numTemplate.expire(username+"page",ttl.getSeconds(), TimeUnit.SECONDS);
    }

    public Integer getMaxPage(String username){
        return numTemplate.opsForValue().get(username+"page");
    }
}
