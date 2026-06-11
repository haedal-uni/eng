package com.eng.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class AiFeedbackResponseDto {
    private Double vocabularyCorrectRate; // 어휘 정답률
    private Double grammarCorrectRate;    // 문법 정답률
    private Double avgResponseTime;       // 평균 응답 시간
    private String errorPattern; // 오답 패턴
    private String aiFeedback; // AI 분석 결과

    public AiFeedbackResponseDto(AiMappingInterface dto) {
        this.vocabularyCorrectRate = dto.getVocabularyCorrectRate();
        this.grammarCorrectRate = dto.getGrammarCorrectRate();
        this.avgResponseTime = dto.getAvgResponseTime();
    }

    @Builder
    public AiFeedbackResponseDto(String errorPattern, String aiFeedback) {
        this.errorPattern = errorPattern;
        this.aiFeedback = aiFeedback;
    }

}
