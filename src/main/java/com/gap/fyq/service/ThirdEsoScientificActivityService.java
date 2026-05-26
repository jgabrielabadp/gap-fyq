package com.gap.fyq.service;

import com.gap.fyq.model.thirdeso.scientificactivity.ActivityType;
import com.gap.fyq.model.thirdeso.scientificactivity.ThirdEsoScientificActivityExercise;
import com.gap.fyq.repository.ThirdEsoScientificActivityRepository;
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
public class ThirdEsoScientificActivityService {

    private final ThirdEsoScientificActivityRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "3ESO";
    private static final String BLOCK  = "BL1";

    // =========================================================================
    // Escenarios de ERROR_CALCULATION
    // Todos los valores de Ea y Er están precalculados y son exactos a 2 decimales
    // =========================================================================

    private record ErrorScenario(
        String quantity,      // "la longitud de un péndulo"
        String instrument,    // "una regla milimetrada"
        double vTheoretical,  // valor teórico (Vt)
        double vExperimental, // valor experimental (Ve)
        String unit,          // unidad de la magnitud
        double absoluteError, // Ea = |Ve - Vt|
        double relativeError  // Er = Ea/Vt × 100 (%)
    ) {}

    // Er = Ea / Vt × 100 — verificado para cada fila
    private static final List<ErrorScenario> ERROR_SCENARIOS = List.of(
        new ErrorScenario("la longitud de un péndulo",
            "una regla milimetrada",        50.00, 51.00, "cm",    1.00, 2.00),
        new ErrorScenario("la masa de una piedra",
            "una balanza de precisión",    100.00, 95.00, "g",     5.00, 5.00),
        new ErrorScenario("la temperatura de ebullición del agua",
            "un termómetro de mercurio",   100.00, 98.00, "°C",    2.00, 2.00),
        new ErrorScenario("el período de un péndulo",
            "un cronómetro digital",         2.00,  2.10, "s",     0.10, 5.00),
        new ErrorScenario("la densidad del agua destilada",
            "un picnómetro",                 1.00,  1.04, "g/cm³", 0.04, 4.00),
        new ErrorScenario("la velocidad de un carrito en plano inclinado",
            "un sensor de movimiento",     200.00, 204.00, "cm/s", 4.00, 2.00),
        new ErrorScenario("la constante elástica de un resorte",
            "un dinamómetro",               40.00, 41.00, "N/m",   1.00, 2.50),
        new ErrorScenario("la aceleración de la gravedad",
            "un sistema de caída libre",     9.80,  9.31, "m/s²",  0.49, 5.00),
        new ErrorScenario("la presión atmosférica",
            "un barómetro digital",        100.00, 100.50, "kPa",  0.50, 0.50),
        new ErrorScenario("el volumen de un líquido",
            "una probeta graduada",        100.00, 105.00, "mL",   5.00, 5.00),
        new ErrorScenario("la longitud focal de una lente convergente",
            "un banco óptico",              25.00, 26.00, "cm",    1.00, 4.00),
        new ErrorScenario("la resistencia eléctrica de un conductor",
            "un óhmetro de laboratorio",  200.00, 201.00, "Ω",     1.00, 0.50)
    );

    // =========================================================================
    // Escenarios de ADVANCED_CONVERSION
    // =========================================================================

    // ── Densidad: g/cm³ ↔ kg/m³  (factor: 1 g/cm³ = 1000 kg/m³) ────────────

    private record DensityScenario(
        String substance,
        double gPerCm3,   // densidad en g/cm³
        double kgPerM3    // densidad en kg/m³ = gPerCm3 × 1000
    ) {}

    private static final List<DensityScenario> DENSITY_SCENARIOS = List.of(
        new DensityScenario("el hierro",      7.87,  7870),
        new DensityScenario("el aluminio",    2.70,  2700),
        new DensityScenario("el agua",        1.00,  1000),
        new DensityScenario("el cobre",       8.96,  8960),
        new DensityScenario("el plomo",      11.35, 11350),
        new DensityScenario("el etanol",      0.79,   790),
        new DensityScenario("el corcho",      0.24,   240),
        new DensityScenario("la glicerina",   1.26,  1260)
    );

