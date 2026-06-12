package com.gap.fyq.repository;

import com.gap.fyq.model.secondbach.organic.OrganicType;
import com.gap.fyq.model.secondbach.organic.SecondBachOrganicExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecondBachOrganicRepository
        extends JpaRepository<SecondBachOrganicExercise, Long> {

    List<SecondBachOrganicExercise> findByCourseAndBlock(String course, String block);

    List<SecondBachOrganicExercise> findByCourseAndBlockAndOrganicType(
            String course, String block, OrganicType type);
}
