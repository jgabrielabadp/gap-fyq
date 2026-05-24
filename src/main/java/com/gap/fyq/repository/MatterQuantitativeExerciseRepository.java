package com.gap.fyq.repository;

import com.gap.fyq.model.matter.GasLaw;
import com.gap.fyq.model.matter.MatterExerciseType;
import com.gap.fyq.model.matter.MatterQuantitativeExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatterQuantitativeExerciseRepository
        extends JpaRepository<MatterQuantitativeExercise, Long> {

    List<MatterQuantitativeExercise> findByCourseAndBlock(String course, String block);

    List<MatterQuantitativeExercise> findByCourseAndBlockAndExerciseType(
            String course, String block, MatterExerciseType type);

    List<MatterQuantitativeExercise> findByCourseAndBlockAndGasLaw(
            String course, String block, GasLaw gasLaw);
}
