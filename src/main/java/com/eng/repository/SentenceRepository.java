package com.eng.repository;

import com.eng.domain.Sentence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SentenceRepository extends JpaRepository<Sentence, Long> {
    boolean existsByMeaning_IdAndSentence(Long meaningId, String sentence);

    List<Sentence> findByMeaning_Id(Long meaningId);
}
