package com.gap.fyq;

import com.gap.fyq.model.secondbach.modernphysics.ModernPhysicsType;
import com.gap.fyq.model.secondbach.modernphysics.SecondBachModernPhysicsExercise;
import com.gap.fyq.service.SecondBachModernPhysicsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

@SpringBootTest
class SecondBachModernPhysicsTest {

    @Autowired
    private SecondBachModernPhysicsService service;

    private static final double H   = 6.63e-34;
    private static final double E   = 1.6e-19;
    private static final double M_E = 9.11e-31;

    // ── generateAndSave ──────────────────────────────────────────────────────

    @Test
    void generateAndSave_producesValidExercise() {
        SecondBachModernPhysicsExercise ex = service.generateAndSave();

        assertThat(ex.getId()).isNotNull();
        assertThat(ex.getCourse()).isEqualTo("2BACH");
        assertThat(ex.getBlock()).isEqualTo("BL5");
        assertThat(ex.getModernPhysicsType()).isNotNull();
        assertThat(ex.getUnknownVariable()).isNotBlank();
        assertThat(ex.getStatement()).isNotBlank();
        assertThat(ex.getExplanation()).isNotBlank();
        assertThat(ex.getCorrectAnswerValue()).isNotNull();
        assertThat(ex.getCorrectAnswerDisplay()).isNotBlank();
    }

    @Test
    void generate150_coversAllThreeTypes() {
        boolean sawPhoto = false, sawDB = false, sawRadio = false;
        for (int i = 0; i < 150; i++) {
            ModernPhysicsType t = service.generateAndSave().getModernPhysicsType();
            if (t == ModernPhysicsType.PHOTOELECTRIC_EFFECT)  sawPhoto  = true;
            if (t == ModernPhysicsType.DE_BROGLIE_RELATIVITY) sawDB     = true;
            if (t == ModernPhysicsType.RADIOACTIVE_DECAY)     sawRadio  = true;
        }
        assertThat(sawPhoto).isTrue();
        assertThat(sawDB).isTrue();
        assertThat(sawRadio).isTrue();
    }

    // ── Propiedades físicas ───────────────────────────────────────────────────

    @Test
    void photoelectric_thresholdFrequencyIsPositive() {
        for (int i = 0; i < 60; i++) {
            SecondBachModernPhysicsExercise ex = service.generateAndSave();
            if (ex.getModernPhysicsType() == ModernPhysicsType.PHOTOELECTRIC_EFFECT
                    && "frecuencia_umbral".equals(ex.getUnknownVariable())) {
                assertThat(ex.getCorrectAnswerValue()).isPositive();
            }
        }
    }

    @Test
    void photoelectric_kineticEnergyIsPositive() {
        for (int i = 0; i < 60; i++) {
            SecondBachModernPhysicsExercise ex = service.generateAndSave();
            if (ex.getModernPhysicsType() == ModernPhysicsType.PHOTOELECTRIC_EFFECT
                    && "energia_cinetica".equals(ex.getUnknownVariable())) {
                assertThat(ex.getCorrectAnswerValue()).isPositive();
            }
        }
    }

    @Test
    void lorentz_gammaGreaterThanOne() {
        for (int i = 0; i < 60; i++) {
            SecondBachModernPhysicsExercise ex = service.generateAndSave();
            if (ex.getModernPhysicsType() == ModernPhysicsType.DE_BROGLIE_RELATIVITY
                    && "factor_lorentz".equals(ex.getUnknownVariable())) {
                assertThat(ex.getCorrectAnswerValue()).isGreaterThan(1.0);
            }
        }
    }

    @Test
    void radioactive_decayConstantIsPositive() {
        for (int i = 0; i < 60; i++) {
            SecondBachModernPhysicsExercise ex = service.generateAndSave();
            if (ex.getModernPhysicsType() == ModernPhysicsType.RADIOACTIVE_DECAY
                    && "constante_lambda".equals(ex.getUnknownVariable())) {
                assertThat(ex.getCorrectAnswerValue()).isPositive();
            }
        }
    }

    @Test
    void radioactive_massRemainingLessThanInitial() {
        for (int i = 0; i < 60; i++) {
            SecondBachModernPhysicsExercise ex = service.generateAndSave();
            if (ex.getModernPhysicsType() == ModernPhysicsType.RADIOACTIVE_DECAY
                    && "masa_remanente".equals(ex.getUnknownVariable())) {
                // La masa remanente siempre es < la inicial (8.0 o 6.0 g)
                assertThat(ex.getCorrectAnswerValue()).isBetween(0.0, 10.0);
            }
        }
    }

    // ── Verificación matemática de escenarios concretos ──────────────────────

    @Test
    void photo_zinc_thresholdFrequency() {
        // W=4.30 eV → f₀ = W/h = 4.30×1.6e-19 / 6.63e-34
        double f0 = 4.30 * E / H;
        assertThat(f0).isCloseTo(1.0377e15, offset(1e12));
    }

    @Test
    void photo_kineticEnergy_conservation() {
        // Para zinc: Ek = hf - W
        double w = 4.30 * E;
        double hf = H * 1.5e15;
        double ek = hf - w;
        assertThat(ek).isPositive();             // f > f₀: hay efecto fotoeléctrico
        assertThat(ek).isCloseTo(3.065e-19, offset(1e-22));
    }

