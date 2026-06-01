package com.gap.fyq.repository;

import com.gap.fyq.model.firstbach.kinematics.FirstBachKinematicsExercise;
import com.gap.fyq.model.firstbach.kinematics.KinematicsType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FirstBachKinematicsRepository
        extends JpaRepository<FirstBachKinematicsExercise, Long> {

    List<FirstBachKinematicsExercise> findByCourseAndBlock(
            String course, String block);

    List<FirstBachKinematicsExercise> findByCourseAndBlockAndKinematicsType(
            String course, String block, KinematicsType kinematicsType);
}
