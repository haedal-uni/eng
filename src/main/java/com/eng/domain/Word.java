package com.eng.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // builder
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String word;

    @JsonIgnore
    @OneToMany(mappedBy = "word", cascade = CascadeType.REMOVE, orphanRemoval=true)
    private List<Meaning> meaningList = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "word", cascade = CascadeType.REMOVE, orphanRemoval=true)
    private List<Study> studyList = new ArrayList<>();

    @Builder
    public Word(String word){
        this.word = word;
    }

    // equals와 hashCode를 'word' 필드를 기준으로 재정의
    // java.sql.SQLIntegrityConstraintViolationException error
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word1 = (Word) o;
        return Objects.equals(word, word1.word);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word);
    }
}
