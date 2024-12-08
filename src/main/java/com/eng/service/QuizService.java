package com.eng.service;

import com.eng.domain.Quiz;
import com.eng.domain.Study;
import com.eng.domain.User;
import com.eng.dto.StudyResponseDto;
import com.eng.exception.notfound.UserNotFoundException;
import com.eng.repository.QuizRepository;
import com.eng.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class QuizService {
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;

    public List<StudyResponseDto> quizList(String username){
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        List<StudyResponseDto> list = new ArrayList<>();
        // 2번
        List<Quiz> randomQuiz = quizRepository.findRandomQuiz(user.getId());
        for(Quiz quiz : randomQuiz){
            Study study = quiz.getStudy();
            list.add(StudyResponseDto.of(study.getWord().getWord(),
                    study.getMeaning().getMeaning(),
                    study.getSentence().getSentence(),
                    study.getSentence().getSentence_meaning(),
                    study.getSentence().getLevel()));
        }
        return list;
    }
}
