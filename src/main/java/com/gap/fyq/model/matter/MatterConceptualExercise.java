package com.gap.fyq.model.matter;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "matter_conceptual_exercises")
@Getter
@Setter
@NoArgsConstructor
public class MatterConceptualExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatterConceptualVariant variant;

    @Column(nullable = false, length = 300)
    private String option0;
    @Column(nullable = false, length = 300)
    private String option1;
    @Column(nullable = false, length = 300)
    private String option2;
    @Column(nullable = false, length = 300)
    private String option3;

    // Índice (0-3) de la opción correcta
    @Column(nullable = false)
    private int correctIndex;

    // Justificación teórica mostrada al alumno tras responder
    @Column(nullable = false, length = 2000)
    private String explanation;

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank()) return false;
        try {
            return Integer.parseInt(input.trim()) == correctIndex;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public List<String> getOptions() {
        return List.of(option0, option1, option2, option3);
    }

    public String getCorrectAnswerDisplay() {
        return getOptions().get(correctIndex);
    }
}
