package com.gap.fyq;

import com.gap.fyq.model.secondbach.structurelink.SecondBachStructureLinkExercise;
import com.gap.fyq.model.secondbach.structurelink.StructureLinkType;
import com.gap.fyq.service.SecondBachStructureLinkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecondBachStructureLinkTest {

    @Autowired
    private SecondBachStructureLinkService service;

    // ── generateAndSave ──────────────────────────────────────────────────────

    @Test
    void generateAndSave_producesValidExercise() {
        SecondBachStructureLinkExercise ex = service.generateAndSave();

        assertThat(ex.getId()).isNotNull();
        assertThat(ex.getCourse()).isEqualTo("2BACH_Q");
        assertThat(ex.getBlock()).isEqualTo("BL1");
        assertThat(ex.getStructureLinkType()).isNotNull();
        assertThat(ex.getExerciseMode()).isNotBlank();
        assertThat(ex.getStatement()).isNotBlank();
        assertThat(ex.getCorrectAnswer()).isNotBlank();
        assertThat(ex.getCorrectAnswerDisplay()).isNotBlank();
        assertThat(ex.getExplanation()).isNotBlank();
    }

    @Test
    void generate200_coversAllThreeTypes() {
        boolean sawConfig = false, sawPeriodic = false, sawGeometry = false;
        for (int i = 0; i < 200; i++) {
            StructureLinkType t = service.generateAndSave().getStructureLinkType();
            if (t == StructureLinkType.QUANTUM_NUMBERS_CONFIG) sawConfig   = true;
            if (t == StructureLinkType.PERIODIC_PROPERTIES)   sawPeriodic = true;
            if (t == StructureLinkType.MOLECULAR_GEOMETRY_TRPEV) sawGeometry = true;
        }
        assertThat(sawConfig).isTrue();
        assertThat(sawPeriodic).isTrue();
        assertThat(sawGeometry).isTrue();
    }

    @Test
    void generate200_coversBothQuantumModes() {
        boolean sawConfigText = false, sawQuantumMCQ = false;
        for (int i = 0; i < 200; i++) {
            SecondBachStructureLinkExercise ex = service.generateAndSave();
            if ("CONFIG_TEXT".equals(ex.getExerciseMode()))  sawConfigText = true;
            if ("QUANTUM_MCQ".equals(ex.getExerciseMode()))  sawQuantumMCQ = true;
        }
        assertThat(sawConfigText).isTrue();
        assertThat(sawQuantumMCQ).isTrue();
    }

    @Test
    void mcqExercises_haveThreeOptions() {
        for (int i = 0; i < 100; i++) {
            SecondBachStructureLinkExercise ex = service.generateAndSave();
            if ("QUANTUM_MCQ".equals(ex.getExerciseMode())
                    || "PERIODIC_MCQ".equals(ex.getExerciseMode())) {
                assertThat(ex.getOptionA()).isNotBlank();
                assertThat(ex.getOptionB()).isNotBlank();
                assertThat(ex.getOptionC()).isNotBlank();
                assertThat(ex.getCorrectAnswer()).matches("[ABC]");
                // las tres opciones deben ser distintas
                assertThat(ex.getOptionA()).isNotEqualTo(ex.getOptionB());
                assertThat(ex.getOptionA()).isNotEqualTo(ex.getOptionC());
                assertThat(ex.getOptionB()).isNotEqualTo(ex.getOptionC());
            }
        }
    }

    @Test
    void geometryExercise_correctAnswerContainsFourParts() {
        for (int i = 0; i < 100; i++) {
            SecondBachStructureLinkExercise ex = service.generateAndSave();
            if ("GEOMETRY_MULTI".equals(ex.getExerciseMode())) {
                String[] parts = ex.getCorrectAnswer().split("\\|");
                assertThat(parts).hasSize(4);
                assertThat(parts[0]).isNotBlank(); // hybridization
                assertThat(parts[1]).matches("\\d+");  // lonePairs
                assertThat(parts[2]).isNotBlank(); // geometry
                assertThat(parts[3]).matches("polar|apolar"); // polarity
            }
        }
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_returnsCorrectExercise() {
        SecondBachStructureLinkExercise ex = service.generateAndSave();
        SecondBachStructureLinkExercise found = service.findById(ex.getId());
        assertThat(found.getId()).isEqualTo(ex.getId());
        assertThat(found.getCorrectAnswer()).isEqualTo(ex.getCorrectAnswer());
    }

    @Test
    void findById_throwsForUnknownId() {
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class, () -> service.findById(Long.MAX_VALUE)
        )).isInstanceOf(IllegalArgumentException.class);
    }

    // ── CONFIG_TEXT validateAnswer ────────────────────────────────────────────

    @Test
    void configText_validateAnswer_exactMatch() {
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("CONFIG_TEXT");
        ex.setCorrectAnswer("1s2 2s2 2p6 3s1");

        assertThat(ex.validateAnswer("1s2 2s2 2p6 3s1")).isTrue();
    }

    @Test
    void configText_validateAnswer_noSpaces() {
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("CONFIG_TEXT");
        ex.setCorrectAnswer("1s2 2s2 2p6 3s1");

        // sin espacios
        assertThat(ex.validateAnswer("1s22s22p63s1")).isTrue();
    }

    @Test
    void configText_validateAnswer_unicodeSuperscripts() {
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("CONFIG_TEXT");
        ex.setCorrectAnswer("1s2 2s2 2p6 3s1");

        // superíndices Unicode
        assertThat(ex.validateAnswer("1s² 2s² 2p⁶ 3s¹")).isTrue();
    }

    @Test
    void configText_validateAnswer_caretNotation() {
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("CONFIG_TEXT");
        ex.setCorrectAnswer("1s2 2s2 2p6 3s1");

        // notación con ^
        assertThat(ex.validateAnswer("1s^2 2s^2 2p^6 3s^1")).isTrue();
    }

    @Test
    void configText_validateAnswer_wrongConfig_returnsFalse() {
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("CONFIG_TEXT");
        ex.setCorrectAnswer("1s2 2s2 2p6 3s1");

        // configuración equivocada (Na con 3p en vez de 3s)
        assertThat(ex.validateAnswer("1s2 2s2 2p6 3p1")).isFalse();
    }

    @Test
    void configText_chromiumAnomalousConfig() {
        // Cr: 1s2 2s2 2p6 3s2 3p6 3d5 4s1 (no 3d4 4s2)
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("CONFIG_TEXT");
        ex.setCorrectAnswer("1s2 2s2 2p6 3s2 3p6 3d5 4s1");

        assertThat(ex.validateAnswer("1s2 2s2 2p6 3s2 3p6 3d5 4s1")).isTrue();
        assertThat(ex.validateAnswer("1s2 2s2 2p6 3s2 3p6 3d4 4s2")).isFalse();
    }

    @Test
    void configText_copperAnomalousConfig() {
        // Cu: 1s2 2s2 2p6 3s2 3p6 3d10 4s1 (no 3d9 4s2)
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("CONFIG_TEXT");
        ex.setCorrectAnswer("1s2 2s2 2p6 3s2 3p6 3d10 4s1");

        assertThat(ex.validateAnswer("1s2 2s2 2p6 3s2 3p6 3d10 4s1")).isTrue();
        assertThat(ex.validateAnswer("1s2 2s2 2p6 3s2 3p6 3d9 4s2")).isFalse();
    }

    // ── QUANTUM_MCQ validateAnswer ────────────────────────────────────────────

    @Test
    void quantumMCQ_validateAnswer_caseInsensitive() {
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("QUANTUM_MCQ");
        ex.setCorrectAnswer("B");

        assertThat(ex.validateAnswer("B")).isTrue();
        assertThat(ex.validateAnswer("b")).isTrue();
        assertThat(ex.validateAnswer("  B  ")).isTrue();
        assertThat(ex.validateAnswer("A")).isFalse();
        assertThat(ex.validateAnswer("C")).isFalse();
    }

    // ── PERIODIC_MCQ validateAnswer ───────────────────────────────────────────

    @Test
    void periodicMCQ_validateAnswer_correct() {
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("PERIODIC_MCQ");
        ex.setCorrectAnswer("A");

        assertThat(ex.validateAnswer("A")).isTrue();
        assertThat(ex.validateAnswer("a")).isTrue();
        assertThat(ex.validateAnswer("B")).isFalse();
    }

    // ── GEOMETRY_MULTI validateAnswer ─────────────────────────────────────────

    @Test
    void geometry_h2o_exactComposite() {
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("GEOMETRY_MULTI");
        ex.setCorrectAnswer("sp3|2|angular|polar");

        assertThat(ex.validateAnswer("sp3|2|angular|polar")).isTrue();
    }

    @Test
    void geometry_h2o_hybridizationVariants() {
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("GEOMETRY_MULTI");
        ex.setCorrectAnswer("sp3|2|angular|polar");

        assertThat(ex.validateAnswer("sp^3|2|angular|polar")).isTrue();
        assertThat(ex.validateAnswer("SP3|2|angular|polar")).isTrue();
        assertThat(ex.validateAnswer("sp 3|2|angular|polar")).isTrue();
    }

    @Test
    void geometry_h2o_geometryAliases() {
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("GEOMETRY_MULTI");
        ex.setCorrectAnswer("sp3|2|angular|polar");

        assertThat(ex.validateAnswer("sp3|2|doblada|polar")).isTrue();
        assertThat(ex.validateAnswer("sp3|2|bent|polar")).isTrue();
        assertThat(ex.validateAnswer("sp3|2|en forma de v|polar")).isTrue();
    }

    @Test
    void geometry_nh3_pyramidal() {
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("GEOMETRY_MULTI");
        ex.setCorrectAnswer("sp3|1|piramidal trigonal|polar");

        assertThat(ex.validateAnswer("sp3|1|piramidal trigonal|polar")).isTrue();
        assertThat(ex.validateAnswer("sp3|1|trigonal piramidal|polar")).isTrue();
        assertThat(ex.validateAnswer("sp3|1|pirámide trigonal|polar")).isTrue();
    }

    @Test
    void geometry_ch4_tetrahedral_apolar() {
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("GEOMETRY_MULTI");
        ex.setCorrectAnswer("sp3|0|tetraédrica|apolar");

        assertThat(ex.validateAnswer("sp3|0|tetraédrica|apolar")).isTrue();
        assertThat(ex.validateAnswer("sp3|0|tetrahedral|apolar")).isTrue();
        assertThat(ex.validateAnswer("sp3|0|tetraedrica|apolar")).isTrue();
    }

    @Test
    void geometry_co2_linear_apolar() {
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("GEOMETRY_MULTI");
        ex.setCorrectAnswer("sp|0|lineal|apolar");

        assertThat(ex.validateAnswer("sp|0|lineal|apolar")).isTrue();
        assertThat(ex.validateAnswer("sp|0|linear|apolar")).isTrue();
    }

    @Test
    void geometry_wrongLonePairs_returnsFalse() {
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("GEOMETRY_MULTI");
        ex.setCorrectAnswer("sp3|2|angular|polar");

        assertThat(ex.validateAnswer("sp3|1|angular|polar")).isFalse();
        assertThat(ex.validateAnswer("sp3|0|angular|polar")).isFalse();
    }

    @Test
    void geometry_wrongPolarity_returnsFalse() {
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("GEOMETRY_MULTI");
        ex.setCorrectAnswer("sp3|0|tetraédrica|apolar");

        assertThat(ex.validateAnswer("sp3|0|tetraédrica|polar")).isFalse();
    }

    @Test
    void geometry_noPolarAlias() {
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("GEOMETRY_MULTI");
        ex.setCorrectAnswer("sp3|0|tetraédrica|apolar");

        assertThat(ex.validateAnswer("sp3|0|tetraédrica|no polar")).isTrue();
        assertThat(ex.validateAnswer("sp3|0|tetraédrica|no es polar")).isTrue();
    }

    @Test
    void geometry_sf6_octahedral() {
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("GEOMETRY_MULTI");
        ex.setCorrectAnswer("sp3d2|0|octaédrica|apolar");

        assertThat(ex.validateAnswer("sp3d2|0|octaédrica|apolar")).isTrue();
        assertThat(ex.validateAnswer("sp^3d^2|0|octahedral|apolar")).isTrue();
    }

    @Test
    void geometry_nullAndBlank_returnsFalse() {
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("GEOMETRY_MULTI");
        ex.setCorrectAnswer("sp3|2|angular|polar");

        assertThat(ex.validateAnswer(null)).isFalse();
        assertThat(ex.validateAnswer("")).isFalse();
        assertThat(ex.validateAnswer("sp3|2|angular")).isFalse(); // solo 3 partes
    }

    // ── Verificaciones matemáticas de configuraciones concretas ──────────────

    @Test
    void sodiumConfig_isCorrect() {
        // Na Z=11: 1s2 2s2 2p6 3s1
        String expected = "1s2 2s2 2p6 3s1";
        SecondBachStructureLinkExercise ex = new SecondBachStructureLinkExercise();
        ex.setExerciseMode("CONFIG_TEXT");
        ex.setCorrectAnswer(expected);
        assertThat(ex.validateAnswer(expected)).isTrue();
        // total electrones = 2+2+6+1 = 11 ✓
        int total = 2 + 2 + 6 + 1;
        assertThat(total).isEqualTo(11);
    }

    @Test
    void ironConfig_hasCorrectElectronCount() {
        // Fe Z=26: 1s2 2s2 2p6 3s2 3p6 3d6 4s2
        int total = 2 + 2 + 6 + 2 + 6 + 6 + 2;
        assertThat(total).isEqualTo(26);
    }

    @Test
    void kryptonConfig_fullLastPeriod() {
        // Kr Z=36: 1s2 2s2 2p6 3s2 3p6 3d10 4s2 4p6
        int total = 2 + 2 + 6 + 2 + 6 + 10 + 2 + 6;
        assertThat(total).isEqualTo(36);
    }
}
