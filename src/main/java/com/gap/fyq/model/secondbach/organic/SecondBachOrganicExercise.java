package com.gap.fyq.model.secondbach.organic;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "second_bach_organic_exercises")
@AttributeOverride(name = "statement", column = @Column(nullable = false, length = 2000))
@Getter
@Setter
@NoArgsConstructor
public class SecondBachOrganicExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrganicType organicType;

    /**
     * REACTION_TYPE_MCQ      – clasificar adición / eliminación / sustitución (A/B/C)
     * MARKOVNIKOV_PRODUCT    – producto mayoritario por regla de Markovnikov (A/B/C)
     * SAYTZEFF_PRODUCT       – alqueno mayoritario por regla de Saytzeff (A/B/C)
     * GEOMETRIC_ISOMERS_MCQ  – ¿tiene isómeros geométricos? (A=Sí / B=No)
     * CHIRAL_CARBONS_COUNT   – número de carbonos quirales (entero 0-n)
     * POLYMER_MONOMER_MCQ    – monómero del polímero indicado (A/B/C)
     * POLYMER_TYPE_MCQ       – adición vs condensación (A/B/C)
     */
    @Column(nullable = false, length = 25)
    private String exerciseMode;

    @Column(nullable = false, length = 200)
    private String correctAnswer;

    @Column(nullable = false, length = 300)
    private String correctAnswerDisplay;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(length = 500) private String optionA;
    @Column(length = 500) private String optionB;
    @Column(length = 500) private String optionC;

    @Column(nullable = false, length = 20000)
    private String explanation;

    // ── validateAnswer ──────────────────────────────────────────────────────────

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank()) return false;
        return switch (exerciseMode) {
            case "REACTION_TYPE_MCQ",
                 "MARKOVNIKOV_PRODUCT",
                 "SAYTZEFF_PRODUCT",
                 "GEOMETRIC_ISOMERS_MCQ",
                 "POLYMER_MONOMER_MCQ",
                 "POLYMER_TYPE_MCQ"   -> validateMCQ(input);
            case "CHIRAL_CARBONS_COUNT" -> validateInteger(input);
            default -> false;
        };
    }

    private boolean validateMCQ(String input) {
        return input.trim().equalsIgnoreCase(correctAnswer.trim());
    }

    private boolean validateInteger(String input) {
        try {
            return Integer.parseInt(input.trim()) == Integer.parseInt(correctAnswer.trim());
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ── normalización de nombres IUPAC (utilidad para texto libre) ─────────────

    public static String normalizeIupac(String s) {
        if (s == null) return "";
        return s.trim()
                .toLowerCase()
                .replaceAll("[\\s\\-]+", "")    // elimina espacios y guiones
                .replaceAll("[áàä]", "a")
                .replaceAll("[éèë]", "e")
                .replaceAll("[íìï]", "i")
                .replaceAll("[óòö]", "o")
                .replaceAll("[úùü]", "u");
    }
}
