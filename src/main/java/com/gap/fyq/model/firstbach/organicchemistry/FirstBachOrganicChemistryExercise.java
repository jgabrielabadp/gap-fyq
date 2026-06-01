package com.gap.fyq.model.firstbach.organicchemistry;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "first_bach_organic_chemistry_exercises")
@Getter
@Setter
@NoArgsConstructor
public class FirstBachOrganicChemistryExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrganicChemistryType organicChemistryType;

    /** BL5 usa exclusivamente "MULTIPLE_CHOICE" en los tres tipos. */
    @Column(nullable = false, length = 20)
    private String exerciseMode;

    // ── Opciones ──────────────────────────────────────────────────────────────

    @Column(length = 400)
    private String option0, option1, option2, option3;

    @Column(nullable = false)
    private int correctIndex = -1;

    // ── Explicación ───────────────────────────────────────────────────────────

    @Column(nullable = false, length = 8000)
    private String explanation;

    // ── validateAnswer ────────────────────────────────────────────────────────

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank()) return false;
        try {
            return Integer.parseInt(input.trim()) == correctIndex;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ── Helpers para la vista ─────────────────────────────────────────────────

    public String getCorrectAnswerDisplay() {
        List<String> opts = getOptions();
        return (correctIndex >= 0 && correctIndex < opts.size())
            ? opts.get(correctIndex) : "";
    }

    public List<String> getOptions() {
        if (option0 == null) return List.of();
        return List.of(option0, option1, option2, option3);
    }
}
