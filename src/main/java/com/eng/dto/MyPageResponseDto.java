package com.eng.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
public class MyPageResponseDto {
    private Long study_time = 0L;
    private Long quiz_time = 0L;
    private LocalDate date;

    @Builder
    public MyPageResponseDto(Long study_time, Long quiz_time, LocalDate date) {
        this.study_time = study_time;
        this.quiz_time = quiz_time;
        this.date = date;
    }

    public static MyPageResponseDto of(Long study_time, Long quiz_time, LocalDate date) {
        return MyPageResponseDto.builder()
                .study_time(study_time)
                .quiz_time(quiz_time)
                .date(date)
                .build();
    }
}
