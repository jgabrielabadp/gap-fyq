package com.gap.fyq.repository;

import com.gap.fyq.model.firstbach.thermochemistry.FirstBachThermochemistryExercise;
import com.gap.fyq.model.firstbach.thermochemistry.ThermochemistryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FirstBachThermochemistryRepository
        extends JpaRepository<FirstBachThermochemistryExercise, Long> {

    List<FirstBachThermochemistryExercise> findByCourseAndBlock(
            String course, String block);

    List<FirstBachThermochemistryExercise> findByCourseAndBlockAndThermochemistryType(
            String course, String block, ThermochemistryType thermochemistryType);
}
