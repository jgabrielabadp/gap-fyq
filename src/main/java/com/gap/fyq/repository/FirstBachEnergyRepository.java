package com.gap.fyq.repository;

import com.gap.fyq.model.firstbach.energy.EnergyType;
import com.gap.fyq.model.firstbach.energy.FirstBachEnergyExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FirstBachEnergyRepository
        extends JpaRepository<FirstBachEnergyExercise, Long> {

    List<FirstBachEnergyExercise> findByCourseAndBlock(String course, String block);

    List<FirstBachEnergyExercise> findByCourseAndBlockAndEnergyType(
            String course, String block, EnergyType energyType);
}
