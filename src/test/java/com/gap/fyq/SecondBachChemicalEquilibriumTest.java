package com.gap.fyq;

import com.gap.fyq.model.secondbach.chemicalequilibrium.EquilibriumType;
import com.gap.fyq.model.secondbach.chemicalequilibrium.SecondBachChemicalEquilibriumExercise;
import com.gap.fyq.service.SecondBachChemicalEquilibriumService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
class SecondBachChemicalEquilibriumTest {

    @Autowired
    private SecondBachChemicalEquilibriumService service;

    // ── generateAndSave ──────────────────────────────────────────────────────

    @Test
    void generateAndSave_producesValidExercise() {
        SecondBachChemicalEquilibriumExercise ex = service.generateAndSave();

        assertThat(ex.getId()).isNotNull();
        assertThat(ex.getCourse()).isEqualTo("2BACH_Q");
        assertThat(ex.getBlock()).isEqualTo("BL3");
        assertThat(ex.getEquilibriumType()).isNotNull();
        assertThat(ex.getExerciseMode()).isNotBlank();
        assertThat(ex.getStatement()).isNotBlank();
        assertThat(ex.getCorrectAnswer()).isNotBlank();
        assertThat(ex.getCorrectAnswerDisplay()).isNotBlank();
        assertThat(ex.getUnit()).isNotBlank();
        assertThat(ex.getExplanation()).isNotBlank();
    }

    @Test
    void generate300_coversAllThreeTypes() {
        boolean sawHomo = false, sawLeChat = false, sawSol = false;
        for (int i = 0; i < 300; i++) {
            EquilibriumType t = service.generateAndSave().getEquilibriumType();
            if (t == EquilibriumType.HOMOGENEOUS_KC_KP)         sawHomo   = true;
            if (t == EquilibriumType.LE_CHATELIER_PERTURBATION) sawLeChat = true;
            if (t == EquilibriumType.SOLUBILITY_ION_COMMON)     sawSol    = true;
        }
        assertThat(sawHomo).isTrue();
        assertThat(sawLeChat).isTrue();
        assertThat(sawSol).isTrue();
    }

