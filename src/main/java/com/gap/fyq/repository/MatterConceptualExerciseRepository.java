package com.gap.fyq.repository;

import com.gap.fyq.model.matter.MatterConceptualExercise;
import com.gap.fyq.model.matter.MatterConceptualVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatterConceptualExerciseRepository
        extends JpaRepository<MatterConceptualExercise, Long> {

    List<MatterConceptualExercise> findByCourseAndBlock(String course, String block);

    List<MatterConceptualExercise> findByCourseAndBlockAndVariant(
            String course, String block, MatterConceptualVariant variant);
}
