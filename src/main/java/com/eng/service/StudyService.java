package com.eng.service;

import com.eng.domain.*;
import com.eng.dto.StudyResponseDto;
import com.eng.exception.notfound.UserNotFoundException;
import com.eng.exception.notfound.WordNotFoundException;
import com.eng.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyService {
    private final StudyRepository studyRepository;
    private final WordRepository wordRepository;
    private final UserRepository userRepository;
    private final MeanRepository meanRepository;

    /*
    단어 학습하기 10개씩 보여주기

    단어를 어떻게 학습할 것인가?
    사용자가 이전에 공부했던 것은 제외해야함
    테이블 생성
    Study : 사용자가 공부한 word_id 목록, user_id
    Wrong : 사용자가 퀴즈를 풀어서 틀린 word_id 목록, user_id
    User : 사용자 정보
     */
    // 단어 가져오기
    public List<StudyResponseDto> getStudyWord(String username){
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        List<Object[]> results = meanRepository.findByMeanForStudyWithSentence(user.getId());
        List<StudyResponseDto> list = new ArrayList<>();
        for(Object[] result:results){
            Meaning meaning = (Meaning) result[0];
            Sentence sentence = (Sentence) result[1];
            list.add(StudyResponseDto.of(
                    meaning.getWord().getWord(),
                    meaning.getMeaning(),
                    sentence.getSentence(),
                    sentence.getSentence_meaning(),
                    sentence.getLevel()
            ));
        }
        return list;
    }

    // 단어 저장하기
    @Transactional
    public void saveStudyWord(Long wordId, String username, String meaning){
        Word word = wordRepository.findById(wordId).orElseThrow(WordNotFoundException::new);
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        Meaning mean = meanRepository.findByMean(wordId, meaning).orElseThrow();
        Study study = Study.createStudy(user, word, mean);
        studyRepository.save(study);
    }
}
