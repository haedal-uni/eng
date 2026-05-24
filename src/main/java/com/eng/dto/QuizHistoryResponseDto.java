package com.eng.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
public class QuizHistoryResponseDto {
    String word;
    Long wrongCount;
    Double correctRate;
    String quizSentence;
    String userAnswer;
    String quizType;
    Double responseTime;
    LocalDate createdAt;

    // 복습 우선 순위
    public QuizHistoryResponseDto(RepeatWordMapping mapping) {
      this.word = mapping.getWord();
      this.wrongCount = mapping.getWrongCount();
      this.correctRate = mapping.getCorrectRate();
    }

   // 최근 오답 기록
    public QuizHistoryResponseDto(RecentWrongQuiz mapping) {
      this.word = mapping.getWord();
      this.quizSentence = mapping.getQuizSentence();
      this.userAnswer = mapping.getUserAnswer();
      this.quizType = mapping.getQuizType();
      this.responseTime = mapping.getResponseTime();
    }
}
