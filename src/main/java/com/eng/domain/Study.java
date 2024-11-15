package com.eng.domain;

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
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "word_id","meaning_id"})
)
public class Study {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "word_id")
    private Word word;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "meaning_id")
    private Meaning meaning;

    @Column
    private LocalDate date;

    @Builder
    private Study(User user, Word word, Meaning meaning, LocalDate date) {
        this.user = user;
        this.word = word;
        this.meaning = meaning;
        this.date = date;
    }

    public static Study createStudy(User user, Word word, Meaning meaning, LocalDate date) {
        return Study.builder()
                .user(user)
                .word(word)
                .meaning(meaning)
                .date(date)
                .build();
    }
}
