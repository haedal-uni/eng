package com.eng.repository;

import com.eng.domain.Quiz;
import com.eng.dto.QuizRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcRepository {
    private final JdbcTemplate template;

    public void saveQuizList(List<Quiz> quizList) {
        String sql = "INSERT INTO quiz (study_id, correct, user_id) VALUES (?, ?, ?)";
        template.batchUpdate(sql, quizList, quizList.size(), (qz, quiz) -> {
            qz.setLong(1, quiz.getStudy().getId());
            qz.setBoolean(2, quiz.isCorrect());
            qz.setLong(3, quiz.getUser().getId());
        });
    }

    public void updateCorrect(QuizRequestDto quizIdList) {
        String sql = "UPDATE quiz SET correct = true WHERE quiz.id=?";
        template.batchUpdate(sql, quizIdList.getQuizId_List(), quizIdList.getQuizId_List().size(), (qz, quizId) ->{
            qz.setLong(1, quizId);
                });
    }
}
