package com.eng.repository;

import com.eng.domain.Quiz;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcRepository {
    private final JdbcTemplate template;

    public void batchInsert(List<Quiz> quizList) {
        String sql = "INSERT INTO quiz (study_id, correct, user_id) VALUES (?, ?, ?)";
        template.batchUpdate(sql, quizList, quizList.size(), (ps, quiz) -> {
            ps.setLong(1, quiz.getStudy().getId());
            ps.setBoolean(2, quiz.isCorrect());
            ps.setLong(3, quiz.getUser().getId());
        });
    }
}
