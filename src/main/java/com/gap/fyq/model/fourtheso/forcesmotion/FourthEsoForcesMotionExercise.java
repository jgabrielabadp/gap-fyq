package com.gap.fyq.model.fourtheso.forcesmotion;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fourth_eso_forces_motion_exercises")
@Getter
@Setter
@NoArgsConstructor
public class FourthEsoForcesMotionExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private FourthEsoDynamicsType dynamicsType;

    /** Siempre "NUMERICAL"; reservado para coherencia con otros bloques. */
    @Column(nullable = false, length = 20)
    private String exerciseMode = "NUMERICAL";

    @Column
    private Double correctAnswerValue;

    @Column(length = 120)
    private String correctAnswerDisplay;

    /** Unidad de la respuesta: "s", "m", "m/s", "rad/s", "m/s²", "N". */
    @Column(length = 20)
    private String answerUnit;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    /**
     * Clave semántica de la incógnita pedida.
     * Valores: "tiempo_s", "altura_m", "velocidad_ms",
     *          "omega_rads", "velocidad_lineal", "aceleracion_centripeta",
     *          "fuerza_rozamiento", "aceleracion_ms2".
     */
    @Column(length = 30)
    private String unknownVariable;

    @Column(nullable = false, length = 5000)
    private String explanation;

    // ── validateAnswer ─────────────────────────────────────────────────────────

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank() || correctAnswerValue == null) return false;
        try {
            double parsed = Double.parseDouble(
                input.trim().replace(",", ".").replace(" ", "")
            );
            if (correctAnswerValue == 0) return Math.abs(parsed) < 1e-9;
            double relErr = Math.abs(parsed - correctAnswerValue) / Math.abs(correctAnswerValue);
            return relErr <= (tolerancePercent / 100.0);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
