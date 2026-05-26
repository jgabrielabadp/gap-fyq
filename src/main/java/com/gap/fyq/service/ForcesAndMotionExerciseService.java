package com.gap.fyq.service;

import com.gap.fyq.model.motionforces.ForcesAndMotionExercise;
import com.gap.fyq.model.motionforces.ForcesAndMotionSubTopic;
import com.gap.fyq.repository.ForcesAndMotionExerciseRepository;
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
public class ForcesAndMotionExerciseService {

    private final ForcesAndMotionExerciseRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "2ESO";
    private static final String BLOCK  = "BL4";
    private static final double G      = 9.8;   // N/kg

    // =========================================================================
    // Escenarios de SPEED_CONVERSION
    // Pares limpios (m/s, km/h) donde 1 km/h = 1/3,6 m/s
    // =========================================================================

    private record SpeedPair(double ms, double kmh) {}

    private static final List<SpeedPair> SPEED_PAIRS = List.of(
        new SpeedPair(5,  18),
        new SpeedPair(10, 36),
        new SpeedPair(15, 54),
        new SpeedPair(20, 72),
        new SpeedPair(25, 90),
        new SpeedPair(30, 108),
        new SpeedPair(40, 144),
        new SpeedPair(50, 180)
    );

    private static final List<String> CONTEXTS_KMH_TO_MS = List.of(
        "Un coche circula por la autopista",
        "Un tren de cercanías viaja",
        "Un autobús urbano circula",
        "Una motocicleta avanza",
        "Un camión circula por carretera"
    );

    private static final List<String> CONTEXTS_MS_TO_KMH = List.of(
        "Un corredor de atletismo alcanza",
        "Un ciclista avanza a",
        "Un barco a motor navega a",
        "Un patinador en línea alcanza",
        "Un deportista en kayak rema a"
    );

    // =========================================================================
    // Escenarios de MRU_PROBLEMS  (v = s/t)
    // Todos los valores son enteros o decimales exactos a 1-2 cifras
    // =========================================================================

    private record MruScenario(
        String subject,       // "Un ciclista"
        String verb,          // "pedalea a velocidad constante"
        double speed, String speedUnit,
        double time,  String timeUnit,
        double dist,  String distUnit
    ) {}

    private static final List<MruScenario> MRU_SCENARIOS = List.of(
        // Escenarios cortos en m/s, s, m
        new MruScenario("Un corredor", "avanza a velocidad constante",
            5, "m/s", 10, "s", 50, "m"),
        new MruScenario("Un patinador", "desliza a velocidad constante",
            10, "m/s", 6, "s", 60, "m"),
        new MruScenario("Un ciclista", "pedalea a velocidad constante",
            15, "m/s", 4, "s", 60, "m"),
        new MruScenario("Un barco", "navega a velocidad constante",
            20, "m/s", 5, "s", 100, "m"),
        new MruScenario("Una pelota", "rueda a velocidad constante",
            25, "m/s", 4, "s", 100, "m"),
        new MruScenario("Un tren", "circula a velocidad constante",
            30, "m/s", 3, "s", 90, "m"),
        new MruScenario("Un corredor", "entrena a velocidad uniforme",
            8, "m/s", 10, "s", 80, "m"),
        new MruScenario("Un kayakista", "avanza a velocidad constante",
            6, "m/s", 15, "s", 90, "m"),
        // Escenarios largos en km/h, h, km
        new MruScenario("Un autobús", "viaja a velocidad constante",
            100, "km/h", 2, "h", 200, "km"),
        new MruScenario("Un tren de alta velocidad", "circula a velocidad constante",
            120, "km/h", 3, "h", 360, "km"),
        new MruScenario("Un coche", "realiza un viaje a velocidad uniforme",
            80, "km/h", 4, "h", 320, "km"),
        new MruScenario("Un tren de mercancías", "avanza a velocidad constante",
            60, "km/h", 5, "h", 300, "km"),
        new MruScenario("Un autobús de línea", "recorre su ruta a velocidad uniforme",
            150, "km/h", 2, "h", 300, "km")
    );

    private static final List<String> MRU_UNKNOWNS = List.of("espacio", "tiempo", "velocidad");

    // =========================================================================
    // Escenarios de WEIGHT_AND_FORCES — parte cuantitativa
    // P = m·g,  g = 9,8 N/kg
    // =========================================================================

