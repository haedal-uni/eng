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
    private Long studyTime; // 학습 시간(s)

    @Column
    private Long quizTime; // 퀴즈 풀이 시간(s)

    public void addStudyTime(Long time) {
        this.studyTime = time;
    }

    public void addQuizTime(Long time) {
        this.quizTime = time;
    }

    @Builder
    private StudyHistory(User user, LocalDate date, Long studyTime, Long quizTime) {
        this.user = user;
        this.date = date;
        this.studyTime = studyTime;
        this.quizTime = quizTime;
    }

    public static StudyHistory addStudyHistory(User user, LocalDate date, Long studyTime, Long quizTime) {
        return StudyHistory.builder()
                .user(user)
                .date(date)
                .studyTime(studyTime)
                .quizTime(quizTime)
                .build();
    }
}
