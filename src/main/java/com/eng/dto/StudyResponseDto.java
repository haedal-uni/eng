package com.eng.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StudyResponseDto {
    private String word;
    private String meaning;
    private String sentence;
    private String sentence_meaning;
    private int level;

    @Builder
    public StudyResponseDto(String word, String meaning, String sentence, String sentence_meaning, int level) {
        this.word = word;
        this.meaning = meaning;
        this.sentence = sentence;
        this.sentence_meaning = sentence_meaning;
        this.level = level;
    }

    public static StudyResponseDto of (String word, String meaning, String sentence, String sentence_meaning, int level) {
        return StudyResponseDto.builder()
                .word(word)
                .meaning(meaning)
                .sentence(sentence)
                .sentence_meaning(sentence_meaning)
                .level(level)
                .build();
    }
}
