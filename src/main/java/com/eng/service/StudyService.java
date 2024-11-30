package com.eng.service;

import com.eng.domain.*;
import com.eng.dto.StudyDto;
import com.eng.dto.StudyResponseDto;
import com.eng.exception.notfound.UserNotFoundException;
import com.eng.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyService {
    private final UserRepository userRepository;
    private final MeanRepository meanRepository;
    private final StudyRepository studyRepository;
    private final SentenceRepository sentenceRepository;
    private final RedisService redisService;
    private final WordRepository wordRepository;
    private final JdbcRepository jdbcRepository;

    // 단어 가져오기(Study 테이블 저장 제거)
    @Cacheable(key = "#username", value = "getStudyWord", unless = "#result==null", cacheManager = "cacheManager")
    public List<StudyResponseDto> getStudyWord(String username) {
        List<StudyResponseDto> list = new ArrayList<>();
        List<StudyDto> studyList = new ArrayList<>();
        LocalDate date = studyRepository.findLastDay();
        LocalDate today = LocalDate.now();
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        if(date==null || !date.isEqual(today)){ // 오늘 날짜가 아니라면 학습하지 않은 데이터 10개 조회
            findNotInStudy(user, list, 10, studyList, today);
        }else{ // 오늘 날짜라면 Study 테이블에서 조회
            List<Study> study = studyRepository.findLastDayForStudy(today); // 오늘날짜에 학습한 데이터 조회
            for(Study st : study){
                // 단어에 해당하는 예문이 중복일 수 있으므로 첫번째 조회
                Sentence sentence = sentenceRepository.findBySentence(st.getMeaning().getId()).get(0);
                list.add(StudyResponseDto.of(
                        st.getWord().getWord(),
                        st.getMeaning().getMeaning(),
                        sentence.getSentence(),
                        sentence.getSentence_meaning(),
                        sentence.getLevel()
                ));
            }
            if(study.size()<10){ // 10개의 단어 중 학습하지 않은 일부 단어들 list에 추가적으로 저장
                findNotInStudy(user, list, 10-study.size(), studyList,today);
            }
        }
        //redisService.addStudyList(username, studyList);
        return list;
    }

    // 학습할 10개의 단어 중 Study에 저장되어있지 않은 데이터 추가적으로 저장
    private void findNotInStudy (User user, List<StudyResponseDto> list, int len, List<StudyDto> studyList, LocalDate date){
        Pageable pageable = PageRequest.of(0, len);
        Page<Object[]> results = meanRepository.findByMeanForStudyWithSentence(user.getId(), pageable);
        for (Object[] result : results) {
            Meaning meaning = (Meaning) result[0];
            Sentence sentence = (Sentence) result[1];
            studyList.add(StudyDto.of(user.getId(),meaning.getWord().getId(),meaning.getId(),sentence.getId(),date));
            list.add(StudyResponseDto.of(
                    meaning.getWord().getWord(),
                    meaning.getMeaning(),
                    sentence.getSentence(),
                    sentence.getSentence_meaning(),
                    sentence.getLevel()
            ));
        }
    }

    public void saveStudyWord(String username, int maxPage){
        LocalDate today = LocalDate.now();
        List<StudyDto> cachedStudyList = redisService.getStudyList(username);
        Integer lastPage = redisService.getMaxPage(username);
        int startIndex = (lastPage != null) ? lastPage : studyRepository.countLastDayForStudy(today);
        if(maxPage<startIndex){
            return;
        }
        if(cachedStudyList!=null && !cachedStudyList.isEmpty()){ // 캐시가 있을 경우
            int endIndex;
            List<StudyDto> studiesToSaveDto;
            List<StudyDto> remainingCache;
            if(lastPage==null) { // 처음 저장 시
                // 저장할 데이터와 Redis에 남길 데이터를 분리
                endIndex = Math.min(maxPage + 1 - startIndex, cachedStudyList.size());
                studiesToSaveDto = new ArrayList<>(cachedStudyList.subList(0, endIndex));
                remainingCache = new ArrayList<>(cachedStudyList.subList(endIndex, cachedStudyList.size()));
            } else{ // 이후 나머지 데이터 계산
                endIndex = Math.min(maxPage-startIndex, cachedStudyList.size());
                studiesToSaveDto = new ArrayList<>(cachedStudyList.subList(0, endIndex));
                remainingCache = new ArrayList<>(cachedStudyList.subList(endIndex, cachedStudyList.size()));
            }
            // DTO -> Entity 변환
            List<Study> studiesToSave = studiesToSaveDto.stream()
                    .map(dto -> Study.createStudy(
                            userRepository.getReferenceById(dto.getUserId()),
                            wordRepository.getReferenceById(dto.getWordId()),
                            meanRepository.getReferenceById(dto.getMeaningId()),
                            sentenceRepository.getReferenceById(dto.getSentenceId()),
                            dto.getDate()
                    ))
                    .toList();
            // Study 테이블에 저장
            studyRepository.saveAll(studiesToSave);

            List<Quiz> quizList = studiesToSave.stream()
                    .map(Quiz::addQuiz)
                            .toList();
            // Quiz 저장
            jdbcRepository.batchInsert(quizList);

            // Redis Cache 업데이트: 학습하지 않은 데이터만 갱신
            redisService.addStudyList(username, remainingCache);

            // Redis 상태 업데이트
            redisService.addMaxPage(username, maxPage);
        }
        else {
            // 캐시가 없을 경우 데이터 생성
            generateStudyList(username, maxPage+1, startIndex, today);
        }
    }

    private void generateStudyList(String username, int maxPage, int startPage, LocalDate today) {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        Pageable pageable = PageRequest.of(0, 10-startPage);
        Page<Object[]> results = meanRepository.findByMeanForStudyWithSentence(user.getId(), pageable);
        List<StudyDto> totalStudy = new ArrayList<>();
        for (Object[] result : results) {
            Meaning meaning = (Meaning) result[0];
            Sentence sentence = (Sentence) result[1];
            Word word = meaning.getWord();
            totalStudy.add(StudyDto.of(user.getId(), word.getId(), meaning.getId(), sentence.getId(), today));
        }
        List<StudyDto> addStudy  = new ArrayList<>(totalStudy.subList(0, maxPage-startPage));
        List<StudyDto> addCache = new ArrayList<>(totalStudy.subList(maxPage - startPage, totalStudy.size()));

        // DB 저장
        List<Study> studiesToSave = addStudy.stream()
                .map(dto -> Study.createStudy(
                        userRepository.getReferenceById(dto.getUserId()),
                        wordRepository.getReferenceById(dto.getWordId()),
                        meanRepository.getReferenceById(dto.getMeaningId()),
                        sentenceRepository.getReferenceById(dto.getSentenceId()),
                        dto.getDate()
                ))
                .toList();
        studyRepository.saveAll(studiesToSave);

        List<Quiz> quizList = studiesToSave.stream()
                .map(Quiz::addQuiz)
                .toList();
        jdbcRepository.batchInsert(quizList);

        // Redis cache 저장
        redisService.addStudyList(user.getUsername(), addCache);
        redisService.addMaxPage(username, maxPage-1);
    }
}
