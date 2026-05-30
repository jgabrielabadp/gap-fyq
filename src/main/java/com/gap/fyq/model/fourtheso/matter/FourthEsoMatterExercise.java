package com.gap.fyq.model.fourtheso.matter;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "fourth_eso_matter_exercises")
@Getter
@Setter
@NoArgsConstructor
public class FourthEsoMatterExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FourthEsoMatterType matterType;

    /** "TEXT" · "NUMERICAL" · "MULTIPLE_CHOICE" */
    @Column(nullable = false, length = 20)
    private String exerciseMode;

    // ── Campos exclusivos de TEXT (ELECTRONIC_CONFIGURATION) ──────────────────

    /** Configuración normalizada para comparación: "1s2 2s2 2p6 3s1" */
    @Column(length = 120)
    private String correctConfigNormalized;

    /** Configuración con superíndices Unicode para mostrar: "1s² 2s² 2p⁶ 3s¹" */
    @Column(length = 120)
    private String correctConfigDisplay;

    // ── Campos exclusivos de NUMERICAL (ISOTOPE_MASS_CALCULATION) ─────────────

    @Column
    private Double correctAnswerValue;

    @Column(length = 120)
    private String correctAnswerDisplay;

    /** Unidad de la respuesta (p.ej. "u"). */
    @Column(length = 20)
    private String answerUnit;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    @Column(length = 30)
    private String unknownVariable;

    // ── Campos exclusivos de MULTIPLE_CHOICE (CHEMICAL_BOND_PROPERTIES) ───────

    @Column(length = 300)
    private String option0;

    @Column(length = 300)
    private String option1;

    @Column(length = 300)
    private String option2;

    @Column(length = 300)
    private String option3;

    /** Índice (0-3) de la opción correcta; -1 si el ejercicio no es tipo test. */
    @Column(nullable = false)
    private int correctIndex = -1;

    // ── Explicación pedagógica ─────────────────────────────────────────────────

    @Column(nullable = false, length = 5000)
    private String explanation;

    // ── validateAnswer ─────────────────────────────────────────────────────────

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank()) return false;
        return switch (exerciseMode) {
            case "TEXT" -> correctConfigNormalized != null
                    && correctConfigNormalized.equals(normalizeConfig(input));
            case "MULTIPLE_CHOICE" -> {
                try { yield Integer.parseInt(input.trim()) == correctIndex; }
                catch (NumberFormatException e) { yield false; }
            }
            default -> { // NUMERICAL
                if (correctAnswerValue == null) yield false;
                try {
                    double parsed = parseNumericInput(input);
                    if (correctAnswerValue == 0) yield Math.abs(parsed) < 1e-9;
                    double relErr = Math.abs(parsed - correctAnswerValue) / Math.abs(correctAnswerValue);
                    yield relErr <= (tolerancePercent / 100.0);
                } catch (NumberFormatException e) { yield false; }
            }
        };
    }

    // ── Helpers para la vista ──────────────────────────────────────────────────

    public List<String> getOptions() {
        if (option0 == null) return List.of();
        return List.of(option0, option1, option2, option3);
    }

    public String getCorrectAnswerDisplay() {
        return switch (exerciseMode) {
            case "TEXT"            -> correctConfigDisplay;
            case "MULTIPLE_CHOICE" -> getOptions().get(correctIndex);
            default                -> correctAnswerDisplay;
        };
    }

    // ── Normalización de configuraciones electrónicas (también usada por el servicio) ──

    public static String normalizeConfig(String input) {
        if (input == null) return "";
        return input.trim()
                .toLowerCase()
                .replace("¹", "1").replace("²", "2").replace("³", "3")
                .replace("⁴", "4").replace("⁵", "5").replace("⁶", "6")
                .replace("⁷", "7").replace("⁸", "8").replace("⁹", "9")
                .replace("⁰", "0")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private double parseNumericInput(String raw) {
        return Double.parseDouble(
            raw.trim().replace(",", ".").replace(" ", "")
        );
    }
}
