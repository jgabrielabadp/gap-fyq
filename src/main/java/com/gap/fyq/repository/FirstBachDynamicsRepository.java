package com.gap.fyq.repository;

import com.gap.fyq.model.firstbach.dynamics.DynamicsType;
import com.gap.fyq.model.firstbach.dynamics.FirstBachDynamicsExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FirstBachDynamicsRepository
        extends JpaRepository<FirstBachDynamicsExercise, Long> {

    List<FirstBachDynamicsExercise> findByCourseAndBlock(
            String course, String block);

    List<FirstBachDynamicsExercise> findByCourseAndBlockAndDynamicsType(
            String course, String block, DynamicsType dynamicsType);
}