    private record WeightScenario(
        String item,          // "un libro de texto"
        double massDisplay,   // valor mostrado al alumno (puede ser g o kg)
        String massUnit       // "kg" o "g"
    ) {}

    private static final List<WeightScenario> WEIGHT_KG_SCENARIOS = List.of(
        new WeightScenario("un libro de texto",     1,   "kg"),
        new WeightScenario("una mochila escolar",   2,   "kg"),
        new WeightScenario("una bicicleta",         5,   "kg"),
        new WeightScenario("una maleta de viaje",  10,   "kg"),
        new WeightScenario("una persona adulta",   20,   "kg"),  // mitad de persona, más pedagógico
        new WeightScenario("una caja de libros",   50,   "kg"),
        new WeightScenario("un bloque de cemento", 100,  "kg")
    );

    private static final List<WeightScenario> WEIGHT_G_SCENARIOS = List.of(
        new WeightScenario("una naranja",           200, "g"),
        new WeightScenario("un vaso de agua",       500, "g"),
        new WeightScenario("una botella pequeña",  1000, "g"),
        new WeightScenario("un paquete de arroz",  2000, "g"),
        new WeightScenario("un libro grueso",       100, "g"),
        new WeightScenario("un paquete de sal",     500, "g"),
        new WeightScenario("una lata de refresco",  200, "g")
    );

    // =========================================================================
    // Escenarios de WEIGHT_AND_FORCES — parte cualitativa (tipo test)
    // =========================================================================

    private record McQuestion(
        String statement,
        String opt0, String opt1, String opt2, String opt3,
        int correct,
        String explanation
    ) {}

    private static final List<McQuestion> FORCES_MC = List.of(

        new McQuestion(
            "Una goma de borrar recupera su forma original al dejar de estirarla. " +
            "¿Qué tipo de material es?",
            "Plástico",
            "Elástico",
            "Rígido",
            "Frágil",
            1,
            "Los <strong>cuerpos elásticos</strong> vuelven a su forma y tamaño originales " +
            "cuando cesa la fuerza que los deforma. Una goma de borrar es un ejemplo clásico de material elástico."
        ),

        new McQuestion(
            "Al moldear una figura con plastilina, esta mantiene la nueva forma. " +
            "¿Qué tipo de cuerpo es la plastilina?",
            "Elástico",
            "Rígido",
            "Plástico",
            "Indeformable",
            2,
            "Los <strong>cuerpos plásticos</strong> se deforman de manera permanente: no recuperan " +
            "su forma original cuando cesa la fuerza. La plastilina es el ejemplo más familiar."
        ),

        new McQuestion(
            "¿Cuál de las siguientes opciones recoge TODOS los efectos que puede " +
            "tener una fuerza sobre un cuerpo?",
            "Solo cambiar su velocidad (o ponerlo en movimiento)",
            "Solo cambiar su forma o tamaño",
            "Cambiar su velocidad y/o su forma o tamaño",
            "Solo cambiar la dirección de su movimiento",
            2,
            "Las fuerzas pueden producir dos tipos de efectos: " +
            "<strong>dinámicos</strong> (cambiar el estado de movimiento: velocidad o dirección) " +
            "y <strong>deformadores</strong> (cambiar la forma o el tamaño del cuerpo). " +
            "Ambos efectos pueden ocurrir a la vez."
        ),

        new McQuestion(
            "¿Cuál de los siguientes objetos es el mejor ejemplo de cuerpo rígido?",
            "Un globo de goma inflado",
            "Un muelle de acero",
            "Un trozo de mantequilla",
            "Una llave de hierro",
            3,
            "Los <strong>cuerpos rígidos</strong> prácticamente no se deforman al aplicarles " +
            "fuerzas ordinarias. Una llave de hierro mantiene su forma aunque ejerzamos " +
            "fuerzas sobre ella, a diferencia del globo (elástico), el muelle (elástico) " +
            "o la mantequilla (plástica)."
        ),

        new McQuestion(
            "Un muelle se estira al colgarle una pesa y vuelve a su longitud original " +
            "al quitarla. ¿Cómo actúa el muelle?",
            "Como cuerpo plástico",
            "Como cuerpo rígido",
            "Como cuerpo elástico",
            "Como cuerpo frágil",
            2,
            "El muelle es el ejemplo más claro de <strong>cuerpo elástico</strong>: almacena energía " +
            "al deformarse y la devuelve recuperando su longitud original al cesar la fuerza."
        ),

        new McQuestion(
            "Un balón de fútbol está parado. Al chutarlo, se pone en movimiento. " +
            "¿Qué ha producido la fuerza del pie?",
            "Ha cambiado la masa del balón",
            "Ha cambiado el estado de movimiento del balón",
            "Ha deformado permanentemente el balón",
            "Ha reducido el peso del balón",
            1,
            "La fuerza del pie ha actuado como <strong>efecto dinámico</strong>: ha cambiado el " +
            "estado de movimiento del balón (de reposo a movimiento). " +
            "La masa y el peso no cambian por aplicar una fuerza ordinaria."
        )
    );

