package com.gap.fyq.repository;

import com.gap.fyq.model.firstbach.scientificactivity.FirstBachActivityType;
import com.gap.fyq.model.firstbach.scientificactivity.FirstBachScientificActivityExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FirstBachScientificActivityRepository
        extends JpaRepository<FirstBachScientificActivityExercise, Long> {

    List<FirstBachScientificActivityExercise> findByCourseAndBlock(String course, String block);

    List<FirstBachScientificActivityExercise> findByCourseAndBlockAndActivityType(
            String course, String block, FirstBachActivityType activityType);
}
