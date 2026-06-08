package com.gap.fyq.repository;

import com.gap.fyq.model.secondbach.structurelink.SecondBachStructureLinkExercise;
import com.gap.fyq.model.secondbach.structurelink.StructureLinkType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecondBachStructureLinkRepository
        extends JpaRepository<SecondBachStructureLinkExercise, Long> {

    List<SecondBachStructureLinkExercise> findByCourseAndBlock(String course, String block);

    List<SecondBachStructureLinkExercise> findByCourseAndBlockAndStructureLinkType(
            String course, String block, StructureLinkType structureLinkType);
}
