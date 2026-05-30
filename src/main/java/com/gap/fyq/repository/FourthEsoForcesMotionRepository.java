package com.gap.fyq.repository;

import com.gap.fyq.model.fourtheso.forcesmotion.FourthEsoForcesMotionExercise;
import com.gap.fyq.model.fourtheso.forcesmotion.FourthEsoDynamicsType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FourthEsoForcesMotionRepository
        extends JpaRepository<FourthEsoForcesMotionExercise, Long> {

    List<FourthEsoForcesMotionExercise> findByCourseAndBlock(String course, String block);

    List<FourthEsoForcesMotionExercise> findByCourseAndBlockAndDynamicsType(
            String course, String block, FourthEsoDynamicsType dynamicsType);
}
