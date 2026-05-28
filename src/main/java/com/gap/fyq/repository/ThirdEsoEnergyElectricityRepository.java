package com.gap.fyq.repository;

import com.gap.fyq.model.thirdeso.energyelectricity.ThirdEsoEnergyElectricityExercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThirdEsoEnergyElectricityRepository
        extends JpaRepository<ThirdEsoEnergyElectricityExercise, Long> {
}
