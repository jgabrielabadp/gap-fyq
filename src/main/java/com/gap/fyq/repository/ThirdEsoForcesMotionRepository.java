package com.gap.fyq.repository;

import com.gap.fyq.model.thirdeso.forcesmotion.ThirdEsoForcesMotionExercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThirdEsoForcesMotionRepository
        extends JpaRepository<ThirdEsoForcesMotionExercise, Long> {
}
