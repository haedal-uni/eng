package com.eng.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sentence {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name="meaning_id")
    private Meaning meaning;

    @Column(nullable = false)
    private String sentence;

    @Column(nullable = false)
    private String sentence_meaning;

    @Column
    private int level;

    @Builder
    private Sentence(Meaning meaning, String sentence, String sentence_meaning, int level) {
        this.meaning = meaning;
        this.sentence = sentence;
        this.sentence_meaning = sentence_meaning;
        this.level = level;
        meaning.getSentenceList().add(this);
    }

    public static Sentence createSentence(Meaning meaning, String sentence, String sentence_meaning, int level) {
        return Sentence.builder()
                .meaning(meaning)
                .sentence(sentence)
                .sentence_meaning(sentence_meaning)
                .level(level)
                .build();
    }
}
