package com.gap.fyq.repository;

import com.gap.fyq.model.secondbach.transfer.SecondBachTransferExercise;
import com.gap.fyq.model.secondbach.transfer.TransferType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecondBachTransferRepository
        extends JpaRepository<SecondBachTransferExercise, Long> {

    List<SecondBachTransferExercise> findByCourseAndBlock(String course, String block);

    List<SecondBachTransferExercise> findByCourseAndBlockAndTransferType(
            String course, String block, TransferType type);
}
