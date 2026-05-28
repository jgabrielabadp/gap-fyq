package com.gap.fyq.service;

import com.gap.fyq.model.thirdeso.forcesmotion.DynamicsType;
import com.gap.fyq.model.thirdeso.forcesmotion.ThirdEsoForcesMotionExercise;
import com.gap.fyq.repository.ThirdEsoForcesMotionRepository;
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
public class ThirdEsoForcesMotionService {

    private final ThirdEsoForcesMotionRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "3ESO";
    private static final String BLOCK  = "BL4";

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public ThirdEsoForcesMotionExercise generateAndSave() {
        int r = random.nextInt(3);
        ThirdEsoForcesMotionExercise ex = switch (r) {
            case 0 -> buildAccelerationMruv();
            case 1 -> buildNewtonSecondLaw();
            default -> buildHydraulicPress();
        };
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);
        log.debug("3ESO BL4 generado: tipo={} incógnita={}", ex.getDynamicsType(), ex.getUnknownVariable());
        return repository.save(ex);
    }

    public ThirdEsoForcesMotionExercise findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ejercicio no encontrado: " + id));
    }

    // =========================================================================
    // ACELERACIÓN / MRUV   a = (vf - vi) / t
    // Incógnitas posibles: a, vf, vi, t
    // =========================================================================

    private record MruvScenario(
        String context,     // descripción del objeto en movimiento
        double vi,          // velocidad inicial (m/s)
        double vf,          // velocidad final (m/s)
        double t,           // tiempo (s)
        boolean viInKmh,    // si true, el enunciado da vi en km/h
        boolean vfInKmh     // si true, el enunciado da vf en km/h
    ) {}

    private static final List<MruvScenario> MRUV_SCENARIOS = List.of(
        // vi=0 (arranques desde reposo)
        new MruvScenario("Un coche arranca desde el reposo",          0,  20,  10, false, false),
        new MruvScenario("Un tren parte de la estación",              0,  30,  15, false, false),
        new MruvScenario("Una moto arranca desde el semáforo",        0,  15,   5, false, false),
        new MruvScenario("Un avión inicia el despegue",               0,  60,  12, false, false),
        // vi y vf en m/s
        new MruvScenario("Un ciclista acelera en llano",              5,  15,   4, false, false),
        new MruvScenario("Un corredor aumenta su velocidad",          2,   8,   3, false, false),
        new MruvScenario("Un patinador gana velocidad",               1,   7,   3, false, false),
        new MruvScenario("Un barco aumenta su velocidad",             3,  13,   5, false, false),
        // vi o vf en km/h (requieren conversión)
        new MruvScenario("Un automóvil acelera en autopista",        72, 108,  10,  true,  true),
        new MruvScenario("Un ciclista pasa de 18 a 36 km/h",        18,  36,   5,  true,  true),
        new MruvScenario("Un tren pasa de 90 a 144 km/h",           90, 144,  15,  true,  true),
        new MruvScenario("Un coche frena de 90 km/h hasta detenerse", 90,  0,  25,  true, false)
    );

    private ThirdEsoForcesMotionExercise buildAccelerationMruv() {
        MruvScenario sc = MRUV_SCENARIOS.get(random.nextInt(MRUV_SCENARIOS.size()));

        // Convertir a m/s para cálculo
        double viMs = sc.viInKmh() ? round2(sc.vi() / 3.6) : sc.vi();
        double vfMs = sc.vfInKmh() ? round2(sc.vf() / 3.6) : sc.vf();
        double t    = sc.t();
        double a    = round2((vfMs - viMs) / t);

        // Elegir incógnita (sesgo hacia 'a' para reforzar el concepto principal)
        int roll = random.nextInt(5); // 0-1→a, 2→vf, 3→vi, 4→t
        String unknown = roll <= 1 ? "a" : roll == 2 ? "vf" : roll == 3 ? "vi" : "t";

        ThirdEsoForcesMotionExercise ex = new ThirdEsoForcesMotionExercise();
        ex.setDynamicsType(DynamicsType.ACCELERATION_MRUV);

        return switch (unknown) {
            case "a" -> {
                ex.setUnknownVariable("a");
                ex.setAnswerUnit("m/s²");
                ex.setCorrectAnswerValue(a);
                ex.setCorrectAnswerDisplay(fmt(a) + " m/s²");
                ex.setStatement(buildMruvStatement(sc, "a"));
                ex.setExplanation(buildMruvExplanation(sc, viMs, vfMs, t, a, "a"));
                yield ex;
            }
            case "vf" -> {
                ex.setUnknownVariable("vf");
                ex.setAnswerUnit("m/s");
                ex.setCorrectAnswerValue(vfMs);
                ex.setCorrectAnswerDisplay(fmt(vfMs) + " m/s");
                ex.setStatement(buildMruvStatement(sc, "vf"));
                ex.setExplanation(buildMruvExplanation(sc, viMs, vfMs, t, a, "vf"));
                yield ex;
            }
            case "vi" -> {
                ex.setUnknownVariable("vi");
                ex.setAnswerUnit("m/s");
                ex.setCorrectAnswerValue(viMs);
                ex.setCorrectAnswerDisplay(fmt(viMs) + " m/s");
                ex.setStatement(buildMruvStatement(sc, "vi"));
                ex.setExplanation(buildMruvExplanation(sc, viMs, vfMs, t, a, "vi"));
                yield ex;
            }
            default -> { // t
                ex.setUnknownVariable("t");
                ex.setAnswerUnit("s");
                ex.setCorrectAnswerValue(t);
                ex.setCorrectAnswerDisplay(fmt(t) + " s");
                ex.setStatement(buildMruvStatement(sc, "t"));
                ex.setExplanation(buildMruvExplanation(sc, viMs, vfMs, t, a, "t"));
                yield ex;
            }
        };
    }

    private String buildMruvStatement(MruvScenario sc, String unknown) {
        String viStr = sc.viInKmh()  ? fmtInt(sc.vi())  + " km/h" : fmtInt(sc.vi())  + " m/s";
        String vfStr = sc.vfInKmh()  ? fmtInt(sc.vf())  + " km/h" : fmtInt(sc.vf())  + " m/s";
        double viMs  = sc.viInKmh()  ? round2(sc.vi() / 3.6) : sc.vi();
        double vfMs  = sc.vfInKmh()  ? round2(sc.vf() / 3.6) : sc.vf();
        double a     = round2((vfMs - viMs) / sc.t());

        return switch (unknown) {
            case "a"  -> sc.context() + " y en " + fmtInt(sc.t()) + " s pasa de "
                       + viStr + " a " + vfStr + ". Calcula la aceleración media.";
            case "vf" -> sc.context() + " con velocidad inicial " + viStr
                       + " y aceleración " + fmt(a) + " m/s². ¿Cuál es su velocidad final tras "
                       + fmtInt(sc.t()) + " s?";
            case "vi" -> sc.context() + " alcanza " + vfStr + " tras " + fmtInt(sc.t())
                       + " s con una aceleración de " + fmt(a) + " m/s². ¿Cuál era su velocidad inicial?";
            default   -> sc.context() + " acelera de " + viStr + " a " + vfStr
                       + " con aceleración " + fmt(a) + " m/s². ¿Cuánto tiempo tarda?";
        };
    }

    private String buildMruvExplanation(MruvScenario sc,
                                         double viMs, double vfMs, double t, double a,
                                         String unknown) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p><strong>Fórmula de la aceleración media:</strong></p>");
        sb.append("\\[a = \\frac{v_f - v_i}{t}\\]");

        // Conversión de unidades si procede
        if (sc.viInKmh() && !unknown.equals("vi")) {
            sb.append(String.format(
                "<p>Conversión de velocidad inicial: " +
                "\\(v_i = %s\\,\\text{km/h} \\times \\frac{1\\,\\text{m/s}}{3{,}6\\,\\text{km/h}} = %s\\,\\text{m/s}\\)</p>",
                fmtInt(sc.vi()), fmt(viMs)));
        }
        if (sc.vfInKmh() && !unknown.equals("vf")) {
            sb.append(String.format(
                "<p>Conversión de velocidad final: " +
                "\\(v_f = %s\\,\\text{km/h} \\times \\frac{1\\,\\text{m/s}}{3{,}6\\,\\text{km/h}} = %s\\,\\text{m/s}\\)</p>",
                fmtInt(sc.vf()), fmt(vfMs)));
        }

        switch (unknown) {
            case "a" -> {
                sb.append("<p>Sustituyendo los datos:</p>");
                sb.append(String.format(
                    "\\[a = \\frac{%s - %s}{%s} = \\frac{%s}{%s} = \\boxed{%s\\,\\text{m/s}^2}\\]",
                    fmt(vfMs), fmt(viMs), fmt(t), fmt(round2(vfMs - viMs)), fmt(t), fmt(a)));
            }
            case "vf" -> {
                sb.append("<p>Despejamos \\(v_f\\):</p>");
                sb.append("\\[v_f = v_i + a \\cdot t\\]");
                sb.append(String.format(
                    "\\[v_f = %s + %s \\times %s = \\boxed{%s\\,\\text{m/s}}\\]",
                    fmt(viMs), fmt(a), fmt(t), fmt(vfMs)));
            }
            case "vi" -> {
                sb.append("<p>Despejamos \\(v_i\\):</p>");
                sb.append("\\[v_i = v_f - a \\cdot t\\]");
                sb.append(String.format(
                    "\\[v_i = %s - %s \\times %s = \\boxed{%s\\,\\text{m/s}}\\]",
                    fmt(vfMs), fmt(a), fmt(t), fmt(viMs)));
            }
            default -> { // t
                sb.append("<p>Despejamos \\(t\\):</p>");
                sb.append("\\[t = \\frac{v_f - v_i}{a}\\]");
                sb.append(String.format(
                    "\\[t = \\frac{%s - %s}{%s} = \\boxed{%s\\,\\text{s}}\\]",
                    fmt(vfMs), fmt(viMs), fmt(a), fmt(t)));
            }
        }
        return sb.toString();
    }

    // =========================================================================
    // SEGUNDA LEY DE NEWTON   F = m · a
    // Incógnitas posibles: F, m, a
    // Masas en kg (a veces el dato se da en g)
    // =========================================================================

    private record NewtonScenario(
        String context,
        double massKg,
        double accelMs2,
        boolean massInGrams  // si true, el enunciado presenta la masa en gramos
    ) {}

    private static final List<NewtonScenario> NEWTON_SCENARIOS = List.of(
        new NewtonScenario("Un bloque de madera",         2.0,   3.0, false),
        new NewtonScenario("Una caja de cartón",          5.0,   2.0, false),
        new NewtonScenario("Un carrito de laboratorio",   0.5,   4.0, false),
        new NewtonScenario("Un bloque metálico",         10.0,   1.5, false),
        new NewtonScenario("Una pelota de fútbol",        0.45,  8.0, false),
        new NewtonScenario("Un trineo sobre hielo",      20.0,   0.5, false),
        new NewtonScenario("Una mochila sobre ruedas",    3.0,   2.5, false),
        new NewtonScenario("Un objeto de laboratorio",  200.0,   5.0, true),   // 200 g → 0.2 kg
        new NewtonScenario("Una pelota de tenis",        60.0,  10.0, true),   // 60 g  → 0.06 kg
        new NewtonScenario("Una piedra pequeña",        500.0,   2.0, true),   // 500 g → 0.5 kg
        new NewtonScenario("Un vehículo eléctrico",    1200.0,   2.0, false),
        new NewtonScenario("Una bicicleta con ciclista", 80.0,   1.5, false)
    );

    private ThirdEsoForcesMotionExercise buildNewtonSecondLaw() {
        NewtonScenario sc = NEWTON_SCENARIOS.get(random.nextInt(NEWTON_SCENARIOS.size()));

        double massKg = sc.massInGrams() ? round2(sc.massKg() / 1000.0) : sc.massKg();
        double a      = sc.accelMs2();
        double F      = round2(massKg * a);

        // Incógnita aleatoria
        String[] unknowns = {"F", "m", "a"};
        String unknown = unknowns[random.nextInt(3)];

        ThirdEsoForcesMotionExercise ex = new ThirdEsoForcesMotionExercise();
        ex.setDynamicsType(DynamicsType.NEWTON_SECOND_LAW);

        return switch (unknown) {
            case "F" -> {
                ex.setUnknownVariable("F");
                ex.setAnswerUnit("N");
                ex.setCorrectAnswerValue(F);
                ex.setCorrectAnswerDisplay(fmt(F) + " N");
                ex.setStatement(buildNewtonStatement(sc, massKg, a, F, "F"));
                ex.setExplanation(buildNewtonExplanation(sc, massKg, a, F, "F"));
                yield ex;
            }
            case "m" -> {
                ex.setUnknownVariable("m");
                ex.setAnswerUnit("kg");
                ex.setCorrectAnswerValue(massKg);
                ex.setCorrectAnswerDisplay(fmt(massKg) + " kg");
                ex.setStatement(buildNewtonStatement(sc, massKg, a, F, "m"));
                ex.setExplanation(buildNewtonExplanation(sc, massKg, a, F, "m"));
                yield ex;
            }
            default -> { // a
                ex.setUnknownVariable("a");
                ex.setAnswerUnit("m/s²");
                ex.setCorrectAnswerValue(a);
                ex.setCorrectAnswerDisplay(fmt(a) + " m/s²");
                ex.setStatement(buildNewtonStatement(sc, massKg, a, F, "a"));
                ex.setExplanation(buildNewtonExplanation(sc, massKg, a, F, "a"));
                yield ex;
            }
        };
    }

    private String buildNewtonStatement(NewtonScenario sc, double massKg, double a, double F,
                                         String unknown) {
        String massStr = sc.massInGrams()
            ? fmtInt(sc.massKg()) + " g"
            : fmt(massKg) + " kg";

        return switch (unknown) {
            case "F" -> sc.context() + " de masa " + massStr
                      + " experimenta una aceleración de " + fmt(a)
                      + " m/s². Calcula la fuerza neta aplicada.";
            case "m" -> sc.context() + " sobre el que actúa una fuerza neta de " + fmt(F)
                      + " N adquiere una aceleración de " + fmt(a)
                      + " m/s². Calcula su masa.";
            default  -> sc.context() + " de masa " + massStr
                      + " sobre el que actúa una fuerza neta de " + fmt(F)
                      + " N. Calcula la aceleración que experimenta.";
        };
    }

    private String buildNewtonExplanation(NewtonScenario sc, double massKg, double a, double F,
                                           String unknown) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p><strong>Segunda Ley de Newton:</strong></p>");
        sb.append("\\[F = m \\cdot a\\]");

        if (sc.massInGrams() && !unknown.equals("m")) {
            sb.append(String.format(
                "<p>Conversión de masa: \\(m = %s\\,\\text{g} \\div 1000 = %s\\,\\text{kg}\\)</p>",
                fmtInt(sc.massKg()), fmt(massKg)));
        }

        switch (unknown) {
            case "F" -> sb.append(String.format(
                "<p>Aplicamos directamente la fórmula:</p>" +
                "\\[F = %s\\,\\text{kg} \\times %s\\,\\text{m/s}^2 = \\boxed{%s\\,\\text{N}}\\]",
                fmt(massKg), fmt(a), fmt(F)));
            case "m" -> {
                sb.append("<p>Despejamos \\(m\\):</p>");
                sb.append("\\[m = \\frac{F}{a}\\]");
                sb.append(String.format(
                    "\\[m = \\frac{%s\\,\\text{N}}{%s\\,\\text{m/s}^2} = \\boxed{%s\\,\\text{kg}}\\]",
                    fmt(F), fmt(a), fmt(massKg)));
            }
            default -> { // a
                sb.append("<p>Despejamos \\(a\\):</p>");
                sb.append("\\[a = \\frac{F}{m}\\]");
                sb.append(String.format(
                    "\\[a = \\frac{%s\\,\\text{N}}{%s\\,\\text{kg}} = \\boxed{%s\\,\\text{m/s}^2}\\]",
                    fmt(F), fmt(massKg), fmt(a)));
            }
        }
        return sb.toString();
    }

    // =========================================================================
    // PRENSA HIDRÁULICA / PRINCIPIO DE PASCAL   F1/S1 = F2/S2
    // Incógnitas posibles: F1, F2, S1, S2
    // Superficies en cm² (a veces en m²)
    // =========================================================================

    private record HydraulicScenario(
        String context,
        double F1,      // N
        double S1cm2,   // cm²
        double S2cm2,   // cm²
        boolean s1InM2, // si true, enunciado da S1 en m²
        boolean s2InM2  // si true, enunciado da S2 en m²
    ) {}

    // F2 = F1 * S2 / S1  (se calcula en el servicio)
    private static final List<HydraulicScenario> HYDRAULIC_SCENARIOS = List.of(
        new HydraulicScenario("Un gato hidráulico de taller",   200,    10,  200, false, false),
        new HydraulicScenario("Una prensa industrial",          500,    20,  500, false, false),
        new HydraulicScenario("Un elevador de coches",          300,    15,  600, false, false),
        new HydraulicScenario("Un freno hidráulico de bicicleta", 50,    5,   50, false, false),
        new HydraulicScenario("Una prensa de laboratorio",      100,     8,  200, false, false),
        new HydraulicScenario("Un sistema hidráulico industrial", 400,  0.002, 0.04, true, true),
        new HydraulicScenario("Una prensa de imprenta",         250,    25,  500, false, false),
        new HydraulicScenario("Un cilindro hidráulico",         600,    30,  900, false, false)
    );

    private ThirdEsoForcesMotionExercise buildHydraulicPress() {
        HydraulicScenario sc = HYDRAULIC_SCENARIOS.get(random.nextInt(HYDRAULIC_SCENARIOS.size()));

        // Pasar a unidades SI para cálculo
        double S1 = sc.s1InM2() ? sc.S1cm2() : sc.S1cm2() / 10000.0; // m²
        double S2 = sc.s2InM2() ? sc.S2cm2() : sc.S2cm2() / 10000.0; // m²
        double F1 = sc.F1();
        double F2 = round2(F1 * S2 / S1);

        String[] unknowns = {"F1", "F2", "S1", "S2"};
        String unknown = unknowns[random.nextInt(4)];

        ThirdEsoForcesMotionExercise ex = new ThirdEsoForcesMotionExercise();
        ex.setDynamicsType(DynamicsType.HYDRAULIC_PRESS);

        return switch (unknown) {
            case "F1" -> {
                ex.setUnknownVariable("F1");
                ex.setAnswerUnit("N");
                ex.setCorrectAnswerValue(F1);
                ex.setCorrectAnswerDisplay(fmt(F1) + " N");
                ex.setStatement(buildHydraulicStatement(sc, F1, F2, S1, S2, "F1"));
                ex.setExplanation(buildHydraulicExplanation(sc, F1, F2, S1, S2, "F1"));
                yield ex;
            }
            case "F2" -> {
                ex.setUnknownVariable("F2");
                ex.setAnswerUnit("N");
                ex.setCorrectAnswerValue(F2);
                ex.setCorrectAnswerDisplay(fmt(F2) + " N");
                ex.setStatement(buildHydraulicStatement(sc, F1, F2, S1, S2, "F2"));
                ex.setExplanation(buildHydraulicExplanation(sc, F1, F2, S1, S2, "F2"));
                yield ex;
            }
            case "S1" -> {
                ex.setUnknownVariable("S1");
                String unit = sc.s1InM2() ? "m²" : "cm²";
                double answer = sc.s1InM2() ? S1 : sc.S1cm2();
                ex.setAnswerUnit(unit);
                ex.setCorrectAnswerValue(answer);
                ex.setCorrectAnswerDisplay(fmt(answer) + " " + unit);
                ex.setStatement(buildHydraulicStatement(sc, F1, F2, S1, S2, "S1"));
                ex.setExplanation(buildHydraulicExplanation(sc, F1, F2, S1, S2, "S1"));
                yield ex;
            }
            default -> { // S2
                ex.setUnknownVariable("S2");
                String unit = sc.s2InM2() ? "m²" : "cm²";
                double answer = sc.s2InM2() ? S2 : sc.S2cm2();
                ex.setAnswerUnit(unit);
                ex.setCorrectAnswerValue(answer);
                ex.setCorrectAnswerDisplay(fmt(answer) + " " + unit);
                ex.setStatement(buildHydraulicStatement(sc, F1, F2, S1, S2, "S2"));
                ex.setExplanation(buildHydraulicExplanation(sc, F1, F2, S1, S2, "S2"));
                yield ex;
            }
        };
    }

    private String surfaceStr(HydraulicScenario sc, boolean isS1) {
        if (isS1) {
            return sc.s1InM2()
                ? fmt(sc.S1cm2()) + " m²"
                : fmtInt(sc.S1cm2()) + " cm²";
        } else {
            return sc.s2InM2()
                ? fmt(sc.S2cm2()) + " m²"
                : fmtInt(sc.S2cm2()) + " cm²";
        }
    }

    private String buildHydraulicStatement(HydraulicScenario sc,
                                            double F1, double F2, double S1, double S2,
                                            String unknown) {
        String s1Str = surfaceStr(sc, true);
        String s2Str = surfaceStr(sc, false);

        return switch (unknown) {
            case "F2" -> sc.context() + ". El émbolo pequeño tiene una superficie de "
                       + s1Str + " y se le aplica una fuerza de " + fmt(F1)
                       + " N. El émbolo grande tiene una superficie de " + s2Str
                       + ". Calcula la fuerza que ejerce el émbolo grande.";
            case "F1" -> sc.context() + ". El émbolo grande tiene superficie " + s2Str
                       + " y ejerce una fuerza de " + fmt(F2)
                       + " N. El émbolo pequeño tiene superficie " + s1Str
                       + ". Calcula la fuerza que hay que aplicar al émbolo pequeño.";
            case "S1" -> sc.context() + ". Se aplica una fuerza de " + fmt(F1)
                       + " N al émbolo pequeño y se obtiene una fuerza de " + fmt(F2)
                       + " N en el émbolo grande (superficie " + s2Str
                       + "). Calcula la superficie del émbolo pequeño.";
            default   -> sc.context() + ". Se aplica " + fmt(F1) + " N al émbolo pequeño"
                       + " (superficie " + s1Str + ") y se necesita obtener " + fmt(F2)
                       + " N. Calcula la superficie del émbolo grande.";
        };
    }

    private String buildHydraulicExplanation(HydraulicScenario sc,
                                              double F1, double F2, double S1, double S2,
                                              String unknown) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p><strong>Principio de Pascal (prensa hidráulica):</strong></p>");
        sb.append("\\[\\frac{F_1}{S_1} = \\frac{F_2}{S_2}\\]");

        // Conversión a unidades coherentes si las superficies están en cm²
        if (!sc.s1InM2() || !sc.s2InM2()) {
            sb.append("<p>Convertimos las superficies a m² para trabajar en el SI:</p>");
            if (!sc.s1InM2()) {
                sb.append(String.format(
                    "\\[S_1 = %s\\,\\text{cm}^2 \\times 10^{-4} = %s\\,\\text{m}^2\\]",
                    fmtInt(sc.S1cm2()), fmtSci(S1)));
            }
            if (!sc.s2InM2()) {
                sb.append(String.format(
                    "\\[S_2 = %s\\,\\text{cm}^2 \\times 10^{-4} = %s\\,\\text{m}^2\\]",
                    fmtInt(sc.S2cm2()), fmtSci(S2)));
            }
        }

        switch (unknown) {
            case "F2" -> {
                sb.append("<p>Despejamos \\(F_2\\):</p>");
                sb.append("\\[F_2 = \\frac{F_1 \\cdot S_2}{S_1}\\]");
                sb.append(String.format(
                    "\\[F_2 = \\frac{%s\\,\\text{N} \\times %s\\,\\text{m}^2}{%s\\,\\text{m}^2} = \\boxed{%s\\,\\text{N}}\\]",
                    fmt(F1), fmtSci(S2), fmtSci(S1), fmt(F2)));
            }
            case "F1" -> {
                sb.append("<p>Despejamos \\(F_1\\):</p>");
                sb.append("\\[F_1 = \\frac{F_2 \\cdot S_1}{S_2}\\]");
                sb.append(String.format(
                    "\\[F_1 = \\frac{%s\\,\\text{N} \\times %s\\,\\text{m}^2}{%s\\,\\text{m}^2} = \\boxed{%s\\,\\text{N}}\\]",
                    fmt(F2), fmtSci(S1), fmtSci(S2), fmt(F1)));
            }
            case "S1" -> {
                sb.append("<p>Despejamos \\(S_1\\):</p>");
                sb.append("\\[S_1 = \\frac{F_1 \\cdot S_2}{F_2}\\]");
                double s1Answer = sc.s1InM2() ? S1 : sc.S1cm2();
                String s1Unit   = sc.s1InM2() ? "\\,\\text{m}^2" : "\\,\\text{cm}^2";
                double s1Raw    = sc.s1InM2() ? S1 : sc.S1cm2();
                sb.append(String.format(
                    "\\[S_1 = \\frac{%s\\,\\text{N} \\times %s\\,\\text{m}^2}{%s\\,\\text{N}} = %s\\,\\text{m}^2%s\\]",
                    fmt(F1), fmtSci(S2), fmt(F2), fmtSci(S1),
                    sc.s1InM2() ? " = \\boxed{" + fmt(s1Answer) + "\\,\\text{m}^2}" :
                                  " = \\boxed{" + fmt(s1Raw) + "\\,\\text{cm}^2}"));
            }
            default -> { // S2
                sb.append("<p>Despejamos \\(S_2\\):</p>");
                sb.append("\\[S_2 = \\frac{F_2 \\cdot S_1}{F_1}\\]");
                double s2Answer = sc.s2InM2() ? S2 : sc.S2cm2();
                sb.append(String.format(
                    "\\[S_2 = \\frac{%s\\,\\text{N} \\times %s\\,\\text{m}^2}{%s\\,\\text{N}} = %s\\,\\text{m}^2%s\\]",
                    fmt(F2), fmtSci(S1), fmt(F1), fmtSci(S2),
                    sc.s2InM2() ? " = \\boxed{" + fmt(s2Answer) + "\\,\\text{m}^2}" :
                                  " = \\boxed{" + fmt(s2Answer) + "\\,\\text{cm}^2}"));
            }
        }
        return sb.toString();
    }

    // =========================================================================
    // UTILIDADES
    // =========================================================================

    private static double round2(double v) {
        return new BigDecimal(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static String fmt(double v) {
        BigDecimal bd = new BigDecimal(v).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        return bd.toPlainString().replace(".", ",");
    }

    private static String fmtInt(double v) {
        long rounded = Math.round(v);
        return String.valueOf(rounded);
    }

    /** Formatea superficies pequeñas en notación con potencias de 10 para KaTeX. */
    private static String fmtSci(double v) {
        if (v == 0) return "0";
        // Si es >= 0.01 m² (100 cm²), mostramos decimal normal
        if (v >= 0.01) return fmt(v);
        // Si es < 0.01 m², expresamos como X × 10^-4
        double cm2 = v * 10000.0;
        return fmt(round2(cm2)) + " \\times 10^{-4}";
    }
}
