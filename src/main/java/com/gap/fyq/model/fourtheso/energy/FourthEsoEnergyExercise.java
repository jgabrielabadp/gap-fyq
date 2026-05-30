package com.gap.fyq.model.fourtheso.energy;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fourth_eso_energy_exercises")
@Getter
@Setter
@NoArgsConstructor
public class FourthEsoEnergyExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private FourthEsoEnergyType energyType;

    /** Siempre "NUMERICAL" en este bloque. */
    @Column(nullable = false, length = 20)
    private String exerciseMode = "NUMERICAL";

    @Column
    private Double correctAnswerValue;

    @Column(length = 120)
    private String correctAnswerDisplay;

    /** Unidad de la respuesta: "m/s", "m", "kJ", "Hz", "nm". */
    @Column(length = 20)
    private String answerUnit;

    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    /**
     * Clave semántica de la incógnita.
     * Valores: "velocidad_final", "altura_max",
     *          "calor_kJ",
     *          "longitud_onda_m", "frecuencia_sonido",
     *          "longitud_onda_nm", "frecuencia_luz".
     */
    @Column(length = 30)
    private String unknownVariable;

    @Column(nullable = false, length = 5000)
    private String explanation;

    // ── validateAnswer ─────────────────────────────────────────────────────────
    // El parser extendido acepta notación científica (p.ej. 6e14, 6x10^14)
    // para las frecuencias de la luz visible.

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

    private double parseNumericInput(String raw) {
        String s = raw.trim()
                .replace(",", ".")
                .replace(" ", "")
                .toLowerCase()
                .replace("⁰","0").replace("¹","1").replace("²","2")
                .replace("³","3").replace("⁴","4").replace("⁵","5")
                .replace("⁶","6").replace("⁷","7").replace("⁸","8")
                .replace("⁹","9").replace("⁻","-")
                .replace("×","x").replace("·","x");
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
