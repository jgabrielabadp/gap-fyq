package com.gap.fyq.model.motionforces;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "forces_motion_exercises")
@Getter
@Setter
@NoArgsConstructor
public class ForcesAndMotionExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private ForcesAndMotionSubTopic subTopic;

    /** "NUMERICAL" para respuesta numérica; "MULTIPLE_CHOICE" para test de cuatro opciones. */
    @Column(nullable = false, length = 20)
    private String exerciseMode;

    // ── Campos exclusivos de NUMERICAL ────────────────────────────────────

    @Column
    private Double correctAnswerValue;

    @Column(length = 120)
    private String correctAnswerDisplay;

    /** Unidad de la respuesta que se muestra junto al campo de texto (p.ej. "m/s", "N"). */
    @Column(length = 30)
    private String answerUnit;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    /** Variable incógnita del ejercicio (p.ej. "velocidad", "espacio", "tiempo", "peso"). */
    @Column(length = 30)
    private String unknownVariable;

    // ── Campos exclusivos de MULTIPLE_CHOICE ──────────────────────────────

    @Column(length = 300)
    private String option0;

    @Column(length = 300)
    private String option1;

    @Column(length = 300)
    private String option2;

    @Column(length = 300)
    private String option3;

    /** Índice (0-3) de la opción correcta; -1 si el ejercicio es numérico. */
    @Column(nullable = false)
    private int correctIndex = -1;

    // ── Explicación pedagógica ─────────────────────────────────────────────

    @Column(nullable = false, length = 3000)
    private String explanation;

    // ── validateAnswer ─────────────────────────────────────────────────────

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank()) return false;
        if ("MULTIPLE_CHOICE".equals(exerciseMode)) {
            try {
                return Integer.parseInt(input.trim()) == correctIndex;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        // NUMERICAL
        if (correctAnswerValue == null) return false;
        try {
            double parsed = parseNumericInput(input);
            if (correctAnswerValue == 0) return Math.abs(parsed) < 1e-9;
            double relErr = Math.abs(parsed - correctAnswerValue) / Math.abs(correctAnswerValue);
            return relErr <= (tolerancePercent / 100.0);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ── Helpers para la vista ──────────────────────────────────────────────

    public List<String> getOptions() {
        if (option0 == null) return List.of();
        return List.of(option0, option1, option2, option3);
    }

    public String getCorrectAnswerDisplay() {
        if ("MULTIPLE_CHOICE".equals(exerciseMode)) {
            return getOptions().get(correctIndex);
        }
        return correctAnswerDisplay;
    }

    private double parseNumericInput(String raw) {
        return Double.parseDouble(
            raw.trim()
               .replace(",", ".")
               .replace(" ", "")
        );
    }
}
