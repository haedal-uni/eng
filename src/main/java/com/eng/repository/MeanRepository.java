package com.eng.repository;

import com.eng.domain.Meaning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MeanRepository extends JpaRepository<Meaning, Long> {
    @Query(nativeQuery = true, value = "SELECT EXISTS " +
            "(SELECT 1 FROM Meaning m WHERE m.word_id = :wordId AND m.meaning = :meaning)")
    int existsByWordAndMeaning(Long wordId, String meaning);

    @Query(nativeQuery = true, value = "SELECT * FROM Meaning m WHERE m.word_id = :wordId AND m.meaning = :meaning LIMIT 1")
    Optional<Meaning> findByMean(Long wordId, String meaning);

    @Query(nativeQuery = true, value =
            "SELECT m.*, s.* " +
                    "FROM meaning m " +
                    "INNER JOIN sentence s ON m.id = s.meaning_id " +
                    "WHERE m.id NOT IN ( " +
                    "    SELECT study.meaning_id FROM study WHERE study.user_id = :userId " +
                    ") " +
                    "ORDER BY m.id LIMIT 10")
    List<Object[]> findByMeanForStudyWithSentence(Long userId);
}
