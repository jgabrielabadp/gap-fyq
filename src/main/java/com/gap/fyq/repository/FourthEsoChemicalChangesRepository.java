package com.gap.fyq.repository;

import com.gap.fyq.model.fourtheso.chemicalchanges.FourthEsoChemicalChangesExercise;
import com.gap.fyq.model.fourtheso.chemicalchanges.FourthEsoChemicalChangesType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FourthEsoChemicalChangesRepository
        extends JpaRepository<FourthEsoChemicalChangesExercise, Long> {

    List<FourthEsoChemicalChangesExercise> findByCourseAndBlock(String course, String block);

    List<FourthEsoChemicalChangesExercise> findByCourseAndBlockAndChangesType(
            String course, String block, FourthEsoChemicalChangesType changesType);
}
