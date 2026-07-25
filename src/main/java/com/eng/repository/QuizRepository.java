package com.eng.repository;

import com.eng.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query(value = "select * from quiz " +
            "where correct = false and user_id = :userId " +
            "order by rand() limit 10", nativeQuery = true)
    List<Quiz> findRandomQuiz(Long userId);
}
