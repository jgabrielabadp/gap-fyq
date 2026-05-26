package com.gap.fyq.service;

import com.gap.fyq.model.energy.EnergyExercise;
import com.gap.fyq.model.energy.EnergyType;
import com.gap.fyq.repository.EnergyExerciseRepository;
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
public class EnergyExerciseService {

    private final EnergyExerciseRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "2ESO";
    private static final String BLOCK  = "BL5";
    private static final double G      = 9.8;      // N/kg
    private static final double CAL_TO_J = 4.18;   // 1 cal = 4,18 J
    private static final double KWH_TO_KJ = 3600.0; // 1 kWh = 3600 kJ

    // =========================================================================
    // Escenarios de ENERGY_UNITS
    // =========================================================================

    private record CalJoulePair(double cal, double joules) {}
    private record KwhKjPair(double kwh, double kj) {}

    // Pares exactos: cal × 4,18 = J (resultado entero)
    private static final List<CalJoulePair> CAL_J_PAIRS = List.of(
        new CalJoulePair(50,   209),
        new CalJoulePair(100,  418),
        new CalJoulePair(200,  836),
        new CalJoulePair(250,  1045),
        new CalJoulePair(500,  2090),
        new CalJoulePair(1000, 4180),
        new CalJoulePair(2000, 8360)
    );

    // Pares exactos: kWh × 3600 = kJ
    private static final List<KwhKjPair> KWH_KJ_PAIRS = List.of(
        new KwhKjPair(0.1,  360),
        new KwhKjPair(0.5,  1800),
        new KwhKjPair(1.0,  3600),
        new KwhKjPair(2.0,  7200),
        new KwhKjPair(5.0,  18000),
        new KwhKjPair(10.0, 36000)
    );

    // =========================================================================
    // Escenarios de WORK_AND_POWER
    // =========================================================================

    private record WorkScenario(
        String agent,    // "Una persona"
        String action,   // "empuja un carro"
        double force,    // N
        double distance, // m
        double work      // J  = force × distance
    ) {}

    private record PowerScenario(
        String device,   // "un motor eléctrico"
        double work,     // J
        double time,     // s
        double power     // W  = work / time
    ) {}

    private static final List<WorkScenario> WORK_SCENARIOS = List.of(
        new WorkScenario("Una persona",    "empuja un carro de la compra",         100,  5,  500),
        new WorkScenario("Una grúa",       "iza una viga metálica",                500,  3,  1500),
        new WorkScenario("Un operario",    "desplaza un armario",                  200,  8,  1600),
        new WorkScenario("Un atleta",      "lanza una jabalina",                   150,  4,  600),
        new WorkScenario("Un tractor",     "ara un campo",                        1000, 10,  10000),
        new WorkScenario("Un deportista",  "realiza una sentadilla con barra",     800,  2,  1600),
        new WorkScenario("Un niño",        "arrastra un trineo en la nieve",        50, 20,  1000),
        new WorkScenario("Una locomotora", "mueve un vagón de mercancías",        5000,  3,  15000)
    );

    private static final List<PowerScenario> POWER_SCENARIOS = List.of(
        new PowerScenario("un motor eléctrico",     1000,  10,  100),
        new PowerScenario("una bomba de agua",      5000,  50,  100),
        new PowerScenario("un calentador de agua",  3600,  60,  60),
        new PowerScenario("un ascensor",           12000,  60,  200),
        new PowerScenario("un corredor de maratón", 2400,  60,  40),
        new PowerScenario("una grúa de obra",      30000,  60,  500),
        new PowerScenario("un ventilador",           720,  60,  12),
        new PowerScenario("una bomba de calor",    18000,  60,  300)
    );

    private static final List<String> WORK_UNKNOWNS  = List.of("trabajo", "fuerza", "distancia");
    private static final List<String> POWER_UNKNOWNS = List.of("potencia", "trabajo", "tiempo");

    // =========================================================================
    // Escenarios de KINETIC_POTENTIAL
    // =========================================================================

