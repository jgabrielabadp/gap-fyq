package com.gap.fyq.repository;

import com.gap.fyq.model.secondbach.waves.SecondBachWavesExercise;
import com.gap.fyq.model.secondbach.waves.WavesType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecondBachWavesRepository
        extends JpaRepository<SecondBachWavesExercise, Long> {

    List<SecondBachWavesExercise> findByCourseAndBlock(String course, String block);

    List<SecondBachWavesExercise> findByCourseAndBlockAndWavesType(
            String course, String block, WavesType wavesType);
}
