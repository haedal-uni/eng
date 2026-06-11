package com.eng.repository;

import com.eng.domain.AiFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiFeedbackRepository  extends JpaRepository<AiFeedback, Long> {
    AiFeedback findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
