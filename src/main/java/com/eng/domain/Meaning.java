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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meaning {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY, cascade = CascadeType.REMOVE) // ON DELETE CASCADE
    @JsonIgnore
    @JoinColumn(name="word_id")
    private Word word;

    @OneToMany(mappedBy = "meaning")
    private List<Sentence> sentenceList = new ArrayList<>();

    @Column(nullable = false)
    private String meaning;

    @OneToMany(mappedBy = "meaning")
    private List<Study> studyList = new ArrayList<>();

    @Builder
    private Meaning(Word word, String meaning){
        this.word = word;
        this.meaning = meaning;
        word.getMeaningList().add(this);
    }

    public static Meaning createMeaning(Word word, String meaning){
        return Meaning.builder()
                .word(word)
                .meaning(meaning)
                .build();
    }
}
