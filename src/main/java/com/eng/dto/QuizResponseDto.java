package com.eng.dto;

import com.eng.domain.Quiz;
import com.eng.domain.Study;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class QuizResponseDto {
    private Long quizId;
    private StudyResponseDto studyResponseDto;

    @Builder
    public QuizResponseDto(Long quizId, StudyResponseDto studyResponseDto) {
        this.quizId = quizId;
        this.studyResponseDto = studyResponseDto;
    }

    public static QuizResponseDto of (Quiz quiz, Study study) {
        return QuizResponseDto.builder()
                .quizId(quiz.getId())
                .studyResponseDto(
                        StudyResponseDto.of(
                                study.getWord().getWord(),
                                study.getMeaning().getMeaning(),
                                study.getSentence().getSentence(),
                                study.getSentence().getSentence_meaning(),
                                study.getSentence().getLevel()
                        )
                )
                .build();
    }
}
