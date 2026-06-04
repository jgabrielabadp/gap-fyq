package com.gap.fyq;

import com.gap.fyq.model.secondbach.optics.OpticsType;
import com.gap.fyq.model.secondbach.optics.SecondBachOpticsExercise;
import com.gap.fyq.service.SecondBachOpticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

@SpringBootTest
class SecondBachOpticsTest {

    @Autowired
    private SecondBachOpticsService service;

    // ── generateAndSave ──────────────────────────────────────────────────────

    @Test
    void generateAndSave_producesValidExercise() {
        SecondBachOpticsExercise ex = service.generateAndSave();

        assertThat(ex.getId()).isNotNull();
        assertThat(ex.getCourse()).isEqualTo("2BACH");
        assertThat(ex.getBlock()).isEqualTo("BL4");
        assertThat(ex.getOpticsType()).isNotNull();
        assertThat(ex.getUnknownVariable()).isNotBlank();
        assertThat(ex.getStatement()).isNotBlank();
        assertThat(ex.getExplanation()).isNotBlank();
        assertThat(ex.getCorrectAnswerValue()).isNotNull();
        assertThat(ex.getCorrectAnswerDisplay()).isNotBlank();
    }

    @Test
    void generate150_coversAllThreeTypes() {
        boolean sawSnell = false, sawLens = false, sawEye = false;
        for (int i = 0; i < 150; i++) {
            OpticsType t = service.generateAndSave().getOpticsType();
            if (t == OpticsType.SNELL_REFRACTION_LIMIT) sawSnell = true;
            if (t == OpticsType.GEOMETRIC_LENSES)       sawLens  = true;
            if (t == OpticsType.EYE_DEFECTS_DIOPTERS)   sawEye   = true;
        }
        assertThat(sawSnell).isTrue();
        assertThat(sawLens).isTrue();
        assertThat(sawEye).isTrue();
    }

    // ── Propiedades físicas ───────────────────────────────────────────────────

    @Test
    void snell_refractionAngleInValidRange() {
        for (int i = 0; i < 60; i++) {
            SecondBachOpticsExercise ex = service.generateAndSave();
            if (ex.getOpticsType() == OpticsType.SNELL_REFRACTION_LIMIT
                    && "angulo_refraccion".equals(ex.getUnknownVariable())) {
                assertThat(ex.getCorrectAnswerValue()).isBetween(0.0, 90.0);
            }
        }
    }

    @Test
    void snell_limitAngleInValidRange() {
        for (int i = 0; i < 60; i++) {
            SecondBachOpticsExercise ex = service.generateAndSave();
            if (ex.getOpticsType() == OpticsType.SNELL_REFRACTION_LIMIT
                    && "angulo_limite".equals(ex.getUnknownVariable())) {
                assertThat(ex.getCorrectAnswerValue()).isBetween(0.0, 90.0);
            }
        }
    }

    @Test
    void snell_velocityLessThanC() {
        for (int i = 0; i < 60; i++) {
            SecondBachOpticsExercise ex = service.generateAndSave();
            if (ex.getOpticsType() == OpticsType.SNELL_REFRACTION_LIMIT
                    && "velocidad_medio".equals(ex.getUnknownVariable())) {
                assertThat(ex.getCorrectAnswerValue())
                    .isGreaterThan(0.0)
                    .isLessThanOrEqualTo(3.0e8);
            }
        }
    }

    @Test
    void lens_divergingProducesVirtualImage() {
        for (int i = 0; i < 60; i++) {
            SecondBachOpticsExercise ex = service.generateAndSave();
            if (ex.getOpticsType() == OpticsType.GEOMETRIC_LENSES
                    && "posicion_imagen".equals(ex.getUnknownVariable())
                    && ex.getStatement().contains("divergente")) {
                // Lente divergente con objeto real siempre da imagen virtual (s' < 0)
                assertThat(ex.getCorrectAnswerValue()).isNegative();
            }
        }
    }

    @Test
    void eye_myopiaGivesNegativePower() {
        for (int i = 0; i < 60; i++) {
            SecondBachOpticsExercise ex = service.generateAndSave();
            if (ex.getOpticsType() == OpticsType.EYE_DEFECTS_DIOPTERS
                    && ex.getStatement().contains("miope")) {
                assertThat(ex.getCorrectAnswerValue()).isNegative();
            }
        }
    }

    @Test
    void eye_hyperopiaGivesPositivePower() {
        for (int i = 0; i < 60; i++) {
            SecondBachOpticsExercise ex = service.generateAndSave();
            if (ex.getOpticsType() == OpticsType.EYE_DEFECTS_DIOPTERS
                    && ex.getStatement().contains("hipermétrope")) {
                assertThat(ex.getCorrectAnswerValue()).isPositive();
            }
        }
    }

    // ── Verificación matemática de escenarios concretos ──────────────────────

    @Test
    void snell_airToGlass_30deg_refraction() {
        // n1=1, n2=1.5, θi=30° → sin(θr)=0.5/1.5=0.333 → θr≈19.47°
        double sinR = 1.0 * Math.sin(Math.toRadians(30)) / 1.5;
        double expected = Math.toDegrees(Math.asin(sinR));
        assertThat(expected).isCloseTo(19.47, offset(0.01));
    }

