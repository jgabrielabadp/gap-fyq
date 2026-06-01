package com.gap.fyq.model.firstbach.thermochemistry;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "first_bach_thermochemistry_exercises")
@Getter
@Setter
@NoArgsConstructor
public class FirstBachThermochemistryExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ThermochemistryType thermochemistryType;

    /**
     * "NUMERICAL"       → REACTION_ENTHALPY (alumno introduce ΔH°r).
     * "GIBBS_COMBINED"  → GIBBS_SPONTANEITY_CALC (ΔG numérico + selección espontaneidad).
     * "MULTIPLE_CHOICE" → CONCEPTUAL_SPONTANEITY (opción A-D).
     */
    @Column(nullable = false, length = 20)
    private String exerciseMode;

    // ── Campos NUMERICAL y GIBBS_COMBINED ─────────────────────────────────────

    @Column
    private Double correctAnswerValue;        // kJ/mol (entalpía) o kJ (Gibbs)

    @Column(length = 120)
    private String correctAnswerDisplay;

    @Column(length = 10)
    private String answerUnit;                // "kJ/mol" | "kJ"

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    // ── Campo exclusivo GIBBS_COMBINED ────────────────────────────────────────

    /**
     * 0 = espontánea (ΔG < 0)
     * 1 = no espontánea (ΔG > 0)
     * 2 = en equilibrio (ΔG ≈ 0)
     * -1 = no aplica
     */
    @Column(nullable = false)
    private int correctSpontaneityIndex = -1;

    // ── Campos MULTIPLE_CHOICE ────────────────────────────────────────────────

    @Column(length = 300)
    private String option0, option1, option2, option3;

    @Column(nullable = false)
    private int correctIndex = -1;

    // ── Tabla de datos termodinámicos (REACTION_ENTHALPY) ─────────────────────

    /** HTML pre-formateado con los valores ΔH°f de cada sustancia. */
    @Column(length = 2000)
    private String dataTableHtml;

    // ── Explicación ───────────────────────────────────────────────────────────

    @Column(nullable = false, length = 8000)
    private String explanation;

    // ── validateAnswer ────────────────────────────────────────────────────────

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank()) return false;
        return switch (exerciseMode != null ? exerciseMode : "") {
            case "MULTIPLE_CHOICE" -> {
                try { yield Integer.parseInt(input.trim()) == correctIndex; }
                catch (NumberFormatException e) { yield false; }
            }
            default -> {    // "NUMERICAL" y "GIBBS_COMBINED" (solo valida el valor numérico)
                if (correctAnswerValue == null) yield false;
                try {
                    double parsed = Double.parseDouble(
                        input.trim().replace(",", ".").replace(" ", ""));
                    if (correctAnswerValue == 0) yield Math.abs(parsed) < 0.05;
                    double relErr = Math.abs(parsed - correctAnswerValue)
                                    / Math.abs(correctAnswerValue);
                    yield relErr <= (tolerancePercent / 100.0);
                } catch (NumberFormatException e) { yield false; }
            }
        };
    }

    /** Valida el índice de espontaneidad (solo GIBBS_COMBINED). */
    public boolean validateSpontaneity(String input) {
        if (!"GIBBS_COMBINED".equals(exerciseMode)) return false;
        if (input == null || input.isBlank()) return false;
        try {
            return Integer.parseInt(input.trim()) == correctSpontaneityIndex;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ── Helpers para la vista ─────────────────────────────────────────────────

    public String getCorrectAnswerDisplay() {
        return switch (exerciseMode != null ? exerciseMode : "") {
            case "MULTIPLE_CHOICE" -> {
                List<String> opts = getOptions();
                yield (correctIndex >= 0 && correctIndex < opts.size())
                    ? opts.get(correctIndex) : "";
            }
            default -> correctAnswerDisplay;
        };
    }

    public List<String> getOptions() {
        if (option0 == null) return List.of();
        return List.of(option0, option1, option2, option3);
    }

    public String getSpontaneityLabel() {
        return switch (correctSpontaneityIndex) {
            case 0  -> "Espontánea (ΔG < 0)";
            case 1  -> "No espontánea (ΔG > 0)";
            case 2  -> "En equilibrio (ΔG ≈ 0)";
            default -> "";
        };
    }
}
