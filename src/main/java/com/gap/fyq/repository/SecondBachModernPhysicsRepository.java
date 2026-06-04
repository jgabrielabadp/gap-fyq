package com.gap.fyq.repository;

import com.gap.fyq.model.secondbach.modernphysics.ModernPhysicsType;
import com.gap.fyq.model.secondbach.modernphysics.SecondBachModernPhysicsExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecondBachModernPhysicsRepository
        extends JpaRepository<SecondBachModernPhysicsExercise, Long> {

    List<SecondBachModernPhysicsExercise> findByCourseAndBlock(String course, String block);

    List<SecondBachModernPhysicsExercise> findByCourseAndBlockAndModernPhysicsType(
            String course, String block, ModernPhysicsType modernPhysicsType);
}
