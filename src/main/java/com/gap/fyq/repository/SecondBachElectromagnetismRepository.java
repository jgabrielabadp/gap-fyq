package com.gap.fyq.repository;

import com.gap.fyq.model.secondbach.electromagnetism.ElectromagnetismType;
import com.gap.fyq.model.secondbach.electromagnetism.SecondBachElectromagnetismExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecondBachElectromagnetismRepository
        extends JpaRepository<SecondBachElectromagnetismExercise, Long> {

    List<SecondBachElectromagnetismExercise> findByCourseAndBlock(String course, String block);

    List<SecondBachElectromagnetismExercise> findByCourseAndBlockAndElectromagnetismType(
            String course, String block, ElectromagnetismType electromagnetismType);
}
