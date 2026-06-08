package com.gap.fyq.model.secondbach.structurelink;

import com.gap.fyq.model.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "second_bach_structure_link_exercises")
@Getter
@Setter
@NoArgsConstructor
public class SecondBachStructureLinkExercise extends Exercise {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StructureLinkType structureLinkType;

    /**
     * CONFIG_TEXT    – alumno escribe configuración electrónica completa
     * QUANTUM_MCQ    – alumno elige el conjunto (n,l,mₗ,mₛ) correcto (A/B/C)
     * PERIODIC_MCQ   – alumno elige el elemento correcto según tendencia (A/B/C)
     * GEOMETRY_MULTI – alumno introduce hibridación|paresLibres|geometría|polaridad
     */
    @Column(nullable = false, length = 20)
    private String exerciseMode;

    @Column(nullable = false, length = 300)
    private String correctAnswer;

    @Column(nullable = false, length = 500)
    private String correctAnswerDisplay;

    @Column(length = 300)
    private String optionA;

    @Column(length = 300)
    private String optionB;

    @Column(length = 300)
    private String optionC;

    @Column(nullable = false, length = 15000)
    private String explanation;

    // ── validateAnswer ─────────────────────────────────────────────────────────

    @Override
    public boolean validateAnswer(String input) {
        if (input == null || input.isBlank()) return false;
        return switch (exerciseMode) {
            case "CONFIG_TEXT"   -> normalizeConfig(input).equals(normalizeConfig(correctAnswer));
            case "QUANTUM_MCQ",
                 "PERIODIC_MCQ" -> input.trim().equalsIgnoreCase(correctAnswer.trim());
            case "GEOMETRY_MULTI" -> validateGeometry(input);
            default -> false;
        };
    }

    // ── normalización de configuración electrónica ─────────────────────────────
    // Acepta: "1s2 2s2 2p6", "1s² 2s² 2p⁶", "1s^2 2s^2 2p^6", etc.

    private String normalizeConfig(String s) {
        return s.trim()
                .toLowerCase()
                .replace(" ", "")
                .replace("^", "")
                .replace("¹", "1").replace("²", "2").replace("³", "3")
                .replace("⁴", "4").replace("⁵", "5").replace("⁶", "6")
                .replace("⁷", "7").replace("⁸", "8").replace("⁹", "9")
                .replace("⁰", "0");
    }

    // ── validación compuesta para GEOMETRY_MULTI ───────────────────────────────
    // input esperado: "hibridación|paresLibres|geometría|polaridad"

    private boolean validateGeometry(String input) {
        String[] parts   = input.split("\\|", -1);
        String[] correct = correctAnswer.split("\\|", -1);
        if (parts.length != 4 || correct.length != 4) return false;

        String hibIn   = normalizeHybrid(parts[0]);
        String hibCorr = normalizeHybrid(correct[0]);
        if (!hibIn.equals(hibCorr)) return false;

        try {
            int lpIn   = Integer.parseInt(parts[1].trim());
            int lpCorr = Integer.parseInt(correct[1].trim());
            if (lpIn != lpCorr) return false;
        } catch (NumberFormatException e) {
            return false;
        }

        String geomIn   = normalizeGeometry(parts[2]);
        String geomCorr = normalizeGeometry(correct[2]);
        if (!geomIn.equals(geomCorr)) return false;

        String polIn   = normalizePolar(parts[3]);
        String polCorr = normalizePolar(correct[3]);
        return polIn.equals(polCorr);
    }

    private String normalizeHybrid(String s) {
        return s.trim().toLowerCase().replace(" ", "").replace("^", "").replace("·", "");
    }

    private String normalizeGeometry(String s) {
        String n = s.trim().toLowerCase().replaceAll("\\s+", " ");
        return switch (n) {
            case "angular", "doblada", "doblado", "en forma de v", "bent" -> "angular";
            case "piramidal trigonal", "trigonal piramidal",
                 "pirámide trigonal", "piramide trigonal"                  -> "piramidal trigonal";
            case "trigonal plana", "plana trigonal",
                 "triangular plana", "trigonal planar"                     -> "trigonal plana";
            case "tetraédrica", "tetraedrica", "tetraedral",
                 "tetrahedral"                                              -> "tetraédrica";
            case "lineal", "linear"                                        -> "lineal";
            case "bipiramidal trigonal", "bipirámide trigonal",
                 "bipiramidale trigonal", "trigonal bipiramidal",
                 "bipiramide trigonal"                                     -> "bipiramidal trigonal";
            case "octaédrica", "octaedrica", "octahedral"                  -> "octaédrica";
            default -> n;
        };
    }

    private String normalizePolar(String s) {
        String n = s.trim().toLowerCase();
        return n.replace("no polar", "apolar").replace("no es polar", "apolar");
    }
}
