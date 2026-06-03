package com.gap.fyq;

import com.gap.fyq.model.secondbach.gravitational.GravitationalType;
import com.gap.fyq.model.secondbach.gravitational.SecondBachGravitationalExercise;
import com.gap.fyq.service.SecondBachGravitationalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecondBachGravitationalTest {

    @Autowired
    private SecondBachGravitationalService service;

    // ── generateAndSave ──────────────────────────────────────────────────────

    @Test
    void generateAndSave_producesValidExercise() {
        SecondBachGravitationalExercise ex = service.generateAndSave();

        assertThat(ex.getId()).isNotNull();
        assertThat(ex.getCourse()).isEqualTo("2BACH");
        assertThat(ex.getBlock()).isEqualTo("BL1");
        assertThat(ex.getGravitationalType()).isNotNull();
        assertThat(ex.getUnknownVariable()).isNotBlank();
        assertThat(ex.getStatement()).isNotBlank();
        assertThat(ex.getExplanation()).isNotBlank();
        assertThat(ex.getCorrectAnswerValue()).isNotNull();
        assertThat(ex.getCorrectAnswerDisplay()).isNotBlank();
    }

    @Test
    void generate150_coversAllThreeTypes() {
        boolean sawOrbital = false, sawEscape = false, sawField = false;
        for (int i = 0; i < 150; i++) {
            GravitationalType t = service.generateAndSave().getGravitationalType();
            if (t == GravitationalType.ORBITAL_MECHANICS)     sawOrbital = true;
            if (t == GravitationalType.ESCAPE_VELOCITY_WORK)  sawEscape  = true;
            if (t == GravitationalType.FIELD_POTENTIAL_POINTS) sawField  = true;
        }
        assertThat(sawOrbital).isTrue();
        assertThat(sawEscape).isTrue();
        assertThat(sawField).isTrue();
    }

    @Test
    void orbitalMechanics_energiaEsSiempreNegativa() {
        for (int i = 0; i < 60; i++) {
            SecondBachGravitationalExercise ex = service.generateAndSave();
            if (ex.getGravitationalType() == GravitationalType.ORBITAL_MECHANICS
                    && "E_mecanica".equals(ex.getUnknownVariable())) {
                assertThat(ex.getCorrectAnswerValue()).isNegative();
            }
        }
    }

    @Test
    void fieldPotential_potencialEsSiempreNegativo() {
        for (int i = 0; i < 60; i++) {
            SecondBachGravitationalExercise ex = service.generateAndSave();
            if (ex.getGravitationalType() == GravitationalType.FIELD_POTENTIAL_POINTS
                    && "potencial_V".equals(ex.getUnknownVariable())) {
                assertThat(ex.getCorrectAnswerValue()).isNegative();
            }
        }
    }

    @Test
    void fieldPotential_campoEsSiemprePositivo() {
        for (int i = 0; i < 60; i++) {
            SecondBachGravitationalExercise ex = service.generateAndSave();
            if (ex.getGravitationalType() == GravitationalType.FIELD_POTENTIAL_POINTS
                    && "campo_g".equals(ex.getUnknownVariable())) {
                assertThat(ex.getCorrectAnswerValue()).isPositive();
            }
        }
    }

    // ── validateAnswer — notación científica estándar ────────────────────────

    @Test
    void validate_positiveScientificNotation_exactValue() {
        SecondBachGravitationalExercise ex = new SecondBachGravitationalExercise();
        ex.setCorrectAnswerValue(7.67e3);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("7.67e3")).isTrue();
        assertThat(ex.validateAnswer("7.67E3")).isTrue();
        assertThat(ex.validateAnswer("7670")).isTrue();
        assertThat(ex.validateAnswer("7,67e3")).isTrue();   // coma como decimal
    }

    @Test
    void validate_negativeScientificNotation_exactValue() {
        SecondBachGravitationalExercise ex = new SecondBachGravitationalExercise();
        ex.setCorrectAnswerValue(-1.47e10);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("-1.47e10")).isTrue();
        assertThat(ex.validateAnswer("-1.47E10")).isTrue();
        assertThat(ex.validateAnswer("-1,47e10")).isTrue();
        assertThat(ex.validateAnswer("-14700000000")).isTrue();
    }

    @Test
    void validate_negativeSign_enforcement() {
        SecondBachGravitationalExercise ex = new SecondBachGravitationalExercise();
        ex.setCorrectAnswerValue(-1.47e10);
        ex.setTolerancePercent(2.0);

        // Signo incorrecto debe fallar
        assertThat(ex.validateAnswer("1.47e10")).isFalse();
        assertThat(ex.validateAnswer("1.47E10")).isFalse();
    }

    @Test
    void validate_withinTolerance_2percent() {
        SecondBachGravitationalExercise ex = new SecondBachGravitationalExercise();
        ex.setCorrectAnswerValue(7669.9);
        ex.setTolerancePercent(2.0);

        // ±2 % de 7669.9 ≈ ±153.4 → rango [7516.5, 7823.3]
        assertThat(ex.validateAnswer("7600")).isTrue();    // −0.9 % dentro
        assertThat(ex.validateAnswer("7800")).isTrue();    // +1.7 % dentro
        assertThat(ex.validateAnswer("7300")).isFalse();   // −4.8 % fuera
        assertThat(ex.validateAnswer("8000")).isFalse();   // +4.3 % fuera
    }

    @Test
    void validate_alternativeNotation_multiplicative() {
        SecondBachGravitationalExercise ex = new SecondBachGravitationalExercise();
        ex.setCorrectAnswerValue(3.0e-4);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("3×10^-4")).isTrue();
        assertThat(ex.validateAnswer("3·10^-4")).isTrue();
        assertThat(ex.validateAnswer("3x10^-4")).isTrue();
        assertThat(ex.validateAnswer("3*10^-4")).isTrue();
    }

    @Test
    void validate_smallNegativePotential() {
        SecondBachGravitationalExercise ex = new SecondBachGravitationalExercise();
        ex.setCorrectAnswerValue(-8.89e5);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("-8.89e5")).isTrue();
        assertThat(ex.validateAnswer("-889000")).isTrue();
        assertThat(ex.validateAnswer("-8,89e5")).isTrue();
        assertThat(ex.validateAnswer("8.89e5")).isFalse();  // signo incorrecto
    }

    @Test
    void validate_verySmallField_exponentNegative3() {
        SecondBachGravitationalExercise ex = new SecondBachGravitationalExercise();
        ex.setCorrectAnswerValue(1.98e-3);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("1.98e-3")).isTrue();
        assertThat(ex.validateAnswer("1.98E-3")).isTrue();
        assertThat(ex.validateAnswer("0.00198")).isTrue();
        assertThat(ex.validateAnswer("1.99e-3")).isTrue();   // +0.5 % dentro
        assertThat(ex.validateAnswer("2.10e-3")).isFalse();  // +6 % fuera
    }

    // ── validateAnswer — casos límite ────────────────────────────────────────

    @Test
    void validate_nullOrBlankInput_returnsFalse() {
        SecondBachGravitationalExercise ex = new SecondBachGravitationalExercise();
        ex.setCorrectAnswerValue(7669.9);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer(null)).isFalse();
        assertThat(ex.validateAnswer("")).isFalse();
        assertThat(ex.validateAnswer("   ")).isFalse();
        assertThat(ex.validateAnswer("abc")).isFalse();
    }

}
