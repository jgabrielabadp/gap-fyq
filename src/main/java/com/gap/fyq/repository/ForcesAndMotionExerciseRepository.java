package com.gap.fyq.repository;

import com.gap.fyq.model.motionforces.ForcesAndMotionExercise;
import com.gap.fyq.model.motionforces.ForcesAndMotionSubTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForcesAndMotionExerciseRepository
        extends JpaRepository<ForcesAndMotionExercise, Long> {

    List<ForcesAndMotionExercise> findByCourseAndBlock(String course, String block);

    List<ForcesAndMotionExercise> findByCourseAndBlockAndSubTopic(
            String course, String block, ForcesAndMotionSubTopic subTopic);
}
