package com.eng.service;

import com.eng.domain.Quiz;
import com.eng.domain.Study;
import com.eng.domain.User;
import com.eng.dto.QuizRequestDto;
import com.eng.dto.QuizResponseDto;
import com.eng.exception.notfound.UserNotFoundException;
import com.eng.repository.JdbcRepository;
import com.eng.repository.QuizRepository;
import com.eng.repository.UserRepository;
import jakarta.transaction.Transactional;
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
    private final JdbcRepository jdbcRepository;

    public List<QuizResponseDto> quizList(String username){
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        List<QuizResponseDto> list = new ArrayList<>();
        List<Quiz> randomQuiz = quizRepository.findRandomQuiz(user.getId());
        for(Quiz quiz : randomQuiz){
            Study study = quiz.getStudy();
            list.add(QuizResponseDto.of(quiz, study));
        }
        return list;
    }

    @Transactional
    public void quiz_correct(QuizRequestDto quizIds){
        log.info("correct=true 총 개수 : {} ",quizIds.getQuizId_List().size());
        jdbcRepository.updateCorrect(quizIds);
    }
}
