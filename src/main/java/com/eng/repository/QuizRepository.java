package com.eng.repository;

import com.eng.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query(value = "select * from " +
            "(select row_number() over(order by id) r, quiz.*" +
            "from quiz " +
            "where correct = false and user_id = :userId) sub " +
            "where mod(r, floor(rand() * 3) + 1 )=0 limit 10", nativeQuery = true)
    List<Quiz> findRandomQuiz(Long userId);
}
