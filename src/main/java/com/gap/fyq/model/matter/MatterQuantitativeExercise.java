package com.gap.fyq.model.matter;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "matter_quantitative_exercises")
@Getter
@Setter
@NoArgsConstructor
public class MatterQuantitativeExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatterExerciseType exerciseType;

    // Solo para GAS_LAWS; null para DENSITY
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private GasLaw gasLaw;

    // Qué variable es incógnita: "densidad", "masa", "volumen", "P2", "V2", "T2"
    @Column(nullable = false, length = 20)
    private String unknownVariable;

    // Valor numérico exacto para comparar con tolerancia
    @Column(nullable = false)
    private double correctAnswerValue;

    // Cadena legible para mostrar al alumno, con KaTeX si procede
    @Column(nullable = false, length = 120)
    private String correctAnswerDisplay;

    // Explicación paso a paso con marcadores KaTeX \[...\] y \(...\)
    @Column(nullable = false, length = 3000)
    private String explanation;

    // Tolerancia ligeramente mayor que BL1 por redondeo en conversiones K↔°C
    @Column(nullable = false)
    private double tolerancePercent = 2.0;

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank()) return false;
        try {
            double parsed = parseNumericInput(input);
            if (correctAnswerValue == 0) return Math.abs(parsed) < 1e-9;
            double relErr = Math.abs(parsed - correctAnswerValue) / Math.abs(correctAnswerValue);
            return relErr <= (tolerancePercent / 100.0);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private double parseNumericInput(String raw) {
        return Double.parseDouble(
            raw.trim()
               .replace(",", ".")
               .replace(" ", "")
               .replaceAll("[×xX]10\\^", "E")
               .replaceAll("[×xX]10",    "E")
        );
    }
}
