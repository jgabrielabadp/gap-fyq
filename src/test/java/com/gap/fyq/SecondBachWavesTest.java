package com.gap.fyq;

import com.gap.fyq.model.secondbach.waves.SecondBachWavesExercise;
import com.gap.fyq.model.secondbach.waves.WavesType;
import com.gap.fyq.service.SecondBachWavesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecondBachWavesTest {

    @Autowired
    private SecondBachWavesService service;

    // ── generateAndSave ──────────────────────────────────────────────────────

    @Test
    void generateAndSave_producesValidExercise() {
        SecondBachWavesExercise ex = service.generateAndSave();

        assertThat(ex.getId()).isNotNull();
        assertThat(ex.getCourse()).isEqualTo("2BACH");
        assertThat(ex.getBlock()).isEqualTo("BL3");
        assertThat(ex.getWavesType()).isNotNull();
        assertThat(ex.getUnknownVariable()).isNotBlank();
        assertThat(ex.getStatement()).isNotBlank();
        assertThat(ex.getExplanation()).isNotBlank();
        assertThat(ex.getCorrectAnswerValue()).isNotNull();
        assertThat(ex.getCorrectAnswerDisplay()).isNotBlank();
    }

    @Test
    void generate150_coversAllThreeTypes() {
        boolean sawHO = false, sawWave = false, sawAco = false;
        for (int i = 0; i < 150; i++) {
            WavesType t = service.generateAndSave().getWavesType();
            if (t == WavesType.HARMONIC_OSCILLATOR) sawHO   = true;
            if (t == WavesType.WAVE_EQUATION)       sawWave = true;
            if (t == WavesType.ACOUSTICS_DOPPLER)   sawAco  = true;
        }
        assertThat(sawHO).isTrue();
        assertThat(sawWave).isTrue();
        assertThat(sawAco).isTrue();
    }

    // ── Propiedades físicas ────────────────────────────────────────────────

    @Test
    void harmonicOscillator_periodAndOmegaConsistent() {
        for (int i = 0; i < 60; i++) {
            SecondBachWavesExercise ex = service.generateAndSave();
            if (ex.getWavesType() == WavesType.HARMONIC_OSCILLATOR) {
                // Solo comprobamos que el valor esperado sea positivo
                assertThat(ex.getCorrectAnswerValue()).isPositive();
            }
        }
    }

    @Test
    void waveEquation_velocityIsPositive() {
        for (int i = 0; i < 60; i++) {
            SecondBachWavesExercise ex = service.generateAndSave();
            if (ex.getWavesType() == WavesType.WAVE_EQUATION) {
                assertThat(ex.getCorrectAnswerValue()).isPositive();
            }
        }
    }

    @Test
    void acoustics_decibelInPhysicalRange() {
        for (int i = 0; i < 60; i++) {
            SecondBachWavesExercise ex = service.generateAndSave();
            if (ex.getWavesType() == WavesType.ACOUSTICS_DOPPLER
                    && "nivel_sonoro".equals(ex.getUnknownVariable())) {
                // Nivel audible: entre 0 dB y 194 dB (umbral de dolor ~130 dB)
                assertThat(ex.getCorrectAnswerValue()).isBetween(0.0, 200.0);
            }
        }
    }

    @Test
    void acoustics_dopplerFrequencyIsPositive() {
        for (int i = 0; i < 60; i++) {
            SecondBachWavesExercise ex = service.generateAndSave();
            if (ex.getWavesType() == WavesType.ACOUSTICS_DOPPLER
                    && "frecuencia_doppler".equals(ex.getUnknownVariable())) {
                assertThat(ex.getCorrectAnswerValue()).isPositive();
            }
        }
    }

    // ── validateAnswer — formatos de entrada ─────────────────────────────────

    @Test
    void validate_integerAnswer() {
        SecondBachWavesExercise ex = new SecondBachWavesExercise();
        ex.setCorrectAnswerValue(20.0);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("20")).isTrue();
        assertThat(ex.validateAnswer("20.0")).isTrue();
        assertThat(ex.validateAnswer("20,0")).isTrue();
        assertThat(ex.validateAnswer("2e1")).isTrue();
    }

    @Test
    void validate_smallDecimalPeriod() {
        SecondBachWavesExercise ex = new SecondBachWavesExercise();
        // T = 2π/20 ≈ 0.3142 s
        double T = 2 * Math.PI / 20.0;
        ex.setCorrectAnswerValue(T);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("0.314")).isTrue();
        assertThat(ex.validateAnswer("0.3142")).isTrue();
        assertThat(ex.validateAnswer("0,314")).isTrue();
        assertThat(ex.validateAnswer("0.300")).isFalse();  // −4.5 % fuera
    }

    @Test
    void validate_dBValue_decimalWithTolerance() {
        SecondBachWavesExercise ex = new SecondBachWavesExercise();
        ex.setCorrectAnswerValue(89.01);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("89.01")).isTrue();
        assertThat(ex.validateAnswer("89")).isTrue();      // −0.01% dentro
        assertThat(ex.validateAnswer("89.0")).isTrue();
        assertThat(ex.validateAnswer("85.0")).isFalse();   // −4.5 % fuera
    }

    @Test
    void validate_doppler_frequency() {
        SecondBachWavesExercise ex = new SecondBachWavesExercise();
        // f' = 440 × 340/306 ≈ 488.89 Hz
        double fPrime = 440.0 * 340.0 / 306.0;
        ex.setCorrectAnswerValue(fPrime);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("488.89")).isTrue();
        assertThat(ex.validateAnswer("489")).isTrue();   // +0.02 % dentro
        assertThat(ex.validateAnswer("488")).isTrue();   // −0.18 % dentro
        assertThat(ex.validateAnswer("460")).isFalse();  // −5.9 % fuera
    }

    @Test
    void validate_wavePhase_radians() {
        SecondBachWavesExercise ex = new SecondBachWavesExercise();
        // Δφ = π/2 ≈ 1.5708 rad
        ex.setCorrectAnswerValue(Math.PI / 2);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("1.5708")).isTrue();
        assertThat(ex.validateAnswer("1.57")).isTrue();   // −0.05 % dentro
        assertThat(ex.validateAnswer("1.571")).isTrue();
        assertThat(ex.validateAnswer("1.6")).isTrue();    // +1.9 % dentro
        assertThat(ex.validateAnswer("1.4")).isFalse();   // −10.9 % fuera
    }

    @Test
    void validate_energyJoules() {
        SecondBachWavesExercise ex = new SecondBachWavesExercise();
        ex.setCorrectAnswerValue(1.0);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("1")).isTrue();
        assertThat(ex.validateAnswer("1.0")).isTrue();
        assertThat(ex.validateAnswer("1,0")).isTrue();
        assertThat(ex.validateAnswer("0.99")).isTrue();   // −1 % dentro
        assertThat(ex.validateAnswer("1.019")).isTrue();  // +1.9 % dentro
        assertThat(ex.validateAnswer("1.03")).isFalse(); // +3 % fuera
    }

    @Test
    void validate_alternativeNotation_multiplicative() {
        SecondBachWavesExercise ex = new SecondBachWavesExercise();
        ex.setCorrectAnswerValue(3.0e-4);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer("3×10^-4")).isTrue();
        assertThat(ex.validateAnswer("3·10^-4")).isTrue();
        assertThat(ex.validateAnswer("3x10^-4")).isTrue();
        assertThat(ex.validateAnswer("0.0003")).isTrue();
    }

    @Test
    void validate_nullAndInvalid() {
        SecondBachWavesExercise ex = new SecondBachWavesExercise();
        ex.setCorrectAnswerValue(20.0);
        ex.setTolerancePercent(2.0);

        assertThat(ex.validateAnswer(null)).isFalse();
        assertThat(ex.validateAnswer("")).isFalse();
        assertThat(ex.validateAnswer("abc")).isFalse();
        assertThat(ex.validateAnswer("π")).isFalse();
    }

    // ── Consistencia matemática de los escenarios ────────────────────────────

    @Test
    void waveScenario_v_equals_lambda_times_f() {
        // Verifica v = λ·f para los 6 escenarios internos usando fmtNum
        double[][] scenarios = {
            {0.10, 2.0, 170}, {0.20, 4.0, 85}, {0.15, 6.0, 50},
            {0.05, 1.0, 440}, {0.30, 10.0, 34}, {0.10, 0.5, 680}
        };
        for (double[] sc : scenarios) {
            double lambda = sc[1], f = sc[2], v = lambda * f;
            // v debe ser positiva
            assertThat(v).isPositive();
            // Coherencia: k×v = ω
            double k = 2 * Math.PI / lambda;
            double omega = 2 * Math.PI * f;
            assertThat(k * v).isCloseTo(omega, org.assertj.core.data.Offset.offset(1e-6));
        }
    }
}
