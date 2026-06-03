package com.gap.fyq.repository;

import com.gap.fyq.model.secondbach.gravitational.GravitationalType;
import com.gap.fyq.model.secondbach.gravitational.SecondBachGravitationalExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecondBachGravitationalRepository
        extends JpaRepository<SecondBachGravitationalExercise, Long> {

    List<SecondBachGravitationalExercise> findByCourseAndBlock(String course, String block);

    List<SecondBachGravitationalExercise> findByCourseAndBlockAndGravitationalType(
            String course, String block, GravitationalType gravitationalType);
}
