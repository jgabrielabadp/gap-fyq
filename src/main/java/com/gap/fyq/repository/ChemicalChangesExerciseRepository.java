package com.gap.fyq.repository;

import com.gap.fyq.model.changes.ChemicalChangesExercise;
import com.gap.fyq.model.changes.ChemicalChangesVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChemicalChangesExerciseRepository
        extends JpaRepository<ChemicalChangesExercise, Long> {

    List<ChemicalChangesExercise> findByCourseAndBlock(String course, String block);

    List<ChemicalChangesExercise> findByCourseAndBlockAndVariant(
            String course, String block, ChemicalChangesVariant variant);
}
