package com.gap.fyq.service;

import com.gap.fyq.model.matter.GasLaw;
import com.gap.fyq.model.matter.MatterExerciseType;
import com.gap.fyq.model.matter.MatterQuantitativeExercise;
import com.gap.fyq.repository.MatterQuantitativeExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

import static com.gap.fyq.model.matter.GasLaw.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatterQuantitativeExerciseService {

    private final MatterQuantitativeExerciseRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "2ESO";
    private static final String BLOCK  = "BL2";
    private static final double K_OFFSET = 273.0;   // T(K) = T(°C) + 273

    // =========================================================================
    // Escenarios de DENSIDAD  d = m / V
    // =========================================================================

    private record DensityScenario(
        String substance,
        double mass,    // gramos
        double volume,  // cm³
        double density  // g/cm³  — debe cumplir density = mass/volume
    ) {}

    // "Unknown" varía en el servicio: cualquiera de las tres variables puede ser incógnita
    private static final List<DensityScenario> DENSITY_SCENARIOS = List.of(
        new DensityScenario("hierro",    787.0,  100.0, 7.87),
        new DensityScenario("hierro",    393.5,   50.0, 7.87),
        new DensityScenario("aluminio",  270.0,  100.0, 2.70),
        new DensityScenario("aluminio",  135.0,   50.0, 2.70),
        new DensityScenario("cobre",     448.0,   50.0, 8.96),
        new DensityScenario("cobre",     224.0,   25.0, 8.96),
        new DensityScenario("plomo",     567.5,   50.0, 11.35),
        new DensityScenario("plomo",     227.0,   20.0, 11.35),
        new DensityScenario("agua",      250.0,  250.0, 1.00),
        new DensityScenario("agua",      500.0,  500.0, 1.00),
        new DensityScenario("etanol",     79.0,  100.0, 0.79),
        new DensityScenario("etanol",    158.0,  200.0, 0.79),
        new DensityScenario("corcho",     48.0,  200.0, 0.24),
        new DensityScenario("glicerina", 126.0,  100.0, 1.26)
    );

    // Incógnitas posibles para densidad (distribución uniforme)
    private static final List<String> DENSITY_UNKNOWNS = List.of("densidad", "masa", "volumen");

    // =========================================================================
    // Escenarios de GASES
    // =========================================================================

    // --- Boyle: P₁·V₁ = P₂·V₂  (T = cte.) ---
    private record BoyleScenario(
        String unknownVar,           // "P2" o "V2"
        double p1, String pressureUnit,
        double v1, String volumeUnit,
        double p2, double v2         // ambos conocidos; el unknownVar es el que se pregunta
    ) {}

    private static final List<BoyleScenario> BOYLE_SCENARIOS = List.of(
        new BoyleScenario("V2", 1.0,"atm",   10.0,"L", 2.0,   5.0),
        new BoyleScenario("V2", 100.0,"kPa",  6.0,"L", 150.0, 4.0),
        new BoyleScenario("V2", 200.0,"kPa",  3.0,"L", 100.0, 6.0),
        new BoyleScenario("V2", 1.0,"atm",   20.0,"L", 4.0,   5.0),
        new BoyleScenario("V2", 2.0,"atm",   15.0,"L", 5.0,   6.0),
        new BoyleScenario("V2", 300.0,"kPa",  8.0,"L", 400.0, 6.0),
        new BoyleScenario("P2", 1.0,"atm",   10.0,"L", 2.0,   5.0),
        new BoyleScenario("P2", 200.0,"kPa",  3.0,"L", 100.0, 6.0),
        new BoyleScenario("P2", 100.0,"kPa",  4.0,"L", 200.0, 2.0),
        new BoyleScenario("P2", 3.0,"atm",    4.0,"L", 2.0,   6.0),
        new BoyleScenario("P2", 400.0,"kPa",  6.0,"L", 300.0, 8.0)
    );

    // --- Charles: V₁/T₁ = V₂/T₂  (P = cte.) — temperaturas en °C, fórmula en K ---
    private record CharlesScenario(
        String unknownVar,           // "V2" o "T2"
        double v1, String volumeUnit,
        double t1Celsius,
        double v2, double t2Celsius  // ambos conocidos; el unknownVar es el que se pregunta
    ) {}

    private static final List<CharlesScenario> CHARLES_SCENARIOS = List.of(
        // unknownVar = V2
        new CharlesScenario("V2", 10.0,"L",  27.0,  20.0, 327.0),  // 300K→600K
        new CharlesScenario("V2",  5.0,"L",   0.0,  10.0, 273.0),  // 273K→546K
        new CharlesScenario("V2",  6.0,"L",  27.0,   9.0, 177.0),  // 300K→450K
        new CharlesScenario("V2",  4.0,"L", -73.0,   8.0, 127.0),  // 200K→400K
        new CharlesScenario("V2",  3.0,"L",  27.0,   4.0, 127.0),  // 300K→400K
        new CharlesScenario("V2",  8.0,"L",  27.0,  12.0, 177.0),  // 300K→450K
        // unknownVar = T2 (respuesta en K)
        new CharlesScenario("T2", 10.0,"L",  27.0,  20.0, 327.0),  // T2=600K
        new CharlesScenario("T2",  5.0,"L",   0.0,  10.0, 273.0),  // T2=546K
        new CharlesScenario("T2",  3.0,"L",  27.0,   4.0, 127.0),  // T2=400K
        new CharlesScenario("T2",  4.0,"L",  27.0,   6.0, 177.0),  // T2=450K
        new CharlesScenario("T2",  8.0,"L",  27.0,  12.0, 177.0)   // T2=450K
    );

    // --- Gay-Lussac: P₁/T₁ = P₂/T₂  (V = cte.) — temperaturas en °C, fórmula en K ---
    private record GayLussacScenario(
        String unknownVar,           // "P2" o "T2"
        double p1, String pressureUnit,
        double t1Celsius,
        double p2, double t2Celsius  // ambos conocidos; el unknownVar es el que se pregunta
    ) {}

    private static final List<GayLussacScenario> GAY_LUSSAC_SCENARIOS = List.of(
        // unknownVar = P2
        new GayLussacScenario("P2", 100.0,"kPa",  27.0, 200.0, 327.0),  // 300K→600K
        new GayLussacScenario("P2",   2.0,"atm",   0.0,   4.0, 273.0),  // 273K→546K
        new GayLussacScenario("P2", 150.0,"kPa",  27.0, 200.0, 127.0),  // 300K→400K
        new GayLussacScenario("P2", 300.0,"kPa",  27.0, 600.0, 327.0),  // 300K→600K
        new GayLussacScenario("P2",   1.0,"atm",  27.0,   2.0, 327.0),  // 300K→600K
        // unknownVar = T2 (respuesta en K)
        new GayLussacScenario("T2",   1.0,"atm",  27.0,   3.0, 627.0),  // T2=900K
        new GayLussacScenario("T2", 100.0,"kPa",  27.0, 200.0, 327.0),  // T2=600K
        new GayLussacScenario("T2",   2.0,"atm",   0.0,   4.0, 273.0),  // T2=546K
        new GayLussacScenario("T2", 150.0,"kPa",  27.0, 200.0, 127.0),  // T2=400K
        new GayLussacScenario("T2",   1.0,"atm",  27.0,   2.0, 327.0)   // T2=600K
    );

    // =========================================================================
    // API pública
    // =========================================================================

    public MatterQuantitativeExercise generateAndSave() {
        // 40% densidad, 60% gases (Boyle/Charles/Gay-Lussac a partes iguales)
        MatterQuantitativeExercise ex = switch (random.nextInt(10)) {
            case 0, 1, 2, 3 -> buildDensityExercise();
            case 4, 5, 6    -> buildBoyleExercise();
            case 7, 8       -> buildCharlesExercise();
            default         -> buildGayLussacExercise();
        };
        log.debug("Guardando ejercicio BL2: tipo={} ley={} incógnita={}",
            ex.getExerciseType(), ex.getGasLaw(), ex.getUnknownVariable());
        return repository.save(ex);
    }

    public MatterQuantitativeExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ejercicio BL2 no encontrado: " + id));
    }

    // =========================================================================
    // Constructores internos — DENSIDAD
    // =========================================================================

    private MatterQuantitativeExercise buildDensityExercise() {
        DensityScenario sc = DENSITY_SCENARIOS.get(random.nextInt(DENSITY_SCENARIOS.size()));
        String unknown = DENSITY_UNKNOWNS.get(random.nextInt(DENSITY_UNKNOWNS.size()));

        double correctValue;
        String correctDisplay;
        String statement;
        String explanation;

        switch (unknown) {
            case "densidad" -> {
                correctValue   = sc.density();
                correctDisplay = fmt(sc.density()) + " g/cm³";
                statement = String.format(
                    "Una muestra de %s tiene una masa de %s g y ocupa un volumen de %s cm³. " +
                    "Calcula su densidad.",
                    sc.substance(), fmtInt(sc.mass()), fmtInt(sc.volume()));
                explanation = buildDensityExplanation("densidad", sc);
            }
            case "masa" -> {
                correctValue   = sc.mass();
                correctDisplay = fmt(sc.mass()) + " g";
                statement = String.format(
                    "La densidad del %s es %s g/cm³. Si tenemos una muestra de %s cm³, " +
                    "¿cuál es su masa?",
                    sc.substance(), fmt(sc.density()), fmtInt(sc.volume()));
                explanation = buildDensityExplanation("masa", sc);
            }
            default -> {   // volumen
                correctValue   = sc.volume();
                correctDisplay = fmt(sc.volume()) + " cm³";
                statement = String.format(
                    "La densidad del %s es %s g/cm³. Si tenemos una muestra de %s g, " +
                    "¿cuál es su volumen?",
                    sc.substance(), fmt(sc.density()), fmtInt(sc.mass()));
                explanation = buildDensityExplanation("volumen", sc);
            }
        }

        MatterQuantitativeExercise ex = new MatterQuantitativeExercise();
        ex.setCourse(COURSE); ex.setBlock(BLOCK);
        ex.setExerciseType(MatterExerciseType.DENSITY);
        ex.setUnknownVariable(unknown);
        ex.setStatement(statement);
        ex.setCorrectAnswerValue(correctValue);
        ex.setCorrectAnswerDisplay(correctDisplay);
        ex.setExplanation(explanation);
        return ex;
    }

    private String buildDensityExplanation(String unknown, DensityScenario sc) {
        String mStr = fmtInt(sc.mass());
        String vStr = fmtInt(sc.volume());
        String dStr = fmt(sc.density());
        return switch (unknown) {
            case "densidad" -> String.format(
                "Aplicamos la definición de densidad:\n" +
                "\\[d = \\dfrac{m}{V} = \\dfrac{%s\\,\\text{g}}{%s\\,\\text{cm}^3} = %s\\,\\text{g/cm}^3\\]\n" +
                "∴  d(%s) = %s g/cm³",
                mStr, vStr, dStr, sc.substance(), dStr);
            case "masa" -> String.format(
                "Despejamos la masa de la fórmula de densidad:\n" +
                "\\[m = d \\times V = %s\\,\\text{g/cm}^3 \\times %s\\,\\text{cm}^3 = %s\\,\\text{g}\\]\n" +
                "∴  m = %s g",
                dStr, vStr, mStr, mStr);
            default -> String.format(
                "Despejamos el volumen de la fórmula de densidad:\n" +
                "\\[V = \\dfrac{m}{d} = \\dfrac{%s\\,\\text{g}}{%s\\,\\text{g/cm}^3} = %s\\,\\text{cm}^3\\]\n" +
                "∴  V = %s cm³",
                mStr, dStr, vStr, vStr);
        };
    }

    // =========================================================================
    // Constructores internos — BOYLE
    // =========================================================================

    private MatterQuantitativeExercise buildBoyleExercise() {
        BoyleScenario sc = BOYLE_SCENARIOS.get(random.nextInt(BOYLE_SCENARIOS.size()));

        double correctValue;
        String correctDisplay;
        String statement;
        String explanation;

        if ("V2".equals(sc.unknownVar())) {
            correctValue   = sc.v2();
            correctDisplay = fmt(sc.v2()) + " " + sc.volumeUnit();
            statement = String.format(
                "Un gas ocupa un volumen de %s %s a una presión de %s %s. " +
                "Si la presión aumenta a %s %s (temperatura constante), ¿cuál será el nuevo volumen?",
                fmt(sc.v1()), sc.volumeUnit(), fmt(sc.p1()), sc.pressureUnit(),
                fmt(sc.p2()), sc.pressureUnit());
            explanation = buildBoyleExplanation(sc, "V2");
        } else {
            correctValue   = sc.p2();
            correctDisplay = fmt(sc.p2()) + " " + sc.pressureUnit();
            statement = String.format(
                "Un gas ocupa un volumen de %s %s a una presión de %s %s. " +
                "Si el volumen cambia a %s %s (temperatura constante), ¿cuál será la nueva presión?",
                fmt(sc.v1()), sc.volumeUnit(), fmt(sc.p1()), sc.pressureUnit(),
                fmt(sc.v2()), sc.volumeUnit());
            explanation = buildBoyleExplanation(sc, "P2");
        }

        MatterQuantitativeExercise ex = new MatterQuantitativeExercise();
        ex.setCourse(COURSE); ex.setBlock(BLOCK);
        ex.setExerciseType(MatterExerciseType.GAS_LAWS);
        ex.setGasLaw(BOYLE);
        ex.setUnknownVariable(sc.unknownVar());
        ex.setStatement(statement);
        ex.setCorrectAnswerValue(correctValue);
        ex.setCorrectAnswerDisplay(correctDisplay);
        ex.setExplanation(explanation);
        return ex;
    }

    private String buildBoyleExplanation(BoyleScenario sc, String unknown) {
        String p1 = fmt(sc.p1()); String p2 = fmt(sc.p2());
        String v1 = fmt(sc.v1()); String v2 = fmt(sc.v2());
        String pu = sc.pressureUnit(); String vu = sc.volumeUnit();
        if ("V2".equals(unknown)) {
            return String.format(
                "Ley de Boyle-Mariotte: \\(P_1 \\cdot V_1 = P_2 \\cdot V_2\\)  (T = cte.)\n\n" +
                "Datos: \\(P_1=%s\\,\\text{%s},\\; V_1=%s\\,\\text{%s},\\; P_2=%s\\,\\text{%s}\\)\n\n" +
                "Despejamos \\(V_2\\):\n" +
                "\\[V_2 = \\dfrac{P_1 \\cdot V_1}{P_2} = \\dfrac{%s\\,\\text{%s} \\times %s\\,\\text{%s}}{%s\\,\\text{%s}} = %s\\,\\text{%s}\\]\n" +
                "∴  V₂ = %s %s",
                p1,pu, v1,vu, p2,pu,
                p1,pu, v1,vu, p2,pu, v2,vu,
                v2,vu);
        } else {
            return String.format(
                "Ley de Boyle-Mariotte: \\(P_1 \\cdot V_1 = P_2 \\cdot V_2\\)  (T = cte.)\n\n" +
                "Datos: \\(P_1=%s\\,\\text{%s},\\; V_1=%s\\,\\text{%s},\\; V_2=%s\\,\\text{%s}\\)\n\n" +
                "Despejamos \\(P_2\\):\n" +
                "\\[P_2 = \\dfrac{P_1 \\cdot V_1}{V_2} = \\dfrac{%s\\,\\text{%s} \\times %s\\,\\text{%s}}{%s\\,\\text{%s}} = %s\\,\\text{%s}\\]\n" +
                "∴  P₂ = %s %s",
                p1,pu, v1,vu, v2,vu,
                p1,pu, v1,vu, v2,vu, p2,pu,
                p2,pu);
        }
    }

    // =========================================================================
    // Constructores internos — CHARLES
    // =========================================================================

    private MatterQuantitativeExercise buildCharlesExercise() {
        CharlesScenario sc = CHARLES_SCENARIOS.get(random.nextInt(CHARLES_SCENARIOS.size()));
        double t1K = sc.t1Celsius() + K_OFFSET;
        double t2K = sc.t2Celsius() + K_OFFSET;

        double correctValue;
        String correctDisplay;
        String statement;
        String explanation;

        if ("V2".equals(sc.unknownVar())) {
            correctValue   = sc.v2();
            correctDisplay = fmt(sc.v2()) + " " + sc.volumeUnit();
            statement = String.format(
                "Un gas ocupa %s %s a %s°C. Si la temperatura aumenta a %s°C " +
                "(presión constante), ¿cuál será el nuevo volumen?",
                fmt(sc.v1()), sc.volumeUnit(), fmtInt(sc.t1Celsius()), fmtInt(sc.t2Celsius()));
            explanation = buildCharlesExplanation(sc, t1K, t2K, "V2");
        } else {
            // T2 unknown — answer in K
            correctValue   = t2K;
            int t2CEq      = (int) sc.t2Celsius();
            correctDisplay = fmt(t2K) + " K (" + t2CEq + "°C)";
            statement = String.format(
                "Un gas ocupa %s %s a %s°C. ¿A qué temperatura (en K) ocupará %s %s " +
                "(presión constante)?",
                fmt(sc.v1()), sc.volumeUnit(), fmtInt(sc.t1Celsius()),
                fmt(sc.v2()), sc.volumeUnit());
            explanation = buildCharlesExplanation(sc, t1K, t2K, "T2");
        }

        MatterQuantitativeExercise ex = new MatterQuantitativeExercise();
        ex.setCourse(COURSE); ex.setBlock(BLOCK);
        ex.setExerciseType(MatterExerciseType.GAS_LAWS);
        ex.setGasLaw(CHARLES);
        ex.setUnknownVariable(sc.unknownVar());
        ex.setStatement(statement);
        ex.setCorrectAnswerValue(correctValue);
        ex.setCorrectAnswerDisplay(correctDisplay);
        ex.setExplanation(explanation);
        return ex;
    }

    private String buildCharlesExplanation(CharlesScenario sc, double t1K, double t2K, String unknown) {
        String v1s  = fmt(sc.v1()); String v2s = fmt(sc.v2()); String vu = sc.volumeUnit();
        String t1cs = fmtInt(sc.t1Celsius()); String t2cs = fmtInt(sc.t2Celsius());
        String t1Ks = fmt(t1K); String t2Ks = fmt(t2K);

        String convBlock = String.format(
            "Convertimos las temperaturas a Kelvin (T(K) = T(°C) + 273):\n" +
            "        T₁ = %s°C + 273 = %s K\n" +
            "        T₂ = %s°C + 273 = %s K\n", t1cs, t1Ks, t2cs, t2Ks);

        if ("V2".equals(unknown)) {
            return String.format(
                "Ley de Charles: \\(\\dfrac{V_1}{T_1} = \\dfrac{V_2}{T_2}\\)  (P = cte.)\n\n" +
                "%s\n" +
                "Datos: \\(V_1=%s\\,\\text{%s},\\; T_1=%s\\,\\text{K},\\; T_2=%s\\,\\text{K}\\)\n\n" +
                "Despejamos \\(V_2\\):\n" +
                "\\[V_2 = \\dfrac{V_1 \\cdot T_2}{T_1} = \\dfrac{%s\\,\\text{%s} \\times %s\\,\\text{K}}{%s\\,\\text{K}} = %s\\,\\text{%s}\\]\n" +
                "∴  V₂ = %s %s",
                convBlock, v1s,vu, t1Ks, t2Ks,
                v1s,vu, t2Ks, t1Ks, v2s,vu,
                v2s,vu);
        } else {
            return String.format(
                "Ley de Charles: \\(\\dfrac{V_1}{T_1} = \\dfrac{V_2}{T_2}\\)  (P = cte.)\n\n" +
                "%s\n" +
                "Datos: \\(V_1=%s\\,\\text{%s},\\; T_1=%s\\,\\text{K},\\; V_2=%s\\,\\text{%s}\\)\n\n" +
                "Despejamos \\(T_2\\):\n" +
                "\\[T_2 = \\dfrac{V_2 \\cdot T_1}{V_1} = \\dfrac{%s\\,\\text{%s} \\times %s\\,\\text{K}}{%s\\,\\text{%s}} = %s\\,\\text{K}\\]\n" +
                "T₂ = %s K = %s - 273 = %s°C\n" +
                "∴  T₂ = %s K (%s°C)",
                convBlock, v1s,vu, t1Ks, v2s,vu,
                v2s,vu, t1Ks, v1s,vu, t2Ks,
                t2Ks, t2Ks, fmtInt(sc.t2Celsius()), t2Ks, fmtInt(sc.t2Celsius()));
        }
    }

    // =========================================================================
    // Constructores internos — GAY-LUSSAC
    // =========================================================================

    private MatterQuantitativeExercise buildGayLussacExercise() {
        GayLussacScenario sc = GAY_LUSSAC_SCENARIOS.get(random.nextInt(GAY_LUSSAC_SCENARIOS.size()));
        double t1K = sc.t1Celsius() + K_OFFSET;
        double t2K = sc.t2Celsius() + K_OFFSET;

        double correctValue;
        String correctDisplay;
        String statement;
        String explanation;

        if ("P2".equals(sc.unknownVar())) {
            correctValue   = sc.p2();
            correctDisplay = fmt(sc.p2()) + " " + sc.pressureUnit();
            statement = String.format(
                "Un gas se encuentra a %s %s y %s°C (volumen constante). " +
                "Si la temperatura sube a %s°C, ¿cuál será la nueva presión?",
                fmt(sc.p1()), sc.pressureUnit(), fmtInt(sc.t1Celsius()), fmtInt(sc.t2Celsius()));
            explanation = buildGayLussacExplanation(sc, t1K, t2K, "P2");
        } else {
            correctValue   = t2K;
            int t2CEq      = (int) sc.t2Celsius();
            correctDisplay = fmt(t2K) + " K (" + t2CEq + "°C)";
            statement = String.format(
                "Un gas se encuentra a %s %s y %s°C (volumen constante). " +
                "Si la presión cambia a %s %s, ¿cuál será la nueva temperatura (en K)?",
                fmt(sc.p1()), sc.pressureUnit(), fmtInt(sc.t1Celsius()),
                fmt(sc.p2()), sc.pressureUnit());
            explanation = buildGayLussacExplanation(sc, t1K, t2K, "T2");
        }

        MatterQuantitativeExercise ex = new MatterQuantitativeExercise();
        ex.setCourse(COURSE); ex.setBlock(BLOCK);
        ex.setExerciseType(MatterExerciseType.GAS_LAWS);
        ex.setGasLaw(GAY_LUSSAC);
        ex.setUnknownVariable(sc.unknownVar());
        ex.setStatement(statement);
        ex.setCorrectAnswerValue(correctValue);
        ex.setCorrectAnswerDisplay(correctDisplay);
        ex.setExplanation(explanation);
        return ex;
    }

    private String buildGayLussacExplanation(GayLussacScenario sc, double t1K, double t2K, String unknown) {
        String p1s = fmt(sc.p1()); String p2s = fmt(sc.p2()); String pu = sc.pressureUnit();
        String t1cs = fmtInt(sc.t1Celsius()); String t2cs = fmtInt(sc.t2Celsius());
        String t1Ks = fmt(t1K); String t2Ks = fmt(t2K);

        String convBlock = String.format(
            "Convertimos las temperaturas a Kelvin (T(K) = T(°C) + 273):\n" +
            "        T₁ = %s°C + 273 = %s K\n" +
            "        T₂ = %s°C + 273 = %s K\n", t1cs, t1Ks, t2cs, t2Ks);

        if ("P2".equals(unknown)) {
            return String.format(
                "Ley de Gay-Lussac: \\(\\dfrac{P_1}{T_1} = \\dfrac{P_2}{T_2}\\)  (V = cte.)\n\n" +
                "%s\n" +
                "Datos: \\(P_1=%s\\,\\text{%s},\\; T_1=%s\\,\\text{K},\\; T_2=%s\\,\\text{K}\\)\n\n" +
                "Despejamos \\(P_2\\):\n" +
                "\\[P_2 = \\dfrac{P_1 \\cdot T_2}{T_1} = \\dfrac{%s\\,\\text{%s} \\times %s\\,\\text{K}}{%s\\,\\text{K}} = %s\\,\\text{%s}\\]\n" +
                "∴  P₂ = %s %s",
                convBlock, p1s,pu, t1Ks, t2Ks,
                p1s,pu, t2Ks, t1Ks, p2s,pu,
                p2s,pu);
        } else {
            return String.format(
                "Ley de Gay-Lussac: \\(\\dfrac{P_1}{T_1} = \\dfrac{P_2}{T_2}\\)  (V = cte.)\n\n" +
                "%s\n" +
                "Datos: \\(P_1=%s\\,\\text{%s},\\; T_1=%s\\,\\text{K},\\; P_2=%s\\,\\text{%s}\\)\n\n" +
                "Despejamos \\(T_2\\):\n" +
                "\\[T_2 = \\dfrac{P_2 \\cdot T_1}{P_1} = \\dfrac{%s\\,\\text{%s} \\times %s\\,\\text{K}}{%s\\,\\text{%s}} = %s\\,\\text{K}\\]\n" +
                "T₂ = %s K = %s - 273 = %s°C\n" +
                "∴  T₂ = %s K (%s°C)",
                convBlock, p1s,pu, t1Ks, p2s,pu,
                p2s,pu, t1Ks, p1s,pu, t2Ks,
                t2Ks, t2Ks, fmtInt(sc.t2Celsius()), t2Ks, fmtInt(sc.t2Celsius()));
        }
    }

    // =========================================================================
    // Utilidades de formato numérico
    // =========================================================================

    private String fmt(double value) {
        return new BigDecimal(Double.toString(value))
            .stripTrailingZeros()
            .toPlainString()
            .replace(".", ",");
    }

    /** Formatea como entero si no tiene parte decimal, si no como decimal. */
    private String fmtInt(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return fmt(value);
    }
}
