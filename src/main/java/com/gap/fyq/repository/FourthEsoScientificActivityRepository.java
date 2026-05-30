package com.gap.fyq.repository;

import com.gap.fyq.model.fourtheso.scientificactivity.FourthEsoActivityType;
import com.gap.fyq.model.fourtheso.scientificactivity.FourthEsoScientificActivityExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FourthEsoScientificActivityRepository
        extends JpaRepository<FourthEsoScientificActivityExercise, Long> {

    List<FourthEsoScientificActivityExercise> findByCourseAndBlock(String course, String block);

    List<FourthEsoScientificActivityExercise> findByCourseAndBlockAndActivityType(
            String course, String block, FourthEsoActivityType activityType);
}
