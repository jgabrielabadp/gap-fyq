package com.gap.fyq.model.secondbach.waves;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "second_bach_waves_exercises")
@Getter
@Setter
@NoArgsConstructor
public class SecondBachWavesExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WavesType wavesType;

    @Column(nullable = false, length = 15)
    private String exerciseMode;

    @Column
    private Double correctAnswerValue;

    @Column(length = 120)
    private String correctAnswerDisplay;

    /**
     * HARMONIC_OSCILLATOR → "frecuencia_angular" | "constante_elastica" | "periodo"
     *                       | "velocidad_maxima"  | "energia_mecanica"
     * WAVE_EQUATION       → "longitud_onda" | "periodo" | "velocidad_onda"
     *                       | "desfase"      | "elongacion"
     * ACOUSTICS_DOPPLER   → "nivel_sonoro"  | "frecuencia_doppler"
     */
    @Column(length = 25)
    private String unknownVariable;

    /** "rad/s", "N/m", "s", "m/s", "J", "m", "rad", "dB", "Hz" */
    @Column(length = 10)
    private String answerUnit;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    @Column(nullable = false, length = 10000)
    private String explanation;

    // ── validateAnswer ────────────────────────────────────────────────────────
    // Acepta: 2.00, 0.314, 3.14e-3, -0.05, 89.01, 3×10^-4, etc.
    // Tolerancia relativa ±2 % (escala-independiente).

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
