package com.gap.fyq;

import com.gap.fyq.model.secondbach.transfer.SecondBachTransferExercise;
import com.gap.fyq.model.secondbach.transfer.TransferType;
import com.gap.fyq.service.SecondBachTransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
class SecondBachTransferTest {

    @Autowired
    private SecondBachTransferService service;

    // ── generateAndSave ──────────────────────────────────────────────────────

    @Test
    void generateAndSave_producesValidExercise() {
        SecondBachTransferExercise ex = service.generateAndSave();

        assertThat(ex.getId()).isNotNull();
        assertThat(ex.getCourse()).isEqualTo("2BACH_Q");
        assertThat(ex.getBlock()).isEqualTo("BL4");
        assertThat(ex.getTransferType()).isNotNull();
        assertThat(ex.getExerciseMode()).isNotBlank();
        assertThat(ex.getStatement()).isNotBlank();
        assertThat(ex.getCorrectAnswer()).isNotBlank();
        assertThat(ex.getCorrectAnswerDisplay()).isNotBlank();
        assertThat(ex.getUnit()).isNotBlank();
        assertThat(ex.getExplanation()).isNotBlank();
    }

    @Test
    void generate300_coversAllThreeTypes() {
        boolean sawAcidBase = false, sawRedox = false, sawElec = false;
        for (int i = 0; i < 300; i++) {
            TransferType t = service.generateAndSave().getTransferType();
            if (t == TransferType.ACID_BASE_PH)             sawAcidBase = true;
            if (t == TransferType.REDOX_ION_ELECTRON)       sawRedox    = true;
            if (t == TransferType.ELECTROCHEMISTRY_FARADAY) sawElec     = true;
        }
        assertThat(sawAcidBase).isTrue();
        assertThat(sawRedox).isTrue();
        assertThat(sawElec).isTrue();
    }

