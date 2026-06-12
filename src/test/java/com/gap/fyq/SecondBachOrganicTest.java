package com.gap.fyq;

import com.gap.fyq.model.secondbach.organic.OrganicType;
import com.gap.fyq.model.secondbach.organic.SecondBachOrganicExercise;
import com.gap.fyq.service.SecondBachOrganicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class SecondBachOrganicTest {

    @Autowired
    SecondBachOrganicService service;

    // ── básicos ──────────────────────────────────────────────────────────────

    @Test
    void generateAndSave_producesValidExercise() {
        SecondBachOrganicExercise ex = service.generateAndSave();

        assertThat(ex.getId()).isNotNull().isPositive();
        assertThat(ex.getCourse()).isEqualTo("2BACH_Q");
        assertThat(ex.getBlock()).isEqualTo("BL5");
        assertThat(ex.getOrganicType()).isNotNull();
        assertThat(ex.getExerciseMode()).isNotBlank();
        assertThat(ex.getStatement()).isNotBlank();
        assertThat(ex.getCorrectAnswer()).isNotBlank();
        assertThat(ex.getCorrectAnswerDisplay()).isNotBlank();
        assertThat(ex.getExplanation()).isNotBlank();
    }

    @Test
    void generate_300_coversAllThreeOrganicTypes() {
        Set<OrganicType> seen = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            seen.add(service.generateAndSave().getOrganicType());
        }
        assertThat(seen).containsExactlyInAnyOrder(
            OrganicType.ORGANIC_REACTIONS_RULES,
            OrganicType.SPATIAL_ISOMERISM,
            OrganicType.POLYMERS_INDUSTRY
        );
    }

    @Test
    void generate_500_coversAllSevenModes() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            seen.add(service.generateAndSave().getExerciseMode());
        }
        assertThat(seen).containsExactlyInAnyOrder(
            "REACTION_TYPE_MCQ",
            "MARKOVNIKOV_PRODUCT",
            "SAYTZEFF_PRODUCT",
            "GEOMETRIC_ISOMERS_MCQ",
            "CHIRAL_CARBONS_COUNT",
            "POLYMER_MONOMER_MCQ",
            "POLYMER_TYPE_MCQ"
        );
    }

    @Test
    void findById_returnsCorrectExercise() {
        SecondBachOrganicExercise ex = service.generateAndSave();
        SecondBachOrganicExercise found = service.findById(ex.getId());
        assertThat(found.getId()).isEqualTo(ex.getId());
        assertThat(found.getExerciseMode()).isEqualTo(ex.getExerciseMode());
    }

    @Test
    void findById_throwsForUnknownId() {
        assertThatThrownBy(() -> service.findById(999_999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("999999");
    }

    // ── validación MCQ ────────────────────────────────────────────────────────

    @Test
    void validateMCQ_caseSensitiveLetterAccepted() {
        SecondBachOrganicExercise ex = generateOfMode("REACTION_TYPE_MCQ");
        String correct = ex.getCorrectAnswer(); // "A", "B" o "C"
        assertThat(ex.validateAnswer(correct)).isTrue();
        assertThat(ex.validateAnswer(correct.toLowerCase())).isTrue();
    }

    @Test
    void validateMCQ_wrongLetterRejected() {
        SecondBachOrganicExercise ex = generateOfMode("REACTION_TYPE_MCQ");
        String correct = ex.getCorrectAnswer();
        String wrong = correct.equals("A") ? "B" : "A";
        assertThat(ex.validateAnswer(wrong)).isFalse();
    }

    @Test
    void validateMCQ_nullAndBlankRejected() {
        SecondBachOrganicExercise ex = generateOfMode("MARKOVNIKOV_PRODUCT");
        assertThat(ex.validateAnswer(null)).isFalse();
        assertThat(ex.validateAnswer("")).isFalse();
        assertThat(ex.validateAnswer("   ")).isFalse();
    }

    // ── validación entero ─────────────────────────────────────────────────────

    @Test
    void validateInteger_correctCountAccepted() {
        SecondBachOrganicExercise ex = generateOfMode("CHIRAL_CARBONS_COUNT");
        assertThat(ex.validateAnswer(ex.getCorrectAnswer())).isTrue();
    }

    @Test
    void validateInteger_wrongCountRejected() {
        SecondBachOrganicExercise ex = generateOfMode("CHIRAL_CARBONS_COUNT");
        int correct = Integer.parseInt(ex.getCorrectAnswer());
        assertThat(ex.validateAnswer(String.valueOf(correct + 1))).isFalse();
    }

    @Test
    void validateInteger_textRejected() {
        SecondBachOrganicExercise ex = generateOfMode("CHIRAL_CARBONS_COUNT");
        assertThat(ex.validateAnswer("abc")).isFalse();
    }

    // ── escenarios de reacción: MCQ tiene opciones A/B/C y tipo correcto ─────

    @Test
    void reactionType_correctAnswerIsValidLetter() {
        for (int i = 0; i < 50; i++) {
            SecondBachOrganicExercise ex = generateOfMode("REACTION_TYPE_MCQ");
            assertThat(ex.getCorrectAnswer()).isIn("A", "B", "C");
            assertThat(ex.getOptionA()).isNotBlank();
            assertThat(ex.getOptionB()).isNotBlank();
            assertThat(ex.getOptionC()).isNotBlank();
        }
    }

    // ── Markovnikov ───────────────────────────────────────────────────────────

    @Test
    void markovnikov_correctAnswerIsValidLetter() {
        for (int i = 0; i < 50; i++) {
            SecondBachOrganicExercise ex = generateOfMode("MARKOVNIKOV_PRODUCT");
            assertThat(ex.getCorrectAnswer()).isIn("A", "B", "C");
            assertThat(ex.getExplanation()).containsIgnoringCase("Markovnikov");
        }
    }

    @Test
    void markovnikov_explanationContainsCarbocation() {
        SecondBachOrganicExercise ex = generateOfMode("MARKOVNIKOV_PRODUCT");
        assertThat(ex.getExplanation()).containsIgnoringCase("carbocatión");
    }

    // ── Saytzeff ──────────────────────────────────────────────────────────────

    @Test
    void saytzeff_correctAnswerIsValidLetter() {
        for (int i = 0; i < 50; i++) {
            SecondBachOrganicExercise ex = generateOfMode("SAYTZEFF_PRODUCT");
            assertThat(ex.getCorrectAnswer()).isIn("A", "B", "C");
            assertThat(ex.getExplanation()).containsIgnoringCase("Saytzeff");
        }
    }

    @Test
    void saytzeff_explanationMentionsMostSubstituted() {
        SecondBachOrganicExercise ex = generateOfMode("SAYTZEFF_PRODUCT");
        assertThat(ex.getExplanation()).containsIgnoringCase("sustituido");
    }

    // ── Isomería geométrica ───────────────────────────────────────────────────

    @Test
    void geometricIsomers_correctAnswerIsAorB() {
        for (int i = 0; i < 100; i++) {
            SecondBachOrganicExercise ex = generateOfMode("GEOMETRIC_ISOMERS_MCQ");
            assertThat(ex.getCorrectAnswer()).isIn("A", "B");
        }
    }

    @Test
    void geometricIsomers_but2ene_hasGeometric() {
        // but-2-eno siempre tiene isomería geométrica → respuesta A
        boolean found = false;
        for (int i = 0; i < 300 && !found; i++) {
            SecondBachOrganicExercise ex = generateOfMode("GEOMETRIC_ISOMERS_MCQ");
            if (ex.getStatement().contains("but-2-eno")) {
                assertThat(ex.getCorrectAnswer()).isEqualTo("A");
                found = true;
            }
        }
        // si no apareció en 300 iteraciones, el test pasa (probabilidad: 1-((7/8)^300) ≈ 1)
    }

    @Test
    void geometricIsomers_but1ene_noGeometric() {
        boolean found = false;
        for (int i = 0; i < 300 && !found; i++) {
            SecondBachOrganicExercise ex = generateOfMode("GEOMETRIC_ISOMERS_MCQ");
            if (ex.getStatement().contains("but-1-eno")) {
                assertThat(ex.getCorrectAnswer()).isEqualTo("B");
                found = true;
            }
        }
    }

    // ── Carbonos quirales ─────────────────────────────────────────────────────

    @Test
    void chiralCarbons_countIsNonNegative() {
        for (int i = 0; i < 50; i++) {
            SecondBachOrganicExercise ex = generateOfMode("CHIRAL_CARBONS_COUNT");
            int count = Integer.parseInt(ex.getCorrectAnswer());
            assertThat(count).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    void chiralCarbons_lacticAcid_has1ChiralCarbon() {
        boolean found = false;
        for (int i = 0; i < 300 && !found; i++) {
            SecondBachOrganicExercise ex = generateOfMode("CHIRAL_CARBONS_COUNT");
            if (ex.getStatement().contains("láctico")) {
                assertThat(ex.getCorrectAnswer()).isEqualTo("1");
                found = true;
            }
        }
    }

    @Test
    void chiralCarbons_glycine_has0ChiralCarbons() {
        boolean found = false;
        for (int i = 0; i < 300 && !found; i++) {
            SecondBachOrganicExercise ex = generateOfMode("CHIRAL_CARBONS_COUNT");
            if (ex.getStatement().contains("glicina")) {
                assertThat(ex.getCorrectAnswer()).isEqualTo("0");
                found = true;
            }
        }
    }

    @Test
    void chiralCarbons_tartaric_has2ChiralCarbons() {
        boolean found = false;
        for (int i = 0; i < 300 && !found; i++) {
            SecondBachOrganicExercise ex = generateOfMode("CHIRAL_CARBONS_COUNT");
            if (ex.getStatement().contains("tartrato") || ex.getStatement().contains("tart")) {
                assertThat(ex.getCorrectAnswer()).isEqualTo("2");
                found = true;
            }
        }
    }

    @Test
    void chiralCarbons_glycerol_has0ChiralCarbons() {
        boolean found = false;
        for (int i = 0; i < 300 && !found; i++) {
            SecondBachOrganicExercise ex = generateOfMode("CHIRAL_CARBONS_COUNT");
            if (ex.getStatement().contains("glicerol")) {
                assertThat(ex.getCorrectAnswer()).isEqualTo("0");
                found = true;
            }
        }
    }

    // ── Polímeros — monómero ──────────────────────────────────────────────────

    @Test
    void polymerMonomer_correctAnswerIsValidLetter() {
        for (int i = 0; i < 50; i++) {
            SecondBachOrganicExercise ex = generateOfMode("POLYMER_MONOMER_MCQ");
            assertThat(ex.getCorrectAnswer()).isIn("A", "B", "C");
        }
    }

    @Test
    void polymerMonomer_nylon_monomerIsHexamethylenediamine() {
        boolean found = false;
        for (int i = 0; i < 300 && !found; i++) {
            SecondBachOrganicExercise ex = generateOfMode("POLYMER_MONOMER_MCQ");
            if (ex.getStatement().contains("Nylon")) {
                assertThat(ex.getCorrectAnswerDisplay()).containsIgnoringCase("Hexametilendiamina");
                found = true;
            }
        }
    }

    @Test
    void polymerMonomer_pet_monomerIsEthyleneGlycolAndTerephthalic() {
        boolean found = false;
        for (int i = 0; i < 300 && !found; i++) {
            SecondBachOrganicExercise ex = generateOfMode("POLYMER_MONOMER_MCQ");
            if (ex.getStatement().contains("PET")) {
                assertThat(ex.getCorrectAnswerDisplay()).containsIgnoringCase("Etilenglicol");
                found = true;
            }
        }
    }

    // ── Polímeros — tipo ──────────────────────────────────────────────────────

    @Test
    void polymerType_correctAnswerIsValidLetter() {
        for (int i = 0; i < 50; i++) {
            SecondBachOrganicExercise ex = generateOfMode("POLYMER_TYPE_MCQ");
            assertThat(ex.getCorrectAnswer()).isIn("A", "B", "C");
        }
    }

    @Test
    void polymerType_pe_isAddition() {
        boolean found = false;
        for (int i = 0; i < 300 && !found; i++) {
            SecondBachOrganicExercise ex = generateOfMode("POLYMER_TYPE_MCQ");
            if (ex.getStatement().contains("Polietileno")) {
                assertThat(ex.getCorrectAnswerDisplay()).containsIgnoringCase("Adición");
                found = true;
            }
        }
    }

    @Test
    void polymerType_nylon_isCondensation() {
        boolean found = false;
        for (int i = 0; i < 300 && !found; i++) {
            SecondBachOrganicExercise ex = generateOfMode("POLYMER_TYPE_MCQ");
            if (ex.getStatement().contains("Nylon")) {
                assertThat(ex.getCorrectAnswerDisplay()).containsIgnoringCase("Condensación");
                found = true;
            }
        }
    }

    @Test
    void polymerType_pet_isCondensation() {
        boolean found = false;
        for (int i = 0; i < 300 && !found; i++) {
            SecondBachOrganicExercise ex = generateOfMode("POLYMER_TYPE_MCQ");
            if (ex.getStatement().contains("PET") || ex.getStatement().contains("poliéster")) {
                assertThat(ex.getCorrectAnswerDisplay()).containsIgnoringCase("Condensación");
                found = true;
            }
        }
    }

    // ── normalizeIupac ────────────────────────────────────────────────────────

    @Test
    void normalizeIupac_stripsSpacesAndHyphens() {
        assertThat(SecondBachOrganicExercise.normalizeIupac("but-2-eno"))
            .isEqualTo("but2eno");
    }

    @Test
    void normalizeIupac_handlesAccents() {
        assertThat(SecondBachOrganicExercise.normalizeIupac("ácido láctico"))
            .isEqualTo("acidolactico");
    }

    @Test
    void normalizeIupac_nullReturnsEmpty() {
        assertThat(SecondBachOrganicExercise.normalizeIupac(null)).isEmpty();
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private SecondBachOrganicExercise generateOfMode(String mode) {
        for (int attempt = 0; attempt < 500; attempt++) {
            SecondBachOrganicExercise ex = service.generateAndSave();
            if (mode.equals(ex.getExerciseMode())) return ex;
        }
        throw new AssertionError("No se generó ningún ejercicio con mode=" + mode + " en 500 intentos");
    }
}
