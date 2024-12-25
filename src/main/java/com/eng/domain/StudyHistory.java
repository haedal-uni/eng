package com.eng.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class StudyHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private LocalDate date;

    @Column
    private Long study_time; // 학습 시간(s)

    @Column
    private Long quiz_time; // 퀴즈 풀이 시간(s)

    public void addStudy_time(Long time) {
        this.study_time = time;
    }
    public void addQuiz_time(Long time) {
        this.quiz_time = time;
    }
    @Builder
    private StudyHistory(User user, LocalDate date, Long study_time, Long quiz_time) {
        this.user = user;
        this.date = date;
        this.study_time = study_time;
        this.quiz_time = quiz_time;
    }

    public static StudyHistory addStudyHistory(User user, LocalDate date, Long study_time, Long quiz_time) {
        return StudyHistory.builder()
                .user(user)
                .date(date)
                .study_time(study_time)
                .quiz_time(quiz_time)
                .build();
    }
}
