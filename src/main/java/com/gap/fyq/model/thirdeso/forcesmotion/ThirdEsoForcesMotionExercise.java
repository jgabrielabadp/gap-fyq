package com.gap.fyq.model.thirdeso.forcesmotion;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "third_eso_forces_motion_exercises")
@Getter
@Setter
@NoArgsConstructor
public class ThirdEsoForcesMotionExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private DynamicsType dynamicsType;

    /** Variable que se pide calcular: "a", "vf", "vi", "t", "F", "m", "F1", "F2", "S1", "S2" */
    @Column(nullable = false, length = 10)
    private String unknownVariable;

    @Column(nullable = false)
    private double correctAnswerValue;

    @Column(nullable = false, length = 120)
    private String correctAnswerDisplay;

    /** Unidad mostrada junto al campo de entrada: "m/s²", "N", "kg", "cm²", "m²"… */
    @Column(nullable = false, length = 20)
    private String answerUnit;

    @Column(nullable = false, length = 4000)
    private String explanation;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank()) return false;
        try {
            double parsed = Double.parseDouble(
                    input.trim().replace(",", ".").replace(" ", ""));
            if (correctAnswerValue == 0) return Math.abs(parsed) < 1e-9;
            double relErr = Math.abs(parsed - correctAnswerValue) / Math.abs(correctAnswerValue);
            return relErr <= (tolerancePercent / 100.0);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
