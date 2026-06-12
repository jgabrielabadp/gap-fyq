package com.gap.fyq.repository;

import com.gap.fyq.model.secondbach.kineticsthermal.KineticsThermalType;
import com.gap.fyq.model.secondbach.kineticsthermal.SecondBachKineticsThermalExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecondBachKineticsThermalRepository
        extends JpaRepository<SecondBachKineticsThermalExercise, Long> {

    List<SecondBachKineticsThermalExercise> findByCourseAndBlock(String course, String block);

    List<SecondBachKineticsThermalExercise> findByCourseAndBlockAndKineticsThermalType(
            String course, String block, KineticsThermalType type);
}
