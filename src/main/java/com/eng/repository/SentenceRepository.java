package com.eng.repository;

import com.eng.domain.Sentence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SentenceRepository extends JpaRepository<Sentence, Long> {
    @Query(nativeQuery = true, value = "SELECT EXISTS " +
            "(SELECT 1 FROM Sentence s WHERE s.meaning_id = :meaningId AND s.sentence = :sentence)")
    int existsByMeanAndSentence(Long meaningId, String sentence);
}