    private record KineticScenario(
        String subject,   // "Un automóvil"
        double mass,      // kg
        double speed,     // m/s
        double energy     // J  = 0,5 × mass × speed²
    ) {}

    private record PotentialScenario(
        String subject,   // "Un libro de texto"
        double mass,      // kg
        double height,    // m
        double energy     // J  = mass × 9,8 × height
    ) {}

    private static final List<KineticScenario> KINETIC_SCENARIOS = List.of(
        new KineticScenario("Un automóvil",               1000,  10, 50000),
        new KineticScenario("Un ciclista (con bicicleta)",   80,   5,  1000),
        new KineticScenario("Una pelota de fútbol",         0.5,   4,     4),
        new KineticScenario("Un patinador",                  60,   2,   120),
        new KineticScenario("Una roca",                       5,   4,    40),
        new KineticScenario("Un bloque de madera",            2,   4,    16),
        new KineticScenario("Una flecha de arco",          0.05,  40,    40),
        new KineticScenario("Un balón de baloncesto",       0.6,  10,    30),
        new KineticScenario("Un corredor",                   70,   4,   560),
        new KineticScenario("Una pelota de tenis",          0.06, 50,    90)
    );

    // Todos los valores de Ep son exactos con g = 9,8
    private static final List<PotentialScenario> POTENTIAL_SCENARIOS = List.of(
        new PotentialScenario("un libro de texto",      1,  10,   98),
        new PotentialScenario("una maceta",             2,   5,   98),
        new PotentialScenario("una caja de herramientas", 5, 10,  490),
        new PotentialScenario("un frigorífico",        50,   1,  490),
        new PotentialScenario("un escalador",          80,  25, 19600),
        new PotentialScenario("una pelota de tenis",  0.06, 10,  5.88),
        new PotentialScenario("un bidón de agua",      20,   2,  392),
        new PotentialScenario("un ladrillo",            3,   5,  147)
    );

    private static final List<String> POTENTIAL_UNKNOWNS =
        List.of("energia_potencial", "masa", "altura");

    // =========================================================================
    // Escenarios de SUSTAINABILITY_TEST
    // =========================================================================

    private record McQuestion(
        String statement,
        String opt0, String opt1, String opt2, String opt3,
        int correct,
        String explanation
    ) {}