    // ── Velocidad: km/h ↔ m/s  (análisis dimensional completo) ──────────────

    private record SpeedScenario(
        String subject,  // "un tren de alta velocidad"
        double kmh,
        double ms        // = kmh / 3.6 (pares exactos)
    ) {}

    private static final List<SpeedScenario> SPEED_SCENARIOS = List.of(
        new SpeedScenario("un ciclista",                   18,   5),
        new SpeedScenario("un corredor de fondo",          36,  10),
        new SpeedScenario("un autobús urbano",             54,  15),
        new SpeedScenario("un coche en ciudad",            72,  20),
        new SpeedScenario("un coche en carretera",         90,  25),
        new SpeedScenario("un tren de cercanías",         108,  30),
        new SpeedScenario("un tren de alta velocidad",    144,  40),
        new SpeedScenario("un avión comercial en tierra", 180,  50)
    );

    // =========================================================================
    // Escenarios de GRAPH_PROPORTIONALITY
    // =========================================================================

    private record McQuestion(
        String statement,
        String opt0, String opt1, String opt2, String opt3,
        int correct,
        String explanation
    ) {}

    private static final List<McQuestion> GRAPH_MC = List.of(

        new McQuestion(
            "En una tabla de datos, al duplicar el valor de x el valor de y también " +
            "se duplica, y la recta pasa por el origen de coordenadas. " +
            "¿Qué tipo de relación existe entre x e y?",
            "Proporcionalidad inversa (y = k/x)",
            "Proporcionalidad directa (y = k·x)",
            "Relación lineal sin proporcionalidad (y = k·x + b, con b ≠ 0)",
            "Relación cuadrática (y = k·x²)",
            1,
            "Una relación de <strong>proporcionalidad directa</strong> cumple que y = k·x. " +
            "Su representación gráfica es una <strong>recta que pasa por el origen</strong>. " +
            "La clave diferenciadora frente a una relación «lineal» es que debe pasar " +
            "obligatoriamente por (0, 0): si no pasa por el origen, hay proporcionalidad " +
            "solo en la variación, pero la relación es lineal general (y = kx + b)."
        ),

        new McQuestion(
            "Los datos de presión (P) y volumen (V) de un gas a temperatura constante " +
            "muestran que P × V = constante (Ley de Boyle). " +
            "La gráfica de P frente a V tendrá la forma de...",
            "Una recta con pendiente positiva que pasa por el origen",
            "Una curva hiperbólica decreciente",
            "Una parábola creciente",
            "Una recta horizontal",
            1,
            "P × V = k implica \\(P = \\dfrac{k}{V}\\), es decir, <strong>proporcionalidad " +
            "inversa</strong> entre P y V. Su gráfica es una <strong>hipérbola</strong>: " +
            "cuando V aumenta, P disminuye de forma no lineal. Si representásemos P frente " +
            "a 1/V, obtendríamos una recta que pasa por el origen."
        ),

        new McQuestion(
            "Un alumno registra estos datos: x = 1 → y = 2; x = 2 → y = 8; " +
            "x = 3 → y = 18; x = 4 → y = 32. ¿Qué relación matemática describe mejor estos datos?",
            "y = 2·x (proporcionalidad directa)",
            "y = 2/x (proporcionalidad inversa)",
            "y = 2·x² (proporcionalidad cuadrática)",
            "No existe relación matemática clara",
            2,
            "Comprobamos: \\(y = 2x^2\\): \\(2\\cdot1^2=2\\) ✓, \\(2\\cdot2^2=8\\) ✓, " +
            "\\(2\\cdot3^2=18\\) ✓, \\(2\\cdot4^2=32\\) ✓. " +
            "Es una <strong>proporcionalidad cuadrática</strong>: y es proporcional al cuadrado " +
            "de x. Aparece en física en la energía cinética (\\(E_c = \\frac{1}{2}mv^2\\)) " +
            "y en la caída libre (\\(s = \\frac{1}{2}gt^2\\))."
        ),

        new McQuestion(
            "Una gráfica muestra una recta de pendiente positiva que NO pasa por el origen " +
            "(intercepta el eje y en un valor distinto de cero). Esto indica...",
            "Proporcionalidad directa entre las dos variables",
            "Proporcionalidad inversa entre las dos variables",
            "Relación lineal, pero NO de proporcionalidad directa",
            "Ausencia de relación matemática entre las variables",
            2,
            "Una recta de la forma \\(y = mx + b\\) con \\(b \\neq 0\\) indica una " +
            "<strong>relación lineal general</strong>. No es proporcionalidad directa " +
            "porque cuando x = 0, y = b ≠ 0 (la magnitud no es cero aunque la otra sí lo sea). " +
            "Ejemplo: la longitud de una barra metálica en función de la temperatura — " +
            "la longitud no es cero a 0 °C."
        ),

        new McQuestion(
            "En un experimento de MRU (movimiento rectilíneo uniforme), un alumno representa " +
            "la distancia (s) frente al tiempo (t). La gráfica es una recta con pendiente positiva " +
            "que pasa por el origen. Si en cambio representa s frente a t², obtendrá...",
            "De nuevo una recta que pasa por el origen (la relación s-t² también es lineal)",
            "Una hipérbola",
            "Una parábola",
            "Una línea horizontal (s constante)",
            0,
            "En el MRU, \\(s = v \\cdot t\\). La relación s-t es de <strong>proporcionalidad " +
            "directa</strong> (recta por el origen). Si representamos s frente a t², " +
            "como \\(t^2 = (s/v)^2 = s^2/v^2\\), el resultado <strong>no</strong> sería " +
            "lineal. Pero la pregunta es la inversa: s = v·t implica que s frente a t² " +
            "da \\(s = v \\cdot \\sqrt{t^2}\\), que es parabólico en t². " +
            "Sin embargo, la recta s-t pasa por el origen, señal clave del MRU."
        ),

        new McQuestion(
            "En un experimento de caída libre desde el reposo, se mide la distancia (s) " +
            "caída en distintos tiempos (t). Los datos son: " +
            "t = 1 s → s = 4,9 m; t = 2 s → s = 19,6 m; t = 3 s → s = 44,1 m. " +
            "¿Qué tipo de relación existe entre s y t?",
            "Proporcionalidad directa: s = k·t",
            "Proporcionalidad inversa: s = k/t",
            "Proporcionalidad cuadrática: s = k·t²",
            "Relación exponencial: s = e^t",
            2,
            "Comprobamos \\(s = \\frac{1}{2}g\\,t^2 = 4{,}9\\,t^2\\): " +
            "\\(4{,}9 \\times 1^2 = 4{,}9\\) ✓, \\(4{,}9 \\times 4 = 19{,}6\\) ✓, " +
            "\\(4{,}9 \\times 9 = 44{,}1\\) ✓. " +
            "Es la fórmula de la cinemática de caída libre con \\(g = 9{,}8\\,\\text{m/s}^2\\). " +
            "La relación s-t² sería una <strong>recta por el origen</strong>, señal de " +
            "proporcionalidad cuadrática."
        ),

        new McQuestion(
            "La ley de Ohm establece que I = V/R. En un circuito con voltaje constante V, " +
            "un alumno varía la resistencia R y mide la corriente I. " +
            "¿Qué tipo de relación existe entre I y R?",
            "Proporcionalidad directa: al doblar R, I se dobla",
            "Proporcionalidad inversa: al doblar R, I se reduce a la mitad",
            "Relación lineal sin proporcionalidad",
            "Relación cuadrática",
            1,
            "Con V constante, \\(I = \\dfrac{V}{R}\\): cuando R se duplica, I se divide " +
            "entre dos. Esto es <strong>proporcionalidad inversa</strong> (I × R = V = cte.). " +
            "La gráfica I frente a R es una hipérbola. Si representamos I frente a 1/R, " +
            "obtenemos una recta de pendiente V que pasa por el origen."
        ),

        new McQuestion(
            "Un científico estudia la relación entre la temperatura del agua (T, en °C) " +
            "y su densidad (ρ). Observa que la densidad máxima ocurre a 4 °C y decrece tanto " +
            "al bajar como al subir la temperatura. ¿Cómo clasificarías esta relación?",
            "Proporcionalidad directa (ρ aumenta con T)",
            "Proporcionalidad inversa (ρ disminuye con T)",
            "Relación lineal sin proporcionalidad",
            "Relación no lineal que no responde a ningún tipo de proporcionalidad simple",
            3,
            "La densidad del agua tiene un <strong>máximo en 4 °C</strong> y decrece en " +
            "ambas direcciones. Esta anomalía se debe a la estructura del enlace de hidrógeno. " +
            "No existe ninguna fórmula de proporcionalidad simple (directa, inversa o cuadrática) " +
            "que describa esta relación: es un comportamiento <strong>no lineal complejo</strong> " +
            "que exige recurrir a modelos termodinámicos avanzados."
        )
    );