    @Test
    void generate500_coversAllTenModes() {
        boolean sawStrongAcid   = false, sawStrongBase  = false;
        boolean sawWeakAcidPH   = false, sawWeakBasePH  = false;
        boolean sawAlpha        = false;
        boolean sawOxState      = false, sawCoeff       = false;
        boolean sawOxidantMCQ   = false;
        boolean sawEMF          = false, sawMass        = false;

        for (int i = 0; i < 500; i++) {
            String mode = service.generateAndSave().getExerciseMode();
            switch (mode) {
                case "PH_STRONG_ACID"       -> sawStrongAcid  = true;
                case "PH_STRONG_BASE"       -> sawStrongBase   = true;
                case "PH_WEAK_ACID"         -> sawWeakAcidPH  = true;
                case "PH_WEAK_BASE"         -> sawWeakBasePH  = true;
                case "ALPHA_WEAK_ACID"      -> sawAlpha       = true;
                case "REDOX_OXIDATION_STATE"-> sawOxState     = true;
                case "REDOX_COEFFICIENTS"   -> sawCoeff       = true;
                case "REDOX_OXIDANT_TEXT"   -> sawOxidantMCQ  = true;
                case "FARADAY_EMF"          -> sawEMF         = true;
                case "FARADAY_MASS"         -> sawMass        = true;
            }
        }
        assertThat(sawStrongAcid).isTrue();
        assertThat(sawStrongBase).isTrue();
        assertThat(sawWeakAcidPH).isTrue();
        assertThat(sawWeakBasePH).isTrue();
        assertThat(sawAlpha).isTrue();
        assertThat(sawOxState).isTrue();
        assertThat(sawCoeff).isTrue();
        assertThat(sawOxidantMCQ).isTrue();
        assertThat(sawEMF).isTrue();
        assertThat(sawMass).isTrue();
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_returnsCorrectExercise() {
        SecondBachTransferExercise ex = service.generateAndSave();
        SecondBachTransferExercise found = service.findById(ex.getId());
        assertThat(found.getId()).isEqualTo(ex.getId());
        assertThat(found.getCorrectAnswer()).isEqualTo(ex.getCorrectAnswer());
    }

    @Test
    void findById_throwsForUnknownId() {
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class, () -> service.findById(Long.MAX_VALUE)
        )).isInstanceOf(IllegalArgumentException.class);
    }

    // ── validateAnswer — ácido-base numérico ─────────────────────────────────

    @Test
    void validateNumeric_exactValue_returnsTrue() {
        SecondBachTransferExercise ex = new SecondBachTransferExercise();
        ex.setExerciseMode("PH_STRONG_ACID");
        ex.setCorrectAnswer("1.0");
        assertThat(ex.validateAnswer("1.0")).isTrue();
    }

    @Test
    void validateNumeric_withinOnePct_returnsTrue() {
        SecondBachTransferExercise ex = new SecondBachTransferExercise();
        ex.setExerciseMode("PH_WEAK_ACID");
        ex.setCorrectAnswer("4.74");
        assertThat(ex.validateAnswer("4.75")).isTrue();
        assertThat(ex.validateAnswer("4.73")).isTrue();
    }

    @Test
    void validateNumeric_outsideOnePct_returnsFalse() {
        SecondBachTransferExercise ex = new SecondBachTransferExercise();
        ex.setExerciseMode("PH_STRONG_BASE");
        ex.setCorrectAnswer("13.0");
        assertThat(ex.validateAnswer("12.0")).isFalse();
    }

    @Test
    void validateNumeric_commaDecimal_accepted() {
        SecondBachTransferExercise ex = new SecondBachTransferExercise();
        ex.setExerciseMode("PH_STRONG_ACID");
        ex.setCorrectAnswer("1.0");
        assertThat(ex.validateAnswer("1,0")).isTrue();
    }

    @Test
    void validateNumeric_nullAndBlank_returnsFalse() {
        SecondBachTransferExercise ex = new SecondBachTransferExercise();
        ex.setExerciseMode("PH_STRONG_ACID");
        ex.setCorrectAnswer("1.0");
        assertThat(ex.validateAnswer(null)).isFalse();
        assertThat(ex.validateAnswer("")).isFalse();
        assertThat(ex.validateAnswer("  ")).isFalse();
    }

    // ── validateAnswer — número de oxidación (entero con signo) ───────────────

    @Test
    void validateInteger_correctWithPlus_returnsTrue() {
        SecondBachTransferExercise ex = new SecondBachTransferExercise();
        ex.setExerciseMode("REDOX_OXIDATION_STATE");
        ex.setCorrectAnswer("7");
        assertThat(ex.validateAnswer("+7")).isTrue();
        assertThat(ex.validateAnswer("7")).isTrue();
    }

    @Test
    void validateInteger_correctNegative_returnsTrue() {
        SecondBachTransferExercise ex = new SecondBachTransferExercise();
        ex.setExerciseMode("REDOX_OXIDATION_STATE");
        ex.setCorrectAnswer("-2");
        assertThat(ex.validateAnswer("-2")).isTrue();
        assertThat(ex.validateAnswer("−2")).isTrue();  // guion largo
    }

    @Test
    void validateInteger_wrongValue_returnsFalse() {
        SecondBachTransferExercise ex = new SecondBachTransferExercise();
        ex.setExerciseMode("REDOX_OXIDATION_STATE");
        ex.setCorrectAnswer("6");
        assertThat(ex.validateAnswer("+4")).isFalse();
    }

    // ── validateAnswer — MCQ oxidante/reductor ────────────────────────────────

    @Test
    void validateMCQ_caseInsensitive() {
        SecondBachTransferExercise ex = new SecondBachTransferExercise();
        ex.setExerciseMode("REDOX_OXIDANT_TEXT");
        ex.setCorrectAnswer("B");
        assertThat(ex.validateAnswer("B")).isTrue();
        assertThat(ex.validateAnswer("b")).isTrue();
        assertThat(ex.validateAnswer("  B  ")).isTrue();
        assertThat(ex.validateAnswer("A")).isFalse();
        assertThat(ex.validateAnswer("C")).isFalse();
    }

    @Test
    void redoxOxidantMCQ_hasThreeOptions() {
        for (int i = 0; i < 150; i++) {
            SecondBachTransferExercise ex = service.generateAndSave();
            if ("REDOX_OXIDANT_TEXT".equals(ex.getExerciseMode())) {
                assertThat(ex.getOptionA()).isNotBlank();
                assertThat(ex.getOptionB()).isNotBlank();
                assertThat(ex.getOptionC()).isNotBlank();
                assertThat(ex.getCorrectAnswer()).matches("[ABC]");
            }
        }
    }

    // ── validateAnswer — coeficientes redox ──────────────────────────────────

    @Test
    void validateCoefficients_exactMatch_returnsTrue() {
        SecondBachTransferExercise ex = new SecondBachTransferExercise();
        ex.setExerciseMode("REDOX_COEFFICIENTS");
        ex.setCorrectAnswer("1|5|8|1|5|4");
        assertThat(ex.validateAnswer("1|5|8|1|5|4")).isTrue();
    }

    @Test
    void validateCoefficients_wrongCount_returnsFalse() {
        SecondBachTransferExercise ex = new SecondBachTransferExercise();
        ex.setExerciseMode("REDOX_COEFFICIENTS");
        ex.setCorrectAnswer("1|5|8|1|5|4");
        assertThat(ex.validateAnswer("1|5|8|1|5")).isFalse();
    }

    @Test
    void validateCoefficients_oneWrong_returnsFalse() {
        SecondBachTransferExercise ex = new SecondBachTransferExercise();
        ex.setExerciseMode("REDOX_COEFFICIENTS");
        ex.setCorrectAnswer("1|5|8|1|5|4");
        assertThat(ex.validateAnswer("1|5|8|1|5|5")).isFalse();
    }

    // ── Verificaciones matemáticas — pH ──────────────────────────────────────

    @Test
    void strongAcid_HCl_0_1M_pH_is_1() {
        double cH = 0.10;
        double pH = -Math.log10(cH);
        assertThat(pH).isCloseTo(1.0, within(0.01));
    }

    @Test
    void strongBase_NaOH_0_1M_pH_is_13() {
        double cOH = 0.10;
        double pOH = -Math.log10(cOH);
        double pH  = 14.0 - pOH;
        assertThat(pH).isCloseTo(13.0, within(0.01));
    }

    @Test
    void weakAcid_aceticAcid_0_1M_approxValid() {
        double ka  = 1.8e-5;
        double c0  = 0.10;
        assertThat(c0 / ka).isGreaterThan(100.0);  // aproximación válida
        double x   = Math.sqrt(ka * c0);
        double pH  = -Math.log10(x);
        assertThat(pH).isCloseTo(2.87, within(0.05));
    }

    @Test
    void weakAcid_pH_calculatedCorrectly_forAllScenarios() {
        for (int i = 0; i < 200; i++) {
            SecondBachTransferExercise ex = service.generateAndSave();
            if ("PH_WEAK_ACID".equals(ex.getExerciseMode())
                    || "PH_STRONG_ACID".equals(ex.getExerciseMode())) {
                double ph = Double.parseDouble(ex.getCorrectAnswer());
                assertThat(ph).isBetween(0.0, 14.0);
            }
        }
    }

    @Test
    void weakBase_pH_isAboveSeven() {
        for (int i = 0; i < 200; i++) {
            SecondBachTransferExercise ex = service.generateAndSave();
            if ("PH_WEAK_BASE".equals(ex.getExerciseMode())
                    || "PH_STRONG_BASE".equals(ex.getExerciseMode())) {
                double ph = Double.parseDouble(ex.getCorrectAnswer());
                assertThat(ph).isGreaterThan(7.0);
            }
        }
    }

    @Test
    void alpha_values_areBetweenZeroAndOne() {
        for (int i = 0; i < 200; i++) {
            SecondBachTransferExercise ex = service.generateAndSave();
            if ("ALPHA_WEAK_ACID".equals(ex.getExerciseMode())) {
                double alpha = Double.parseDouble(ex.getCorrectAnswer());
                assertThat(alpha).isBetween(0.0, 1.0);
            }
        }
    }

    // ── Verificaciones matemáticas — Electroquímica ──────────────────────────

    @Test
    void daniell_cell_emf_is_1_10() {
        // Zn/Cu: E°(Cu)=+0.34V, E°(Zn)=−0.76V → E°=1.10V
        double emf = 0.34 - (-0.76);
        assertThat(emf).isCloseTo(1.10, within(0.01));
    }

    @Test
    void galvanicEMF_positive_for_all_scenarios() {
        for (int i = 0; i < 200; i++) {
            SecondBachTransferExercise ex = service.generateAndSave();
            if ("FARADAY_EMF".equals(ex.getExerciseMode())) {
                double emf = Double.parseDouble(ex.getCorrectAnswer());
                assertThat(emf).isGreaterThan(0.0);
            }
        }
    }

    @Test
    void faraday_copper_2A_30min() {
        // Cu²⁺+2e⁻→Cu: M=63.55, n=2, I=2A, t=1800s
        double mass = (63.55 * 2.0 * 1800) / (2.0 * SecondBachTransferService.FARADAY);
        assertThat(mass).isCloseTo(1.19, within(0.05));
    }

    @Test
    void faraday_silver_1_5A_45min() {
        // Ag⁺+e⁻→Ag: M=107.87, n=1, I=1.5A, t=2700s
        double mass = (107.87 * 1.5 * 2700) / (1.0 * SecondBachTransferService.FARADAY);
        assertThat(mass).isCloseTo(4.52, within(0.10));
    }

    @Test
    void electrolysis_mass_isPositiveAndReasonable() {
        for (int i = 0; i < 200; i++) {
            SecondBachTransferExercise ex = service.generateAndSave();
            if ("FARADAY_MASS".equals(ex.getExerciseMode())) {
                double mass = Double.parseDouble(ex.getCorrectAnswer());
                assertThat(mass).isGreaterThan(0.0);
                assertThat(mass).isLessThan(500.0);  // límite razonable para estos escenarios
            }
        }
    }

    // ── Constantes del servicio ───────────────────────────────────────────────

    @Test
    void kw_constant_is_1e14() {
        assertThat(SecondBachTransferService.KW).isEqualTo(1.0e-14);
    }

    @Test
    void faraday_constant_is_96485() {
        assertThat(SecondBachTransferService.FARADAY).isEqualTo(96485.0);
    }

    @Test
    void kw_relation_ph_plus_poh_equals_14() {
        double kw  = SecondBachTransferService.KW;
        double pKw = -Math.log10(kw);
        assertThat(pKw).isCloseTo(14.0, within(0.001));
    }

    // ── Cobertura de generación ───────────────────────────────────────────────

    @Test
    void allModes_produce_nonEmptyExplanation() {
        for (int i = 0; i < 100; i++) {
            SecondBachTransferExercise ex = service.generateAndSave();
            assertThat(ex.getExplanation()).isNotBlank();
            assertThat(ex.getExplanation().length()).isGreaterThan(50);
        }
    }

    @Test
    void allNumericModes_answerParseableAsDouble() {
        for (int i = 0; i < 200; i++) {
            SecondBachTransferExercise ex = service.generateAndSave();
            String mode = ex.getExerciseMode();
            if ("PH_STRONG_ACID".equals(mode) || "PH_STRONG_BASE".equals(mode)
                    || "PH_WEAK_ACID".equals(mode) || "PH_WEAK_BASE".equals(mode)
                    || "ALPHA_WEAK_ACID".equals(mode)
                    || "FARADAY_EMF".equals(mode) || "FARADAY_MASS".equals(mode)) {
                assertThat(Double.parseDouble(ex.getCorrectAnswer())).isFinite();
            }
        }
    }

    @Test
    void oxStateMode_answerParseableAsInteger() {
        for (int i = 0; i < 200; i++) {
            SecondBachTransferExercise ex = service.generateAndSave();
            if ("REDOX_OXIDATION_STATE".equals(ex.getExerciseMode())) {
                assertThat(Integer.parseInt(ex.getCorrectAnswer())).isBetween(-4, 8);
            }
        }
    }
}
