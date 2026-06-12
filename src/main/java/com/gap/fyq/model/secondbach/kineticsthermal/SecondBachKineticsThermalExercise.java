package com.gap.fyq.model.secondbach.kineticsthermal;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "second_bach_kinetics_thermal_exercises")
@AttributeOverride(name = "statement", column = @Column(nullable = false, length = 2000))
@Getter
@Setter
@NoArgsConstructor
public class SecondBachKineticsThermalExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private KineticsThermalType kineticsThermalType;

    /**
     * HESS_NUMERIC        – alumno introduce ΔH neta (kJ) con signo
     * GIBBS_DELTA_G       – alumno calcula ΔG (kJ) dada T, ΔH y ΔS
     * GIBBS_T_LIMIT       – alumno calcula temperatura límite de espontaneidad (K)
     * ARRHENIUS_EA        – alumno calcula Ea (kJ/mol) a partir de dos (k,T)
     * ARRHENIUS_ORDER     – alumno deduce órdenes parciales de reacción (texto compuesto)
     */
    @Column(nullable = false, length = 20)
    private String exerciseMode;

    /**
     * Respuesta numérica correcta redondeada a 2 decimales.
     * Para ARRHENIUS_ORDER: formato "orden_A|orden_B|orden_global".
     */
    @Column(nullable = false, length = 100)
    private String correctAnswer;

    @Column(nullable = false, length = 200)
    private String correctAnswerDisplay;

    /** Unidad de la respuesta esperada, p.ej. "kJ", "K", "kJ/mol". */
    @Column(nullable = false, length = 20)
    private String unit;

    @Column(nullable = false, length = 20000)
    private String explanation;

    // ── validateAnswer ──────────────────────────────────────────────────────────

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank()) return false;
        return switch (exerciseMode) {
            case "HESS_NUMERIC",
                 "GIBBS_DELTA_G",
                 "GIBBS_T_LIMIT",
                 "ARRHENIUS_EA"    -> validateNumeric(input);
            case "ARRHENIUS_ORDER" -> validateOrders(input);
            default -> false;
        };
    }

    // ── validación numérica con tolerancia adaptativa del 1 % ──────────────────

    private boolean validateNumeric(String input) {
        try {
            double student  = Double.parseDouble(input.trim().replace(",", "."));
            double expected = Double.parseDouble(correctAnswer.trim());
            if (expected == 0.0) return Math.abs(student) < 0.01;
            double tolerance = Math.abs(expected) * 0.01;
            return Math.abs(student - expected) <= tolerance;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ── validación de órdenes parciales: "n_A|n_B|n_global" ───────────────────

    private boolean validateOrders(String input) {
        String[] parts   = input.split("\\|", -1);
        String[] correct = correctAnswer.split("\\|", -1);
        if (parts.length != correct.length) return false;
        for (int i = 0; i < correct.length; i++) {
            try {
                double s = Double.parseDouble(parts[i].trim());
                double c = Double.parseDouble(correct[i].trim());
                if (Math.abs(s - c) > 0.01) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }
}
