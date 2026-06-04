package com.gap.fyq.repository;

import com.gap.fyq.model.secondbach.optics.OpticsType;
import com.gap.fyq.model.secondbach.optics.SecondBachOpticsExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecondBachOpticsRepository
        extends JpaRepository<SecondBachOpticsExercise, Long> {

    List<SecondBachOpticsExercise> findByCourseAndBlock(String course, String block);

    List<SecondBachOpticsExercise> findByCourseAndBlockAndOpticsType(
            String course, String block, OpticsType opticsType);
}
