package com.gap.fyq.model.secondbach.gravitational;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "second_bach_gravitational_exercises")
@Getter
@Setter
@NoArgsConstructor
public class SecondBachGravitationalExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GravitationalType gravitationalType;

    @Column(nullable = false, length = 15)
    private String exerciseMode;

    @Column
    private Double correctAnswerValue;

    @Column(length = 120)
    private String correctAnswerDisplay;

    /**
     * ORBITAL_MECHANICS        → "v_orbital" | "T_orbital" | "E_mecanica"
     * ESCAPE_VELOCITY_WORK     → "v_escape"  | "trabajo_orbital"
     * FIELD_POTENTIAL_POINTS   → "potencial_V" | "campo_g"
     */
    @Column(length = 20)
    private String unknownVariable;

    /** "m/s", "s", "J", "J/kg", "N/kg" */
    @Column(length = 10)
    private String answerUnit;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    @Column(nullable = false, length = 10000)
    private String explanation;

    // ── validateAnswer ────────────────────────────────────────────────────────
    // Acepta: 7.67e3, -1.47e10, -1,47e10, 3×10^-4, 3x10^-4, 3·10^-4, etc.

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank() || correctAnswerValue == null) return false;
        try {
            String s = input.trim()
                .replace(",", ".")
                .replace(" ", "")
                .toLowerCase()
                .replaceAll("[×x·]10\\^?(-?\\d)", "e$1")
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
