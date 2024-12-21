package com.eng.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table( // 복합 unique key 설정
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "word_id","meaning_id", "sentence_id"})
)
public class Study {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "word_id")
    private Word word;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "meaning_id")
    private Meaning meaning;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "sentence_id")
    private Sentence sentence;

    @Column
    private LocalDate date;

    @OneToOne(mappedBy = "study", cascade = CascadeType.REMOVE, orphanRemoval=true)
    @JsonIgnore
    private Quiz quiz;

    @Builder
    private Study(User user, Word word, Meaning meaning, Sentence sentence, LocalDate date) {
        this.user = user;
        this.word = word;
        this.meaning = meaning;
        this.sentence = sentence;
        this.date = date;
    }

    public static Study createStudy(User user, Word word, Meaning meaning, Sentence sentence, LocalDate date) {
        return Study.builder()
                .user(user)
                .word(word)
                .meaning(meaning)
                .sentence(sentence)
                .date(date)
                .build();
    }
}
