package com.gap.fyq.model.firstbach.quantitativechemistry;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "first_bach_quantitative_chemistry_exercises")
@Getter
@Setter
@NoArgsConstructor
public class FirstBachQuantitativeChemistryExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuantitativeChemistryType chemistryType;

    /**
     * "FORMULA"  → alumno escribe la fórmula molecular (ej: C6H12O6).
     * "NUMERICAL" → alumno introduce un valor numérico.
     */
    @Column(nullable = false, length = 15)
    private String exerciseMode;

    // ── Campos FORMULA (EMPIRICAL_MOLECULAR_FORMULA) ──────────────────────────

    /** Fórmula canónica en ASCII: "C6H12O6". Usada para validación. */
    @Column(length = 40)
    private String correctFormulaCanonical;

    /** Fórmula con subíndices Unicode para mostrar al alumno: "C₆H₁₂O₆". */
    @Column(length = 60)
    private String correctFormulaDisplay;

    // ── Campos NUMERICAL (ADVANCED_SOLUTIONS, GAS_MIXTURES_DALTON) ───────────

    @Column
    private Double correctAnswerValue;

    @Column(length = 120)
    private String correctAnswerDisplay;

    /** Unidad: "mol/kg", "atm", "—" */
    @Column(length = 20)
    private String answerUnit;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    /**
     * Incógnita semántica:
     * ADVANCED_SOLUTIONS  → "molalidad" | "fraccion_molar"
     * GAS_MIXTURES_DALTON → "presion_parcial_A" | "presion_parcial_B" | "presion_total"
     */
    @Column(length = 25)
    private String unknownVariable;

    // ── Explicación ───────────────────────────────────────────────────────────

    @Column(nullable = false, length = 8000)
    private String explanation;

    // ── validateAnswer ────────────────────────────────────────────────────────

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank()) return false;
        if ("FORMULA".equals(exerciseMode)) {
            if (correctFormulaCanonical == null) return false;
            Map<String, Integer> correct = parseFormula(correctFormulaCanonical);
            Map<String, Integer> student = parseFormula(input);
            return !correct.isEmpty() && correct.equals(student);
        }
        // NUMERICAL
        if (correctAnswerValue == null) return false;
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

    // ── Helper para la vista ──────────────────────────────────────────────────

    public String getCorrectAnswerDisplay() {
        if ("FORMULA".equals(exerciseMode)) return correctFormulaDisplay;
        return correctAnswerDisplay;
    }

    // ── Parser de fórmulas moleculares ────────────────────────────────────────
    // Acepta ASCII (C6H12O6) y Unicode (C₆H₁₂O₆). No maneja paréntesis.

    public static Map<String, Integer> parseFormula(String input) {
        if (input == null || input.isBlank()) return Map.of();
        String s = input.trim()
            .replace("₀","0").replace("₁","1").replace("₂","2")
            .replace("₃","3").replace("₄","4").replace("₅","5")
            .replace("₆","6").replace("₇","7").replace("₈","8")
            .replace("₉","9");
        Map<String, Integer> result = new LinkedHashMap<>();
        Matcher m = Pattern.compile("([A-Z][a-z]?)(\\d*)").matcher(s);
        while (m.find()) {
            String element = m.group(1);
            int count = m.group(2).isEmpty() ? 1 : Integer.parseInt(m.group(2));
            result.merge(element, count, Integer::sum);
        }
        return result;
    }
}
