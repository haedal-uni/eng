package com.eng.service;

import com.eng.domain.Meaning;
import com.eng.domain.Sentence;
import com.eng.domain.Study;
import com.eng.domain.User;
import com.eng.dto.StudyResponseDto;
import com.eng.exception.notfound.UserNotFoundException;
import com.eng.repository.MeanRepository;
import com.eng.repository.SentenceRepository;
import com.eng.repository.StudyRepository;
import com.eng.repository.UserRepository;
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

    // 단어 가져오기
    @Cacheable(key="#username", value = "getStudyWord", unless = "#result==null", cacheManager = "cacheManager")
    public List<StudyResponseDto> getStudyWord(String username) {
        List<Study> studyList = new ArrayList<>();
        List<StudyResponseDto> list = new ArrayList<>();
        LocalDate date = studyRepository.findLastDay();
        LocalDate today = LocalDate.now();
        if(date==null || !date.isEqual(today)){ // 오늘 날짜가 아니라면 학습하지 않은 데이터 10개 조회
            User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Object[]> results = meanRepository.findByMeanForStudyWithSentence(user.getId(), pageable);
            for (Object[] result : results) {
                Meaning meaning = (Meaning) result[0];
                Sentence sentence = (Sentence) result[1];
                studyList.add(Study.createStudy(user,meaning.getWord(), meaning, today));
                list.add(StudyResponseDto.of(
                        meaning.getWord().getWord(),
                        meaning.getMeaning(),
                        sentence.getSentence(),
                        sentence.getSentence_meaning(),
                        sentence.getLevel()
                ));
            }
            // study에 데이터 저장하기
            studyRepository.saveAll(studyList);
        }else{ // 오늘 날짜라면 Study 테이블에서 조회
            List<Study> study = studyRepository.findLastDayForStudy(today);
            for(Study st : study){
                Sentence sentence = sentenceRepository.findBySentence(st.getMeaning().getId());
                list.add(StudyResponseDto.of(
                        st.getWord().getWord(),
                        st.getMeaning().getMeaning(),
                        sentence.getSentence(),
                        sentence.getSentence_meaning(),
                        sentence.getLevel()
                ));
            }
        }
        return list;
    }
}
