package com.gap.fyq.model.thirdeso.matter;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "third_eso_matter_exercises")
@Getter
@Setter
@NoArgsConstructor
public class ThirdEsoMatterExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private MatterType matterType;

    /** "porcentaje_masa", "concentracion_gl", "P1","V1","T1","P2","V2","T2" */
    @Column(nullable = false, length = 30)
    private String unknownVariable;

    @Column(nullable = false)
    private double correctAnswerValue;

    @Column(nullable = false, length = 120)
    private String correctAnswerDisplay;

    /** Unidad mostrada junto al campo de entrada (p.ej. "%", "g/L", "L", "atm", "K"). */
    @Column(nullable = false, length = 20)
    private String answerUnit;

    @Column(nullable = false, length = 4000)
    private String explanation;

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
        );
    }
}