    @Test
    void generate300_coversAllSixModes() {
        boolean sawKc = false, sawKp = false, sawAlpha = false,
                sawMcq = false, sawPure = false, sawCommon = false;
        for (int i = 0; i < 300; i++) {
            String mode = service.generateAndSave().getExerciseMode();
            if ("KC_VALUE".equals(mode))               sawKc     = true;
            if ("KP_VALUE".equals(mode))               sawKp     = true;
            if ("ALPHA_VALUE".equals(mode))            sawAlpha  = true;
            if ("LE_CHATELIER_MCQ".equals(mode))       sawMcq    = true;
            if ("SOLUBILITY_PURE".equals(mode))        sawPure   = true;
            if ("SOLUBILITY_COMMON_ION".equals(mode))  sawCommon = true;
        }
        assertThat(sawKc).isTrue();
        assertThat(sawKp).isTrue();
        assertThat(sawAlpha).isTrue();
        assertThat(sawMcq).isTrue();
        assertThat(sawPure).isTrue();
        assertThat(sawCommon).isTrue();
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_returnsCorrectExercise() {
        SecondBachChemicalEquilibriumExercise ex = service.generateAndSave();
        SecondBachChemicalEquilibriumExercise found = service.findById(ex.getId());
        assertThat(found.getId()).isEqualTo(ex.getId());
        assertThat(found.getCorrectAnswer()).isEqualTo(ex.getCorrectAnswer());
    }

    @Test
    void findById_throwsForUnknownId() {
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class, () -> service.findById(Long.MAX_VALUE)
        )).isInstanceOf(IllegalArgumentException.class);
    }

    // ── validateAnswer — numérico ─────────────────────────────────────────────

    @Test
    void validateNumeric_exactValue_returnsTrue() {
        SecondBachChemicalEquilibriumExercise ex = new SecondBachChemicalEquilibriumExercise();
        ex.setExerciseMode("KC_VALUE");
        ex.setCorrectAnswer("0.04");
        assertThat(ex.validateAnswer("0.04")).isTrue();
    }

    @Test
    void validateNumeric_withinOnePct_returnsTrue() {
        SecondBachChemicalEquilibriumExercise ex = new SecondBachChemicalEquilibriumExercise();
        ex.setExerciseMode("KC_VALUE");
        ex.setCorrectAnswer("54.0");
        // 1% de 54 = 0.54 → rango [53.46, 54.54]
        assertThat(ex.validateAnswer("54.3")).isTrue();
        assertThat(ex.validateAnswer("53.5")).isTrue();
    }

    @Test
    void validateNumeric_outsideOnePct_returnsFalse() {
        SecondBachChemicalEquilibriumExercise ex = new SecondBachChemicalEquilibriumExercise();
        ex.setExerciseMode("KC_VALUE");
        ex.setCorrectAnswer("54.0");
        assertThat(ex.validateAnswer("55.0")).isFalse();
    }

    @Test
    void validateAlpha_twoPctTolerance_returnsTrue() {
        SecondBachChemicalEquilibriumExercise ex = new SecondBachChemicalEquilibriumExercise();
        ex.setExerciseMode("ALPHA_VALUE");
        ex.setCorrectAnswer("0.88");
        // 2% de 0.88 = 0.0176 → rango [0.8624, 0.8976]
        assertThat(ex.validateAnswer("0.895")).isTrue();
        assertThat(ex.validateAnswer("0.863")).isTrue();
    }

    @Test
    void validateNumeric_commaDecimalSeparator_accepted() {
        SecondBachChemicalEquilibriumExercise ex = new SecondBachChemicalEquilibriumExercise();
        ex.setExerciseMode("SOLUBILITY_PURE");
        ex.setCorrectAnswer(String.valueOf(Math.sqrt(1.8e-10)));
        assertThat(ex.validateAnswer(String.valueOf(Math.sqrt(1.8e-10)).replace(".", ","))).isTrue();
    }

    @Test
    void validateNumeric_nullAndBlank_returnsFalse() {
        SecondBachChemicalEquilibriumExercise ex = new SecondBachChemicalEquilibriumExercise();
        ex.setExerciseMode("KC_VALUE");
        ex.setCorrectAnswer("1.5");
        assertThat(ex.validateAnswer(null)).isFalse();
        assertThat(ex.validateAnswer("")).isFalse();
        assertThat(ex.validateAnswer("  ")).isFalse();
    }

    @Test
    void validateNumeric_nonNumeric_returnsFalse() {
        SecondBachChemicalEquilibriumExercise ex = new SecondBachChemicalEquilibriumExercise();
        ex.setExerciseMode("KC_VALUE");
        ex.setCorrectAnswer("1.5");
        assertThat(ex.validateAnswer("uno punto cinco")).isFalse();
    }

    // ── validateAnswer — LE_CHATELIER_MCQ ─────────────────────────────────────

    @Test
    void leChatelier_MCQ_caseInsensitive() {
        SecondBachChemicalEquilibriumExercise ex = new SecondBachChemicalEquilibriumExercise();
        ex.setExerciseMode("LE_CHATELIER_MCQ");
        ex.setCorrectAnswer("B");
        assertThat(ex.validateAnswer("B")).isTrue();
        assertThat(ex.validateAnswer("b")).isTrue();
        assertThat(ex.validateAnswer("  B  ")).isTrue();
        assertThat(ex.validateAnswer("A")).isFalse();
        assertThat(ex.validateAnswer("C")).isFalse();
    }

    @Test
    void leChatelier_MCQ_hasThreeOptions() {
        for (int i = 0; i < 100; i++) {
            SecondBachChemicalEquilibriumExercise ex = service.generateAndSave();
            if ("LE_CHATELIER_MCQ".equals(ex.getExerciseMode())) {
                assertThat(ex.getOptionA()).isNotBlank();
                assertThat(ex.getOptionB()).isNotBlank();
                assertThat(ex.getOptionC()).isNotBlank();
                assertThat(ex.getCorrectAnswer()).matches("[ABC]");
            }
        }
    }

    // ── Verificaciones matemáticas ────────────────────────────────────────────

    @Test
    void kc_HI_synthesis_isCorrect() {
        // H₂+I₂⇌2HI, Kc=54: α = √54/(2+√54)
        double kc  = 54.0;
        double sq  = Math.sqrt(kc);
        double alpha = sq / (2.0 + sq);
        assertThat(alpha).isBetween(0.0, 1.0);
        // Verificar que Kc se recupera: Kc=(2α)²/(1-α)²
        double kcRecover = Math.pow(2 * alpha, 2) / Math.pow(1 - alpha, 2);
        assertThat(kcRecover).isCloseTo(kc, within(0.01));
    }

    @Test
    void alpha_pcl5_solveQuadratic_isPhysicallyValid() {
        // PCl₅⇌PCl₃+Cl₂: c0=0.5, Kc=0.045 → 0.5α²+0.045α-0.045=0
        double alpha = SecondBachChemicalEquilibriumService
            .solveQuadraticPositive(0.5, 0.045, -0.045);
        assertThat(alpha).isBetween(0.0, 1.0);
        // Verificar Kc: α²·c0/(1-α)
        double kcCheck = alpha * alpha * 0.5 / (1 - alpha);
        assertThat(kcCheck).isCloseTo(0.045, within(0.001));
    }

    @Test
    void solubility_agcl_waterPure_isCorrect() {
        // AgCl: Ks=1.8e-10, s=√Ks
        double ks = 1.8e-10;
        double s  = Math.sqrt(ks);
        double ksCheck = s * s;
        assertThat(ksCheck).isCloseTo(ks, within(ks * 0.001));
    }

    @Test
    void solubility_agcl_commonIon_isLower_thanPure() {
        double ks   = 1.8e-10;
        double sPure   = Math.sqrt(ks);
        double sCommon = ks / 0.10;   // ion común Cl⁻ = 0.10 M
        assertThat(sCommon).isLessThan(sPure);
        // Reducción de al menos 3 órdenes de magnitud
        assertThat(sPure / sCommon).isGreaterThan(1000.0);
    }

    @Test
    void solubility_pbi2_waterPure_cubeRoot() {
        // PbI₂: Ks=9.8e-9, 4s³=Ks → s=∛(Ks/4)
        double ks  = 9.8e-9;
        double s   = Math.cbrt(ks / 4.0);
        double ksCheck = s * Math.pow(2 * s, 2);
        assertThat(ksCheck).isCloseTo(ks, within(ks * 0.001));
    }

    @Test
    void solubility_ag2cro4_cubeRoot() {
        // Ag₂CrO₄: Ks=1.12e-12, 4s³=Ks → s=∛(Ks/4)
        double ks = 1.12e-12;
        double s  = Math.cbrt(ks / 4.0);
        double ksCheck = Math.pow(2 * s, 2) * s;
        assertThat(ksCheck).isCloseTo(ks, within(ks * 0.001));
    }

    @Test
    void solubility_ca3po4_fifthRoot() {
        // Ca₃(PO₄)₂: Ks=2.07e-33, 108s⁵=Ks → s=⁵√(Ks/108)
        double ks = 2.07e-33;
        double s  = Math.pow(ks / 108.0, 0.2);
        double ksCheck = Math.pow(3 * s, 3) * Math.pow(2 * s, 2);
        assertThat(ksCheck).isCloseTo(ks, within(ks * 0.01));
    }

    @Test
    void kp_conversion_so3_deltaNMinus1() {
        // 2SO₂+O₂⇌2SO₃: Δn=-1, Kc=280, T=1000K, R=0.082
        double kc     = 280.0;
        double T      = 1000.0;
        double R_atm  = 0.082;
        double deltaN = -1;
        double kp     = kc * Math.pow(R_atm * T, deltaN);
        assertThat(kp).isCloseTo(kc / (R_atm * T), within(0.001));
        assertThat(kp).isLessThan(kc);  // Δn<0 → Kp < Kc
    }

    @Test
    void generateAll_kcValueAnswers_arePositive() {
        for (int i = 0; i < 200; i++) {
            SecondBachChemicalEquilibriumExercise ex = service.generateAndSave();
            if ("KC_VALUE".equals(ex.getExerciseMode())
                || "KP_VALUE".equals(ex.getExerciseMode())) {
                double val = Double.parseDouble(ex.getCorrectAnswer());
                assertThat(val).isGreaterThan(0.0);
            }
        }
    }

    @Test
    void generateAll_alphaValues_areBetweenZeroAndOne() {
        for (int i = 0; i < 200; i++) {
            SecondBachChemicalEquilibriumExercise ex = service.generateAndSave();
            if ("ALPHA_VALUE".equals(ex.getExerciseMode())) {
                double val = Double.parseDouble(ex.getCorrectAnswer());
                assertThat(val).isBetween(0.0, 1.0);
            }
        }
    }

    @Test
    void generateAll_solubilityValues_arePositiveAndTiny() {
        for (int i = 0; i < 200; i++) {
            SecondBachChemicalEquilibriumExercise ex = service.generateAndSave();
            if ("SOLUBILITY_PURE".equals(ex.getExerciseMode())
                || "SOLUBILITY_COMMON_ION".equals(ex.getExerciseMode())) {
                double val = Double.parseDouble(ex.getCorrectAnswer());
                assertThat(val).isGreaterThan(0.0);
                assertThat(val).isLessThan(1.0);  // toda sal poco soluble
            }
        }
    }

    @Test
    void solveQuadratic_rejectsNegativeRoot() {
        // ax²+bx+c: raíz positiva dentro de (0,1)
        double alpha = SecondBachChemicalEquilibriumService
            .solveQuadraticPositive(1.0, 2.0, -3.0);  // (x+3)(x-1)=0 → x=1
        assertThat(alpha).isCloseTo(1.0, within(0.001));
    }
}
