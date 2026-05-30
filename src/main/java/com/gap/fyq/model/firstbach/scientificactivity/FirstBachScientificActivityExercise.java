package com.gap.fyq.model.firstbach.scientificactivity;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "first_bach_scientific_activity_exercises")
@Getter
@Setter
@NoArgsConstructor
public class FirstBachScientificActivityExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private FirstBachActivityType activityType;

    /** "NUMERICAL" para cálculos numéricos; "DIMENSIONAL" para análisis dimensional. */
    @Column(nullable = false, length = 15)
    private String exerciseMode;

    // ── Campos NUMERICAL ─────────────────────────────────────────────────────

    @Column
    private Double correctAnswerValue;

    @Column(length = 120)
    private String correctAnswerDisplay;

    /** Unidad de la respuesta: "°C", "g", "s", "%", "m/s²", "N/m", etc. */
    @Column(length = 20)
    private String answerUnit;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    /**
     * Clave semántica de la incógnita.
     * EXPERIMENTAL_ERRORS: "valor_medio", "error_absoluto", "error_relativo"
     * GRAPH_SLOPE_ANALYSIS: "pendiente"
     * DIMENSIONAL_ANALYSIS: no se usa (modo DIMENSIONAL)
     */
    @Column(length = 25)
    private String unknownVariable;

    // ── Campos DIMENSIONAL ────────────────────────────────────────────────────

    /**
     * Fórmula dimensional correcta en notación legible, p.ej. "M·L·T⁻²".
     * Se usa tanto para mostrar al alumno como para parsear y validar.
     */
    @Column(length = 60)
    private String correctDimensionFormula;

    // ── Campo para tabla de datos (GRAPH_SLOPE_ANALYSIS) ─────────────────────

    /** HTML pre-formateado de la tabla de datos experimentales. */
    @Column(length = 2000)
    private String tableHtml;

    // ── Explicación ───────────────────────────────────────────────────────────

    @Column(nullable = false, length = 6000)
    private String explanation;

    // ── validateAnswer ────────────────────────────────────────────────────────

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank()) return false;
        if ("DIMENSIONAL".equals(exerciseMode)) {
            if (correctDimensionFormula == null) return false;
            Map<String, Integer> correct = parseDimension(correctDimensionFormula);
            Map<String, Integer> student = parseDimension(input);
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

    // ── Helpers para la vista ────────────────────────────────────────────────

    public String getCorrectAnswerDisplay() {
        if ("DIMENSIONAL".equals(exerciseMode)) return correctDimensionFormula;
        return correctAnswerDisplay;
    }

    // =========================================================================
    // Parser de ecuaciones dimensionales
    // Acepta: M·L·T^-2 | M*L*T^-2 | MLT-2 | M L T^-2 | M·L·T⁻² | M1L1T-2
    // Resultado: mapa {símbolo → exponente}, donde los ceros se omiten.
    // =========================================================================

    public static Map<String, Integer> parseDimension(String input) {
        Map<String, Integer> dims = new HashMap<>();
        if (input == null || input.isBlank()) return dims;

        String s = input.trim()
                .replace("·", "").replace("×", "").replace("*", "").replace(" ", "")
                .replace("⁻", "-").replace("⁰","0").replace("¹","1").replace("²","2")
                .replace("³","3").replace("⁴","4").replace("⁵","5").replace("⁶","6")
                .replace("⁷","7").replace("⁸","8").replace("⁹","9")
                .toUpperCase();

        Pattern p = Pattern.compile("([MLTI])(\\^?-?\\d+)?");
        Matcher m = p.matcher(s);
        while (m.find()) {
            String sym = m.group(1);
            int exp = (m.group(2) != null)
                    ? Integer.parseInt(m.group(2).replace("^", ""))
                    : 1;
            if (exp != 0) {
                dims.merge(sym, exp, Integer::sum);
            }
        }
        return dims;
    }
}