    // =========================================================================
    // API pública
    // =========================================================================

    public ForcesAndMotionExercise generateAndSave() {
        ForcesAndMotionExercise ex = new ForcesAndMotionExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);

        // Distribución: 40 % SPEED_CONVERSION, 40 % MRU, 20 % WEIGHT_AND_FORCES
        int roll = random.nextInt(10);
        if (roll < 4) {
            buildSpeedConversion(ex);
        } else if (roll < 8) {
            buildMruProblem(ex);
        } else {
            buildWeightAndForces(ex);
        }

        log.debug("BL4 generado: subTopic={} modo={} id=pendiente",
            ex.getSubTopic(), ex.getExerciseMode());
        return repository.save(ex);
    }

    public ForcesAndMotionExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ejercicio BL4 no encontrado: " + id));
    }

    // =========================================================================
    // Constructores internos — SPEED_CONVERSION
    // =========================================================================

    private void buildSpeedConversion(ForcesAndMotionExercise ex) {
        ex.setSubTopic(ForcesAndMotionSubTopic.SPEED_CONVERSION);
        ex.setExerciseMode("NUMERICAL");
        ex.setUnknownVariable("velocidad");

        SpeedPair pair = SPEED_PAIRS.get(random.nextInt(SPEED_PAIRS.size()));
        boolean fromKmh = random.nextBoolean();  // true → km/h a m/s; false → m/s a km/h

        if (fromKmh) {
            String ctx = CONTEXTS_KMH_TO_MS.get(random.nextInt(CONTEXTS_KMH_TO_MS.size()));
            ex.setStatement(String.format(
                "%s a %s km/h. Expresa esa velocidad en m/s.",
                ctx, fmtInt(pair.kmh())));
            ex.setCorrectAnswerValue(pair.ms());
            ex.setCorrectAnswerDisplay(fmt(pair.ms()) + " m/s");
            ex.setAnswerUnit("m/s");
            ex.setExplanation(buildSpeedExplanationKmhToMs(pair));
        } else {
            String ctx = CONTEXTS_MS_TO_KMH.get(random.nextInt(CONTEXTS_MS_TO_KMH.size()));
            ex.setStatement(String.format(
                "%s una velocidad de %s m/s. Conviértela a km/h.",
                ctx, fmtInt(pair.ms())));
            ex.setCorrectAnswerValue(pair.kmh());
            ex.setCorrectAnswerDisplay(fmt(pair.kmh()) + " km/h");
            ex.setAnswerUnit("km/h");
            ex.setExplanation(buildSpeedExplanationMsToKmh(pair));
        }
    }

    private String buildSpeedExplanationKmhToMs(SpeedPair pair) {
        return String.format(
            "Para pasar de km/h a m/s se divide entre 3,6 " +
            "(porque 1 km = 1000 m y 1 h = 3600 s):\n\n" +
            "\\[v(\\text{m/s}) = \\dfrac{v(\\text{km/h})}{3{,}6}\\]\n\n" +
            "Aplicando:\n" +
            "\\[v = \\dfrac{%s\\,\\text{km/h}}{3{,}6} = %s\\,\\text{m/s}\\]\n\n" +
            "∴  %s km/h = %s m/s",
            fmtInt(pair.kmh()), fmt(pair.ms()),
            fmtInt(pair.kmh()), fmt(pair.ms()));
    }

    private String buildSpeedExplanationMsToKmh(SpeedPair pair) {
        return String.format(
            "Para pasar de m/s a km/h se multiplica por 3,6 " +
            "(porque 1 m/s = 3,6 km/h):\n\n" +
            "\\[v(\\text{km/h}) = v(\\text{m/s}) \\times 3{,}6\\]\n\n" +
            "Aplicando:\n" +
            "\\[v = %s\\,\\text{m/s} \\times 3{,}6 = %s\\,\\text{km/h}\\]\n\n" +
            "∴  %s m/s = %s km/h",
            fmtInt(pair.ms()), fmt(pair.kmh()),
            fmtInt(pair.ms()), fmt(pair.kmh()));
    }

    // =========================================================================
    // Constructores internos — MRU_PROBLEMS
    // =========================================================================

    private void buildMruProblem(ForcesAndMotionExercise ex) {
        ex.setSubTopic(ForcesAndMotionSubTopic.MRU_PROBLEMS);
        ex.setExerciseMode("NUMERICAL");

        MruScenario sc = MRU_SCENARIOS.get(random.nextInt(MRU_SCENARIOS.size()));
        String unknown = MRU_UNKNOWNS.get(random.nextInt(MRU_UNKNOWNS.size()));
        ex.setUnknownVariable(unknown);

        switch (unknown) {
            case "espacio" -> buildMruEspacio(ex, sc);
            case "tiempo"  -> buildMruTiempo(ex, sc);
            default        -> buildMruVelocidad(ex, sc);
        }
    }

    private void buildMruEspacio(ForcesAndMotionExercise ex, MruScenario sc) {
        ex.setStatement(String.format(
            "%s %s a una velocidad de %s %s durante %s %s. " +
            "¿Qué distancia recorre?",
            sc.subject(), sc.verb(),
            fmtNum(sc.speed()), sc.speedUnit(),
            fmtNum(sc.time()), sc.timeUnit()));
        double s = round2(sc.speed() * sc.time());
        ex.setCorrectAnswerValue(s);
        ex.setCorrectAnswerDisplay(fmt(s) + " " + sc.distUnit());
        ex.setAnswerUnit(sc.distUnit());
        ex.setExplanation(buildMruExplanationEspacio(sc, s));
    }

    private void buildMruTiempo(ForcesAndMotionExercise ex, MruScenario sc) {
        ex.setStatement(String.format(
            "%s %s. Su velocidad es %s %s y recorre %s %s. " +
            "¿Cuánto tiempo tarda?",
            sc.subject(), sc.verb(),
            fmtNum(sc.speed()), sc.speedUnit(),
            fmtNum(sc.dist()), sc.distUnit()));
        double t = round2(sc.dist() / sc.speed());
        ex.setCorrectAnswerValue(t);
        ex.setCorrectAnswerDisplay(fmt(t) + " " + sc.timeUnit());
        ex.setAnswerUnit(sc.timeUnit());
        ex.setExplanation(buildMruExplanationTiempo(sc, t));
    }

    private void buildMruVelocidad(ForcesAndMotionExercise ex, MruScenario sc) {
        ex.setStatement(String.format(
            "%s %s. Recorre %s %s en %s %s. " +
            "¿Cuál es su velocidad?",
            sc.subject(), sc.verb(),
            fmtNum(sc.dist()), sc.distUnit(),
            fmtNum(sc.time()), sc.timeUnit()));
        double v = round2(sc.dist() / sc.time());
        ex.setCorrectAnswerValue(v);
        ex.setCorrectAnswerDisplay(fmt(v) + " " + sc.speedUnit());
        ex.setAnswerUnit(sc.speedUnit());
        ex.setExplanation(buildMruExplanationVelocidad(sc, v));
    }

    private String buildMruExplanationEspacio(MruScenario sc, double s) {
        return String.format(
            "Fórmula del MRU: \\(v = \\dfrac{s}{t}\\)\n\n" +
            "Despejamos el espacio:\n" +
            "\\[s = v \\cdot t = %s\\,\\text{%s} \\times %s\\,\\text{%s} = %s\\,\\text{%s}\\]\n\n" +
            "∴  s = %s %s",
            fmtNum(sc.speed()), sc.speedUnit(),
            fmtNum(sc.time()),  sc.timeUnit(),
            fmt(s),             sc.distUnit(),
            fmt(s),             sc.distUnit());
    }

    private String buildMruExplanationTiempo(MruScenario sc, double t) {
        return String.format(
            "Fórmula del MRU: \\(v = \\dfrac{s}{t}\\)\n\n" +
            "Despejamos el tiempo:\n" +
            "\\[t = \\dfrac{s}{v} = \\dfrac{%s\\,\\text{%s}}{%s\\,\\text{%s}} = %s\\,\\text{%s}\\]\n\n" +
            "∴  t = %s %s",
            fmtNum(sc.dist()),  sc.distUnit(),
            fmtNum(sc.speed()), sc.speedUnit(),
            fmt(t),             sc.timeUnit(),
            fmt(t),             sc.timeUnit());
    }

    private String buildMruExplanationVelocidad(MruScenario sc, double v) {
        return String.format(
            "Fórmula del MRU: \\(v = \\dfrac{s}{t}\\)\n\n" +
            "Aplicamos directamente:\n" +
            "\\[v = \\dfrac{s}{t} = \\dfrac{%s\\,\\text{%s}}{%s\\,\\text{%s}} = %s\\,\\text{%s}\\]\n\n" +
            "∴  v = %s %s",
            fmtNum(sc.dist()),  sc.distUnit(),
            fmtNum(sc.time()),  sc.timeUnit(),
            fmt(v),             sc.speedUnit(),
            fmt(v),             sc.speedUnit());
    }

    // =========================================================================
    // Constructores internos — WEIGHT_AND_FORCES
    // =========================================================================

    private void buildWeightAndForces(ForcesAndMotionExercise ex) {
        ex.setSubTopic(ForcesAndMotionSubTopic.WEIGHT_AND_FORCES);
        // 50 % cálculo numérico del Peso, 50 % pregunta cualitativa tipo test
        if (random.nextBoolean()) {
            buildWeightNumerical(ex);
        } else {
            buildForcesMC(ex);
        }
    }

    private void buildWeightNumerical(ForcesAndMotionExercise ex) {
        ex.setExerciseMode("NUMERICAL");
        ex.setUnknownVariable("peso");
        ex.setAnswerUnit("N");

        // 40 % masa en kg, 60 % masa en g (fuerza cambio de unidades)
        boolean useGrams = random.nextInt(5) >= 2;
        List<WeightScenario> pool = useGrams ? WEIGHT_G_SCENARIOS : WEIGHT_KG_SCENARIOS;
        WeightScenario sc = pool.get(random.nextInt(pool.size()));

        double massKg = "g".equals(sc.massUnit())
            ? sc.massDisplay() / 1000.0
            : sc.massDisplay();
        double weight = round2(massKg * G);

        ex.setStatement(String.format(
            "Calcula el peso de %s que tiene una masa de %s %s. " +
            "Usa g = 9,8 N/kg.",
            sc.item(), fmtNum(sc.massDisplay()), sc.massUnit()));
        ex.setCorrectAnswerValue(weight);
        ex.setCorrectAnswerDisplay(fmt(weight) + " N");
        ex.setExplanation(buildWeightExplanation(sc, massKg, weight));
    }

    private String buildWeightExplanation(WeightScenario sc, double massKg, double weight) {
        if ("g".equals(sc.massUnit())) {
            return String.format(
                "Primero convertimos la masa a kilogramos:\n" +
                "\\[m = \\dfrac{%s\\,\\text{g}}{1000} = %s\\,\\text{kg}\\]\n\n" +
                "Luego aplicamos la fórmula del Peso \\(P = m \\cdot g\\):\n" +
                "\\[P = %s\\,\\text{kg} \\times 9{,}8\\,\\dfrac{\\text{N}}{\\text{kg}} = %s\\,\\text{N}\\]\n\n" +
                "∴  P = %s N",
                fmtNum(sc.massDisplay()), fmt(massKg),
                fmt(massKg), fmt(weight),
                fmt(weight));
        } else {
            return String.format(
                "Aplicamos la fórmula del Peso \\(P = m \\cdot g\\):\n" +
                "\\[P = %s\\,\\text{kg} \\times 9{,}8\\,\\dfrac{\\text{N}}{\\text{kg}} = %s\\,\\text{N}\\]\n\n" +
                "∴  P = %s N",
                fmtNum(sc.massDisplay()), fmt(weight),
                fmt(weight));
        }
    }

    private void buildForcesMC(ForcesAndMotionExercise ex) {
        ex.setExerciseMode("MULTIPLE_CHOICE");
        McQuestion q = FORCES_MC.get(random.nextInt(FORCES_MC.size()));
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

    /** Decimal con punto → coma española, sin ceros finales innecesarios. */
    private String fmt(double value) {
        return new BigDecimal(Double.toString(value))
            .stripTrailingZeros()
            .toPlainString()
            .replace(".", ",");
    }

    /** Como fmt(), pero entero si no tiene decimales. */
    private String fmtNum(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return fmt(value);
    }

    /** Alias semántico para valores que siempre son enteros (índices, unidades enteras). */
    private String fmtInt(double value) {
        return String.valueOf((long) value);
    }
}
