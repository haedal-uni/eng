package com.eng.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String errorPattern;

    @Column(nullable = false)
    private String aiFeedback;

    @CreatedDate
    @Column
    private LocalDate createdAt = LocalDate.now();

    @Builder
    private AiFeedback(User user, String errorPattern, String aiFeedback) {
        this.user = user;
        this.errorPattern = errorPattern;
        this.aiFeedback = aiFeedback;
    }

}