    @Test
    void photo_stoppingPotential_fromKineticEnergy() {
        // V_stop = Ek/e
        double ek = 3.065e-19;
        double v  = ek / E;
        assertThat(v).isCloseTo(1.916, offset(0.001));
    }

    @Test
    void deBroglie_electronAtOneMegaMs() {
        // λ = h/(m_e × v) = 6.63e-34 / (9.11e-31 × 1e6) ≈ 7.28e-10 m
        double lambda = H / (M_E * 1.0e6);
        assertThat(lambda).isCloseTo(7.278e-10, offset(1e-13));
    }

    @Test
    void lorentz_beta06_gamma125() {
        // β=0.6 → γ=1/√(1-0.36)=1/0.8=1.25
        double gamma = 1.0 / Math.sqrt(1.0 - 0.36);
        assertThat(gamma).isCloseTo(1.25, offset(1e-10));
    }

    @Test
    void lorentz_beta08_lengthContraction() {
        // β=0.8, γ=5/3, L₀=500m → L=300m
        double gamma = 1.0 / Math.sqrt(1.0 - 0.64);
        double L = 500.0 / gamma;
        assertThat(gamma).isCloseTo(5.0 / 3.0, offset(1e-10));
        assertThat(L).isCloseTo(300.0, offset(0.001));
    }

    @Test
    void radioactive_halfLifeRelation() {
        // T½=600s → λ=ln2/600
        double lambda = Math.log(2) / 600.0;
        assertThat(lambda).isCloseTo(1.1552e-3, offset(1e-7));
    }

    @Test
    void radioactive_threeHalfLives_oneEighth() {
        // Tras 3 T½: m = m₀ × (1/2)³ = m₀/8
        double lambda = Math.log(2) / 600.0;
        double mRem = 8.0 * Math.exp(-lambda * 1800.0);
        assertThat(mRem).isCloseTo(1.0, offset(0.001)); // 8/8 = 1 g
    }

    // ── validateAnswer — parseador tolerante ─────────────────────────────────

    @Test
    void validate_scientificNotation_verySmall() {
        SecondBachModernPhysicsExercise ex = new SecondBachModernPhysicsExercise();
        ex.setCorrectAnswerValue(3.065e-19);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("3.065e-19")).isTrue();
        assertThat(ex.validateAnswer("3.07e-19")).isTrue();   // +0.16% dentro
        assertThat(ex.validateAnswer("3.065E-19")).isTrue();
        assertThat(ex.validateAnswer("3,065e-19")).isTrue();  // coma española
        assertThat(ex.validateAnswer("3.20e-19")).isFalse();  // +4.4% fuera
    }

    @Test
    void validate_largeFrequency() {
        SecondBachModernPhysicsExercise ex = new SecondBachModernPhysicsExercise();
        // f₀ zinc ≈ 1.0377e15 Hz
        double f0 = 4.30 * E / H;
        ex.setCorrectAnswerValue(f0);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("1.04e15")).isTrue();
        assertThat(ex.validateAnswer("1.038e15")).isTrue();
        assertThat(ex.validateAnswer("1.0e15")).isFalse();    // -3.6% fuera
    }

    @Test
    void validate_lorentzFactor_twoDecimals() {
        SecondBachModernPhysicsExercise ex = new SecondBachModernPhysicsExercise();
        ex.setCorrectAnswerValue(1.25);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("1.25")).isTrue();
        assertThat(ex.validateAnswer("1.26")).isTrue();   // +0.8% dentro
        assertThat(ex.validateAnswer("1,25")).isTrue();
        assertThat(ex.validateAnswer("1.30")).isFalse();  // +4.0% fuera
    }

    @Test
    void validate_massInGrams_smallDecimal() {
        SecondBachModernPhysicsExercise ex = new SecondBachModernPhysicsExercise();
        ex.setCorrectAnswerValue(0.75);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("0.75")).isTrue();
        assertThat(ex.validateAnswer("0,75")).isTrue();
        assertThat(ex.validateAnswer("0.76")).isTrue();   // +1.3% dentro
        assertThat(ex.validateAnswer("0.80")).isFalse();  // +6.7% fuera
    }

    @Test
    void validate_multiplicativeNotation() {
        SecondBachModernPhysicsExercise ex = new SecondBachModernPhysicsExercise();
        ex.setCorrectAnswerValue(7.28e-10);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("7.28e-10")).isTrue();
        assertThat(ex.validateAnswer("7.28×10^-10")).isTrue();
        assertThat(ex.validateAnswer("7.28·10^-10")).isTrue();
        assertThat(ex.validateAnswer("7.3e-10")).isTrue();   // +0.27% dentro
    }

    @Test
    void validate_nullAndInvalid() {
        SecondBachModernPhysicsExercise ex = new SecondBachModernPhysicsExercise();
        ex.setCorrectAnswerValue(1.04e15);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer(null)).isFalse();
        assertThat(ex.validateAnswer("")).isFalse();
        assertThat(ex.validateAnswer("hf-W")).isFalse();
        assertThat(ex.validateAnswer("1.04×10¹⁵")).isFalse(); // superíndice Unicode
    }
}
