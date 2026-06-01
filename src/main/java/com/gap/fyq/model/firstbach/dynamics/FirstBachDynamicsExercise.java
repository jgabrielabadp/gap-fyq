package com.gap.fyq.model.firstbach.dynamics;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "first_bach_dynamics_exercises")
@Getter
@Setter
@NoArgsConstructor
public class FirstBachDynamicsExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DynamicsType dynamicsType;

    /** Siempre "NUMERICAL". */
    @Column(nullable = false, length = 15)
    private String exerciseMode;

    @Column
    private Double correctAnswerValue;

    @Column(length = 120)
    private String correctAnswerDisplay;

    /** "m/s²", "N", "m/s" */
    @Column(length = 10)
    private String answerUnit;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    /**
     * CONNECTED_BODIES      → "aceleracion" | "tension"
     * MOMENTUM_CONSERVATION → "vf_inelastico" | "v2f_elastico" | "v1f_elastico"
     * FIELD_FORCES_COMPARISON → "fuerza_gravitatoria" | "fuerza_coulomb"
     */
    @Column(length = 25)
    private String unknownVariable;

    @Column(nullable = false, length = 8000)
    private String explanation;

    // ── validateAnswer ────────────────────────────────────────────────────────
    // Acepta notación decimal estándar Y notación científica (1.98e20, 3.27e-7).

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank() || correctAnswerValue == null) return false;
        try {
            String s = input.trim()
                .replace(",", ".")
                .replace(" ", "")
                .toLowerCase()
                // "×10^" → "e",  "×10" → "e",  "x10^" → "e",  "·10^" → "e"
                .replaceAll("[×x·]10\\^?(-?\\d)", "e$1")
                // "*10^" y "e+" → estandarizar
                .replace("*10^", "e").replace("e+", "e");
            double parsed = Double.parseDouble(s);
            if (correctAnswerValue == 0) return Math.abs(parsed) < 1e-12;
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
