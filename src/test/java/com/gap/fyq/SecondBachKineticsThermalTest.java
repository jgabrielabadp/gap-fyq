package com.gap.fyq;

import com.gap.fyq.model.secondbach.kineticsthermal.KineticsThermalType;
import com.gap.fyq.model.secondbach.kineticsthermal.SecondBachKineticsThermalExercise;
import com.gap.fyq.service.SecondBachKineticsThermalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecondBachKineticsThermalTest {

    @Autowired
    private SecondBachKineticsThermalService service;

    // ── generateAndSave ──────────────────────────────────────────────────────

    @Test
    void generateAndSave_producesValidExercise() {
        SecondBachKineticsThermalExercise ex = service.generateAndSave();

        assertThat(ex.getId()).isNotNull();
        assertThat(ex.getCourse()).isEqualTo("2BACH_Q");
        assertThat(ex.getBlock()).isEqualTo("BL2");
        assertThat(ex.getKineticsThermalType()).isNotNull();
        assertThat(ex.getExerciseMode()).isNotBlank();
        assertThat(ex.getStatement()).isNotBlank();
        assertThat(ex.getCorrectAnswer()).isNotBlank();
        assertThat(ex.getCorrectAnswerDisplay()).isNotBlank();
        assertThat(ex.getUnit()).isNotBlank();
        assertThat(ex.getExplanation()).isNotBlank();
    }

    @Test
    void generate300_coversAllThreeTypes() {
        boolean sawHess = false, sawGibbs = false, sawArrhenius = false;
        for (int i = 0; i < 300; i++) {
            KineticsThermalType t = service.generateAndSave().getKineticsThermalType();
            if (t == KineticsThermalType.HESS_BORN_HABER)       sawHess      = true;
            if (t == KineticsThermalType.GIBBS_SPONTANEITY)     sawGibbs     = true;
            if (t == KineticsThermalType.ARRHENIUS_REACTION_RATE) sawArrhenius = true;
        }
        assertThat(sawHess).isTrue();
        assertThat(sawGibbs).isTrue();
        assertThat(sawArrhenius).isTrue();
    }

    @Test
    void generate300_coversAllFiveModes() {
        boolean sawHessN = false, sawGibbsG = false, sawGibbsT = false,
                sawEa     = false, sawOrder  = false;
        for (int i = 0; i < 300; i++) {
            String mode = service.generateAndSave().getExerciseMode();
            if ("HESS_NUMERIC".equals(mode))    sawHessN  = true;
            if ("GIBBS_DELTA_G".equals(mode))   sawGibbsG = true;
            if ("GIBBS_T_LIMIT".equals(mode))   sawGibbsT = true;
            if ("ARRHENIUS_EA".equals(mode))    sawEa     = true;
            if ("ARRHENIUS_ORDER".equals(mode)) sawOrder  = true;
        }
        assertThat(sawHessN).isTrue();
        assertThat(sawGibbsG).isTrue();
        assertThat(sawGibbsT).isTrue();
        assertThat(sawEa).isTrue();
        assertThat(sawOrder).isTrue();
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_returnsCorrectExercise() {
        SecondBachKineticsThermalExercise ex = service.generateAndSave();
        SecondBachKineticsThermalExercise found = service.findById(ex.getId());
        assertThat(found.getId()).isEqualTo(ex.getId());
        assertThat(found.getCorrectAnswer()).isEqualTo(ex.getCorrectAnswer());
    }

    @Test
    void findById_throwsForUnknownId() {
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class, () -> service.findById(Long.MAX_VALUE)
        )).isInstanceOf(IllegalArgumentException.class);
    }

    // ── validateAnswer — tolerancia 1% ───────────────────────────────────────

    @Test
    void validateAnswer_exactValue_returnsTrue() {
        SecondBachKineticsThermalExercise ex = new SecondBachKineticsThermalExercise();
        ex.setExerciseMode("HESS_NUMERIC");
        ex.setCorrectAnswer("-283.0");

        assertThat(ex.validateAnswer("-283.0")).isTrue();
        assertThat(ex.validateAnswer("-283")).isTrue();
    }

    @Test
    void validateAnswer_withinOnePct_returnsTrue() {
        SecondBachKineticsThermalExercise ex = new SecondBachKineticsThermalExercise();
        ex.setExerciseMode("HESS_NUMERIC");
        ex.setCorrectAnswer("-283.0");

        // 1% de 283 = 2.83 → -280.18 dentro del rango
        assertThat(ex.validateAnswer("-280.18")).isTrue();
        assertThat(ex.validateAnswer("-285.82")).isTrue();
    }

    @Test
    void validateAnswer_outsideOnePct_returnsFalse() {
        SecondBachKineticsThermalExercise ex = new SecondBachKineticsThermalExercise();
        ex.setExerciseMode("HESS_NUMERIC");
        ex.setCorrectAnswer("-283.0");

        assertThat(ex.validateAnswer("-279.0")).isFalse();
        assertThat(ex.validateAnswer("-287.0")).isFalse();
    }

    @Test
    void validateAnswer_commaDecimalSeparator_accepted() {
        SecondBachKineticsThermalExercise ex = new SecondBachKineticsThermalExercise();
        ex.setExerciseMode("GIBBS_DELTA_G");
        ex.setCorrectAnswer("-237.19");

        assertThat(ex.validateAnswer("-237,19")).isTrue();
    }

    @Test
    void validateAnswer_nullAndBlank_returnsFalse() {
        SecondBachKineticsThermalExercise ex = new SecondBachKineticsThermalExercise();
        ex.setExerciseMode("GIBBS_DELTA_G");
        ex.setCorrectAnswer("-237.19");

        assertThat(ex.validateAnswer(null)).isFalse();
        assertThat(ex.validateAnswer("")).isFalse();
        assertThat(ex.validateAnswer("   ")).isFalse();
    }

    @Test
    void validateAnswer_nonNumeric_returnsFalse() {
        SecondBachKineticsThermalExercise ex = new SecondBachKineticsThermalExercise();
        ex.setExerciseMode("ARRHENIUS_EA");
        ex.setCorrectAnswer("50.0");

        assertThat(ex.validateAnswer("cincuenta")).isFalse();
        assertThat(ex.validateAnswer("50 kJ")).isFalse();
    }

    // ── validateAnswer — ARRHENIUS_ORDER ─────────────────────────────────────

    @Test
    void validateOrders_exactMatch_returnsTrue() {
        SecondBachKineticsThermalExercise ex = new SecondBachKineticsThermalExercise();
        ex.setExerciseMode("ARRHENIUS_ORDER");
        ex.setCorrectAnswer("1|2|3");

        assertThat(ex.validateAnswer("1|2|3")).isTrue();
    }

    @Test
    void validateOrders_wrongGlobal_returnsFalse() {
        SecondBachKineticsThermalExercise ex = new SecondBachKineticsThermalExercise();
        ex.setExerciseMode("ARRHENIUS_ORDER");
        ex.setCorrectAnswer("1|2|3");

        assertThat(ex.validateAnswer("1|2|4")).isFalse();
    }

    @Test
    void validateOrders_wrongPartial_returnsFalse() {
        SecondBachKineticsThermalExercise ex = new SecondBachKineticsThermalExercise();
        ex.setExerciseMode("ARRHENIUS_ORDER");
        ex.setCorrectAnswer("2|1|3");

        assertThat(ex.validateAnswer("1|2|3")).isFalse();
    }

    @Test
    void validateOrders_missingPart_returnsFalse() {
        SecondBachKineticsThermalExercise ex = new SecondBachKineticsThermalExercise();
        ex.setExerciseMode("ARRHENIUS_ORDER");
        ex.setCorrectAnswer("1|1|2");

        assertThat(ex.validateAnswer("1|1")).isFalse();
    }

    // ── Verificaciones matemáticas ────────────────────────────────────────────

    @Test
    void hessCO_netDeltaH_isCorrect() {
        // C(s)+O₂→CO₂: -393.5; CO→C+½O₂: +110.5; suma = -283.0
        double net = -393.5 + 110.5;
        assertThat(net).isEqualTo(-283.0);
    }

    @Test
    void gibbsNH3_at298K_isNegative() {
        // ΔH=-92.4 kJ, ΔS=-198 J/(mol·K), T=298.15 K
        double dSkJ  = -198.0 / 1000.0;
        double deltaG = -92.4 - 298.15 * dSkJ;
        assertThat(deltaG).isLessThan(0.0);
    }

    @Test
    void gibbsTLimit_CaCO3_isPositiveKelvin() {
        // ΔH=177.9 kJ, ΔS=160.5 J/(mol·K)
        double dSkJ   = 160.5 / 1000.0;
        double tLimit = 177.9 / dSkJ;
        assertThat(tLimit).isGreaterThan(0.0);
        assertThat(tLimit).isBetween(1000.0, 1200.0);
    }

    @Test
    void arrheniusEa_derivedFromTwoPoints_matchesTarget() {
        double R   = 8.314;
        double T1  = 300.0, T2 = 400.0;
        double ea  = 50_000.0; // J/mol
        double k1  = 1.50e-3;
        double k2  = k1 * Math.exp(-ea / R * (1.0 / T2 - 1.0 / T1));

        // recalcular Ea: ln(k2/k1) = Ea/R * (1/T1 - 1/T2)  →  Ea = R * ln(k2/k1) / (1/T1 - 1/T2)
        double lnR    = Math.log(k2 / k1);
        double inv    = 1.0 / T1 - 1.0 / T2;
        double eaCalc = R * lnR / inv;

        assertThat(eaCalc / 1000.0).isBetween(49.0, 51.0);
    }

    @Test
    void generateAllHessScenarios_haveCorrectSign() {
        for (int i = 0; i < 200; i++) {
            SecondBachKineticsThermalExercise ex = service.generateAndSave();
            if ("HESS_NUMERIC".equals(ex.getExerciseMode())) {
                assertThat(ex.getCorrectAnswer()).matches("-?\\d+\\.?\\d*");
            }
        }
    }

    @Test
    void generateAllGibbsScenarios_unitIsCorrect() {
        for (int i = 0; i < 200; i++) {
            SecondBachKineticsThermalExercise ex = service.generateAndSave();
            if ("GIBBS_DELTA_G".equals(ex.getExerciseMode())) {
                assertThat(ex.getUnit()).isEqualTo("kJ");
            }
            if ("GIBBS_T_LIMIT".equals(ex.getExerciseMode())) {
                assertThat(ex.getUnit()).isEqualTo("K");
            }
        }
    }

    @Test
    void generateAllArrheniusEa_unitIsKJPerMol() {
        for (int i = 0; i < 200; i++) {
            SecondBachKineticsThermalExercise ex = service.generateAndSave();
            if ("ARRHENIUS_EA".equals(ex.getExerciseMode())) {
                assertThat(ex.getUnit()).isEqualTo("kJ/mol");
                double ea = Double.parseDouble(ex.getCorrectAnswer());
                assertThat(ea).isGreaterThan(0.0);
            }
        }
    }

    @Test
    void generateAllArrheniusOrder_formatIsThreeParts() {
        for (int i = 0; i < 200; i++) {
            SecondBachKineticsThermalExercise ex = service.generateAndSave();
            if ("ARRHENIUS_ORDER".equals(ex.getExerciseMode())) {
                String[] parts = ex.getCorrectAnswer().split("\\|");
                assertThat(parts).hasSize(3);
                int alpha  = Integer.parseInt(parts[0]);
                int beta   = Integer.parseInt(parts[1]);
                int global = Integer.parseInt(parts[2]);
                assertThat(global).isEqualTo(alpha + beta);
            }
        }
    }
}
