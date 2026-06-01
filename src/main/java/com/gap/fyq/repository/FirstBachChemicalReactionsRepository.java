package com.gap.fyq.repository;

import com.gap.fyq.model.firstbach.chemicalreactions.ChemicalReactionsType;
import com.gap.fyq.model.firstbach.chemicalreactions.FirstBachChemicalReactionsExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FirstBachChemicalReactionsRepository
        extends JpaRepository<FirstBachChemicalReactionsExercise, Long> {

    List<FirstBachChemicalReactionsExercise> findByCourseAndBlock(
            String course, String block);

    List<FirstBachChemicalReactionsExercise> findByCourseAndBlockAndReactionsType(
            String course, String block, ChemicalReactionsType reactionsType);
}
