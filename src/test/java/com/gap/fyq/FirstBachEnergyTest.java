package com.gap.fyq;

import com.gap.fyq.model.firstbach.energy.EnergyType;
import com.gap.fyq.model.firstbach.energy.FirstBachEnergyExercise;
import com.gap.fyq.service.FirstBachEnergyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FirstBachEnergyTest {

    @Autowired
    private FirstBachEnergyService service;

    // ── generateAndSave ──────────────────────────────────────────────────────

    @Test
    void generateAndSave_producesValidExercise() {
        FirstBachEnergyExercise ex = service.generateAndSave();

        assertThat(ex.getId()).isNotNull();
        assertThat(ex.getCourse()).isEqualTo("1BACH");
        assertThat(ex.getBlock()).isEqualTo("BL8");
        assertThat(ex.getEnergyType()).isNotNull();
        assertThat(ex.getUnknownVariable()).isNotBlank();
        assertThat(ex.getStatement()).isNotBlank();
        assertThat(ex.getExplanation()).isNotBlank();
        assertThat(ex.getCorrectAnswerValue()).isNotNull();
        assertThat(ex.getCorrectAnswerDisplay()).isNotBlank();
    }

    @Test
    void generate100_coversAllThreeTypes() {
        boolean sawWet = false, sawHo = false, sawEp = false;
        for (int i = 0; i < 100; i++) {
            EnergyType t = service.generateAndSave().getEnergyType();
            if (t == EnergyType.WORK_ENERGY_THEOREM)        sawWet = true;
            if (t == EnergyType.HARMONIC_OSCILLATOR_ENERGY) sawHo  = true;
            if (t == EnergyType.ELECTRIC_POTENTIAL_WORK)    sawEp  = true;
        }
        assertThat(sawWet).isTrue();
        assertThat(sawHo).isTrue();
        assertThat(sawEp).isTrue();
    }

    // ── validateAnswer — WORK_ENERGY_THEOREM (decimal, tolerancia ±2 %) ──────

    @Test
    void validate_workEnergy_exactValue() {
        FirstBachEnergyExercise ex = new FirstBachEnergyExercise();
        ex.setCorrectAnswerValue(40.0);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("40")).isTrue();
        assertThat(ex.validateAnswer("40.0")).isTrue();
        assertThat(ex.validateAnswer("40,0")).isTrue();
        assertThat(ex.validateAnswer("40.00")).isTrue();
    }

    @Test
    void validate_workEnergy_withinTolerance() {
        FirstBachEnergyExercise ex = new FirstBachEnergyExercise();
        ex.setCorrectAnswerValue(62.5);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("62")).isTrue();    // -0,8 % dentro del 2 %
        assertThat(ex.validateAnswer("63")).isTrue();    // +0,8 % dentro del 2 %
        assertThat(ex.validateAnswer("60")).isFalse();   // -4 % fuera del 2 %
        assertThat(ex.validateAnswer("65")).isFalse();   // +4 % fuera del 2 %
    }

    // ── validateAnswer — HARMONIC_OSCILLATOR_ENERGY ──────────────────────────

    @Test
    void validate_harmonic_smallDecimal() {
        FirstBachEnergyExercise ex = new FirstBachEnergyExercise();
        ex.setCorrectAnswerValue(0.64);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("0.64")).isTrue();
        assertThat(ex.validateAnswer("0,64")).isTrue();
        assertThat(ex.validateAnswer("0.63")).isTrue();    // -1,56 % dentro
        assertThat(ex.validateAnswer("0.60")).isFalse();   // -6,25 % fuera
    }

    @Test
    void validate_harmonic_commaDecimal() {
        FirstBachEnergyExercise ex = new FirstBachEnergyExercise();
        ex.setCorrectAnswerValue(2.56);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("2,56")).isTrue();
        assertThat(ex.validateAnswer("2.56")).isTrue();
        // |2,50-2,56|/2,56 ≈ 2,34 % → fuera de la tolerancia del 2 %
        assertThat(ex.validateAnswer("2,50")).isFalse();
        // |2,60-2,56|/2,56 ≈ 1,56 % → dentro de la tolerancia del 2 %
        assertThat(ex.validateAnswer("2,60")).isTrue();
    }

    // ── validateAnswer — ELECTRIC_POTENTIAL_WORK (notación científica) ────────

    @Test
    void validate_electric_scientificNotation() {
        FirstBachEnergyExercise ex = new FirstBachEnergyExercise();
        ex.setCorrectAnswerValue(2e-4);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("2e-4")).isTrue();
        assertThat(ex.validateAnswer("2E-4")).isTrue();
        assertThat(ex.validateAnswer("2.00e-4")).isTrue();
        assertThat(ex.validateAnswer("0.0002")).isTrue();
        assertThat(ex.validateAnswer("2.0e-4")).isTrue();
    }

    @Test
    void validate_electric_negativeWork() {
        FirstBachEnergyExercise ex = new FirstBachEnergyExercise();
        ex.setCorrectAnswerValue(-6e-4);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("-6e-4")).isTrue();
        assertThat(ex.validateAnswer("-6.00e-4")).isTrue();
        assertThat(ex.validateAnswer("-0.0006")).isTrue();
        assertThat(ex.validateAnswer("6e-4")).isFalse();   // signo incorrecto
    }

    @Test
    void validate_electric_verySmallValue() {
        FirstBachEnergyExercise ex = new FirstBachEnergyExercise();
        ex.setCorrectAnswerValue(1.602e-16);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("1.60e-16")).isTrue();
        assertThat(ex.validateAnswer("1.602e-16")).isTrue();
        assertThat(ex.validateAnswer("1.6e-16")).isTrue();
        assertThat(ex.validateAnswer("1.63e-16")).isTrue();    // +1,75 % dentro
        assertThat(ex.validateAnswer("1.70e-16")).isFalse();   // +6,1 % fuera
    }

    @Test
    void validate_electric_alternativeNotation() {
        FirstBachEnergyExercise ex = new FirstBachEnergyExercise();
        ex.setCorrectAnswerValue(3e-6);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("3×10^-6")).isTrue();
        assertThat(ex.validateAnswer("3·10^-6")).isTrue();
        assertThat(ex.validateAnswer("3x10^-6")).isTrue();
        assertThat(ex.validateAnswer("3*10^-6")).isTrue();
    }

    // ── validateAnswer — casos límite ─────────────────────────────────────────

    @Test
    void validate_nullOrBlankInput() {
        FirstBachEnergyExercise ex = new FirstBachEnergyExercise();
        ex.setCorrectAnswerValue(40.0);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer(null)).isFalse();
        assertThat(ex.validateAnswer("")).isFalse();
        assertThat(ex.validateAnswer("   ")).isFalse();
        assertThat(ex.validateAnswer("abc")).isFalse();
    }
}
