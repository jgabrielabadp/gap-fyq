package com.gap.fyq.repository;

import com.gap.fyq.model.fourtheso.energy.FourthEsoEnergyExercise;
import com.gap.fyq.model.fourtheso.energy.FourthEsoEnergyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FourthEsoEnergyRepository
        extends JpaRepository<FourthEsoEnergyExercise, Long> {

    List<FourthEsoEnergyExercise> findByCourseAndBlock(String course, String block);

    List<FourthEsoEnergyExercise> findByCourseAndBlockAndEnergyType(
            String course, String block, FourthEsoEnergyType energyType);
}
