package com.gap.fyq.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String statement;

    // "2ESO", "3ESO", "1BACH", "2BACH-FIS", "2BACH-QUI"
    @Column(nullable = false, length = 10)
    private String course;

    // "BL1", "BL2", etc.
    @Column(nullable = false, length = 10)
    private String block;

    public abstract boolean validateAnswer(String input);
}
