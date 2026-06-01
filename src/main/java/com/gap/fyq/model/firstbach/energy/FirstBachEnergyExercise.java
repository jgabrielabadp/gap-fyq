package com.gap.fyq.model.firstbach.energy;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "first_bach_energy_exercises")
@Getter
@Setter
@NoArgsConstructor
public class FirstBachEnergyExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EnergyType energyType;

    /** Siempre "NUMERICAL". */
    @Column(nullable = false, length = 15)
    private String exerciseMode;

    @Column
    private Double correctAnswerValue;

    @Column(length = 120)
    private String correctAnswerDisplay;

    /** "J", "m" */
    @Column(length = 10)
    private String answerUnit;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    /**
     * WORK_ENERGY_THEOREM        → "trabajo_neto" | "distancia_frenada" | "trabajo_disipado"
     * HARMONIC_OSCILLATOR_ENERGY → "energia_cinetica" | "energia_potencial"
     * ELECTRIC_POTENTIAL_WORK    → "trabajo_electrico"
     */
    @Column(length = 25)
    private String unknownVariable;

    @Column(nullable = false, length = 8000)
    private String explanation;

    // ── validateAnswer ────────────────────────────────────────────────────────
    // Acepta notación decimal estándar Y notación científica (1.6e-16, 3.00e-4).

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank() || correctAnswerValue == null) return false;
        try {
            String s = input.trim()
                .replace(",", ".")
                .replace(" ", "")
                .toLowerCase()
                // "×10^" → "e",  "x10^" → "e",  "·10^" → "e"
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
