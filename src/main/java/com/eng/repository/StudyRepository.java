package com.eng.repository;

import com.eng.domain.Study;
import com.eng.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface StudyRepository extends JpaRepository<Study, Long> {
    @Query("SELECT max(s.date) FROM Study s WHERE s.user.id = :userId")
    LocalDate findLastDay(Long userId);

    @Query("select s from Study s where s.date = :today and s.user.id = :userId")
    List<Study> findLastDayForStudy(LocalDate today, Long userId);

    @Query("select count(*) from Study s where s.date = :today and s.user.id = :userId")
    int countLastDayForStudy(LocalDate today, Long userId);
}
