package com.gap.fyq.model.fourtheso.chemicalchanges;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fourth_eso_chemical_changes_exercises")
@Getter
@Setter
@NoArgsConstructor
public class FourthEsoChemicalChangesExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FourthEsoChemicalChangesType changesType;

    /** Siempre "NUMERICAL" en este bloque; reservado para coherencia con otros bloques. */
    @Column(nullable = false, length = 20)
    private String exerciseMode = "NUMERICAL";

    @Column
    private Double correctAnswerValue;

    @Column(length = 120)
    private String correctAnswerDisplay;

    /** Unidad de la respuesta: "mol", "g", "L", "mol/L", "partículas". */
    @Column(length = 20)
    private String answerUnit;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    /**
     * Clave semántica que identifica la incógnita pedida.
     * Valores usados: "n_moles", "masa_g", "num_particulas",
     * "volumen_L", "molaridad", "masa_producto_g".
     */
    @Column(length = 30)
    private String unknownVariable;

    @Column(nullable = false, length = 5000)
    private String explanation;

    // ── validateAnswer ─────────────────────────────────────────────────────────

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank() || correctAnswerValue == null) return false;
        try {
            double parsed = parseNumericInput(input);
            if (correctAnswerValue == 0) return Math.abs(parsed) < 1e-9;
            double relErr = Math.abs(parsed - correctAnswerValue) / Math.abs(correctAnswerValue);
            return relErr <= (tolerancePercent / 100.0);
        } catch (NumberFormatException | ArithmeticException e) {
            return false;
        }
    }

    // ── Parser con soporte de notación científica ──────────────────────────────
    // Acepta: "3.01e23", "3.01E23", "3.01x10^23", "3,01×10²³", "3.01x1023"

    private double parseNumericInput(String raw) {
        String s = raw.trim()
                .replace(",", ".")
                .replace(" ", "")
                .toLowerCase()
                .replace("⁰", "0").replace("¹", "1").replace("²", "2")
                .replace("³", "3").replace("⁴", "4").replace("⁵", "5")
                .replace("⁶", "6").replace("⁷", "7").replace("⁸", "8")
                .replace("⁹", "9").replace("⁻", "-")
                .replace("×", "x").replace("·", "x");

        if (s.contains("x10")) {
            int idx = s.indexOf("x10");
            double mantissa = Double.parseDouble(s.substring(0, idx));
            String expPart = s.substring(idx + 3).replace("^", "");
            int exp = Integer.parseInt(expPart);
            return mantissa * Math.pow(10, exp);
        }
        return Double.parseDouble(s);
    }
}
