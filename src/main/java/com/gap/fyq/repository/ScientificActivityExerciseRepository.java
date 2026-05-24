package com.gap.fyq.repository;

import com.gap.fyq.model.scientificactivity.ExerciseType;
import com.gap.fyq.model.scientificactivity.ScientificActivityExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScientificActivityExerciseRepository
        extends JpaRepository<ScientificActivityExercise, Long> {

    List<ScientificActivityExercise> findByCourseAndBlock(String course, String block);

    List<ScientificActivityExercise> findByCourseAndBlockAndExerciseType(
            String course, String block, ExerciseType exerciseType);
}
