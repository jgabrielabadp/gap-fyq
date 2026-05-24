package com.gap.fyq.model.scientificactivity;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "scientific_activity_exercises")
@Getter
@Setter
@NoArgsConstructor
public class ScientificActivityExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Magnitude magnitude;

    // valor numérico de la magnitud de origen (ej: 350 en "350 mm → m")
    @Column(name = "source_value", nullable = false)
    private double value;

    // null cuando el tipo es SCIENTIFIC_NOTATION sin cambio de unidad
    @Column(length = 20)
    private String sourceUnit;

    @Column(length = 20)
    private String targetUnit;

    // valor numérico exacto para comparación con tolerancia
    @Column(nullable = false)
    private double correctAnswerValue;

    // cadena legible para mostrar al alumno tras corrección, ej: "0,35 m" o "1,25 × 10^5"
    @Column(nullable = false, length = 100)
    private String correctAnswerDisplay;

    // explicación paso a paso, separada por \n
    @Column(nullable = false, length = 3000)
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExerciseType exerciseType;

    // error relativo máximo aceptable (%) — 1 % por defecto
    @Column(nullable = false)
    private double tolerancePercent = 1.0;

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank()) return false;
        try {
            double parsed = parseNumericInput(input);
            if (correctAnswerValue == 0) return Math.abs(parsed) < 1e-10;
            double relativeError = Math.abs(parsed - correctAnswerValue) / Math.abs(correctAnswerValue);
            return relativeError <= (tolerancePercent / 100.0);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private double parseNumericInput(String raw) {
        // normaliza: coma decimal española, espacios, y notación ×10^N o xE o eE
        return Double.parseDouble(
            raw.trim()
               .replace(",", ".")
               .replace(" ", "")
               .replaceAll("[×xX]10\\^", "E")  // "1,25×10^5"  → "1.25E5"
               .replaceAll("[×xX]10",    "E")  // "1,25×10-5"  → "1.25E-5"
        );
    }
}
