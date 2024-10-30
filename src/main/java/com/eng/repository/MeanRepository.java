package com.eng.repository;

import com.eng.domain.Meaning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MeanRepository extends JpaRepository<Meaning, Long> {
    @Query(nativeQuery = true, value = "SELECT EXISTS " +
            "(SELECT 1 FROM Meaning m WHERE m.word_id = :wordId AND m.meaning = :meaning)")
    int existsByWordAndMeaning(Long wordId, String meaning);
}
