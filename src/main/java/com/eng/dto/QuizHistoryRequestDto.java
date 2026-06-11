package com.eng.dto;

import lombok.Getter;

@Getter
public class QuizHistoryRequestDto {
  private Long quizId;
  private String username;
  private String quizType;
  private boolean correct;
  private String word;
  private String quizSentence;
  private String userAnswer;
  private Double responseTime;
}
