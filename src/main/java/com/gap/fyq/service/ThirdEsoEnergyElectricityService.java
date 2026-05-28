package com.gap.fyq.service;

import com.gap.fyq.model.thirdeso.energyelectricity.ElectricityEnergyType;
import com.gap.fyq.model.thirdeso.energyelectricity.ThirdEsoEnergyElectricityExercise;
import com.gap.fyq.repository.ThirdEsoEnergyElectricityRepository;
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
public class ThirdEsoEnergyElectricityService {

    private final ThirdEsoEnergyElectricityRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "3ESO";
    private static final String BLOCK  = "BL5";

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public ThirdEsoEnergyElectricityExercise generateAndSave() {
        int r = random.nextInt(3);
        ThirdEsoEnergyElectricityExercise ex = switch (r) {
            case 0 -> buildHeatCalculation();
            case 1 -> buildOhmLaw();
            default -> buildElectricCost();
        };
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);
        log.debug("3ESO BL5 generado: tipo={} incógnita={}", ex.getEnergyType(), ex.getUnknownVariable());
        return repository.save(ex);
    }

    public ThirdEsoEnergyElectricityExercise findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ejercicio no encontrado: " + id));
    }

    // =========================================================================
    // CALOR   Q = m · ce · ΔT
    // Incógnitas: Q, m, deltaT
    // Sustancias: agua (ce=4180 J/kg·°C), hierro (ce=450), aluminio (ce=900),
    //             cobre (ce=385), aceite (ce=2000)
    // Masa a veces en gramos (requiere conversión a kg)
    // =========================================================================

    private record HeatScenario(
        String substance,
        double ce,          // J/(kg·°C)
        String ceDisplay,   // para la explicación
        double massKg,
        double deltaT,      // °C  (siempre positivo; contexto define si absorbe o cede)
        boolean massInGrams // si true, el enunciado presenta la masa en gramos
    ) {}

    private static final List<HeatScenario> HEAT_SCENARIOS = List.of(
        // Agua en kg
        new HeatScenario("agua",     4180, "4180", 1.0,  20.0, false),
        new HeatScenario("agua",     4180, "4180", 2.0,  50.0, false),
        new HeatScenario("agua",     4180, "4180", 0.5,  80.0, false),
        new HeatScenario("agua",     4180, "4180", 1.5,  40.0, false),
        new HeatScenario("agua",     4180, "4180", 3.0,  30.0, false),
        // Agua en gramos
        new HeatScenario("agua",     4180, "4180", 200,  50.0, true),   // 200 g → 0,2 kg
        new HeatScenario("agua",     4180, "4180", 500,  40.0, true),   // 500 g → 0,5 kg
        new HeatScenario("agua",     4180, "4180", 100,  80.0, true),   // 100 g → 0,1 kg
        new HeatScenario("agua",     4180, "4180", 250,  20.0, true),   // 250 g → 0,25 kg
        // Hierro
        new HeatScenario("hierro",    450, "450",  2.0,  100.0, false),
        new HeatScenario("hierro",    450, "450",  5.0,   80.0, false),
        new HeatScenario("hierro",    450, "450",  500,   60.0, true),  // 500 g
        // Aluminio
        new HeatScenario("aluminio",  900, "900",  1.0,   50.0, false),
        new HeatScenario("aluminio",  900, "900",  2.0,   30.0, false),
        new HeatScenario("aluminio",  900, "900",  300,  100.0, true),  // 300 g
        // Cobre
        new HeatScenario("cobre",     385, "385",  1.0,   80.0, false),
        new HeatScenario("cobre",     385, "385",  2.0,   60.0, false),
        // Aceite de cocina
        new HeatScenario("aceite",   2000, "2000", 0.5,  100.0, false),
        new HeatScenario("aceite",   2000, "2000", 1.0,   50.0, false)
    );

    private ThirdEsoEnergyElectricityExercise buildHeatCalculation() {
        HeatScenario sc = HEAT_SCENARIOS.get(random.nextInt(HEAT_SCENARIOS.size()));

        double massKg   = sc.massInGrams() ? round2(sc.massKg() / 1000.0) : sc.massKg();
        double Q        = round2(massKg * sc.ce() * sc.deltaT());

        // Incógnita aleatoria (sesgo hacia Q para reforzar la fórmula principal)
        int roll = random.nextInt(5); // 0,1→Q, 2,3→deltaT, 4→m
        String unknown = roll <= 1 ? "Q" : roll <= 3 ? "deltaT" : "m";

        ThirdEsoEnergyElectricityExercise ex = new ThirdEsoEnergyElectricityExercise();
        ex.setEnergyType(ElectricityEnergyType.HEAT_CALCULATION);

        return switch (unknown) {
            case "Q" -> {
                ex.setUnknownVariable("Q");
                ex.setAnswerUnit("J");
                ex.setCorrectAnswerValue(Q);
                ex.setCorrectAnswerDisplay(fmt(Q) + " J");
                ex.setStatement(buildHeatStatement(sc, massKg, Q, "Q"));
                ex.setExplanation(buildHeatExplanation(sc, massKg, Q, "Q"));
                yield ex;
            }
            case "deltaT" -> {
                ex.setUnknownVariable("deltaT");
                ex.setAnswerUnit("°C");
                ex.setCorrectAnswerValue(sc.deltaT());
                ex.setCorrectAnswerDisplay(fmt(sc.deltaT()) + " °C");
                ex.setStatement(buildHeatStatement(sc, massKg, Q, "deltaT"));
                ex.setExplanation(buildHeatExplanation(sc, massKg, Q, "deltaT"));
                yield ex;
            }
            default -> { // m
                ex.setUnknownVariable("m");
                ex.setAnswerUnit("kg");
                ex.setCorrectAnswerValue(massKg);
                ex.setCorrectAnswerDisplay(fmt(massKg) + " kg");
                ex.setStatement(buildHeatStatement(sc, massKg, Q, "m"));
                ex.setExplanation(buildHeatExplanation(sc, massKg, Q, "m"));
                yield ex;
            }
        };
    }

    private String buildHeatStatement(HeatScenario sc, double massKg, double Q, String unknown) {
        String massStr = sc.massInGrams() ? fmtInt(sc.massKg()) + " g" : fmt(massKg) + " kg";
        boolean absorbe = random.nextBoolean();
        String verb = absorbe ? "absorbe" : "cede";

        return switch (unknown) {
            case "Q" -> "Se calienta " + massStr + " de " + sc.substance()
                      + " (ce = " + sc.ceDisplay() + " J/kg·°C)"
                      + " aumentando su temperatura en " + fmtInt(sc.deltaT()) + " °C."
                      + " Calcula el calor absorbido.";
            case "deltaT" -> "Una muestra de " + massStr + " de " + sc.substance()
                           + " (ce = " + sc.ceDisplay() + " J/kg·°C)"
                           + " " + verb + " " + fmt(Q) + " J de calor."
                           + " Calcula la variación de temperatura.";
            default -> "Al calentar cierta masa de " + sc.substance()
                     + " (ce = " + sc.ceDisplay() + " J/kg·°C)"
                     + " " + fmtInt(sc.deltaT()) + " °C se necesitan " + fmt(Q) + " J."
                     + " Calcula la masa de " + sc.substance() + ".";
        };
    }

    private String buildHeatExplanation(HeatScenario sc, double massKg, double Q, String unknown) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p><strong>Fórmula del calor:</strong></p>");
        sb.append("\\[Q = m \\cdot c_e \\cdot \\Delta T\\]");

        if (sc.massInGrams() && !unknown.equals("m")) {
            sb.append(String.format(
                "<p>Conversión de masa: \\(m = %s\\,\\text{g} \\div 1000 = %s\\,\\text{kg}\\)</p>",
                fmtInt(sc.massKg()), fmt(massKg)));
        }

        switch (unknown) {
            case "Q" -> sb.append(String.format(
                "<p>Aplicamos la fórmula directamente:</p>" +
                "\\[Q = %s\\,\\text{kg} \\times %s\\,\\frac{\\text{J}}{\\text{kg}\\cdot°\\text{C}}" +
                " \\times %s\\,°\\text{C} = \\boxed{%s\\,\\text{J}}\\]",
                fmt(massKg), sc.ceDisplay(), fmtInt(sc.deltaT()), fmt(Q)));
            case "deltaT" -> {
                sb.append("<p>Despejamos \\(\\Delta T\\):</p>");
                sb.append("\\[\\Delta T = \\frac{Q}{m \\cdot c_e}\\]");
                sb.append(String.format(
                    "\\[\\Delta T = \\frac{%s\\,\\text{J}}{%s\\,\\text{kg} \\times %s\\,\\frac{\\text{J}}{\\text{kg}\\cdot°\\text{C}}}" +
                    " = \\boxed{%s\\,°\\text{C}}\\]",
                    fmt(Q), fmt(massKg), sc.ceDisplay(), fmt(sc.deltaT())));
            }
            default -> { // m
                sb.append("<p>Despejamos \\(m\\):</p>");
                sb.append("\\[m = \\frac{Q}{c_e \\cdot \\Delta T}\\]");
                sb.append(String.format(
                    "\\[m = \\frac{%s\\,\\text{J}}{%s\\,\\frac{\\text{J}}{\\text{kg}\\cdot°\\text{C}} \\times %s\\,°\\text{C}}" +
                    " = \\boxed{%s\\,\\text{kg}}\\]",
                    fmt(Q), sc.ceDisplay(), fmtInt(sc.deltaT()), fmt(massKg)));
            }
        }
        return sb.toString();
    }

    // =========================================================================
    // LEY DE OHM   V = I · R
    // Incógnitas: V, I, R
    // Intensidades a veces en mA (requieren conversión a A)
    // =========================================================================

    private record OhmScenario(
        String context,
        double voltios,
        double amperios,    // siempre en A para el cálculo
        double ohmios,
        boolean iInMilliA   // si true, el enunciado da la intensidad en mA
    ) {}

    private static final List<OhmScenario> OHM_SCENARIOS = List.of(
        // Circuitos domésticos / escolares (corrientes en A)
        new OhmScenario("Una bombilla de un circuito escolar",   12.0,  0.5,   24.0, false),
        new OhmScenario("Un motor de juguete",                    6.0,  0.3,   20.0, false),
        new OhmScenario("Una resistencia de laboratorio",         9.0,  0.6,   15.0, false),
        new OhmScenario("Un zumbador electrónico",                5.0,  0.25,  20.0, false),
        new OhmScenario("Una lámpara de linterna",                4.5,  0.15,  30.0, false),
        new OhmScenario("Un LED de un circuito",                  3.0,  0.02, 150.0, false),
        new OhmScenario("Un calentador eléctrico pequeño",      230.0,  2.0,  115.0, false),
        new OhmScenario("Una resistencia calefactora",          220.0,  4.0,   55.0, false),
        // Circuitos con intensidad en mA
        new OhmScenario("Un sensor electrónico",                  5.0, 0.020,  250.0, true), // 20 mA
        new OhmScenario("Un micrófono de condensador",            9.0, 0.003, 3000.0, true), // 3 mA
        new OhmScenario("Un receptor de radio portátil",          6.0, 0.050,  120.0, true), // 50 mA
        new OhmScenario("Un módulo Bluetooth",                    3.3, 0.080,   41.25, true) // 80 mA
    );

    private ThirdEsoEnergyElectricityExercise buildOhmLaw() {
        OhmScenario sc = OHM_SCENARIOS.get(random.nextInt(OHM_SCENARIOS.size()));

        double V = sc.voltios();
        double I = sc.amperios();
        double R = sc.ohmios();

        String[] unknowns = {"V", "I", "R"};
        String unknown = unknowns[random.nextInt(3)];

        ThirdEsoEnergyElectricityExercise ex = new ThirdEsoEnergyElectricityExercise();
        ex.setEnergyType(ElectricityEnergyType.OHM_LAW);

        return switch (unknown) {
            case "V" -> {
                ex.setUnknownVariable("V");
                ex.setAnswerUnit("V");
                ex.setCorrectAnswerValue(V);
                ex.setCorrectAnswerDisplay(fmt(V) + " V");
                ex.setStatement(buildOhmStatement(sc, "V"));
                ex.setExplanation(buildOhmExplanation(sc, "V"));
                yield ex;
            }
            case "I" -> {
                double answerI = sc.iInMilliA() ? round2(I * 1000) : I;
                String unitI   = sc.iInMilliA() ? "mA" : "A";
                ex.setUnknownVariable("I");
                ex.setAnswerUnit(unitI);
                ex.setCorrectAnswerValue(answerI);
                ex.setCorrectAnswerDisplay(fmt(answerI) + " " + unitI);
                ex.setStatement(buildOhmStatement(sc, "I"));
                ex.setExplanation(buildOhmExplanation(sc, "I"));
                yield ex;
            }
            default -> { // R
                ex.setUnknownVariable("R");
                ex.setAnswerUnit("Ω");
                ex.setCorrectAnswerValue(R);
                ex.setCorrectAnswerDisplay(fmt(R) + " Ω");
                ex.setStatement(buildOhmStatement(sc, "R"));
                ex.setExplanation(buildOhmExplanation(sc, "R"));
                yield ex;
            }
        };
    }

    private String buildOhmStatement(OhmScenario sc, String unknown) {
        String iStr = sc.iInMilliA()
            ? fmt(round2(sc.amperios() * 1000)) + " mA"
            : fmt(sc.amperios()) + " A";

        return switch (unknown) {
            case "V" -> sc.context() + " tiene una resistencia de " + fmt(sc.ohmios())
                      + " Ω y circula por él una corriente de " + iStr
                      + ". Calcula la tensión aplicada.";
            case "I" -> sc.context() + " está conectado a una tensión de " + fmt(sc.voltios())
                      + " V y tiene una resistencia de " + fmt(sc.ohmios())
                      + " Ω. Calcula la intensidad de corriente.";
            default  -> sc.context() + " está conectado a " + fmt(sc.voltios())
                      + " V y circula por él una corriente de " + iStr
                      + ". Calcula su resistencia eléctrica.";
        };
    }

    private String buildOhmExplanation(OhmScenario sc, String unknown) {
        double V = sc.voltios();
        double I = sc.amperios();
        double R = sc.ohmios();

        StringBuilder sb = new StringBuilder();
        sb.append("<p><strong>Ley de Ohm:</strong></p>");
        sb.append("\\[V = I \\cdot R\\]");

        if (sc.iInMilliA() && !unknown.equals("I")) {
            double iMa = round2(I * 1000);
            sb.append(String.format(
                "<p>Conversión de intensidad: " +
                "\\(I = %s\\,\\text{mA} \\times \\frac{1\\,\\text{A}}{1000\\,\\text{mA}} = %s\\,\\text{A}\\)</p>",
                fmt(iMa), fmt(I)));
        }

        switch (unknown) {
            case "V" -> sb.append(String.format(
                "<p>Aplicamos la fórmula directamente:</p>" +
                "\\[V = %s\\,\\text{A} \\times %s\\,\\Omega = \\boxed{%s\\,\\text{V}}\\]",
                fmt(I), fmt(R), fmt(V)));
            case "I" -> {
                sb.append("<p>Despejamos \\(I\\):</p>");
                sb.append("\\[I = \\frac{V}{R}\\]");
                if (sc.iInMilliA()) {
                    double iMa = round2(I * 1000);
                    sb.append(String.format(
                        "\\[I = \\frac{%s\\,\\text{V}}{%s\\,\\Omega} = %s\\,\\text{A}" +
                        " = \\boxed{%s\\,\\text{mA}}\\]",
                        fmt(V), fmt(R), fmt(I), fmt(iMa)));
                } else {
                    sb.append(String.format(
                        "\\[I = \\frac{%s\\,\\text{V}}{%s\\,\\Omega} = \\boxed{%s\\,\\text{A}}\\]",
                        fmt(V), fmt(R), fmt(I)));
                }
            }
            default -> { // R
                sb.append("<p>Despejamos \\(R\\):</p>");
                sb.append("\\[R = \\frac{V}{I}\\]");
                sb.append(String.format(
                    "\\[R = \\frac{%s\\,\\text{V}}{%s\\,\\text{A}} = \\boxed{%s\\,\\Omega}\\]",
                    fmt(V), fmt(I), fmt(R)));
            }
        }
        return sb.toString();
    }

    // =========================================================================
    // CONSUMO ELÉCTRICO   E (kWh) = P (kW) · t (h)   coste = E · tarifa
    // Incógnitas: E_kWh (energía), coste, t (tiempo), P (potencia)
    // Tiempos a veces en minutos (requieren conversión a horas)
    // =========================================================================

    private record CostScenario(
        String appliance,   // nombre del electrodoméstico
        double powerW,      // potencia en W
        double timeH,       // tiempo en horas para el cálculo
        boolean timeInMin,  // si true, el enunciado da el tiempo en minutos
        double tariff       // €/kWh
    ) {}

    private static final List<CostScenario> COST_SCENARIOS = List.of(
        new CostScenario("un hervidor eléctrico de 2000 W",    2000,  0.05, true,  0.18),  // 3 min
        new CostScenario("una lavadora de 1500 W",             1500,  1.5,  false, 0.20),
        new CostScenario("un horno eléctrico de 2000 W",       2000,  1.0,  false, 0.18),
        new CostScenario("un televisor de 150 W",               150,  4.0,  false, 0.18),
        new CostScenario("un frigorífico de 200 W",             200, 24.0,  false, 0.18),
        new CostScenario("un ordenador portátil de 60 W",        60,  8.0,  false, 0.20),
        new CostScenario("una plancha de 1800 W",              1800,  0.5,  false, 0.18),
        new CostScenario("un microondas de 900 W",              900, 10.0/60, true, 0.18),  // 10 min
        new CostScenario("un secador de pelo de 1200 W",       1200, 15.0/60, true, 0.20), // 15 min
        new CostScenario("una bombilla LED de 10 W",             10,  8.0,  false, 0.18),
        new CostScenario("un aire acondicionado de 1500 W",    1500,  6.0,  false, 0.22),
        new CostScenario("un lavavajillas de 1800 W",          1800,  2.0,  false, 0.20)
    );

    private ThirdEsoEnergyElectricityExercise buildElectricCost() {
        CostScenario sc = COST_SCENARIOS.get(random.nextInt(COST_SCENARIOS.size()));

        double powerKw  = sc.powerW() / 1000.0;
        double timeH    = sc.timeH();
        double energyKwh = round2(powerKw * timeH);
        double coste    = round4(energyKwh * sc.tariff());

        // Incógnitas posibles: E_kWh, coste (sesgo 40%/40%/10%/10% hacia las más pedagógicas)
        int roll = random.nextInt(5);
        String unknown = roll <= 1 ? "E_kWh" : roll <= 3 ? "coste" : "t";

        ThirdEsoEnergyElectricityExercise ex = new ThirdEsoEnergyElectricityExercise();
        ex.setEnergyType(ElectricityEnergyType.ELECTRIC_COST);
        ex.setTolerancePercent(3.0); // algo más tolerante por los redondeos de € y kWh

        return switch (unknown) {
            case "E_kWh" -> {
                ex.setUnknownVariable("E_kWh");
                ex.setAnswerUnit("kWh");
                ex.setCorrectAnswerValue(energyKwh);
                ex.setCorrectAnswerDisplay(fmt(energyKwh) + " kWh");
                ex.setStatement(buildCostStatement(sc, powerKw, timeH, energyKwh, coste, "E_kWh"));
                ex.setExplanation(buildCostExplanation(sc, powerKw, timeH, energyKwh, coste, "E_kWh"));
                yield ex;
            }
            case "coste" -> {
                ex.setUnknownVariable("coste");
                ex.setAnswerUnit("€");
                ex.setCorrectAnswerValue(coste);
                ex.setCorrectAnswerDisplay(fmt(coste) + " €");
                ex.setStatement(buildCostStatement(sc, powerKw, timeH, energyKwh, coste, "coste"));
                ex.setExplanation(buildCostExplanation(sc, powerKw, timeH, energyKwh, coste, "coste"));
                yield ex;
            }
            default -> { // t — ¿cuántas horas se necesitan para consumir cierta energía?
                ex.setUnknownVariable("t");
                ex.setAnswerUnit("h");
                ex.setCorrectAnswerValue(timeH);
                ex.setCorrectAnswerDisplay(fmt(timeH) + " h");
                ex.setStatement(buildCostStatement(sc, powerKw, timeH, energyKwh, coste, "t"));
                ex.setExplanation(buildCostExplanation(sc, powerKw, timeH, energyKwh, coste, "t"));
                yield ex;
            }
        };
    }

    private String timeStr(CostScenario sc) {
        if (sc.timeInMin()) {
            long minutos = Math.round(sc.timeH() * 60);
            return minutos + " minutos";
        }
        return fmt(sc.timeH()) + " h";
    }

    private String buildCostStatement(CostScenario sc,
                                       double powerKw, double timeH,
                                       double energyKwh, double coste,
                                       String unknown) {
        String tariffStr = sc.tariff() == Math.floor(sc.tariff() * 100) / 100
            ? fmt(sc.tariff())
            : fmt(sc.tariff());

        return switch (unknown) {
            case "E_kWh" -> "Calcula la energía consumida (en kWh) por " + sc.appliance()
                          + " que funciona durante " + timeStr(sc) + ".";
            case "coste" -> "Calcula el coste económico de usar " + sc.appliance()
                          + " durante " + timeStr(sc)
                          + ", sabiendo que la tarifa eléctrica es de " + tariffStr + " €/kWh.";
            default      -> sc.appliance().substring(0, 1).toUpperCase() + sc.appliance().substring(1)
                          + " consume " + fmt(energyKwh) + " kWh en total."
                          + " ¿Cuántas horas ha estado en funcionamiento?";
        };
    }

    private String buildCostExplanation(CostScenario sc,
                                         double powerKw, double timeH,
                                         double energyKwh, double coste,
                                         String unknown) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p><strong>Energía consumida:</strong></p>");
        sb.append("\\[E\\,(\\text{kWh}) = P\\,(\\text{kW}) \\times t\\,(\\text{h})\\]");

        // Conversión de W a kW
        sb.append(String.format(
            "<p>Conversión de potencia: " +
            "\\(P = %s\\,\\text{W} \\div 1000 = %s\\,\\text{kW}\\)</p>",
            fmtInt(sc.powerW()), fmt(powerKw)));

        // Conversión de minutos a horas si procede
        if (sc.timeInMin()) {
            long min = Math.round(sc.timeH() * 60);
            sb.append(String.format(
                "<p>Conversión de tiempo: " +
                "\\(t = %d\\,\\text{min} \\div 60 = %s\\,\\text{h}\\)</p>",
                min, fmt(timeH)));
        }

        switch (unknown) {
            case "E_kWh" -> sb.append(String.format(
                "<p>Aplicamos la fórmula:</p>" +
                "\\[E = %s\\,\\text{kW} \\times %s\\,\\text{h} = \\boxed{%s\\,\\text{kWh}}\\]",
                fmt(powerKw), fmt(timeH), fmt(energyKwh)));
            case "coste" -> {
                sb.append(String.format(
                    "<p><strong>Paso 1</strong> — Energía consumida:</p>" +
                    "\\[E = %s\\,\\text{kW} \\times %s\\,\\text{h} = %s\\,\\text{kWh}\\]",
                    fmt(powerKw), fmt(timeH), fmt(energyKwh)));
                sb.append("<p><strong>Paso 2</strong> — Coste económico:</p>");
                sb.append("\\[\\text{Coste} = E \\times \\text{tarifa}\\]");
                sb.append(String.format(
                    "\\[\\text{Coste} = %s\\,\\text{kWh} \\times %s\\,\\frac{€}{\\text{kWh}}" +
                    " = \\boxed{%s\\,€}\\]",
                    fmt(energyKwh), fmt(sc.tariff()), fmt(coste)));
            }
            default -> { // t
                sb.append("<p>Despejamos \\(t\\):</p>");
                sb.append("\\[t = \\frac{E}{P}\\]");
                sb.append(String.format(
                    "\\[t = \\frac{%s\\,\\text{kWh}}{%s\\,\\text{kW}} = \\boxed{%s\\,\\text{h}}\\]",
                    fmt(energyKwh), fmt(powerKw), fmt(timeH)));
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

    private static double round4(double v) {
        return new BigDecimal(v).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }

    private static String fmt(double v) {
        BigDecimal bd = new BigDecimal(v).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        return bd.toPlainString().replace(".", ",");
    }

    private static String fmtInt(double v) {
        return String.valueOf(Math.round(v));
    }
}
