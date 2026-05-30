package com.gap.fyq.repository;

import com.gap.fyq.model.fourtheso.matter.FourthEsoMatterExercise;
import com.gap.fyq.model.fourtheso.matter.FourthEsoMatterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FourthEsoMatterRepository
        extends JpaRepository<FourthEsoMatterExercise, Long> {

    List<FourthEsoMatterExercise> findByCourseAndBlock(String course, String block);

    List<FourthEsoMatterExercise> findByCourseAndBlockAndMatterType(
            String course, String block, FourthEsoMatterType matterType);
}