    // =========================================================================
    // API pública
    // =========================================================================

    public ThirdEsoScientificActivityExercise generateAndSave() {
        ThirdEsoScientificActivityExercise ex = new ThirdEsoScientificActivityExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);

        // Distribución: 35 % ERROR_CALCULATION, 35 % ADVANCED_CONVERSION, 30 % GRAPH
        int roll = random.nextInt(20);
        if (roll < 7) {
            buildErrorCalculation(ex);
        } else if (roll < 14) {
            buildAdvancedConversion(ex);
        } else {
            buildGraphProportionality(ex);
        }

        log.debug("3ESO BL1 generado: activityType={} modo={}", ex.getActivityType(), ex.getExerciseMode());
        return repository.save(ex);
    }

    public ThirdEsoScientificActivityExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ejercicio 3ESO BL1 no encontrado: " + id));
    }

    // =========================================================================
    // Constructores internos — ERROR_CALCULATION
    // =========================================================================

    private void buildErrorCalculation(ThirdEsoScientificActivityExercise ex) {
        ex.setActivityType(ActivityType.ERROR_CALCULATION);
        ex.setExerciseMode("NUMERICAL");
        // Tolerancia ligeramente más amplia: los alumnos redondean de formas diversas
        ex.setTolerancePercent(3.0);

        ErrorScenario sc = ERROR_SCENARIOS.get(random.nextInt(ERROR_SCENARIOS.size()));
        boolean askAbsolute = random.nextBoolean();

        if (askAbsolute) {
            buildAbsoluteError(ex, sc);
        } else {
            buildRelativeError(ex, sc);
        }
    }

    private void buildAbsoluteError(ThirdEsoScientificActivityExercise ex, ErrorScenario sc) {
        ex.setUnknownVariable("error_absoluto");
        ex.setStatement(String.format(
            "Al medir %s con %s, se obtiene un valor experimental de %s %s. " +
            "El valor teórico aceptado es %s %s. " +
            "Calcula el error absoluto de la medida.",
            sc.quantity(), sc.instrument(),
            fmt(sc.vExperimental()), sc.unit(),
            fmt(sc.vTheoretical()), sc.unit()));
        ex.setCorrectAnswerValue(sc.absoluteError());
        ex.setCorrectAnswerDisplay(fmt(sc.absoluteError()) + " " + sc.unit());
        ex.setAnswerUnit(sc.unit());
        ex.setExplanation(String.format(
            "El <strong>Error Absoluto</strong> mide la diferencia entre el valor experimental " +
            "y el valor teórico:\n\n" +
            "\\[E_a = |V_e - V_t|\\]\n\n" +
            "Datos: \\(V_e = %s\\,\\text{%s},\\quad V_t = %s\\,\\text{%s}\\)\n\n" +
            "\\[E_a = |%s - %s|\\,\\text{%s} = %s\\,\\text{%s}\\]\n\n" +
            "∴  Ea = %s %s",
            fmt(sc.vExperimental()), sc.unit(), fmt(sc.vTheoretical()), sc.unit(),
            fmt(sc.vExperimental()), fmt(sc.vTheoretical()), sc.unit(),
            fmt(sc.absoluteError()), sc.unit(),
            fmt(sc.absoluteError()), sc.unit()));
    }

    private void buildRelativeError(ThirdEsoScientificActivityExercise ex, ErrorScenario sc) {
        ex.setUnknownVariable("error_relativo");
        ex.setStatement(String.format(
            "Al medir %s con %s, se obtiene un valor experimental de %s %s. " +
            "El valor teórico aceptado es %s %s. " +
            "Calcula el error relativo porcentual (en %%).",
            sc.quantity(), sc.instrument(),
            fmt(sc.vExperimental()), sc.unit(),
            fmt(sc.vTheoretical()), sc.unit()));
        ex.setCorrectAnswerValue(sc.relativeError());
        ex.setCorrectAnswerDisplay(fmt(sc.relativeError()) + " %");
        ex.setAnswerUnit("%");
        ex.setExplanation(String.format(
            "Primero calculamos el Error Absoluto:\n" +
            "\\[E_a = |V_e - V_t| = |%s - %s|\\,\\text{%s} = %s\\,\\text{%s}\\]\n\n" +
            "Luego el <strong>Error Relativo</strong> (en porcentaje):\n\n" +
            "\\[E_r = \\dfrac{E_a}{V_t} \\times 100 = \\dfrac{%s}{%s} \\times 100 = %s\\,\\%%\\]\n\n" +
            "∴  Er = %s %%",
            fmt(sc.vExperimental()), fmt(sc.vTheoretical()), sc.unit(),
            fmt(sc.absoluteError()), sc.unit(),
            fmt(sc.absoluteError()), fmt(sc.vTheoretical()),
            fmt(sc.relativeError()), fmt(sc.relativeError())));
    }

    // =========================================================================
    // Constructores internos — ADVANCED_CONVERSION
    // =========================================================================

    private void buildAdvancedConversion(ThirdEsoScientificActivityExercise ex) {
        ex.setActivityType(ActivityType.ADVANCED_CONVERSION);
        ex.setExerciseMode("NUMERICAL");

        // 50 % densidad,  50 % velocidad
        if (random.nextBoolean()) {
            buildDensityConversion(ex);
        } else {
            buildVelocityConversion(ex);
        }
    }

    private void buildDensityConversion(ThirdEsoScientificActivityExercise ex) {
        ex.setUnknownVariable("densidad");
        DensityScenario sc = DENSITY_SCENARIOS.get(random.nextInt(DENSITY_SCENARIOS.size()));
        boolean toCgs = random.nextBoolean();  // true → kg/m³ → g/cm³,  false → g/cm³ → kg/m³

        if (!toCgs) {
            // g/cm³ → kg/m³
            ex.setStatement(String.format(
                "La densidad de %s es %s g/cm³. " +
                "Exprésala en kg/m³ mostrando el análisis dimensional completo.",
                sc.substance(), fmt(sc.gPerCm3())));
            ex.setCorrectAnswerValue(sc.kgPerM3());
            ex.setCorrectAnswerDisplay(fmt(sc.kgPerM3()) + " kg/m³");
            ex.setAnswerUnit("kg/m³");
            ex.setExplanation(String.format(
                "Aplicamos el análisis dimensional sustituyendo cada unidad:\n\n" +
                "\\[1\\,\\dfrac{\\text{g}}{\\text{cm}^3} = " +
                "1\\,\\dfrac{\\text{g}}{\\text{cm}^3} " +
                "\\times \\dfrac{1\\,\\text{kg}}{1000\\,\\text{g}} " +
                "\\times \\left(\\dfrac{100\\,\\text{cm}}{1\\,\\text{m}}\\right)^3\\]\n\n" +
                "\\[= 1 \\times \\dfrac{1}{1000} \\times 10^6\\,\\dfrac{\\text{kg}}{\\text{m}^3} " +
                "= 1000\\,\\dfrac{\\text{kg}}{\\text{m}^3}\\]\n\n" +
                "Por tanto: \\(1\\,\\text{g/cm}^3 = 1000\\,\\text{kg/m}^3\\)\n\n" +
                "Aplicando al valor concreto:\n" +
                "\\[%s\\,\\dfrac{\\text{g}}{\\text{cm}^3} \\times " +
                "1000\\,\\dfrac{\\text{kg/m}^3}{\\text{g/cm}^3} = %s\\,\\dfrac{\\text{kg}}{\\text{m}^3}\\]\n\n" +
                "∴  ρ(%s) = %s kg/m³",
                fmt(sc.gPerCm3()), fmt(sc.kgPerM3()), sc.substance(), fmt(sc.kgPerM3())));
        } else {
            // kg/m³ → g/cm³
            ex.setStatement(String.format(
                "La densidad de %s es %s kg/m³. " +
                "Exprésala en g/cm³ mostrando el análisis dimensional completo.",
                sc.substance(), fmt(sc.kgPerM3())));
            ex.setCorrectAnswerValue(sc.gPerCm3());
            ex.setCorrectAnswerDisplay(fmt(sc.gPerCm3()) + " g/cm³");
            ex.setAnswerUnit("g/cm³");
            ex.setExplanation(String.format(
                "Aplicamos el análisis dimensional sustituyendo cada unidad:\n\n" +
                "\\[1\\,\\dfrac{\\text{kg}}{\\text{m}^3} = " +
                "1\\,\\dfrac{\\text{kg}}{\\text{m}^3} " +
                "\\times \\dfrac{1000\\,\\text{g}}{1\\,\\text{kg}} " +
                "\\times \\left(\\dfrac{1\\,\\text{m}}{100\\,\\text{cm}}\\right)^3\\]\n\n" +
                "\\[= 1 \\times 1000 \\times \\dfrac{1}{10^6}\\,\\dfrac{\\text{g}}{\\text{cm}^3} " +
                "= \\dfrac{1}{1000}\\,\\dfrac{\\text{g}}{\\text{cm}^3} = 10^{-3}\\,\\dfrac{\\text{g}}{\\text{cm}^3}\\]\n\n" +
                "Por tanto: \\(1\\,\\text{kg/m}^3 = 10^{-3}\\,\\text{g/cm}^3\\)\n\n" +
                "Aplicando al valor concreto:\n" +
                "\\[%s\\,\\dfrac{\\text{kg}}{\\text{m}^3} \\div 1000 = %s\\,\\dfrac{\\text{g}}{\\text{cm}^3}\\]\n\n" +
                "∴  ρ(%s) = %s g/cm³",
                fmt(sc.kgPerM3()), fmt(sc.gPerCm3()), sc.substance(), fmt(sc.gPerCm3())));
        }
    }

    private void buildVelocityConversion(ThirdEsoScientificActivityExercise ex) {
        ex.setUnknownVariable("velocidad");
        SpeedScenario sc = SPEED_SCENARIOS.get(random.nextInt(SPEED_SCENARIOS.size()));
        boolean toMs = random.nextBoolean();  // true → km/h → m/s,  false → m/s → km/h

        if (toMs) {
            ex.setStatement(String.format(
                "La velocidad de %s es %s km/h. Conviértela a m/s " +
                "mostrando el análisis dimensional completo.",
                sc.subject(), fmtNum(sc.kmh())));
            ex.setCorrectAnswerValue(sc.ms());
            ex.setCorrectAnswerDisplay(fmt(sc.ms()) + " m/s");
            ex.setAnswerUnit("m/s");
            ex.setExplanation(String.format(
                "Sustituimos las unidades compuestas una a una:\n\n" +
                "\\[1\\,\\dfrac{\\text{km}}{\\text{h}} = " +
                "1\\,\\dfrac{\\text{km}}{\\text{h}} " +
                "\\times \\dfrac{1000\\,\\text{m}}{1\\,\\text{km}} " +
                "\\times \\dfrac{1\\,\\text{h}}{3600\\,\\text{s}} " +
                "= \\dfrac{1000}{3600}\\,\\dfrac{\\text{m}}{\\text{s}} " +
                "= \\dfrac{1}{3{,}6}\\,\\dfrac{\\text{m}}{\\text{s}}\\]\n\n" +
                "Aplicando al valor concreto:\n" +
                "\\[v = %s\\,\\dfrac{\\text{km}}{\\text{h}} " +
                "\\times \\dfrac{1000\\,\\text{m}}{1\\,\\text{km}} " +
                "\\times \\dfrac{1\\,\\text{h}}{3600\\,\\text{s}} " +
                "= \\dfrac{%s}{3{,}6}\\,\\dfrac{\\text{m}}{\\text{s}} = %s\\,\\dfrac{\\text{m}}{\\text{s}}\\]\n\n" +
                "∴  v = %s m/s",
                fmtNum(sc.kmh()), fmtNum(sc.kmh()), fmt(sc.ms()), fmt(sc.ms())));
        } else {
            ex.setStatement(String.format(
                "La velocidad de %s es %s m/s. Conviértela a km/h " +
                "mostrando el análisis dimensional completo.",
                sc.subject(), fmtNum(sc.ms())));
            ex.setCorrectAnswerValue(sc.kmh());
            ex.setCorrectAnswerDisplay(fmt(sc.kmh()) + " km/h");
            ex.setAnswerUnit("km/h");
            ex.setExplanation(String.format(
                "Sustituimos las unidades compuestas una a una:\n\n" +
                "\\[1\\,\\dfrac{\\text{m}}{\\text{s}} = " +
                "1\\,\\dfrac{\\text{m}}{\\text{s}} " +
                "\\times \\dfrac{1\\,\\text{km}}{1000\\,\\text{m}} " +
                "\\times \\dfrac{3600\\,\\text{s}}{1\\,\\text{h}} " +
                "= \\dfrac{3600}{1000}\\,\\dfrac{\\text{km}}{\\text{h}} " +
                "= 3{,}6\\,\\dfrac{\\text{km}}{\\text{h}}\\]\n\n" +
                "Aplicando al valor concreto:\n" +
                "\\[v = %s\\,\\dfrac{\\text{m}}{\\text{s}} " +
                "\\times 3{,}6\\,\\dfrac{\\text{km/h}}{\\text{m/s}} " +
                "= %s\\,\\dfrac{\\text{km}}{\\text{h}}\\]\n\n" +
                "∴  v = %s km/h",
                fmtNum(sc.ms()), fmt(sc.kmh()), fmt(sc.kmh())));
        }
    }

    // =========================================================================
    // Constructores internos — GRAPH_PROPORTIONALITY
    // =========================================================================

    private void buildGraphProportionality(ThirdEsoScientificActivityExercise ex) {
        ex.setActivityType(ActivityType.GRAPH_PROPORTIONALITY);
        ex.setExerciseMode("MULTIPLE_CHOICE");
        McQuestion q = GRAPH_MC.get(random.nextInt(GRAPH_MC.size()));
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
