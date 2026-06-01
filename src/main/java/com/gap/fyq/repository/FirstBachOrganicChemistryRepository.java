package com.gap.fyq.repository;

import com.gap.fyq.model.firstbach.organicchemistry.FirstBachOrganicChemistryExercise;
import com.gap.fyq.model.firstbach.organicchemistry.OrganicChemistryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FirstBachOrganicChemistryRepository
        extends JpaRepository<FirstBachOrganicChemistryExercise, Long> {

    List<FirstBachOrganicChemistryExercise> findByCourseAndBlock(
            String course, String block);

    List<FirstBachOrganicChemistryExercise> findByCourseAndBlockAndOrganicChemistryType(
            String course, String block, OrganicChemistryType organicChemistryType);
}
