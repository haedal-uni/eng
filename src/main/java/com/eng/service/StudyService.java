package com.eng.service;

import com.eng.domain.Meaning;
import com.eng.domain.Sentence;
import com.eng.domain.User;
import com.eng.dto.StudyResponseDto;
import com.eng.exception.notfound.UserNotFoundException;
import com.eng.repository.MeanRepository;
import com.eng.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyService {
    private final UserRepository userRepository;
    private final MeanRepository meanRepository;

    // 단어 가져오기
    public List<StudyResponseDto> getStudyWord(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Object[]> results = meanRepository.findByMeanForStudyWithSentence(user.getId(), pageable);
        List<StudyResponseDto> list = new ArrayList<>();
        for (Object[] result : results) {
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
}
