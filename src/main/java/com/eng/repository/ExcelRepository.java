package com.eng.repository;

import com.eng.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExcelRepository extends JpaRepository<Word, Long> {

}
