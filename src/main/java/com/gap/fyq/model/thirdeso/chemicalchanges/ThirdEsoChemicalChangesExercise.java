package com.gap.fyq.model.thirdeso.chemicalchanges;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "third_eso_chemical_changes_exercises")
@Getter
@Setter
@NoArgsConstructor
public class ThirdEsoChemicalChangesExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private ChemicalChangeType changeType;

    // ── EQUATION_BALANCING ──────────────────────────────────────────────────
    /**
     * Coeficientes correctos separados por comas en el mismo orden que la
     * ecuación mostrada. Ej: "1,3,2" para N2+3H2→2NH3.
     */
    @Column(length = 60)
    private String correctCoefficients;

    /**
     * Número de coeficientes que el alumno debe introducir.
     * Igual que correctCoefficients.split(",").length.
     */
    @Column
    private int coefficientCount;

    /**
     * Etiquetas visuales de cada hueco, separadas por comas.
     * Ej: "N_{2},H_{2},NH_{3}" (se renderizan con KaTeX).
     */
    @Column(length = 200)
    private String coefficientLabels;

    // ── LAVOISIER_LAW & BASIC_STOICHIOMETRY ────────────────────────────────
    /** Valor numérico exacto de la respuesta esperada. */
    @Column
    private double correctAnswerValue;

    /** Texto a mostrar como respuesta correcta. Ej: "72,00 g". */
    @Column(length = 120)
    private String correctAnswerDisplay;

    /** Unidad mostrada junto al campo de entrada. Ej: "g". */
    @Column(length = 20)
    private String answerUnit;

    // ── COMPARTIDO ─────────────────────────────────────────────────────────
    @Column(nullable = false, length = 4000)
    private String explanation;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    // ── VALIDACIÓN ─────────────────────────────────────────────────────────

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank()) return false;

        return switch (changeType) {
            case EQUATION_BALANCING -> validateCoefficients(input);
            case LAVOISIER_LAW, BASIC_STOICHIOMETRY -> validateNumeric(input);
        };
    }

    private boolean validateCoefficients(String input) {
        try {
            String[] given   = input.trim().split("[,;\\s]+");
            String[] correct = correctCoefficients.split(",");
            if (given.length != correct.length) return false;
            for (int i = 0; i < given.length; i++) {
                if (Integer.parseInt(given[i].trim()) != Integer.parseInt(correct[i].trim())) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean validateNumeric(String raw) {
        try {
            double parsed = Double.parseDouble(raw.trim().replace(",", ".").replace(" ", ""));
            if (correctAnswerValue == 0) return Math.abs(parsed) < 1e-9;
            double relErr = Math.abs(parsed - correctAnswerValue) / Math.abs(correctAnswerValue);
            return relErr <= (tolerancePercent / 100.0);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Devuelve los coeficientes correctos como lista de enteros (para la vista). */
    public List<Integer> getCorrectCoefficientList() {
        return Arrays.stream(correctCoefficients.split(","))
                .map(s -> Integer.parseInt(s.trim()))
                .toList();
    }

    /** Devuelve las etiquetas KaTeX de los huecos como lista (para la vista). */
    public List<String> getCoefficientLabelList() {
        return Arrays.stream(coefficientLabels.split(","))
                .map(String::trim)
                .toList();
    }
}
