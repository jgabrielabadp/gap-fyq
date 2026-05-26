package com.gap.fyq.repository;

import com.gap.fyq.model.thirdeso.matter.ThirdEsoMatterExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThirdEsoMatterRepository extends JpaRepository<ThirdEsoMatterExercise, Long> {
}
