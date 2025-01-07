package com.eng.repository;

import com.eng.domain.Meaning;
import com.eng.dto.MeaningDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MeanRepository extends JpaRepository<Meaning, Long> {
    @Query(nativeQuery = true, value = "SELECT EXISTS " +
            "(SELECT 1 FROM meaning m WHERE m.word_id = :wordId AND m.meaning = :meaning)")
    int existsByWordAndMeaning(Long wordId, String meaning);

    @Query(nativeQuery = true, value = "SELECT * FROM meaning m WHERE m.word_id = :wordId AND m.meaning = :meaning LIMIT 1")
    Optional<Meaning> findByMean(Long wordId, String meaning);

    @Query("SELECT m, s FROM Meaning m JOIN Sentence s ON m.id = s.meaning.id " +
            "WHERE m.id NOT IN (SELECT study.meaning.id FROM Study study WHERE study.user.id = :userId) " +
            "ORDER BY m.id")
    Page<Object[]> findByMeanForStudyWithSentence(Long userId, Pageable pageable);

    @Query("select new com.eng.dto.MeaningDto(m.meaning) from Meaning m where m.word.id= :wordId")
    List<MeaningDto> findByWordApplicableMean(Long wordId);
}
