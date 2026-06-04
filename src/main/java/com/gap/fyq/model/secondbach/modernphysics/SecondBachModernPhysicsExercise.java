package com.gap.fyq.model.secondbach.modernphysics;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "second_bach_modern_physics_exercises")
@Getter
@Setter
@NoArgsConstructor
public class SecondBachModernPhysicsExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ModernPhysicsType modernPhysicsType;

    @Column(nullable = false, length = 15)
    private String exerciseMode;

    @Column
    private Double correctAnswerValue;

    @Column(length = 120)
    private String correctAnswerDisplay;

    /**
     * PHOTOELECTRIC_EFFECT   → "frecuencia_umbral" | "energia_cinetica" | "potencial_frenado"
     * DE_BROGLIE_RELATIVITY  → "longitud_deBroglie" | "factor_lorentz"
     *                          | "tiempo_dilatado"   | "longitud_contraida"
     * RADIOACTIVE_DECAY      → "constante_lambda"   | "actividad_inicial" | "masa_remanente"
     */
    @Column(length = 25)
    private String unknownVariable;

    /** "Hz", "J", "V", "m", "", "s", "1/s", "Bq", "g" */
    @Column(length = 10)
    private String answerUnit;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    @Column(nullable = false, length = 10000)
    private String explanation;

    // ── validateAnswer ────────────────────────────────────────────────────────
    // Acepta: 4.56e-19, 2.31e14, 1.04e15, -3.07e-19, 1.25, 800, 0.75, etc.
    // Parseador tolerante para notación científica y coma/punto decimal.
    // Tolerancia relativa ±2 % (escala-independiente: funciona igual para
    // 10⁻³⁴ que para 10¹⁵).

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
            if (correctAnswerValue == 0) return Math.abs(parsed) < 1e-30;
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
