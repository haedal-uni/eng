package com.eng.repository;

import com.eng.domain.StudyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface MyPageRepository extends JpaRepository<StudyHistory, Long> {
    @Query("SELECT max(s.date) FROM StudyHistory s where s.user.id = :userId")
    LocalDate findLastDay(Long userId);

    @Query("select s from StudyHistory s where s.user.id = :userId and s.date = :today")
    StudyHistory findLastTime(Long userId, LocalDate today);

    @Query("select s from StudyHistory s where s.date between :start and :end and s.user.id=:userId")
    List<StudyHistory> findBy7daysTime(LocalDate start, LocalDate end, Long userId);
}
