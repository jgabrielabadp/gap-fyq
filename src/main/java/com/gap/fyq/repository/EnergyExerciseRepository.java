package com.gap.fyq.repository;

import com.gap.fyq.model.energy.EnergyExercise;
import com.gap.fyq.model.energy.EnergyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnergyExerciseRepository
        extends JpaRepository<EnergyExercise, Long> {

    List<EnergyExercise> findByCourseAndBlock(String course, String block);

    List<EnergyExercise> findByCourseAndBlockAndEnergyType(
            String course, String block, EnergyType energyType);
}
