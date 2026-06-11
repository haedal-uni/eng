package com.eng.repository;

import com.eng.domain.QuizHistory;
import com.eng.dto.AiMappingInterface;
import com.eng.dto.RecentWrongQuiz;
import com.eng.dto.RepeatWordMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuizHistoryRepository extends JpaRepository<QuizHistory, Long> {
    @Query(nativeQuery = true, value =
        "SELECT " +
            "  word AS word, " +
            "  SUM(CASE WHEN correct = false THEN 1 ELSE 0 END) AS wrongCount, " +
            "  (SUM(CASE WHEN correct = true THEN 1 ELSE 0 END) / COUNT(*)) * 100 AS correctRate " +
            "FROM quiz_history " +
            "WHERE user_id = :userId " +
            "GROUP BY word " +
            "ORDER BY (SUM(CASE WHEN correct = true THEN 1 ELSE 0 END) * 0.6) " +
            "  + ((100 - (SUM(CASE WHEN correct = true THEN 1 ELSE 0 END) / COUNT(*)) * 100) * 0.4) DESC " +
            "LIMIT 3")
    List<RepeatWordMapping> getReview(Long userId);

    @Query(nativeQuery = true, value =
        "SELECT " +
            "  quiz_sentence AS quizSentence, " +
            "  word, " +
            "  user_answer AS userAnswer, " +
            "  quiz_type AS quizType, " +
            "  response_time AS responseTime, " +
            "  created_at AS createdAt " +
            "FROM quiz_history " +
            "WHERE id IN (" +
            "  SELECT MAX(id) " +
            "  FROM quiz_history " +
            "  WHERE user_id = :userId and correct=false" +
            "  GROUP BY word " +
            ") " +
            "ORDER BY id DESC " +
            "LIMIT 3")
    List<RecentWrongQuiz> getRecentWrongQuiz(Long userId);


    @Query("SELECT " +
        "  (COUNT(CASE WHEN q.quizType = 'vocabulary' AND q.correct = true THEN 1 END) * 100.0 / " +
        "   NULLIF(COUNT(CASE WHEN q.quizType = 'vocabulary' THEN 1 END), 0)) AS vocabularyCorrectRate, " +
        "  (COUNT(CASE WHEN q.quizType = 'grammar' AND q.correct = true THEN 1 END) * 100.0 / " +
        "   NULLIF(COUNT(CASE WHEN q.quizType = 'grammar' THEN 1 END), 0)) AS grammarCorrectRate, " +
        "  AVG(q.responseTime) AS avgResponseTime " +
        "FROM QuizHistory q " +
        "WHERE q.user.id = :userId")
    AiMappingInterface getAiData(Long userId);

    List<QuizHistory> findTop3ByUserIdAndCorrectFalseOrderByIdDesc(Long userId);
}
