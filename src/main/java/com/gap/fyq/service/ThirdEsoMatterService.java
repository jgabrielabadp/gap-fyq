package com.gap.fyq.service;

import com.gap.fyq.model.thirdeso.matter.MatterType;
import com.gap.fyq.model.thirdeso.matter.ThirdEsoMatterExercise;
import com.gap.fyq.repository.ThirdEsoMatterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThirdEsoMatterService {

    private final ThirdEsoMatterRepository repository;
    private final Random random = new Random();

    private static final String COURSE   = "3ESO";
    private static final String BLOCK    = "BL2";
    private static final double K_OFFSET = 273.0;  // T(K) = T(°C) + 273

    // =========================================================================
    // ESCENARIOS — PORCENTAJE EN MASA
    // % masa = m_soluto / m_disolución × 100
    // needsSum=true  → otherMass es la masa del disolvente (hay que sumar)
    // needsSum=false → otherMass es la masa total de la disolución (dato directo)
    // =========================================================================

    private record MassPercScenario(
        String soluteName,
        double soluteMass,   // g
        double otherMass,    // g — disolvente (needsSum=true) o disolución (needsSum=false)
        boolean needsSum,
        double percentage    // exacto a 2 decimales
    ) {}

    private static final List<MassPercScenario> MASS_PERC_SCENARIOS = List.of(
        // needsSum = true: enunciado da soluto + disolvente por separado
        new MassPercScenario("cloruro de sodio",    10,  90, true,  10.00),  //  10/100×100
        new MassPercScenario("cloruro de sodio",    20,  80, true,  20.00),  //  20/100×100
        new MassPercScenario("sacarosa",            25, 225, true,  10.00),  //  25/250×100
        new MassPercScenario("sacarosa",            30, 120, true,  20.00),  //  30/150×100
        new MassPercScenario("etanol",              15,  85, true,  15.00),  //  15/100×100
        new MassPercScenario("etanol",              40, 160, true,  20.00),  //  40/200×100
        new MassPercScenario("cloruro de potasio",  12,  88, true,  12.00),  //  12/100×100
        new MassPercScenario("cloruro de sodio",    18, 132, true,  12.00),  //  18/150×100
        new MassPercScenario("nitrato de potasio",  35, 140, true,  20.00),  //  35/175×100
        new MassPercScenario("hidróxido de sodio",   8,  92, true,   8.00),  //   8/100×100
        new MassPercScenario("ácido clorhídrico",    5,  95, true,   5.00),  //   5/100×100
        new MassPercScenario("sacarosa",            50, 200, true,  20.00),  //  50/250×100
        // needsSum = false: enunciado da la masa total de la disolución directamente
        new MassPercScenario("cloruro de sodio",    10, 200, false,  5.00),  //  10/200×100
        new MassPercScenario("sacarosa",            30, 150, false, 20.00),  //  30/150×100
        new MassPercScenario("etanol",              25, 100, false, 25.00),  //  25/100×100
        new MassPercScenario("cloruro de sodio",    20, 250, false,  8.00),  //  20/250×100
        new MassPercScenario("cloruro de potasio",  36, 200, false, 18.00),  //  36/200×100
        new MassPercScenario("sacarosa",            45, 300, false, 15.00),  //  45/300×100
        new MassPercScenario("ácido clorhídrico",   15, 150, false, 10.00),  //  15/150×100
        new MassPercScenario("hidróxido de sodio",  50, 200, false, 25.00),  //  50/200×100
        new MassPercScenario("cloruro de sodio",    24, 120, false, 20.00),  //  24/120×100
        new MassPercScenario("etanol",               6, 200, false,  3.00)   //   6/200×100
    );

    // =========================================================================
    // ESCENARIOS — CONCENTRACIÓN EN g/L
    // C = m_soluto (g) / V (L)
    // Si volumeUnit = "mL" el alumno debe convertir a L antes de calcular.
    // =========================================================================

    private record ConcentGlScenario(
        String soluteName,
        double soluteMass,   // g
        double volumeValue,  // valor en volumeUnit
        String volumeUnit,   // "mL" o "L"
        double volumeInL,    // siempre en L para el cálculo
        double concentration // g/L, exacto a 2 decimales
    ) {}

    private static final List<ConcentGlScenario> CONCENT_GL_SCENARIOS = List.of(
        // volumeUnit = "mL" — requiere conversión mL → L
        new ConcentGlScenario("cloruro de sodio",     5,  500, "mL", 0.50,  10.00),
        new ConcentGlScenario("glucosa",              10,  250, "mL", 0.25,  40.00),
        new ConcentGlScenario("cloruro de sodio",     15,  500, "mL", 0.50,  30.00),
        new ConcentGlScenario("cloruro de potasio",    8,  400, "mL", 0.40,  20.00),
        new ConcentGlScenario("sacarosa",             25,  250, "mL", 0.25, 100.00),
        new ConcentGlScenario("ácido clorhídrico",     6,  300, "mL", 0.30,  20.00),
        new ConcentGlScenario("hidróxido de sodio",   12,  400, "mL", 0.40,  30.00),
        new ConcentGlScenario("nitrato de potasio",   20,  500, "mL", 0.50,  40.00),
        new ConcentGlScenario("cloruro de sodio",      4,  200, "mL", 0.20,  20.00),
        new ConcentGlScenario("cloruro de potasio",    9,  300, "mL", 0.30,  30.00),
        // volumeUnit = "L" — no requiere conversión
        new ConcentGlScenario("cloruro de sodio",     20,  2.0,  "L", 2.00,  10.00),
        new ConcentGlScenario("cloruro de potasio",   30,  3.0,  "L", 3.00,  10.00),
        new ConcentGlScenario("glucosa",              50,  2.0,  "L", 2.00,  25.00),
        new ConcentGlScenario("sacarosa",             40,  2.0,  "L", 2.00,  20.00),
        new ConcentGlScenario("nitrato de potasio",   60,  3.0,  "L", 3.00,  20.00),
        new ConcentGlScenario("cloruro de sodio",    120,  4.0,  "L", 4.00,  30.00),
        new ConcentGlScenario("hidróxido de sodio",   15,  3.0,  "L", 3.00,   5.00),
        new ConcentGlScenario("cloruro de potasio",   36,  4.0,  "L", 4.00,   9.00)
    );

    // =========================================================================
    // ESCENARIOS — LEYES DE LOS GASES (AVANZADO)
    // Todos los valores están precalculados para dar resultados limpios.
    // Temperatura almacenada en °C; la fórmula siempre usa Kelvin.
    // =========================================================================

    // --- Boyle: P₁·V₁ = P₂·V₂  (T = cte.) ---
    // unknownVar puede ser "P2", "V2", "P1" o "V1" — todas cuatro variables son incógnita posible.
    private record BoyleScenario(
        String unknownVar,
        double p1, String pUnit,
        double v1, String vUnit,
        double p2, double v2
    ) {}

    private static final List<BoyleScenario> BOYLE_SCENARIOS = List.of(
        // unknownVar = "P2":  p2 = p1·v1/v2
        new BoyleScenario("P2",   2.0, "atm", 5.0, "L",   1.0, 10.0),
        new BoyleScenario("P2", 100.0, "kPa", 8.0, "L", 400.0,  2.0),
        new BoyleScenario("P2",   3.0, "atm", 4.0, "L",   2.0,  6.0),
        new BoyleScenario("P2", 200.0, "kPa", 3.0, "L", 100.0,  6.0),
        // unknownVar = "V2":  v2 = p1·v1/p2
        new BoyleScenario("V2",   1.0, "atm",  6.0, "L",   3.0, 2.0),
        new BoyleScenario("V2", 300.0, "kPa",  4.0, "L", 150.0, 8.0),
        new BoyleScenario("V2",   2.0, "atm",  9.0, "L",   6.0, 3.0),
        new BoyleScenario("V2", 100.0, "kPa", 10.0, "L", 500.0, 2.0),
        // unknownVar = "P1":  p1 = p2·v2/v1
        new BoyleScenario("P1",   1.0, "atm", 6.0, "L",   2.0,  3.0),
        new BoyleScenario("P1", 400.0, "kPa", 2.0, "L", 100.0,  8.0),
        new BoyleScenario("P1",   8.0, "atm", 5.0, "L",   4.0, 10.0),
        new BoyleScenario("P1", 150.0, "kPa", 4.0, "L", 300.0,  2.0),
        // unknownVar = "V1":  v1 = p2·v2/p1
        new BoyleScenario("V1",   3.0, "atm", 3.0, "L",   1.0, 9.0),
        new BoyleScenario("V1", 100.0, "kPa", 8.0, "L", 400.0, 2.0),
        new BoyleScenario("V1",   4.0, "atm", 3.0, "L",   2.0, 6.0),
        new BoyleScenario("V1", 150.0, "kPa", 8.0, "L", 300.0, 4.0)
    );

    // --- Charles: V₁/T₁ = V₂/T₂  (P = cte.) — temperaturas en °C, fórmula en K ---
    // unknownVar puede ser "V2", "T2", "V1" o "T1".
    // Para T2/T1 la respuesta correcta se da en K.
    private record CharlesScenario(
        String unknownVar,
        double v1, String vUnit,
        double t1Celsius,
        double v2,
        double t2Celsius
    ) {}

    private static final List<CharlesScenario> CHARLES_SCENARIOS = List.of(
        // unknownVar = "V2":  v2 = v1·T2K/T1K
        new CharlesScenario("V2",  6.0, "L",  27.0, 12.0, 327.0),  // 300K→600K
        new CharlesScenario("V2",  4.0, "L",   0.0,  8.0, 273.0),  // 273K→546K
        new CharlesScenario("V2",  8.0, "L", 127.0, 16.0, 527.0),  // 400K→800K
        new CharlesScenario("V2",  6.0, "L",  27.0,  9.0, 177.0),  // 300K→450K
        // unknownVar = "T2":  T2K = v2·T1K/v1  (respuesta en K)
        new CharlesScenario("T2",  5.0, "L",  27.0, 10.0, 327.0),  // T2K=600K
        new CharlesScenario("T2",  4.0, "L", -73.0,  8.0, 127.0),  // T2K=400K
        new CharlesScenario("T2",  3.0, "L", 127.0,  6.0, 527.0),  // T2K=800K
        new CharlesScenario("T2",  8.0, "L",  27.0,  4.0,-123.0),  // T2K=150K
        // unknownVar = "V1":  v1 = v2·T1K/T2K
        new CharlesScenario("V1",  6.0, "L",  27.0, 12.0, 327.0),  // v1=6L
        new CharlesScenario("V1",  4.0, "L",   0.0,  8.0, 273.0),  // v1=4L
        new CharlesScenario("V1", 12.0, "L", 127.0,  6.0, -73.0),  // v1=12L
        new CharlesScenario("V1",  6.0, "L",  27.0,  9.0, 177.0),  // v1=6L
        // unknownVar = "T1":  T1K = v1·T2K/v2  (respuesta en K)
        new CharlesScenario("T1",  6.0, "L",  27.0, 12.0, 327.0),  // T1K=300K
        new CharlesScenario("T1",  4.0, "L", -73.0,  8.0, 127.0),  // T1K=200K
        new CharlesScenario("T1",  5.0, "L",  27.0, 10.0, 327.0),  // T1K=300K
        new CharlesScenario("T1",  8.0, "L", 327.0,  4.0,  27.0)   // T1K=600K
    );

    // --- Gay-Lussac: P₁/T₁ = P₂/T₂  (V = cte.) — temperaturas en °C, fórmula en K ---
    // unknownVar puede ser "P2", "T2", "P1" o "T1".
    private record GayLussacScenario(
        String unknownVar,
        double p1, String pUnit,
        double t1Celsius,
        double p2,
        double t2Celsius
    ) {}

    private static final List<GayLussacScenario> GAY_LUSSAC_SCENARIOS = List.of(
        // unknownVar = "P2":  p2 = p1·T2K/T1K
        new GayLussacScenario("P2", 100.0, "kPa",  27.0, 200.0, 327.0),  // 300K→600K
        new GayLussacScenario("P2",   2.0, "atm",   0.0,   4.0, 273.0),  // 273K→546K
        new GayLussacScenario("P2", 150.0, "kPa",  27.0, 100.0, -73.0),  // 300K→200K
        new GayLussacScenario("P2",   3.0, "atm", 127.0,   6.0, 527.0),  // 400K→800K
        // unknownVar = "T2":  T2K = p2·T1K/p1  (respuesta en K)
        new GayLussacScenario("T2", 100.0, "kPa",  27.0, 200.0, 327.0),  // T2K=600K
        new GayLussacScenario("T2",   2.0, "atm",   0.0,   4.0, 273.0),  // T2K=546K
        new GayLussacScenario("T2",   3.0, "atm",  27.0,   6.0, 327.0),  // T2K=600K
        new GayLussacScenario("T2", 100.0, "kPa", -73.0, 300.0, 327.0),  // T2K=600K
        // unknownVar = "P1":  p1 = p2·T1K/T2K
        new GayLussacScenario("P1", 100.0, "kPa",  27.0, 200.0, 327.0),  // p1=100kPa
        new GayLussacScenario("P1",   2.0, "atm",   0.0,   4.0, 273.0),  // p1=2atm
        new GayLussacScenario("P1", 300.0, "kPa", 127.0, 150.0, -73.0),  // p1=300kPa
        new GayLussacScenario("P1",   3.0, "atm", 127.0,   6.0, 527.0),  // p1=3atm
        // unknownVar = "T1":  T1K = p1·T2K/p2  (respuesta en K)
        new GayLussacScenario("T1", 100.0, "kPa",  27.0, 200.0, 327.0),  // T1K=300K
        new GayLussacScenario("T1",   2.0, "atm",   0.0,   4.0, 273.0),  // T1K=273K
        new GayLussacScenario("T1",   3.0, "atm",  27.0,   6.0, 327.0),  // T1K=300K
        new GayLussacScenario("T1", 300.0, "kPa", 127.0, 150.0, -73.0)   // T1K=400K
    );

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public ThirdEsoMatterExercise generateAndSave() {
        ThirdEsoMatterExercise ex = new ThirdEsoMatterExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);

        // Distribución: ~33% cada tipo
        int roll = random.nextInt(9);
        if (roll < 3) {
            buildMassPercentage(ex);
        } else if (roll < 6) {
            buildVolumeConcentGL(ex);
        } else {
            buildGasLaws(ex);
        }

        log.debug("3ESO BL2 generado: tipo={} incógnita={}", ex.getMatterType(), ex.getUnknownVariable());
        return repository.save(ex);
    }

    public ThirdEsoMatterExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ejercicio 3ESO BL2 no encontrado: " + id));
    }

    // =========================================================================
    // CONSTRUCTORES INTERNOS — PORCENTAJE EN MASA
    // =========================================================================

    private void buildMassPercentage(ThirdEsoMatterExercise ex) {
        MassPercScenario sc = MASS_PERC_SCENARIOS.get(random.nextInt(MASS_PERC_SCENARIOS.size()));
        double totalMass = sc.needsSum() ? sc.soluteMass() + sc.otherMass() : sc.otherMass();

        ex.setMatterType(MatterType.MASS_PERCENTAGE);
        ex.setUnknownVariable("porcentaje_masa");
        ex.setAnswerUnit("%");
        ex.setCorrectAnswerValue(sc.percentage());
        ex.setCorrectAnswerDisplay(fmt(sc.percentage()) + " %");

        if (sc.needsSum()) {
            ex.setStatement(String.format(
                "Se disuelven %s g de %s en %s g de agua. " +
                "Calcula el porcentaje en masa (%%) de la disolución resultante.",
                fmtInt(sc.soluteMass()), sc.soluteName(), fmtInt(sc.otherMass())));
        } else {
            ex.setStatement(String.format(
                "Una disolución acuosa de %s tiene una masa total de %s g y contiene %s g de soluto. " +
                "Calcula el porcentaje en masa (%%).",
                sc.soluteName(), fmtInt(sc.otherMass()), fmtInt(sc.soluteMass())));
        }

        ex.setExplanation(buildMassPercExplanation(sc, totalMass));
    }

    private String buildMassPercExplanation(MassPercScenario sc, double totalMass) {
        String soluteStr = fmtInt(sc.soluteMass());
        String otherStr  = fmtInt(sc.otherMass());
        String totalStr  = fmtInt(totalMass);
        String percStr   = fmt(sc.percentage());

        StringBuilder sb = new StringBuilder();
        sb.append("Fórmula del porcentaje en masa:\n");
        sb.append("\\[\\%\\,\\text{masa} = \\frac{m_{\\text{soluto}}}{m_{\\text{disolución}}} \\times 100\\]\n\n");

        if (sc.needsSum()) {
            sb.append("⚠️ <strong>Atención:</strong> el enunciado proporciona la masa del soluto ");
            sb.append("y la del disolvente por separado. Es necesario <strong>sumarlas</strong> ");
            sb.append("para obtener la masa total de la disolución:\n");
            sb.append(String.format(
                "\\[m_{\\text{disolución}} = m_{\\text{soluto}} + m_{\\text{disolvente}} = " +
                "%s\\,\\text{g} + %s\\,\\text{g} = %s\\,\\text{g}\\]\n\n",
                soluteStr, otherStr, totalStr));
        } else {
            sb.append(String.format(
                "La masa de la disolución se proporciona directamente: " +
                "\\(m_{\\text{disolución}} = %s\\,\\text{g}\\)\n\n",
                totalStr));
        }

        sb.append("Sustituyendo en la fórmula:\n");
        sb.append(String.format(
            "\\[\\%%\\,\\text{masa} = \\frac{%s\\,\\text{g}}{%s\\,\\text{g}} \\times 100 = %s\\,\\%%\\]\n\n",
            soluteStr, totalStr, percStr));
        sb.append(String.format("∴  %% masa = %s%%", percStr));

        return sb.toString();
    }

    // =========================================================================
    // CONSTRUCTORES INTERNOS — CONCENTRACIÓN EN g/L
    // =========================================================================

    private void buildVolumeConcentGL(ThirdEsoMatterExercise ex) {
        ConcentGlScenario sc = CONCENT_GL_SCENARIOS.get(random.nextInt(CONCENT_GL_SCENARIOS.size()));

        ex.setMatterType(MatterType.VOLUME_CONCENT_GL);
        ex.setUnknownVariable("concentracion_gl");
        ex.setAnswerUnit("g/L");
        ex.setCorrectAnswerValue(sc.concentration());
        ex.setCorrectAnswerDisplay(fmt(sc.concentration()) + " g/L");

        ex.setStatement(String.format(
            "Se disuelven %s g de %s en agua hasta obtener %s %s de disolución. " +
            "Calcula la concentración en g/L.",
            fmtInt(sc.soluteMass()), sc.soluteName(), fmt(sc.volumeValue()), sc.volumeUnit()));

        ex.setExplanation(buildConcentGlExplanation(sc));
    }

    private String buildConcentGlExplanation(ConcentGlScenario sc) {
        String mStr  = fmtInt(sc.soluteMass());
        String vValStr = fmt(sc.volumeValue());
        String vLStr = fmt(sc.volumeInL());
        String cStr  = fmt(sc.concentration());

        StringBuilder sb = new StringBuilder();
        sb.append("Fórmula de la concentración en masa/volumen:\n");
        sb.append("\\[C = \\frac{m_{\\text{soluto}}\\,(\\text{g})}{V_{\\text{disolución}}\\,(\\text{L})}\\]\n\n");

        if ("mL".equals(sc.volumeUnit())) {
            sb.append("⚠️ <strong>Atención:</strong> el volumen está en mL. ");
            sb.append("Hay que convertirlo a L antes de calcular:\n");
            sb.append(String.format(
                "\\[V = %s\\,\\text{mL} \\times \\frac{1\\,\\text{L}}{1000\\,\\text{mL}} = %s\\,\\text{L}\\]\n\n",
                vValStr, vLStr));
        }

        sb.append("Sustituyendo:\n");
        sb.append(String.format(
            "\\[C = \\frac{%s\\,\\text{g}}{%s\\,\\text{L}} = %s\\,\\text{g/L}\\]\n\n",
            mStr, vLStr, cStr));
        sb.append(String.format("∴  C = %s g/L", cStr));

        return sb.toString();
    }

    // =========================================================================
    // CONSTRUCTORES INTERNOS — LEYES DE LOS GASES
    // =========================================================================

    private void buildGasLaws(ThirdEsoMatterExercise ex) {
        ex.setMatterType(MatterType.GAS_LAWS_ADVANCED);
        switch (random.nextInt(3)) {
            case 0 -> buildBoyle(ex, BOYLE_SCENARIOS.get(random.nextInt(BOYLE_SCENARIOS.size())));
            case 1 -> buildCharles(ex, CHARLES_SCENARIOS.get(random.nextInt(CHARLES_SCENARIOS.size())));
            default -> buildGayLussac(ex, GAY_LUSSAC_SCENARIOS.get(random.nextInt(GAY_LUSSAC_SCENARIOS.size())));
        }
    }

    // ── Boyle ────────────────────────────────────────────────────────────────

    private void buildBoyle(ThirdEsoMatterExercise ex, BoyleScenario sc) {
        ex.setUnknownVariable(sc.unknownVar());
        String p1s = fmt(sc.p1()), v1s = fmt(sc.v1()), p2s = fmt(sc.p2()), v2s = fmt(sc.v2());

        switch (sc.unknownVar()) {
            case "P2" -> {
                ex.setStatement(String.format(
                    "Un gas ocupa %s %s a %s %s. Si se comprime hasta %s %s " +
                    "manteniéndose la temperatura constante, ¿cuál será la nueva presión?",
                    v1s, sc.vUnit(), p1s, sc.pUnit(), v2s, sc.vUnit()));
                ex.setCorrectAnswerValue(sc.p2());
                ex.setCorrectAnswerDisplay(p2s + " " + sc.pUnit());
                ex.setAnswerUnit(sc.pUnit());
            }
            case "V2" -> {
                ex.setStatement(String.format(
                    "Un gas ocupa %s %s a una presión de %s %s. ¿Qué volumen ocupará " +
                    "si la presión cambia a %s %s (temperatura constante)?",
                    v1s, sc.vUnit(), p1s, sc.pUnit(), p2s, sc.pUnit()));
                ex.setCorrectAnswerValue(sc.v2());
                ex.setCorrectAnswerDisplay(v2s + " " + sc.vUnit());
                ex.setAnswerUnit(sc.vUnit());
            }
            case "P1" -> {
                ex.setStatement(String.format(
                    "Un gas ocupa %s %s. Al modificarse el volumen a %s %s a temperatura constante, " +
                    "la presión pasa a ser %s %s. ¿Cuál era la presión inicial?",
                    v1s, sc.vUnit(), v2s, sc.vUnit(), p2s, sc.pUnit()));
                ex.setCorrectAnswerValue(sc.p1());
                ex.setCorrectAnswerDisplay(p1s + " " + sc.pUnit());
                ex.setAnswerUnit(sc.pUnit());
            }
            case "V1" -> {
                ex.setStatement(String.format(
                    "Un gas se encontraba a %s %s. Al cambiar la presión a %s %s " +
                    "(temperatura constante), pasa a ocupar %s %s. ¿Cuál era el volumen inicial?",
                    p1s, sc.pUnit(), p2s, sc.pUnit(), v2s, sc.vUnit()));
                ex.setCorrectAnswerValue(sc.v1());
                ex.setCorrectAnswerDisplay(v1s + " " + sc.vUnit());
                ex.setAnswerUnit(sc.vUnit());
            }
        }
        ex.setExplanation(buildBoyleExplanation(sc));
    }

    private String buildBoyleExplanation(BoyleScenario sc) {
        String p1s = fmt(sc.p1()), v1s = fmt(sc.v1()), p2s = fmt(sc.p2()), v2s = fmt(sc.v2());
        String pu = sc.pUnit(), vu = sc.vUnit();
        String header =
            "Ley de Boyle-Mariotte (T = cte.):\n" +
            "\\[P_1 \\cdot V_1 = P_2 \\cdot V_2\\]\n\n";
        return header + switch (sc.unknownVar()) {
            case "P2" -> String.format(
                "Datos: \\(P_1 = %s\\,\\text{%s},\\; V_1 = %s\\,\\text{%s},\\; V_2 = %s\\,\\text{%s}\\)\n\n" +
                "Despejamos \\(P_2\\):\n" +
                "\\[P_2 = \\frac{P_1 \\cdot V_1}{V_2} = " +
                "\\frac{%s\\,\\text{%s} \\times %s\\,\\text{%s}}{%s\\,\\text{%s}} = %s\\,\\text{%s}\\]\n\n" +
                "∴  P₂ = %s %s",
                p1s,pu, v1s,vu, v2s,vu,
                p1s,pu, v1s,vu, v2s,vu, p2s,pu,
                p2s, pu);
            case "V2" -> String.format(
                "Datos: \\(P_1 = %s\\,\\text{%s},\\; V_1 = %s\\,\\text{%s},\\; P_2 = %s\\,\\text{%s}\\)\n\n" +
                "Despejamos \\(V_2\\):\n" +
                "\\[V_2 = \\frac{P_1 \\cdot V_1}{P_2} = " +
                "\\frac{%s\\,\\text{%s} \\times %s\\,\\text{%s}}{%s\\,\\text{%s}} = %s\\,\\text{%s}\\]\n\n" +
                "∴  V₂ = %s %s",
                p1s,pu, v1s,vu, p2s,pu,
                p1s,pu, v1s,vu, p2s,pu, v2s,vu,
                v2s, vu);
            case "P1" -> String.format(
                "Datos: \\(V_1 = %s\\,\\text{%s},\\; P_2 = %s\\,\\text{%s},\\; V_2 = %s\\,\\text{%s}\\)\n\n" +
                "Despejamos \\(P_1\\):\n" +
                "\\[P_1 = \\frac{P_2 \\cdot V_2}{V_1} = " +
                "\\frac{%s\\,\\text{%s} \\times %s\\,\\text{%s}}{%s\\,\\text{%s}} = %s\\,\\text{%s}\\]\n\n" +
                "∴  P₁ = %s %s",
                v1s,vu, p2s,pu, v2s,vu,
                p2s,pu, v2s,vu, v1s,vu, p1s,pu,
                p1s, pu);
            case "V1" -> String.format(
                "Datos: \\(P_1 = %s\\,\\text{%s},\\; P_2 = %s\\,\\text{%s},\\; V_2 = %s\\,\\text{%s}\\)\n\n" +
                "Despejamos \\(V_1\\):\n" +
                "\\[V_1 = \\frac{P_2 \\cdot V_2}{P_1} = " +
                "\\frac{%s\\,\\text{%s} \\times %s\\,\\text{%s}}{%s\\,\\text{%s}} = %s\\,\\text{%s}\\]\n\n" +
                "∴  V₁ = %s %s",
                p1s,pu, p2s,pu, v2s,vu,
                p2s,pu, v2s,vu, p1s,pu, v1s,vu,
                v1s, vu);
            default -> "";
        };
    }

    // ── Charles ──────────────────────────────────────────────────────────────

    private void buildCharles(ThirdEsoMatterExercise ex, CharlesScenario sc) {
        ex.setUnknownVariable(sc.unknownVar());
        double t1K = sc.t1Celsius() + K_OFFSET;
        double t2K = sc.t2Celsius() + K_OFFSET;
        String v1s = fmt(sc.v1()), v2s = fmt(sc.v2()), vu = sc.vUnit();
        String t1Cs = fmtInt(sc.t1Celsius()), t2Cs = fmtInt(sc.t2Celsius());
        String t1Ks = fmt(t1K), t2Ks = fmt(t2K);

        switch (sc.unknownVar()) {
            case "V2" -> {
                ex.setStatement(String.format(
                    "Un gas ocupa %s %s a %s°C. ¿Qué volumen ocupará a %s°C " +
                    "manteniendo la presión constante?",
                    v1s, vu, t1Cs, t2Cs));
                ex.setCorrectAnswerValue(sc.v2());
                ex.setCorrectAnswerDisplay(v2s + " " + vu);
                ex.setAnswerUnit(vu);
            }
            case "T2" -> {
                ex.setStatement(String.format(
                    "Un gas ocupa %s %s a %s°C. ¿A qué temperatura (en K) " +
                    "ocupará %s %s (presión constante)?",
                    v1s, vu, t1Cs, v2s, vu));
                ex.setCorrectAnswerValue(t2K);
                ex.setCorrectAnswerDisplay(t2Ks + " K");
                ex.setAnswerUnit("K");
            }
            case "V1" -> {
                ex.setStatement(String.format(
                    "Un gas estaba a %s°C. Tras cambiar la temperatura a %s°C " +
                    "(presión constante), el gas ocupa %s %s. ¿Cuál era el volumen inicial?",
                    t1Cs, t2Cs, v2s, vu));
                ex.setCorrectAnswerValue(sc.v1());
                ex.setCorrectAnswerDisplay(v1s + " " + vu);
                ex.setAnswerUnit(vu);
            }
            case "T1" -> {
                ex.setStatement(String.format(
                    "Un gas ocupa %s %s a temperatura desconocida. Al cambiar la temperatura a %s°C " +
                    "(presión constante), pasa a ocupar %s %s. ¿Cuál era la temperatura inicial (en K)?",
                    v1s, vu, t2Cs, v2s, vu));
                ex.setCorrectAnswerValue(t1K);
                ex.setCorrectAnswerDisplay(t1Ks + " K");
                ex.setAnswerUnit("K");
            }
        }
        ex.setExplanation(buildCharlesExplanation(sc, t1K, t2K));
    }

    private String buildCharlesExplanation(CharlesScenario sc, double t1K, double t2K) {
        String v1s = fmt(sc.v1()), v2s = fmt(sc.v2()), vu = sc.vUnit();
        String t1Cs = fmtInt(sc.t1Celsius()), t2Cs = fmtInt(sc.t2Celsius());
        String t1Ks = fmt(t1K), t2Ks = fmt(t2K);
        String header =
            "Ley de Charles (P = cte.):\n" +
            "\\[\\frac{V_1}{T_1} = \\frac{V_2}{T_2}\\]\n\n";

        return header + switch (sc.unknownVar()) {
            case "V2" -> String.format(
                "Convertimos las temperaturas a Kelvin (T(K) = T(°C) + 273):\n" +
                "T₁ = %s°C + 273 = %s K\n" +
                "T₂ = %s°C + 273 = %s K\n\n" +
                "Datos: \\(V_1 = %s\\,\\text{%s},\\; T_1 = %s\\,\\text{K},\\; T_2 = %s\\,\\text{K}\\)\n\n" +
                "Despejamos \\(V_2\\):\n" +
                "\\[V_2 = \\frac{V_1 \\cdot T_2}{T_1} = " +
                "\\frac{%s\\,\\text{%s} \\times %s\\,\\text{K}}{%s\\,\\text{K}} = %s\\,\\text{%s}\\]\n\n" +
                "∴  V₂ = %s %s",
                t1Cs, t1Ks, t2Cs, t2Ks,
                v1s,vu, t1Ks, t2Ks,
                v1s,vu, t2Ks, t1Ks, v2s,vu,
                v2s, vu);
            case "T2" -> String.format(
                "Convertimos T₁ a Kelvin:\n" +
                "T₁ = %s°C + 273 = %s K\n\n" +
                "Datos: \\(V_1 = %s\\,\\text{%s},\\; T_1 = %s\\,\\text{K},\\; V_2 = %s\\,\\text{%s}\\)\n\n" +
                "Despejamos \\(T_2\\):\n" +
                "\\[T_2 = \\frac{V_2 \\cdot T_1}{V_1} = " +
                "\\frac{%s\\,\\text{%s} \\times %s\\,\\text{K}}{%s\\,\\text{%s}} = %s\\,\\text{K}\\]\n\n" +
                "∴  T₂ = %s K (%s°C)",
                t1Cs, t1Ks,
                v1s,vu, t1Ks, v2s,vu,
                v2s,vu, t1Ks, v1s,vu, t2Ks,
                t2Ks, t2Cs);
            case "V1" -> String.format(
                "Convertimos las temperaturas a Kelvin:\n" +
                "T₁ = %s°C + 273 = %s K\n" +
                "T₂ = %s°C + 273 = %s K\n\n" +
                "Datos: \\(T_1 = %s\\,\\text{K},\\; T_2 = %s\\,\\text{K},\\; V_2 = %s\\,\\text{%s}\\)\n\n" +
                "Despejamos \\(V_1\\):\n" +
                "\\[V_1 = \\frac{V_2 \\cdot T_1}{T_2} = " +
                "\\frac{%s\\,\\text{%s} \\times %s\\,\\text{K}}{%s\\,\\text{K}} = %s\\,\\text{%s}\\]\n\n" +
                "∴  V₁ = %s %s",
                t1Cs, t1Ks, t2Cs, t2Ks,
                t1Ks, t2Ks, v2s,vu,
                v2s,vu, t1Ks, t2Ks, v1s,vu,
                v1s, vu);
            case "T1" -> String.format(
                "Convertimos T₂ a Kelvin:\n" +
                "T₂ = %s°C + 273 = %s K\n\n" +
                "Datos: \\(V_1 = %s\\,\\text{%s},\\; V_2 = %s\\,\\text{%s},\\; T_2 = %s\\,\\text{K}\\)\n\n" +
                "Despejamos \\(T_1\\):\n" +
                "\\[T_1 = \\frac{V_1 \\cdot T_2}{V_2} = " +
                "\\frac{%s\\,\\text{%s} \\times %s\\,\\text{K}}{%s\\,\\text{%s}} = %s\\,\\text{K}\\]\n\n" +
                "∴  T₁ = %s K (%s°C)",
                t2Cs, t2Ks,
                v1s,vu, v2s,vu, t2Ks,
                v1s,vu, t2Ks, v2s,vu, t1Ks,
                t1Ks, t1Cs);
            default -> "";
        };
    }

    // ── Gay-Lussac ────────────────────────────────────────────────────────────

    private void buildGayLussac(ThirdEsoMatterExercise ex, GayLussacScenario sc) {
        ex.setUnknownVariable(sc.unknownVar());
        double t1K = sc.t1Celsius() + K_OFFSET;
        double t2K = sc.t2Celsius() + K_OFFSET;
        String p1s = fmt(sc.p1()), p2s = fmt(sc.p2()), pu = sc.pUnit();
        String t1Cs = fmtInt(sc.t1Celsius()), t2Cs = fmtInt(sc.t2Celsius());
        String t1Ks = fmt(t1K), t2Ks = fmt(t2K);

        switch (sc.unknownVar()) {
            case "P2" -> {
                ex.setStatement(String.format(
                    "Un gas a volumen constante tiene una presión de %s %s a %s°C. " +
                    "¿Cuál será la presión si la temperatura cambia a %s°C?",
                    p1s, pu, t1Cs, t2Cs));
                ex.setCorrectAnswerValue(sc.p2());
                ex.setCorrectAnswerDisplay(p2s + " " + pu);
                ex.setAnswerUnit(pu);
            }
            case "T2" -> {
                ex.setStatement(String.format(
                    "Un gas a volumen constante tiene una presión de %s %s a %s°C. " +
                    "¿A qué temperatura (en K) ejercerá una presión de %s %s?",
                    p1s, pu, t1Cs, p2s, pu));
                ex.setCorrectAnswerValue(t2K);
                ex.setCorrectAnswerDisplay(t2Ks + " K");
                ex.setAnswerUnit("K");
            }
            case "P1" -> {
                ex.setStatement(String.format(
                    "Un gas a volumen constante ejerce %s %s a %s°C. " +
                    "¿Cuál era su presión cuando la temperatura era %s°C?",
                    p2s, pu, t2Cs, t1Cs));
                ex.setCorrectAnswerValue(sc.p1());
                ex.setCorrectAnswerDisplay(p1s + " " + pu);
                ex.setAnswerUnit(pu);
            }
            case "T1" -> {
                ex.setStatement(String.format(
                    "Un gas a volumen constante ejerce %s %s a temperatura desconocida. " +
                    "Al cambiar la temperatura, su presión pasa a ser %s %s a %s°C. " +
                    "¿Cuál era la temperatura inicial (en K)?",
                    p1s, pu, p2s, pu, t2Cs));
                ex.setCorrectAnswerValue(t1K);
                ex.setCorrectAnswerDisplay(t1Ks + " K");
                ex.setAnswerUnit("K");
            }
        }
        ex.setExplanation(buildGayLussacExplanation(sc, t1K, t2K));
    }

    private String buildGayLussacExplanation(GayLussacScenario sc, double t1K, double t2K) {
        String p1s = fmt(sc.p1()), p2s = fmt(sc.p2()), pu = sc.pUnit();
        String t1Cs = fmtInt(sc.t1Celsius()), t2Cs = fmtInt(sc.t2Celsius());
        String t1Ks = fmt(t1K), t2Ks = fmt(t2K);
        String header =
            "Ley de Gay-Lussac (V = cte.):\n" +
            "\\[\\frac{P_1}{T_1} = \\frac{P_2}{T_2}\\]\n\n";

        return header + switch (sc.unknownVar()) {
            case "P2" -> String.format(
                "Convertimos las temperaturas a Kelvin (T(K) = T(°C) + 273):\n" +
                "T₁ = %s°C + 273 = %s K\n" +
                "T₂ = %s°C + 273 = %s K\n\n" +
                "Datos: \\(P_1 = %s\\,\\text{%s},\\; T_1 = %s\\,\\text{K},\\; T_2 = %s\\,\\text{K}\\)\n\n" +
                "Despejamos \\(P_2\\):\n" +
                "\\[P_2 = \\frac{P_1 \\cdot T_2}{T_1} = " +
                "\\frac{%s\\,\\text{%s} \\times %s\\,\\text{K}}{%s\\,\\text{K}} = %s\\,\\text{%s}\\]\n\n" +
                "∴  P₂ = %s %s",
                t1Cs, t1Ks, t2Cs, t2Ks,
                p1s,pu, t1Ks, t2Ks,
                p1s,pu, t2Ks, t1Ks, p2s,pu,
                p2s, pu);
            case "T2" -> String.format(
                "Convertimos T₁ a Kelvin:\n" +
                "T₁ = %s°C + 273 = %s K\n\n" +
                "Datos: \\(P_1 = %s\\,\\text{%s},\\; T_1 = %s\\,\\text{K},\\; P_2 = %s\\,\\text{%s}\\)\n\n" +
                "Despejamos \\(T_2\\):\n" +
                "\\[T_2 = \\frac{P_2 \\cdot T_1}{P_1} = " +
                "\\frac{%s\\,\\text{%s} \\times %s\\,\\text{K}}{%s\\,\\text{%s}} = %s\\,\\text{K}\\]\n\n" +
                "∴  T₂ = %s K (%s°C)",
                t1Cs, t1Ks,
                p1s,pu, t1Ks, p2s,pu,
                p2s,pu, t1Ks, p1s,pu, t2Ks,
                t2Ks, t2Cs);
            case "P1" -> String.format(
                "Convertimos las temperaturas a Kelvin:\n" +
                "T₁ = %s°C + 273 = %s K\n" +
                "T₂ = %s°C + 273 = %s K\n\n" +
                "Datos: \\(P_2 = %s\\,\\text{%s},\\; T_1 = %s\\,\\text{K},\\; T_2 = %s\\,\\text{K}\\)\n\n" +
                "Despejamos \\(P_1\\):\n" +
                "\\[P_1 = \\frac{P_2 \\cdot T_1}{T_2} = " +
                "\\frac{%s\\,\\text{%s} \\times %s\\,\\text{K}}{%s\\,\\text{K}} = %s\\,\\text{%s}\\]\n\n" +
                "∴  P₁ = %s %s",
                t1Cs, t1Ks, t2Cs, t2Ks,
                p2s,pu, t1Ks, t2Ks,
                p2s,pu, t1Ks, t2Ks, p1s,pu,
                p1s, pu);
            case "T1" -> String.format(
                "Convertimos T₂ a Kelvin:\n" +
                "T₂ = %s°C + 273 = %s K\n\n" +
                "Datos: \\(P_1 = %s\\,\\text{%s},\\; P_2 = %s\\,\\text{%s},\\; T_2 = %s\\,\\text{K}\\)\n\n" +
                "Despejamos \\(T_1\\):\n" +
                "\\[T_1 = \\frac{P_1 \\cdot T_2}{P_2} = " +
                "\\frac{%s\\,\\text{%s} \\times %s\\,\\text{K}}{%s\\,\\text{%s}} = %s\\,\\text{K}\\]\n\n" +
                "∴  T₁ = %s K (%s°C)",
                t2Cs, t2Ks,
                p1s,pu, p2s,pu, t2Ks,
                p1s,pu, t2Ks, p2s,pu, t1Ks,
                t1Ks, t1Cs);
            default -> "";
        };
    }

    // =========================================================================
    // UTILIDADES DE FORMATO NUMÉRICO
    // =========================================================================

    /** Formatea con 2 decimales significativos, coma decimal, sin ceros finales superfluos. */
    private String fmt(double value) {
        return new BigDecimal(Double.toString(value))
            .setScale(2, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
            .replace(".", ",");
    }

    /** Formatea como entero cuando no hay parte decimal; si no, usa fmt(). */
    private String fmtInt(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return fmt(value);
    }
}
