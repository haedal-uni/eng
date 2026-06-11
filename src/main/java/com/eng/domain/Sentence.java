package com.eng.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sentence {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name="meaning_id")
    private Meaning meaning;

    @JsonIgnore
    @OneToMany(mappedBy = "sentence", cascade = CascadeType.REMOVE, orphanRemoval=true)
    private List<Sentence> sentenceList = new ArrayList<>();

    @Column(nullable = false)
    private String sentence;

    @Column(nullable = false)
    private String sentenceMeaning;

    @Column
    private int level;

    @Column(nullable = false)
    private String quizType;

    @Builder
    private Sentence(Meaning meaning, String sentence, String sentenceMeaning, int level, String quizType) {
        this.meaning = meaning;
        this.sentence = sentence;
        this.sentenceMeaning = sentenceMeaning;
        this.level = level;
        this.quizType = quizType;
        meaning.getSentenceList().add(this);
    }

    public static Sentence createSentence(Meaning meaning, String sentence, String sentenceMeaning, int level, String quizType) {
        return Sentence.builder()
                .meaning(meaning)
                .sentence(sentence)
                .sentenceMeaning(sentenceMeaning)
                .level(level)
                .quizType(quizType)
                .build();
    }
}
