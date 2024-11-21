package com.eng.repository;

import com.eng.domain.Study;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface StudyRepository extends JpaRepository<Study, Long> {
    @Query("SELECT max(s.date) FROM Study s")
    LocalDate findLastDay();

    @Query("select s from Study s where s.date = :today")
    List<Study> findLastDayForStudy(LocalDate today);

    @Query("select count(*) from Study s where s.date = :today")
    int countLastDayForStudy(LocalDate today);
}