    @Test
    void snell_glassToAir_criticalAngle() {
        // n1=1.5, n2=1.0 → θc=arcsin(1/1.5)≈41.81°
        double expected = Math.toDegrees(Math.asin(1.0 / 1.5));
        assertThat(expected).isCloseTo(41.81, offset(0.01));
    }

    @Test
    void lens_convergent_f20_s30_imagePosition() {
        // 1/s' = 1/20 + 1/(-30) = 3/60 - 2/60 = 1/60 → s'=60
        double sPrime = 1.0 / (1.0 / 20.0 + 1.0 / (-30.0));
        assertThat(sPrime).isCloseTo(60.0, offset(0.001));
    }

    @Test
    void lens_divergent_fm20_s30_imageVirtual() {
        // 1/s' = 1/(-20) + 1/(-30) = -3/60-2/60=-5/60 → s'=-12
        double sPrime = 1.0 / (1.0 / (-20.0) + 1.0 / (-30.0));
        assertThat(sPrime).isCloseTo(-12.0, offset(0.001));
        assertThat(sPrime).isNegative();  // virtual image
    }

    @Test
    void eye_myopia_PR200cm() {
        // PR=2m → P=-1/2=-0.5 D
        double potency = -1.0 / 2.0;
        assertThat(potency).isCloseTo(-0.50, offset(0.001));
    }

    @Test
    void eye_hyperopia_PP50cm() {
        // PP=0.5m → P=4-1/0.5=4-2=2.0 D
        double potency = 4.0 - 1.0 / 0.5;
        assertThat(potency).isCloseTo(2.00, offset(0.001));
    }

    // ── validateAnswer — formatos de entrada ─────────────────────────────────

    @Test
    void validate_refractionAngle_degrees() {
        SecondBachOpticsExercise ex = new SecondBachOpticsExercise();
        // θr = arcsin(sin30°/1.5) ≈ 19.47°
        double sinR = Math.sin(Math.toRadians(30)) / 1.5;
        double thetaR = Math.toDegrees(Math.asin(sinR));
        ex.setCorrectAnswerValue(thetaR);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("19.47")).isTrue();
        assertThat(ex.validateAnswer("19.5")).isTrue();   // +0.15 % dentro
        assertThat(ex.validateAnswer("19,47")).isTrue();  // coma como decimal
        assertThat(ex.validateAnswer("20")).isFalse();    // +2.7 % fuera
    }

    @Test
    void validate_imagePosition_withSign() {
        SecondBachOpticsExercise ex = new SecondBachOpticsExercise();
        ex.setCorrectAnswerValue(60.0);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("60")).isTrue();
        assertThat(ex.validateAnswer("60.00")).isTrue();
        assertThat(ex.validateAnswer("60,00")).isTrue();
        assertThat(ex.validateAnswer("-60")).isFalse();   // signo incorrecto
    }

    @Test
    void validate_virtualImage_negativeSign() {
        SecondBachOpticsExercise ex = new SecondBachOpticsExercise();
        ex.setCorrectAnswerValue(-12.0);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("-12")).isTrue();
        assertThat(ex.validateAnswer("-12.00")).isTrue();
        assertThat(ex.validateAnswer("-12,00")).isTrue();
        assertThat(ex.validateAnswer("12")).isFalse();    // signo incorrecto
    }

    @Test
    void validate_myopia_negativeDiopters() {
        SecondBachOpticsExercise ex = new SecondBachOpticsExercise();
        ex.setCorrectAnswerValue(-0.50);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("-0.50")).isTrue();
        assertThat(ex.validateAnswer("-0,50")).isTrue();
        assertThat(ex.validateAnswer("-0.5")).isTrue();
        assertThat(ex.validateAnswer("0.50")).isFalse();  // signo incorrecto
    }

    @Test
    void validate_velocity_scientificNotation() {
        SecondBachOpticsExercise ex = new SecondBachOpticsExercise();
        ex.setCorrectAnswerValue(2.0e8);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("2e8")).isTrue();
        assertThat(ex.validateAnswer("2.0e8")).isTrue();
        assertThat(ex.validateAnswer("2.00e8")).isTrue();
        assertThat(ex.validateAnswer("200000000")).isTrue();
        assertThat(ex.validateAnswer("1.5e8")).isFalse();  // −25 % fuera
    }

    @Test
    void validate_magnification_fractional() {
        SecondBachOpticsExercise ex = new SecondBachOpticsExercise();
        // m = -0.333...
        double m = -1.0 / 3.0;
        ex.setCorrectAnswerValue(m);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("-0.33")).isTrue();
        assertThat(ex.validateAnswer("-0.333")).isTrue();
        assertThat(ex.validateAnswer("-0,33")).isTrue();
        assertThat(ex.validateAnswer("0.33")).isFalse();   // signo incorrecto
    }

    @Test
    void validate_nullAndInvalid() {
        SecondBachOpticsExercise ex = new SecondBachOpticsExercise();
        ex.setCorrectAnswerValue(48.59);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer(null)).isFalse();
        assertThat(ex.validateAnswer("")).isFalse();
        assertThat(ex.validateAnswer("abc")).isFalse();
        assertThat(ex.validateAnswer("48°")).isFalse();    // símbolo no numérico
    }
}
