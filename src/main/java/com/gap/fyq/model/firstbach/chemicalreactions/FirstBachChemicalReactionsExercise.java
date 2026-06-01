package com.gap.fyq.model.firstbach.chemicalreactions;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "first_bach_chemical_reactions_exercises")
@Getter
@Setter
@NoArgsConstructor
public class FirstBachChemicalReactionsExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ChemicalReactionsType reactionsType;

    /** Siempre "NUMERICAL": todos los ejercicios de BL3 piden un valor numérico. */
    @Column(nullable = false, length = 15)
    private String exerciseMode;

    @Column
    private Double correctAnswerValue;

    @Column(length = 120)
    private String correctAnswerDisplay;

    /** "g", "kg" o "L" */
    @Column(length = 5)
    private String answerUnit;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    /**
     * Incógnita semántica.
     * PURITY_AND_YIELD / INDUSTRIAL_SIDERURGY → "masa_producto" | "volumen_gas"
     * LIMITING_WITH_IMPURITIES               → "masa_producto"
     */
    @Column(length = 20)
    private String unknownVariable;

    @Column(nullable = false, length = 8000)
    private String explanation;

    // ── validateAnswer ────────────────────────────────────────────────────────

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank() || correctAnswerValue == null) return false;
        try {
            double parsed = Double.parseDouble(
                input.trim().replace(",", ".").replace(" ", "")
            );
            if (correctAnswerValue == 0) return Math.abs(parsed) < 1e-9;
            double relErr = Math.abs(parsed - correctAnswerValue) / Math.abs(correctAnswerValue);
            return relErr <= (tolerancePercent / 100.0);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String getCorrectAnswerDisplay() {
        return correctAnswerDisplay;
    }
}
