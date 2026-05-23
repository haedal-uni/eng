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
    private String sentence_meaning;

    @Column
    private int level;

    @Column(nullable = false)
    private String quiz_type;

    @Builder
    private Sentence(Meaning meaning, String sentence, String sentence_meaning, int level, String quiz_type) {
        this.meaning = meaning;
        this.sentence = sentence;
        this.sentence_meaning = sentence_meaning;
        this.level = level;
        this.quiz_type = quiz_type;
        meaning.getSentenceList().add(this);
    }

    public static Sentence createSentence(Meaning meaning, String sentence, String sentence_meaning, int level, String quiz_type) {
        return Sentence.builder()
                .meaning(meaning)
                .sentence(sentence)
                .sentence_meaning(sentence_meaning)
                .level(level)
                .quiz_type(quiz_type)
                .build();
    }
}
