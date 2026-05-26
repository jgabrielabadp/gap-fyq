package com.gap.fyq.repository;

import com.gap.fyq.model.thirdeso.scientificactivity.ActivityType;
import com.gap.fyq.model.thirdeso.scientificactivity.ThirdEsoScientificActivityExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThirdEsoScientificActivityRepository
        extends JpaRepository<ThirdEsoScientificActivityExercise, Long> {

    List<ThirdEsoScientificActivityExercise> findByCourseAndBlock(String course, String block);

    List<ThirdEsoScientificActivityExercise> findByCourseAndBlockAndActivityType(
            String course, String block, ActivityType activityType);
}
