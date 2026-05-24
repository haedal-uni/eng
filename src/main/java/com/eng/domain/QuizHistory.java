package com.eng.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // builder
public class QuizHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name="quiz_id")
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private String quizType;

    @Column
    private boolean correct;

    @Column
    private String word;

    @Column
    private String quizSentence;

    @Column
    private String userAnswer;

    @Column
    private Double responseTime;

    @CreatedDate
    @Column
    private LocalDate createdAt = LocalDate.now();

    @Builder
    private QuizHistory(User user, Quiz quiz, String quizType, boolean correct, String word, String quizSentence, String userAnswer, Double responseTime) {
      this.user = user;
      this.quiz = quiz;
      this.quizType = quizType;
      this.correct = correct;
      this.word = word;
      this.quizSentence = quizSentence;
      this.userAnswer = userAnswer;
      this.responseTime = responseTime;
    }
}