    private static final List<McQuestion> SUSTAINABILITY_MC = List.of(

        new McQuestion(
            "¿Cuál de las siguientes fuentes de energía es renovable?",
            "Carbón", "Petróleo", "Energía solar", "Gas natural",
            2,
            "Las <strong>fuentes renovables</strong> se regeneran naturalmente y no se agotan " +
            "en escalas de tiempo humanas. La energía solar, eólica, hidráulica, geotérmica " +
            "y la biomasa son renovables. El carbón, el petróleo y el gas natural son " +
            "<strong>combustibles fósiles</strong> (no renovables): se formaron en millones de años " +
            "y se consumen mucho más rápido de lo que se generan."
        ),

        new McQuestion(
            "¿Cuál es el principal gas de efecto invernadero producido por la quema de combustibles fósiles?",
            "Oxígeno (O₂)", "Nitrógeno (N₂)", "Dióxido de carbono (CO₂)", "Hidrógeno (H₂)",
            2,
            "La <strong>quema de combustibles fósiles</strong> (carbón, petróleo, gas natural) libera " +
            "grandes cantidades de <strong>CO₂</strong>. Este gas atrapa el calor en la atmósfera " +
            "intensificando el efecto invernadero natural, lo que provoca el calentamiento global " +
            "y el cambio climático."
        ),

        new McQuestion(
            "La lluvia ácida está causada principalmente por...",
            "El exceso de oxígeno en la atmósfera",
            "Los óxidos de azufre (SO₂) y nitrógeno (NOₓ) emitidos al quemar combustibles fósiles",
            "La radiación ultravioleta del Sol",
            "El vapor de agua evaporado de los océanos",
            1,
            "Cuando se queman combustibles fósiles se emiten <strong>óxidos de azufre (SO₂) " +
            "y de nitrógeno (NOₓ)</strong>. Al reaccionar con el vapor de agua atmosférico " +
            "forman ácido sulfúrico (H₂SO₄) y ácido nítrico (HNO₃), que precipitan como " +
            "<strong>lluvia ácida</strong>, dañando ecosistemas, bosques y edificios."
        ),

        new McQuestion(
            "¿Cuál de las siguientes acciones supone un MAYOR desperdicio de energía en el hogar?",
            "Usar bombillas LED en lugar de incandescentes",
            "Dejar los electrodomésticos en modo «standby» cuando no se usan",
            "Aprovechar la luz solar durante el día",
            "Aislar térmicamente puertas y ventanas",
            1,
            "Dejar aparatos en modo <strong>standby</strong> consume energía eléctrica " +
            "de forma continua aunque no se estén usando. En un hogar medio, el consumo " +
            "en standby puede representar entre el 5 % y el 10 % de la factura eléctrica. " +
            "Las demás opciones (LED, luz natural, aislamiento) sí son medidas de ahorro."
        ),

        new McQuestion(
            "La energía eólica se obtiene aprovechando...",
            "El calor del interior de la Tierra",
            "El movimiento del agua de los ríos",
            "El movimiento del viento",
            "La radiación solar directa",
            2,
            "Los <strong>aerogeneradores</strong> transforman la energía cinética del " +
            "<strong>viento</strong> en energía eléctrica mediante un generador. " +
            "Es una fuente renovable y no emite CO₂ durante su funcionamiento."
        ),

        new McQuestion(
            "¿Cuál de los siguientes es un combustible fósil?",
            "Hidrógeno verde", "Biomasa", "Gas natural", "Energía mareomotriz",
            2,
            "El <strong>gas natural</strong> es un combustible fósil (principalmente metano, CH₄) " +
            "formado durante millones de años a partir de materia orgánica. " +
            "El hidrógeno verde se obtiene por electrólisis del agua con energías renovables; " +
            "la biomasa es materia orgánica renovable; y la energía mareomotriz aprovecha las mareas."
        ),

        new McQuestion(
            "El principio de conservación de la energía establece que...",
            "La energía se puede crear aplicando una fuerza suficientemente grande",
            "La energía se puede destruir en ciertos procesos químicos exotérmicos",
            "La energía ni se crea ni se destruye: solo se transforma de unas formas en otras",
            "La energía siempre aumenta en cualquier proceso físico",
            2,
            "El <strong>principio de conservación de la energía</strong> es uno de los " +
            "principios fundamentales de la física: la energía total de un sistema aislado " +
            "permanece constante. Puede transformarse (p.ej. química → térmica → mecánica), " +
            "pero nunca crearse ni destruirse."
        ),

        new McQuestion(
            "La energía hidráulica se obtiene aprovechando...",
            "El calor solar acumulado en los embalses",
            "La energía química del agua",
            "La energía cinética del viento sobre la superficie del agua",
            "El movimiento del agua almacenada en embalses y presas",
            3,
            "En las <strong>centrales hidroeléctricas</strong>, el agua almacenada en un " +
            "embalse (energía potencial gravitatoria) cae por tuberías y mueve turbinas " +
            "conectadas a generadores eléctricos. Es una fuente renovable y gestionable " +
            "(se puede regular el caudal según la demanda)."
        )
    );

    // =========================================================================
    // API pública
    // =========================================================================

    public EnergyExercise generateAndSave() {
        EnergyExercise ex = new EnergyExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);

        // Distribución: 20 % ENERGY_UNITS, 30 % WORK_AND_POWER,
        //               30 % KINETIC_POTENTIAL, 20 % SUSTAINABILITY_TEST
        int roll = random.nextInt(10);
        if (roll < 2) {
            buildEnergyUnits(ex);
        } else if (roll < 5) {
            buildWorkAndPower(ex);
        } else if (roll < 8) {
            buildKineticPotential(ex);
        } else {
            buildSustainabilityTest(ex);
        }

