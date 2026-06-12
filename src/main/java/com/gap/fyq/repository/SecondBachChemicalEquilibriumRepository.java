package com.gap.fyq.repository;

import com.gap.fyq.model.secondbach.chemicalequilibrium.EquilibriumType;
import com.gap.fyq.model.secondbach.chemicalequilibrium.SecondBachChemicalEquilibriumExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecondBachChemicalEquilibriumRepository
        extends JpaRepository<SecondBachChemicalEquilibriumExercise, Long> {

    List<SecondBachChemicalEquilibriumExercise> findByCourseAndBlock(String course, String block);

    List<SecondBachChemicalEquilibriumExercise> findByCourseAndBlockAndEquilibriumType(
            String course, String block, EquilibriumType type);
}
