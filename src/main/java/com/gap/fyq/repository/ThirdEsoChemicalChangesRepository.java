package com.gap.fyq.repository;

import com.gap.fyq.model.thirdeso.chemicalchanges.ThirdEsoChemicalChangesExercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThirdEsoChemicalChangesRepository
        extends JpaRepository<ThirdEsoChemicalChangesExercise, Long> {
}
