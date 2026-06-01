package com.gap.fyq.repository;

import com.gap.fyq.model.firstbach.quantitativechemistry.FirstBachQuantitativeChemistryExercise;
import com.gap.fyq.model.firstbach.quantitativechemistry.QuantitativeChemistryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FirstBachQuantitativeChemistryRepository
        extends JpaRepository<FirstBachQuantitativeChemistryExercise, Long> {

    List<FirstBachQuantitativeChemistryExercise> findByCourseAndBlock(
            String course, String block);

    List<FirstBachQuantitativeChemistryExercise> findByCourseAndBlockAndChemistryType(
            String course, String block, QuantitativeChemistryType chemistryType);
}
