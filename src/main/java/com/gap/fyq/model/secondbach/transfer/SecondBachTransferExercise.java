package com.gap.fyq.model.secondbach.transfer;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "second_bach_transfer_exercises")
@AttributeOverride(name = "statement", column = @Column(nullable = false, length = 2000))
@Getter
@Setter
@NoArgsConstructor
public class SecondBachTransferExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransferType transferType;

    /**
     * PH_STRONG_ACID      – pH de ácido fuerte (HCl, HNO₃, H₂SO₄)
     * PH_STRONG_BASE      – pH de base fuerte (NaOH, KOH)
     * PH_WEAK_ACID        – pH de ácido débil vía Ka (con/sin cuadrática)
     * PH_WEAK_BASE        – pH de base débil vía Kb (con/sin cuadrática)
     * ALPHA_WEAK_ACID     – grado de ionización de ácido débil (0–1)
     * REDOX_OXIDATION_STATE – identificar número de oxidación de un elemento
     * REDOX_COEFFICIENTS  – coeficientes estequiométricos clave tras ajuste ion-electrón
     * REDOX_OXIDANT_TEXT  – texto libre: nombrar oxidante y reductor (A/B/C MCQ)
     * FARADAY_EMF         – fuerza electromotriz E° de celda galvánica (V)
     * FARADAY_MASS        – masa depositada en electrólisis (g)
     */
    @Column(nullable = false, length = 25)
    private String exerciseMode;

    /**
     * Respuesta correcta redondeada a 2 decimales.
     * MCQ: "A", "B" o "C".
     * REDOX_COEFFICIENTS: enteros separados por "|" (e.g. "2|5|2|5|8|4").
     */
    @Column(nullable = false, length = 200)
    private String correctAnswer;

    @Column(nullable = false, length = 300)
    private String correctAnswerDisplay;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(length = 400)
    private String optionA;

    @Column(length = 400)
    private String optionB;

    @Column(length = 400)
    private String optionC;

    @Column(nullable = false, length = 20000)
    private String explanation;

    // ── validateAnswer ──────────────────────────────────────────────────────────

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank()) return false;
        return switch (exerciseMode) {
            case "PH_STRONG_ACID",
                 "PH_STRONG_BASE",
                 "PH_WEAK_ACID",
                 "PH_WEAK_BASE",
                 "ALPHA_WEAK_ACID",
                 "FARADAY_EMF",
                 "FARADAY_MASS"          -> validateNumeric(input);
            case "REDOX_OXIDATION_STATE" -> validateInteger(input);
            case "REDOX_OXIDANT_TEXT"    -> input.trim().equalsIgnoreCase(correctAnswer.trim());
            case "REDOX_COEFFICIENTS"    -> validateCoefficients(input);
            default -> false;
        };
    }

    // Tolerancia 1 % para valores numéricos continuos
    private boolean validateNumeric(String input) {
        try {
            double student  = Double.parseDouble(input.trim().replace(",", "."));
            double expected = Double.parseDouble(correctAnswer.trim());
            if (expected == 0.0) return Math.abs(student) < 0.01;
            return Math.abs(student - expected) <= Math.abs(expected) * 0.01;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Números de oxidación son enteros (con signo)
    private boolean validateInteger(String input) {
        try {
            int student  = Integer.parseInt(input.trim().replace("+", "").replace("−", "-"));
            int expected = Integer.parseInt(correctAnswer.trim());
            return student == expected;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Coeficientes: "2|5|2|5|8|4" — todos deben coincidir exactamente
    private boolean validateCoefficients(String input) {
        String[] parts   = input.split("\\|", -1);
        String[] correct = correctAnswer.split("\\|", -1);
        if (parts.length != correct.length) return false;
        for (int i = 0; i < correct.length; i++) {
            try {
                if (Integer.parseInt(parts[i].trim()) != Integer.parseInt(correct[i].trim()))
                    return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }
}