        log.debug("BL5 generado: energyType={} modo={}", ex.getEnergyType(), ex.getExerciseMode());
        return repository.save(ex);
    }

    public EnergyExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ejercicio BL5 no encontrado: " + id));
    }

    // =========================================================================
    // Constructores internos — ENERGY_UNITS
    // =========================================================================

    private void buildEnergyUnits(EnergyExercise ex) {
        ex.setEnergyType(EnergyType.ENERGY_UNITS);
        ex.setExerciseMode("NUMERICAL");
        ex.setUnknownVariable("energia");

        // 50 % cal ↔ J,  50 % kWh ↔ kJ
        if (random.nextBoolean()) {
            buildCalJoule(ex);
        } else {
            buildKwhKj(ex);
        }
    }

    private void buildCalJoule(EnergyExercise ex) {
        CalJoulePair pair = CAL_J_PAIRS.get(random.nextInt(CAL_J_PAIRS.size()));
        boolean calToJ = random.nextBoolean();

        if (calToJ) {
            ex.setStatement(String.format(
                "Convierte %s cal a Julios. (Factor: 1 cal = 4,18 J)",
                fmtNum(pair.cal())));
            ex.setCorrectAnswerValue(pair.joules());
            ex.setCorrectAnswerDisplay(fmt(pair.joules()) + " J");
            ex.setAnswerUnit("J");
            ex.setExplanation(String.format(
                "Factor de conversión: 1 cal = 4,18 J\n\n" +
                "\\[E(\\text{J}) = E(\\text{cal}) \\times 4{,}18\\]\n\n" +
                "Aplicando:\n" +
                "\\[E = %s\\,\\text{cal} \\times 4{,}18\\,\\dfrac{\\text{J}}{\\text{cal}} = %s\\,\\text{J}\\]\n\n" +
                "∴  %s cal = %s J",
                fmtNum(pair.cal()), fmt(pair.joules()), fmtNum(pair.cal()), fmt(pair.joules())));
        } else {
            ex.setStatement(String.format(
                "Convierte %s J a calorías. (Factor: 1 cal = 4,18 J)",
                fmt(pair.joules())));
            ex.setCorrectAnswerValue(pair.cal());
            ex.setCorrectAnswerDisplay(fmt(pair.cal()) + " cal");
            ex.setAnswerUnit("cal");
            ex.setExplanation(String.format(
                "Factor de conversión: 1 cal = 4,18 J → 1 J = 1/4,18 cal\n\n" +
                "\\[E(\\text{cal}) = \\dfrac{E(\\text{J})}{4{,}18}\\]\n\n" +
                "Aplicando:\n" +
                "\\[E = \\dfrac{%s\\,\\text{J}}{4{,}18\\,\\frac{\\text{J}}{\\text{cal}}} = %s\\,\\text{cal}\\]\n\n" +
                "∴  %s J = %s cal",
                fmt(pair.joules()), fmt(pair.cal()), fmt(pair.joules()), fmt(pair.cal())));
        }
    }

    private void buildKwhKj(EnergyExercise ex) {
        KwhKjPair pair = KWH_KJ_PAIRS.get(random.nextInt(KWH_KJ_PAIRS.size()));
        boolean kwhToKj = random.nextBoolean();

        if (kwhToKj) {
            ex.setStatement(String.format(
                "Convierte %s kWh a kilojulios (kJ). (Factor: 1 kWh = 3600 kJ)",
                fmtNum(pair.kwh())));
            ex.setCorrectAnswerValue(pair.kj());
            ex.setCorrectAnswerDisplay(fmt(pair.kj()) + " kJ");
            ex.setAnswerUnit("kJ");
            ex.setExplanation(String.format(
                "Factor de conversión: 1 kWh = 3600 kJ\n\n" +
                "\\[E(\\text{kJ}) = E(\\text{kWh}) \\times 3600\\]\n\n" +
                "Aplicando:\n" +
                "\\[E = %s\\,\\text{kWh} \\times 3600\\,\\dfrac{\\text{kJ}}{\\text{kWh}} = %s\\,\\text{kJ}\\]\n\n" +
                "∴  %s kWh = %s kJ",
                fmtNum(pair.kwh()), fmt(pair.kj()), fmtNum(pair.kwh()), fmt(pair.kj())));
        } else {
            ex.setStatement(String.format(
                "Convierte %s kJ a kilovatios-hora (kWh). (Factor: 1 kWh = 3600 kJ)",
                fmt(pair.kj())));
            ex.setCorrectAnswerValue(pair.kwh());
            ex.setCorrectAnswerDisplay(fmt(pair.kwh()) + " kWh");
            ex.setAnswerUnit("kWh");
            ex.setExplanation(String.format(
                "Factor de conversión: 1 kWh = 3600 kJ → 1 kJ = 1/3600 kWh\n\n" +
                "\\[E(\\text{kWh}) = \\dfrac{E(\\text{kJ})}{3600}\\]\n\n" +
                "Aplicando:\n" +
                "\\[E = \\dfrac{%s\\,\\text{kJ}}{3600\\,\\frac{\\text{kJ}}{\\text{kWh}}} = %s\\,\\text{kWh}\\]\n\n" +
                "∴  %s kJ = %s kWh",
                fmt(pair.kj()), fmt(pair.kwh()), fmt(pair.kj()), fmt(pair.kwh())));
        }
    }

    // =========================================================================
    // Constructores internos — WORK_AND_POWER
    // =========================================================================

    private void buildWorkAndPower(EnergyExercise ex) {
        ex.setEnergyType(EnergyType.WORK_AND_POWER);
        ex.setExerciseMode("NUMERICAL");
        if (random.nextBoolean()) {
            buildWork(ex);
        } else {
            buildPower(ex);
        }
    }

    private void buildWork(EnergyExercise ex) {
        WorkScenario sc = WORK_SCENARIOS.get(random.nextInt(WORK_SCENARIOS.size()));
        String unknown = WORK_UNKNOWNS.get(random.nextInt(WORK_UNKNOWNS.size()));
        ex.setUnknownVariable(unknown);

        switch (unknown) {
            case "trabajo" -> {
                ex.setStatement(String.format(
                    "%s %s aplicando una fuerza de %s N a lo largo de %s m. " +
                    "¿Qué trabajo mecánico realiza?",
                    sc.agent(), sc.action(), fmtNum(sc.force()), fmtNum(sc.distance())));
                ex.setCorrectAnswerValue(sc.work());
                ex.setCorrectAnswerDisplay(fmt(sc.work()) + " J");
                ex.setAnswerUnit("J");
                ex.setExplanation(String.format(
                    "Fórmula del trabajo mecánico: \\(W = F \\cdot d\\)\n\n" +
                    "Datos: \\(F = %s\\,\\text{N},\\; d = %s\\,\\text{m}\\)\n\n" +
                    "\\[W = F \\cdot d = %s\\,\\text{N} \\times %s\\,\\text{m} = %s\\,\\text{J}\\]\n\n" +
                    "∴  W = %s J",
                    fmtNum(sc.force()), fmtNum(sc.distance()),
                    fmtNum(sc.force()), fmtNum(sc.distance()), fmt(sc.work()), fmt(sc.work())));
            }
            case "fuerza" -> {
                ex.setStatement(String.format(
                    "%s %s realizando un trabajo de %s J en una distancia de %s m. " +
                    "¿Qué fuerza se aplicó?",
                    sc.agent(), sc.action(), fmt(sc.work()), fmtNum(sc.distance())));
                ex.setCorrectAnswerValue(sc.force());
                ex.setCorrectAnswerDisplay(fmt(sc.force()) + " N");
                ex.setAnswerUnit("N");
                ex.setExplanation(String.format(
                    "Fórmula del trabajo: \\(W = F \\cdot d\\)\n\n" +
                    "Despejamos la fuerza:\n" +
                    "\\[F = \\dfrac{W}{d} = \\dfrac{%s\\,\\text{J}}{%s\\,\\text{m}} = %s\\,\\text{N}\\]\n\n" +
                    "∴  F = %s N",
                    fmt(sc.work()), fmtNum(sc.distance()), fmt(sc.force()), fmt(sc.force())));
            }
            default -> {  // distancia
                ex.setStatement(String.format(
                    "%s %s aplicando una fuerza de %s N y realizando un trabajo de %s J. " +
                    "¿Qué distancia recorre?",
                    sc.agent(), sc.action(), fmtNum(sc.force()), fmt(sc.work())));
                ex.setCorrectAnswerValue(sc.distance());
                ex.setCorrectAnswerDisplay(fmt(sc.distance()) + " m");
                ex.setAnswerUnit("m");
                ex.setExplanation(String.format(
                    "Fórmula del trabajo: \\(W = F \\cdot d\\)\n\n" +
                    "Despejamos la distancia:\n" +
                    "\\[d = \\dfrac{W}{F} = \\dfrac{%s\\,\\text{J}}{%s\\,\\text{N}} = %s\\,\\text{m}\\]\n\n" +
                    "∴  d = %s m",
                    fmt(sc.work()), fmtNum(sc.force()), fmt(sc.distance()), fmt(sc.distance())));
            }
        }
    }

    private void buildPower(EnergyExercise ex) {
        PowerScenario sc = POWER_SCENARIOS.get(random.nextInt(POWER_SCENARIOS.size()));
        String unknown = POWER_UNKNOWNS.get(random.nextInt(POWER_UNKNOWNS.size()));
        ex.setUnknownVariable(unknown);

        switch (unknown) {
            case "potencia" -> {
                ex.setStatement(String.format(
                    "La potencia de %s que realiza %s J de trabajo en %s s. " +
                    "¿Cuál es su potencia?",
                    sc.device(), fmt(sc.work()), fmtNum(sc.time())));
                ex.setCorrectAnswerValue(sc.power());
                ex.setCorrectAnswerDisplay(fmt(sc.power()) + " W");
                ex.setAnswerUnit("W");
                ex.setExplanation(String.format(
                    "Fórmula de la potencia: \\(P = \\dfrac{W}{t}\\)\n\n" +
                    "Datos: \\(W = %s\\,\\text{J},\\; t = %s\\,\\text{s}\\)\n\n" +
                    "\\[P = \\dfrac{W}{t} = \\dfrac{%s\\,\\text{J}}{%s\\,\\text{s}} = %s\\,\\text{W}\\]\n\n" +
                    "∴  P = %s W",
                    fmt(sc.work()), fmtNum(sc.time()),
                    fmt(sc.work()), fmtNum(sc.time()), fmt(sc.power()), fmt(sc.power())));
            }
            case "trabajo" -> {
                ex.setStatement(String.format(
                    "¿Qué trabajo realiza %s de %s W de potencia que funciona durante %s s?",
                    sc.device(), fmt(sc.power()), fmtNum(sc.time())));
                ex.setCorrectAnswerValue(sc.work());
                ex.setCorrectAnswerDisplay(fmt(sc.work()) + " J");
                ex.setAnswerUnit("J");
                ex.setExplanation(String.format(
                    "Fórmula de la potencia: \\(P = \\dfrac{W}{t}\\)\n\n" +
                    "Despejamos el trabajo:\n" +
                    "\\[W = P \\cdot t = %s\\,\\text{W} \\times %s\\,\\text{s} = %s\\,\\text{J}\\]\n\n" +
                    "∴  W = %s J",
                    fmt(sc.power()), fmtNum(sc.time()), fmt(sc.work()), fmt(sc.work())));
            }
            default -> {  // tiempo
                ex.setStatement(String.format(
                    "¿Cuánto tiempo necesita %s de %s W para realizar %s J de trabajo?",
                    sc.device(), fmt(sc.power()), fmt(sc.work())));
                ex.setCorrectAnswerValue(sc.time());
                ex.setCorrectAnswerDisplay(fmt(sc.time()) + " s");
                ex.setAnswerUnit("s");
                ex.setExplanation(String.format(
                    "Fórmula de la potencia: \\(P = \\dfrac{W}{t}\\)\n\n" +
                    "Despejamos el tiempo:\n" +
                    "\\[t = \\dfrac{W}{P} = \\dfrac{%s\\,\\text{J}}{%s\\,\\text{W}} = %s\\,\\text{s}\\]\n\n" +
                    "∴  t = %s s",
                    fmt(sc.work()), fmt(sc.power()), fmt(sc.time()), fmt(sc.time())));
            }
        }
    }

    // =========================================================================
    // Constructores internos — KINETIC_POTENTIAL
    // =========================================================================

    private void buildKineticPotential(EnergyExercise ex) {
        ex.setEnergyType(EnergyType.KINETIC_POTENTIAL);
        ex.setExerciseMode("NUMERICAL");
        if (random.nextBoolean()) {
            buildKinetic(ex);
        } else {
            buildPotential(ex);
        }
    }

    private void buildKinetic(EnergyExercise ex) {
        KineticScenario sc = KINETIC_SCENARIOS.get(random.nextInt(KINETIC_SCENARIOS.size()));
        ex.setUnknownVariable("energia_cinetica");
        double v2 = round2(sc.speed() * sc.speed());

        ex.setStatement(String.format(
            "%s de masa %s kg se desplaza a %s m/s. Calcula su energía cinética.",
            sc.subject(), fmtNum(sc.mass()), fmtNum(sc.speed())));
        ex.setCorrectAnswerValue(sc.energy());
        ex.setCorrectAnswerDisplay(fmt(sc.energy()) + " J");
        ex.setAnswerUnit("J");
        ex.setExplanation(String.format(
            "Fórmula de la energía cinética:\n" +
            "\\[E_c = \\dfrac{1}{2} \\cdot m \\cdot v^2\\]\n\n" +
            "Datos: \\(m = %s\\,\\text{kg},\\; v = %s\\,\\text{m/s}\\)\n\n" +
            "\\[E_c = \\dfrac{1}{2} \\times %s\\,\\text{kg} \\times (%s\\,\\text{m/s})^2 " +
            "= \\dfrac{1}{2} \\times %s\\,\\text{kg} \\times %s\\,\\text{m}^2\\text{/s}^2 " +
            "= %s\\,\\text{J}\\]\n\n" +
            "∴  Ec = %s J",
            fmtNum(sc.mass()), fmtNum(sc.speed()),
            fmtNum(sc.mass()), fmtNum(sc.speed()),
            fmtNum(sc.mass()), fmt(v2),
            fmt(sc.energy()), fmt(sc.energy())));
    }

    private void buildPotential(EnergyExercise ex) {
        PotentialScenario sc = POTENTIAL_SCENARIOS.get(random.nextInt(POTENTIAL_SCENARIOS.size()));
        String unknown = POTENTIAL_UNKNOWNS.get(random.nextInt(POTENTIAL_UNKNOWNS.size()));
        ex.setUnknownVariable(unknown);

        switch (unknown) {
            case "energia_potencial" -> {
                ex.setStatement(String.format(
                    "¿Cuál es la energía potencial gravitatoria de %s de masa %s kg " +
                    "situada a %s m de altura? (g = 9,8 N/kg)",
                    sc.subject(), fmtNum(sc.mass()), fmtNum(sc.height())));
                ex.setCorrectAnswerValue(sc.energy());
                ex.setCorrectAnswerDisplay(fmt(sc.energy()) + " J");
                ex.setAnswerUnit("J");
                ex.setExplanation(String.format(
                    "Fórmula de la energía potencial gravitatoria:\n" +
                    "\\[E_p = m \\cdot g \\cdot h\\]\n\n" +
                    "Datos: \\(m = %s\\,\\text{kg},\\; g = 9{,}8\\,\\text{N/kg},\\; h = %s\\,\\text{m}\\)\n\n" +
                    "\\[E_p = %s\\,\\text{kg} \\times 9{,}8\\,\\dfrac{\\text{N}}{\\text{kg}} " +
                    "\\times %s\\,\\text{m} = %s\\,\\text{J}\\]\n\n" +
                    "∴  Ep = %s J",
                    fmtNum(sc.mass()), fmtNum(sc.height()),
                    fmtNum(sc.mass()), fmtNum(sc.height()), fmt(sc.energy()), fmt(sc.energy())));
            }
            case "masa" -> {
                double mass = round2(sc.energy() / (G * sc.height()));
                ex.setStatement(String.format(
                    "Un objeto situado a %s m de altura tiene una energía potencial de %s J. " +
                    "¿Cuál es su masa? (g = 9,8 N/kg)",
                    fmtNum(sc.height()), fmt(sc.energy())));
                ex.setCorrectAnswerValue(mass);
                ex.setCorrectAnswerDisplay(fmt(mass) + " kg");
                ex.setAnswerUnit("kg");
                ex.setExplanation(String.format(
                    "Fórmula: \\(E_p = m \\cdot g \\cdot h\\)\n\n" +
                    "Despejamos la masa:\n" +
                    "\\[m = \\dfrac{E_p}{g \\cdot h} = \\dfrac{%s\\,\\text{J}}" +
                    "{9{,}8\\,\\text{N/kg} \\times %s\\,\\text{m}} = %s\\,\\text{kg}\\]\n\n" +
                    "∴  m = %s kg",
                    fmt(sc.energy()), fmtNum(sc.height()), fmt(mass), fmt(mass)));
            }
            default -> {  // altura
                double height = round2(sc.energy() / (sc.mass() * G));
                ex.setStatement(String.format(
                    "Un objeto de masa %s kg tiene una energía potencial de %s J. " +
                    "¿A qué altura se encuentra? (g = 9,8 N/kg)",
                    fmtNum(sc.mass()), fmt(sc.energy())));
                ex.setCorrectAnswerValue(height);
                ex.setCorrectAnswerDisplay(fmt(height) + " m");
                ex.setAnswerUnit("m");
                ex.setExplanation(String.format(
                    "Fórmula: \\(E_p = m \\cdot g \\cdot h\\)\n\n" +
                    "Despejamos la altura:\n" +
                    "\\[h = \\dfrac{E_p}{m \\cdot g} = \\dfrac{%s\\,\\text{J}}" +
                    "{%s\\,\\text{kg} \\times 9{,}8\\,\\text{N/kg}} = %s\\,\\text{m}\\]\n\n" +
                    "∴  h = %s m",
                    fmt(sc.energy()), fmtNum(sc.mass()), fmt(height), fmt(height)));
            }
        }
    }

    // =========================================================================
    // Constructores internos — SUSTAINABILITY_TEST
    // =========================================================================

    private void buildSustainabilityTest(EnergyExercise ex) {
        ex.setEnergyType(EnergyType.SUSTAINABILITY_TEST);
        ex.setExerciseMode("MULTIPLE_CHOICE");
        McQuestion q = SUSTAINABILITY_MC.get(random.nextInt(SUSTAINABILITY_MC.size()));
        ex.setStatement(q.statement());
        ex.setOption0(q.opt0());
        ex.setOption1(q.opt1());
        ex.setOption2(q.opt2());
        ex.setOption3(q.opt3());
        ex.setCorrectIndex(q.correct());
        ex.setExplanation(q.explanation());
    }

    // =========================================================================
    // Utilidades de formato numérico
    // =========================================================================

    private double round2(double value) {
        return new BigDecimal(Double.toString(value))
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
    }

    /** Decimal con punto → coma española, sin ceros finales. */
    private String fmt(double value) {
        return new BigDecimal(Double.toString(value))
            .stripTrailingZeros()
            .toPlainString()
            .replace(".", ",");
    }

    /** Entero si no tiene parte decimal; si no, como fmt(). */
    private String fmtNum(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return fmt(value);
    }
}
