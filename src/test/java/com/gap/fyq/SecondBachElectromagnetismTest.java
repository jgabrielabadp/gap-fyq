package com.gap.fyq;

import com.gap.fyq.model.secondbach.electromagnetism.ElectromagnetismType;
import com.gap.fyq.model.secondbach.electromagnetism.SecondBachElectromagnetismExercise;
import com.gap.fyq.service.SecondBachElectromagnetismService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecondBachElectromagnetismTest {

    @Autowired
    private SecondBachElectromagnetismService service;

    // ── generateAndSave ──────────────────────────────────────────────────────

    @Test
    void generateAndSave_producesValidExercise() {
        SecondBachElectromagnetismExercise ex = service.generateAndSave();

        assertThat(ex.getId()).isNotNull();
        assertThat(ex.getCourse()).isEqualTo("2BACH");
        assertThat(ex.getBlock()).isEqualTo("BL2");
        assertThat(ex.getElectromagnetismType()).isNotNull();
        assertThat(ex.getUnknownVariable()).isNotBlank();
        assertThat(ex.getStatement()).isNotBlank();
        assertThat(ex.getExplanation()).isNotBlank();
        assertThat(ex.getCorrectAnswerValue()).isNotNull();
        assertThat(ex.getCorrectAnswerDisplay()).isNotBlank();
    }

    @Test
    void generate150_coversAllThreeTypes() {
        boolean sawElectro = false, sawLorentz = false, sawFaraday = false;
        for (int i = 0; i < 150; i++) {
            ElectromagnetismType t = service.generateAndSave().getElectromagnetismType();
            if (t == ElectromagnetismType.ELECTROSTATIC_SUPERPOSITION) sawElectro = true;
            if (t == ElectromagnetismType.LORENTZ_MOTION)              sawLorentz = true;
            if (t == ElectromagnetismType.FARADAY_INDUCTION)           sawFaraday = true;
        }
        assertThat(sawElectro).isTrue();
        assertThat(sawLorentz).isTrue();
        assertThat(sawFaraday).isTrue();
    }

    @Test
    void lorentz_radiusIsPositive() {
        for (int i = 0; i < 60; i++) {
            SecondBachElectromagnetismExercise ex = service.generateAndSave();
            if (ex.getElectromagnetismType() == ElectromagnetismType.LORENTZ_MOTION
                    && "radio_r".equals(ex.getUnknownVariable())) {
                assertThat(ex.getCorrectAnswerValue()).isPositive();
            }
        }
    }

    @Test
    void lorentz_frequencyIsPositive() {
        for (int i = 0; i < 60; i++) {
            SecondBachElectromagnetismExercise ex = service.generateAndSave();
            if (ex.getElectromagnetismType() == ElectromagnetismType.LORENTZ_MOTION
                    && "frecuencia_f".equals(ex.getUnknownVariable())) {
                assertThat(ex.getCorrectAnswerValue()).isPositive();
            }
        }
    }

    @Test
    void faraday_emfAndFluxArePositive() {
        for (int i = 0; i < 60; i++) {
            SecondBachElectromagnetismExercise ex = service.generateAndSave();
            if (ex.getElectromagnetismType() == ElectromagnetismType.FARADAY_INDUCTION) {
                assertThat(ex.getCorrectAnswerValue()).isPositive();
            }
        }
    }

    // ── validateAnswer — notación científica ─────────────────────────────────

    @Test
    void validate_exactScientificNotation() {
        SecondBachElectromagnetismExercise ex = new SecondBachElectromagnetismExercise();
        ex.setCorrectAnswerValue(1.14e-4);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("1.14e-4")).isTrue();
        assertThat(ex.validateAnswer("1.14E-4")).isTrue();
        assertThat(ex.validateAnswer("1,14e-4")).isTrue();
        assertThat(ex.validateAnswer("0.000114")).isTrue();
    }

    @Test
    void validate_alternativeNotation() {
        SecondBachElectromagnetismExercise ex = new SecondBachElectromagnetismExercise();
        ex.setCorrectAnswerValue(3.78e5);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("3.78e5")).isTrue();
        assertThat(ex.validateAnswer("3.78E5")).isTrue();
        assertThat(ex.validateAnswer("378000")).isTrue();
        assertThat(ex.validateAnswer("3×10^5")).isFalse();  // 3.00e5 != 3.78e5 (20% off)
        assertThat(ex.validateAnswer("3.8×10^5")).isTrue(); // dentro de ±2%
    }

    @Test
    void validate_negativeVoltage() {
        SecondBachElectromagnetismExercise ex = new SecondBachElectromagnetismExercise();
        ex.setCorrectAnswerValue(-5.40e4);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("-5.40e4")).isTrue();
        assertThat(ex.validateAnswer("-54000")).isTrue();
        assertThat(ex.validateAnswer("-5,40e4")).isTrue();
        assertThat(ex.validateAnswer("5.40e4")).isFalse();  // signo incorrecto
    }

    @Test
    void validate_withinTolerance_2percent() {
        SecondBachElectromagnetismExercise ex = new SecondBachElectromagnetismExercise();
        ex.setCorrectAnswerValue(3.20e6);
        ex.setTolerancePercent(2.0);

        // ±2% de 3.20e6 ≈ ±64000 → rango [3136000, 3264000]
        assertThat(ex.validateAnswer("3.15e6")).isTrue();   // −1.56 % dentro
        assertThat(ex.validateAnswer("3.26e6")).isTrue();   // +1.88 % dentro
        assertThat(ex.validateAnswer("3.00e6")).isFalse();  // −6.25 % fuera
        assertThat(ex.validateAnswer("3.40e6")).isFalse();  // +6.25 % fuera
    }

    @Test
    void validate_verySmallNumber_electronRadius() {
        SecondBachElectromagnetismExercise ex = new SecondBachElectromagnetismExercise();
        // r = 9.11e-31 * 1e7 / (1.6e-19 * 0.5) ≈ 1.13875e-4
        double r = 9.11e-31 * 1.0e7 / (1.6e-19 * 0.5);
        ex.setCorrectAnswerValue(r);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("1.14e-4")).isTrue();
        assertThat(ex.validateAnswer("1.13e-4")).isTrue();   // dentro de ±2%
        assertThat(ex.validateAnswer("1.20e-4")).isFalse();  // +5.4% fuera
    }

    @Test
    void validate_cyclotronFrequency_gigahertz() {
        SecondBachElectromagnetismExercise ex = new SecondBachElectromagnetismExercise();
        // f = 1.6e-19 * 0.05 / (2π * 9.11e-31)
        double f = 1.6e-19 * 0.05 / (2 * Math.PI * 9.11e-31);
        ex.setCorrectAnswerValue(f);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("1.40e9")).isTrue();
        assertThat(ex.validateAnswer("1.4e9")).isTrue();
        assertThat(ex.validateAnswer("1.4E9")).isTrue();
    }

    @Test
    void validate_faradayEMF_smallValue() {
        SecondBachElectromagnetismExercise ex = new SecondBachElectromagnetismExercise();
        ex.setCorrectAnswerValue(0.5);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("0.5")).isTrue();
        assertThat(ex.validateAnswer("0,5")).isTrue();
        assertThat(ex.validateAnswer("5e-1")).isTrue();
        assertThat(ex.validateAnswer("0.505")).isTrue();  // +1% dentro
        assertThat(ex.validateAnswer("0.60")).isFalse();  // +20% fuera
    }

    @Test
    void validate_nullAndInvalid_returnsFalse() {
        SecondBachElectromagnetismExercise ex = new SecondBachElectromagnetismExercise();
        ex.setCorrectAnswerValue(1.14e-4);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer(null)).isFalse();
        assertThat(ex.validateAnswer("")).isFalse();
        assertThat(ex.validateAnswer("   ")).isFalse();
        assertThat(ex.validateAnswer("abc")).isFalse();
    }
}
