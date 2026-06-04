package com.gap.fyq.model.secondbach.optics;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "second_bach_optics_exercises")
@Getter
@Setter
@NoArgsConstructor
public class SecondBachOpticsExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OpticsType opticsType;

    @Column(nullable = false, length = 15)
    private String exerciseMode;

    @Column
    private Double correctAnswerValue;

    @Column(length = 120)
    private String correctAnswerDisplay;

    /**
     * SNELL_REFRACTION_LIMIT → "angulo_refraccion" | "velocidad_medio" | "angulo_limite"
     * GEOMETRIC_LENSES       → "posicion_imagen"   | "aumento_lateral" | "tamanyo_imagen"
     * EYE_DEFECTS_DIOPTERS   → "potencia_lente"
     */
    @Column(length = 25)
    private String unknownVariable;

    /** "°", "m/s", "cm", "", "D" */
    @Column(length = 10)
    private String answerUnit;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    @Column(nullable = false, length = 10000)
    private String explanation;

    // ── validateAnswer ────────────────────────────────────────────────────────
    // Acepta: 48.59, -12.00, -0.50, 2.00e8, 3×10^-4, -2, 0.333, etc.
    // Tolerancia relativa ±2 %; el signo de la respuesta se valida estrictamente.

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
            if (correctAnswerValue == 0) return Math.abs(parsed) < 1e-9;
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
