package com.gap.fyq.model.secondbach.chemicalequilibrium;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "second_bach_chemical_equilibrium_exercises")
@AttributeOverride(name = "statement", column = @Column(nullable = false, length = 2000))
@Getter
@Setter
@NoArgsConstructor
public class SecondBachChemicalEquilibriumExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 35)
    private EquilibriumType equilibriumType;

    /**
     * KC_VALUE        – alumno introduce el valor numérico de Kc
     * KP_VALUE        – alumno introduce el valor numérico de Kp
     * ALPHA_VALUE     – alumno introduce el grado de disociación α (0–1 o porcentaje)
     * LE_CHATELIER_MCQ – alumno elige la dirección del desplazamiento (A/B/C)
     * SOLUBILITY_PURE – alumno calcula solubilidad s en agua pura
     * SOLUBILITY_COMMON_ION – alumno calcula solubilidad con ion común
     */
    @Column(nullable = false, length = 25)
    private String exerciseMode;

    /**
     * Respuesta numérica redondeada a 2 decimales, en notación científica cuando Ks < 1e-4.
     * Para LE_CHATELIER_MCQ: "A", "B" o "C".
     */
    @Column(nullable = false, length = 100)
    private String correctAnswer;

    @Column(nullable = false, length = 200)
    private String correctAnswerDisplay;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(length = 300)
    private String optionA;

    @Column(length = 300)
    private String optionB;

    @Column(length = 300)
    private String optionC;

    @Column(nullable = false, length = 20000)
    private String explanation;

    // ── validateAnswer ──────────────────────────────────────────────────────────

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank()) return false;
        return switch (exerciseMode) {
            case "KC_VALUE",
                 "KP_VALUE",
                 "ALPHA_VALUE",
                 "SOLUBILITY_PURE",
                 "SOLUBILITY_COMMON_ION" -> validateNumeric(input);
            case "LE_CHATELIER_MCQ"      -> input.trim().equalsIgnoreCase(correctAnswer.trim());
            default -> false;
        };
    }

    // ── tolerancia adaptativa: 1 % para Kc/Kp/s, 2 % para α (α con raíz cuadrada) ──

    private boolean validateNumeric(String input) {
        try {
            double student  = parseScientific(input.trim().replace(",", "."));
            double expected = parseScientific(correctAnswer.trim());
            if (expected == 0.0) return Math.abs(student) < 1e-10;
            double pct = "ALPHA_VALUE".equals(exerciseMode) ? 0.02 : 0.01;
            return Math.abs(student - expected) <= Math.abs(expected) * pct;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Acepta "1.23e-5", "1.23E-5", "1.23·10⁻⁵" y notación decimal normal
    private static double parseScientific(String s) {
        String clean = s.replace("·10^", "e")
                        .replace("×10^", "e")
                        .replace("·10", "e")
                        .replace("⁻", "-")
                        .replace("⁰", "0").replace("¹", "1").replace("²", "2")
                        .replace("³", "3").replace("⁴", "4").replace("⁵", "5")
                        .replace("⁶", "6").replace("⁷", "7").replace("⁸", "8")
                        .replace("⁹", "9");
        return Double.parseDouble(clean);
    }
}
