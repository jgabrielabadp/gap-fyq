package com.gap.fyq.model.firstbach.kinematics;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "first_bach_kinematics_exercises")
@Getter
@Setter
@NoArgsConstructor
public class FirstBachKinematicsExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private KinematicsType kinematicsType;

    /** Siempre "NUMERICAL": todos los ejercicios de cinemática piden un valor numérico. */
    @Column(nullable = false, length = 15)
    private String exerciseMode;

    @Column
    private Double correctAnswerValue;

    @Column(length = 120)
    private String correctAnswerDisplay;

    /** "m/s²", "m", "s", "m/s", "rad/s", "rev" */
    @Column(length = 10)
    private String answerUnit;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    /**
     * Incógnita semántica:
     * INTRINSIC_ACCELERATION → "at" | "an" | "a_total"
     * PROJECTILE_MOTION      → "tiempo_vuelo" | "alcance" | "velocidad_final" | "altura_max"
     * ROTATIONAL_MCUA        → "omega_final" | "theta_rad" | "n_vueltas" | "tiempo"
     */
    @Column(length = 20)
    private String unknownVariable;

    @Column(nullable = false, length = 8000)
    private String explanation;

    // ── validateAnswer ────────────────────────────────────────────────────────

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank() || correctAnswerValue == null) return false;
        try {
            double parsed = Double.parseDouble(
                input.trim().replace(",", ".").replace(" ", ""));
            if (correctAnswerValue == 0) return Math.abs(parsed) < 1e-6;
            double relErr = Math.abs(parsed - correctAnswerValue)
                            / Math.abs(correctAnswerValue);
            return relErr <= (tolerancePercent / 100.0);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String getCorrectAnswerDisplay() {
        return correctAnswerDisplay;
    }
}
