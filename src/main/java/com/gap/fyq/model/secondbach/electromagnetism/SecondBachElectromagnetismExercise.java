package com.gap.fyq.model.secondbach.electromagnetism;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "second_bach_electromagnetism_exercises")
@Getter
@Setter
@NoArgsConstructor
public class SecondBachElectromagnetismExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ElectromagnetismType electromagnetismType;

    @Column(nullable = false, length = 15)
    private String exerciseMode;

    @Column
    private Double correctAnswerValue;

    @Column(length = 120)
    private String correctAnswerDisplay;

    /**
     * ELECTROSTATIC_SUPERPOSITION → "potencial_V" | "campo_E"
     * LORENTZ_MOTION              → "radio_r"     | "velocidad_v" | "frecuencia_f"
     * FARADAY_INDUCTION           → "flujo_max"   | "fem_max"
     */
    @Column(length = 20)
    private String unknownVariable;

    /** "V", "V/m", "m", "m/s", "Hz", "Wb" */
    @Column(length = 10)
    private String answerUnit;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    @Column(nullable = false, length = 10000)
    private String explanation;

    // ── validateAnswer ────────────────────────────────────────────────────────
    // Acepta: 1.14e-4, -5.40e4, 3×10^-4, 3·10^-4, 3x10^-4, 0.5, -378000, etc.
    // Tolerancia relativa adaptativa (±2 %): escala-independiente por definición.

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
