package com.gap.fyq.service;

import com.gap.fyq.model.fourtheso.scientificactivity.FourthEsoActivityType;
import com.gap.fyq.model.fourtheso.scientificactivity.FourthEsoScientificActivityExercise;
import com.gap.fyq.repository.FourthEsoScientificActivityRepository;
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
public class FourthEsoScientificActivityService {

    private final FourthEsoScientificActivityRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "4ESO";
    private static final String BLOCK  = "BL1";

    // =========================================================================
    // Escenarios de SCALAR_VECTORIAL (tipo test)
    // =========================================================================

    private record McQuestion(
        String statement,
        String opt0, String opt1, String opt2, String opt3,
        int correct,
        String explanation
    ) {}

    private static final List<McQuestion> SCALAR_VECTORIAL_MC = List.of(

        new McQuestion(
            "¿Cuál de las siguientes es una magnitud VECTORIAL?",
            "Masa",
            "Temperatura",
            "Velocidad",
            "Tiempo",
            2,
            "La <strong>velocidad</strong> es una magnitud vectorial porque para describirla " +
            "completamente se necesita indicar su <strong>módulo</strong> (p.ej. 80 km/h), " +
            "su <strong>dirección</strong> (p.ej. horizontal) y su <strong>sentido</strong> " +
            "(p.ej. hacia el norte). La masa, la temperatura y el tiempo son magnitudes " +
            "<em>escalares</em>: quedan completamente definidas con un número y una unidad."
        ),

        new McQuestion(
            "¿Cuál de las siguientes es una magnitud ESCALAR?",
            "Fuerza",
            "Desplazamiento",
            "Aceleración",
            "Temperatura",
            3,
            "La <strong>temperatura</strong> es una magnitud escalar: basta con indicar su " +
            "valor numérico y la unidad (p.ej. 25 °C) para describirla completamente. " +
            "La fuerza, el desplazamiento y la aceleración son magnitudes <em>vectoriales</em> " +
            "porque poseen módulo, dirección y sentido."
        ),

        new McQuestion(
            "Un alumno empuja una caja con 20 N hacia la derecha y otro alumno la empuja " +
            "con 15 N hacia arriba. ¿Por qué no se puede calcular la resultante sumando " +
            "simplemente 20 + 15 = 35 N?",
            "Porque la fuerza es una magnitud escalar y no se puede sumar con otra fuerza",
            "Porque la fuerza es una magnitud vectorial: tiene módulo, dirección y sentido, " +
            "y no se suman algebraicamente cuando no son paralelas",
            "Porque la unidad Newton no admite sumas directas",
            "Porque la suma solo es válida si las dos fuerzas tienen el mismo módulo",
            1,
            "La <strong>fuerza es una magnitud vectorial</strong>. Al no ser paralelas, " +
            "sus vectores forman un ángulo de 90° y la resultante se calcula con el " +
            "<strong>Teorema de Pitágoras</strong>:\n\n" +
            "\\[R = \\sqrt{20^2 + 15^2} = \\sqrt{400 + 225} = \\sqrt{625} = 25\\,\\text{N}\\]\n\n" +
            "La suma algebraica 20 + 15 = 35 N solo sería válida si ambas fuerzas fuesen " +
            "paralelas y tuviesen el mismo sentido."
        ),

        new McQuestion(
            "Para describir completamente una magnitud vectorial, además de su módulo " +
            "(valor numérico con unidad), ¿qué información adicional es imprescindible?",
            "Solo el nombre del instrumento que la mide",
            "Solo la temperatura del laboratorio donde se mide",
            "Su dirección y su sentido",
            "El color del vector en el diagrama",
            2,
            "Un vector queda completamente determinado por tres elementos:\n\n" +
            "<ul><li><strong>Módulo:</strong> valor numérico con unidad (p.ej. 5 N).</li>" +
            "<li><strong>Dirección:</strong> la recta a lo largo de la cual actúa (p.ej. horizontal).</li>" +
            "<li><strong>Sentido:</strong> hacia cuál de los dos extremos de esa recta (p.ej. hacia la derecha).</li></ul>"
        ),

        new McQuestion(
            "De las siguientes magnitudes físicas, ¿cuáles son VECTORIALES? " +
            "Velocidad (v), masa (m), aceleración (a), tiempo (t).",
            "v y m",
            "v y a",
            "m y t",
            "a y t",
            1,
            "Son vectoriales todas aquellas magnitudes que requieren módulo, dirección y sentido:\n\n" +
            "<ul><li><strong>Velocidad (v):</strong> vectorial — un coche puede ir a 90 km/h " +
            "hacia el norte o hacia el sur: son velocidades distintas.</li>" +
            "<li><strong>Aceleración (a):</strong> vectorial — tiene módulo, dirección y sentido.</li>" +
            "<li>Masa (m) y tiempo (t) son escalares: solo necesitan un valor y una unidad.</li></ul>"
        ),

        new McQuestion(
            "¿Cuál de estas afirmaciones sobre las magnitudes escalares es CORRECTA?",
            "Las magnitudes escalares requieren módulo, dirección y sentido para ser descritas",
            "La fuerza y la velocidad son ejemplos de magnitudes escalares",
            "Una magnitud escalar queda completamente definida con un valor numérico y su unidad",
            "No existen magnitudes escalares en Física",
            2,
            "Una <strong>magnitud escalar</strong> queda totalmente especificada indicando un " +
            "valor numérico y su unidad de medida. Ejemplos: masa (5 kg), temperatura (20 °C), " +
            "tiempo (3 s), energía (100 J), distancia recorrida (50 m). En cambio, la fuerza " +
            "y la velocidad son vectoriales porque hay que especificar también la dirección y el sentido."
        ),

        new McQuestion(
            "Un automóvil circula a 80 km/h. Si además indicamos que va hacia el norte, " +
            "¿qué información hemos añadido para describir el vector velocidad?",
            "El módulo del vector velocidad",
            "La unidad de medida",
            "La dirección y el sentido",
            "La aceleración del vehículo",
            2,
            "El valor «80 km/h» ya da el <strong>módulo</strong>. Al añadir «hacia el norte» " +
            "estamos especificando la <strong>dirección</strong> (eje norte-sur) y el " +
            "<strong>sentido</strong> (hacia el norte). Con estos tres datos el vector velocidad " +
            "queda completamente determinado."
        ),

        new McQuestion(
            "¿Cuál de los siguientes grupos contiene ÚNICAMENTE magnitudes escalares?",
            "Fuerza, desplazamiento, velocidad",
            "Masa, temperatura, tiempo, energía",
            "Aceleración, momento lineal, peso",
            "Velocidad, fuerza, posición",
            1,
            "<ul>" +
            "<li><strong>Masa:</strong> escalar (p.ej. 2 kg).</li>" +
            "<li><strong>Temperatura:</strong> escalar (p.ej. 37 °C).</li>" +
            "<li><strong>Tiempo:</strong> escalar (p.ej. 5 s).</li>" +
            "<li><strong>Energía:</strong> escalar (p.ej. 200 J).</li>" +
            "</ul>\n\n" +
            "Fuerza, desplazamiento, aceleración, momento lineal, peso, posición y velocidad " +
            "son todas magnitudes <em>vectoriales</em>."
        )
    );

    // =========================================================================
    // Escenarios de VECTOR_MATH
    // Todos los resultados son exactos (ternas pitagóricas o sumas enteras).
    // =========================================================================

    private record VectorScenario(
        String subtype,   // "SAME_SAME", "SAME_OPPOSITE", "PERPENDICULAR"
        double vecA,
        double vecB,
        double result,
        String unit,
        String statement
    ) {}

    // ── Misma dirección, mismo sentido (R = A + B) ────────────────────────────

    private static final List<VectorScenario> SAME_SAME_SCENARIOS = List.of(
        new VectorScenario("SAME_SAME", 3, 7, 10, "N",
            "Dos fuerzas actúan sobre un bloque en la misma dirección y el mismo sentido: " +
            "|F₁| = 3 N y |F₂| = 7 N. Calcula el módulo de la fuerza resultante."),
        new VectorScenario("SAME_SAME", 5, 8, 13, "N",
            "Dos fuerzas actúan sobre un objeto en la misma dirección y el mismo sentido: " +
            "|F₁| = 5 N y |F₂| = 8 N. Calcula el módulo de la fuerza resultante."),
        new VectorScenario("SAME_SAME", 12, 9, 21, "km/h",
            "Un barco avanza a 12 km/h en aguas en calma. La corriente del río lleva la misma " +
            "dirección y sentido con una velocidad de 9 km/h. Calcula el módulo de la " +
            "velocidad resultante del barco."),
        new VectorScenario("SAME_SAME", 15, 20, 35, "N",
            "Dos personas empujan un carro en la misma dirección y el mismo sentido: una aplica " +
            "15 N y la otra 20 N. Calcula el módulo de la fuerza resultante."),
        new VectorScenario("SAME_SAME", 6, 14, 20, "m/s",
            "Dos vectores velocidad tienen la misma dirección y el mismo sentido: " +
            "|v₁| = 6 m/s y |v₂| = 14 m/s. Calcula el módulo de la velocidad resultante."),
        new VectorScenario("SAME_SAME", 8, 12, 20, "m",
            "Un objeto realiza dos desplazamientos sucesivos en la misma dirección y sentido: " +
            "d₁ = 8 m y d₂ = 12 m. Calcula el módulo del desplazamiento total.")
    );

    // ── Misma dirección, sentidos opuestos (R = |A − B|, con A > B) ──────────

    private static final List<VectorScenario> SAME_OPPOSITE_SCENARIOS = List.of(
        new VectorScenario("SAME_OPPOSITE", 15, 6, 9, "N",
            "Dos equipos tiran de una cuerda en sentidos opuestos a lo largo de la misma línea: " +
            "el primero aplica 15 N y el segundo 6 N. Calcula el módulo de la fuerza resultante."),
        new VectorScenario("SAME_OPPOSITE", 20, 8, 12, "N",
            "Dos fuerzas actúan sobre un objeto en la misma dirección pero en sentidos opuestos: " +
            "|F₁| = 20 N y |F₂| = 8 N. Calcula el módulo de la fuerza resultante."),
        new VectorScenario("SAME_OPPOSITE", 25, 10, 15, "m/s",
            "Un barco avanza a 25 m/s. La corriente del río va en sentido contrario a 10 m/s. " +
            "Calcula el módulo de la velocidad resultante del barco respecto al fondo."),
        new VectorScenario("SAME_OPPOSITE", 30, 18, 12, "N",
            "Dos fuerzas opuestas actúan sobre un cuerpo: |F₁| = 30 N y |F₂| = 18 N. " +
            "Calcula el módulo de la fuerza resultante."),
        new VectorScenario("SAME_OPPOSITE", 40, 15, 25, "N",
            "Una fuerza de 40 N y otra de 15 N actúan sobre el mismo punto en sentidos opuestos. " +
            "Calcula el módulo de la fuerza resultante."),
        new VectorScenario("SAME_OPPOSITE", 18, 7, 11, "m",
            "Un objeto se desplaza 18 m hacia la derecha y luego 7 m hacia la izquierda sobre la " +
            "misma línea. Calcula el módulo del desplazamiento resultante.")
    );

    // ── Vectores perpendiculares — ternas pitagóricas (R = √(A²+B²)) ─────────

    private static final List<VectorScenario> PERPENDICULAR_SCENARIOS = List.of(
        new VectorScenario("PERPENDICULAR", 3, 4, 5, "N",
            "Dos fuerzas perpendiculares actúan sobre un cuerpo: |F₁| = 3 N (horizontal) " +
            "y |F₂| = 4 N (vertical). Aplica el Teorema de Pitágoras para calcular el " +
            "módulo de la fuerza resultante."),
        new VectorScenario("PERPENDICULAR", 6, 8, 10, "N",
            "Dos fuerzas perpendiculares actúan sobre un bloque: |F₁| = 6 N (horizontal) " +
            "y |F₂| = 8 N (vertical). Aplica el Teorema de Pitágoras."),
        new VectorScenario("PERPENDICULAR", 5, 12, 13, "m/s",
            "Un barco puede navegar a 5 m/s en aguas tranquilas. La corriente del río es " +
            "perpendicular a su rumbo y tiene una velocidad de 12 m/s. Calcula el módulo " +
            "de la velocidad resultante."),
        new VectorScenario("PERPENDICULAR", 9, 12, 15, "m",
            "Desde el punto O, un objeto se desplaza 9 m hacia el este y después 12 m hacia " +
            "el norte. Aplica el Teorema de Pitágoras para calcular el módulo del " +
            "desplazamiento resultante."),
        new VectorScenario("PERPENDICULAR", 8, 15, 17, "N",
            "Dos fuerzas perpendiculares actúan sobre un objeto: |F₁| = 8 N y |F₂| = 15 N. " +
            "Calcula el módulo de la fuerza resultante aplicando el Teorema de Pitágoras."),
        new VectorScenario("PERPENDICULAR", 15, 20, 25, "N",
            "Dos fuerzas perpendiculares actúan sobre un cuerpo: |F₁| = 15 N y |F₂| = 20 N. " +
            "Aplica el Teorema de Pitágoras para calcular el módulo de la resultante."),
        new VectorScenario("PERPENDICULAR", 7, 24, 25, "m/s",
            "Un avión vuela a 7 m/s con respecto al aire. El viento sopla en dirección " +
            "perpendicular a 24 m/s. Calcula el módulo de la velocidad resultante del avión " +
            "respecto al suelo."),
        new VectorScenario("PERPENDICULAR", 10, 24, 26, "N",
            "Dos fuerzas perpendiculares actúan sobre un punto: |F₁| = 10 N y |F₂| = 24 N. " +
            "Aplica el Teorema de Pitágoras para calcular el módulo de la resultante."),
        new VectorScenario("PERPENDICULAR", 20, 21, 29, "N",
            "Dos fuerzas perpendiculares actúan sobre un cuerpo: |F₁| = 20 N y |F₂| = 21 N. " +
            "Aplica el Teorema de Pitágoras para calcular el módulo de la fuerza resultante.")
    );

    // =========================================================================
    // Escenarios de SIGNIFICANT_FIGURES
    // Todos los valores son correctos y sin ambigüedad según las reglas estándar.
    // =========================================================================

    private record SigFigScenario(
        String numberDisplay,  // tal como aparece al alumno
        int sigFigs,
        String explanation
    ) {}

    private static final List<SigFigScenario> SIG_FIG_SCENARIOS = List.of(

        new SigFigScenario("0,0045", 2,
            "Aplicamos las <strong>reglas de cifras significativas</strong> a <strong>0,0045</strong>:\n\n" +
            "<em>Regla:</em> los ceros a la izquierda del primer dígito no nulo " +
            "<strong>nunca son significativos</strong>.\n\n" +
            "Desglose: <code>0,00</code> → no significativos; <code>45</code> → significativos\n\n" +
            "\\[0{,}\\underbrace{00}_{\\text{no sig.}}\\underbrace{45}_{2\\text{ sig.}}\\]\n\n" +
            "∴ <strong>0,0045</strong> tiene <strong>2 cifras significativas</strong>."),

        new SigFigScenario("0,00230", 3,
            "Aplicamos las <strong>reglas de cifras significativas</strong> a <strong>0,00230</strong>:\n\n" +
            "<ul><li>Los ceros iniciales (0,00) <strong>no son significativos</strong>.</li>" +
            "<li>El dígito 2 es significativo (dígito no nulo).</li>" +
            "<li>El dígito 3 es significativo (dígito no nulo).</li>" +
            "<li>El cero final después del 3 <strong>sí es significativo</strong> " +
            "(cero final a la derecha del punto decimal).</li></ul>\n\n" +
            "∴ <strong>0,00230</strong> tiene <strong>3 cifras significativas</strong>: 2, 3, 0."),

        new SigFigScenario("1,050", 4,
            "Aplicamos las <strong>reglas de cifras significativas</strong> a <strong>1,050</strong>:\n\n" +
            "<ul><li>1 → significativo (dígito no nulo).</li>" +
            "<li>0 → <strong>significativo</strong> (cero intercalado entre dos dígitos no nulos).</li>" +
            "<li>5 → significativo (dígito no nulo).</li>" +
            "<li>0 final → <strong>significativo</strong> (cero final con parte decimal).</li></ul>\n\n" +
            "∴ <strong>1,050</strong> tiene <strong>4 cifras significativas</strong>: 1, 0, 5, 0."),

        new SigFigScenario("3,040", 4,
            "Aplicamos las <strong>reglas de cifras significativas</strong> a <strong>3,040</strong>:\n\n" +
            "<ul><li>3 → significativo.</li>" +
            "<li>0 intercalado (entre 3 y 4) → <strong>significativo</strong>.</li>" +
            "<li>4 → significativo.</li>" +
            "<li>0 final con parte decimal → <strong>significativo</strong>.</li></ul>\n\n" +
            "∴ <strong>3,040</strong> tiene <strong>4 cifras significativas</strong>: 3, 0, 4, 0."),

        new SigFigScenario("100,0", 4,
            "Aplicamos las <strong>reglas de cifras significativas</strong> a <strong>100,0</strong>:\n\n" +
            "La presencia del punto decimal hace que <em>todos</em> los ceros sean significativos:\n\n" +
            "<ul><li>1 → significativo.</li>" +
            "<li>0 (decenas) → significativo (cero final con punto decimal).</li>" +
            "<li>0 (unidades) → significativo.</li>" +
            "<li>0 (décimas) → significativo.</li></ul>\n\n" +
            "∴ <strong>100,0</strong> tiene <strong>4 cifras significativas</strong>."),

        new SigFigScenario("0,1010", 4,
            "Aplicamos las <strong>reglas de cifras significativas</strong> a <strong>0,1010</strong>:\n\n" +
            "<ul><li>0 inicial → no significativo.</li>" +
            "<li>1 → significativo (primer dígito no nulo).</li>" +
            "<li>0 intercalado (entre los dos 1) → <strong>significativo</strong>.</li>" +
            "<li>1 → significativo.</li>" +
            "<li>0 final con decimal → <strong>significativo</strong>.</li></ul>\n\n" +
            "∴ <strong>0,1010</strong> tiene <strong>4 cifras significativas</strong>: 1, 0, 1, 0."),

        new SigFigScenario("2,500", 4,
            "Aplicamos las <strong>reglas de cifras significativas</strong> a <strong>2,500</strong>:\n\n" +
            "<ul><li>2 → significativo.</li>" +
            "<li>5 → significativo.</li>" +
            "<li>0 → significativo (cero final con parte decimal).</li>" +
            "<li>0 → significativo (cero final con parte decimal).</li></ul>\n\n" +
            "Los ceros finales después del punto decimal siempre son significativos " +
            "porque indican que la medida es precisa hasta esas décimas.\n\n" +
            "∴ <strong>2,500</strong> tiene <strong>4 cifras significativas</strong>."),

        new SigFigScenario("1,002", 4,
            "Aplicamos las <strong>reglas de cifras significativas</strong> a <strong>1,002</strong>:\n\n" +
            "<ul><li>1 → significativo.</li>" +
            "<li>0 intercalado (entre 1 y 0) → <strong>significativo</strong>.</li>" +
            "<li>0 intercalado (entre los dos ceros) → <strong>significativo</strong>.</li>" +
            "<li>2 → significativo.</li></ul>\n\n" +
            "Regla clave: los ceros encerrados entre dígitos no nulos (ceros intercalados) " +
            "son <strong>siempre significativos</strong>.\n\n" +
            "∴ <strong>1,002</strong> tiene <strong>4 cifras significativas</strong>."),

        new SigFigScenario("0,0305", 3,
            "Aplicamos las <strong>reglas de cifras significativas</strong> a <strong>0,0305</strong>:\n\n" +
            "<ul><li>0,0 iniciales → no significativos (ceros a la izquierda).</li>" +
            "<li>3 → significativo (primer dígito no nulo).</li>" +
            "<li>0 intercalado (entre 3 y 5) → <strong>significativo</strong>.</li>" +
            "<li>5 → significativo.</li></ul>\n\n" +
            "∴ <strong>0,0305</strong> tiene <strong>3 cifras significativas</strong>: 3, 0, 5."),

        new SigFigScenario("7,0000", 5,
            "Aplicamos las <strong>reglas de cifras significativas</strong> a <strong>7,0000</strong>:\n\n" +
            "<ul><li>7 → significativo.</li>" +
            "<li>0 (décimas) → significativo (cero final con decimal).</li>" +
            "<li>0 (centésimas) → significativo.</li>" +
            "<li>0 (milésimas) → significativo.</li>" +
            "<li>0 (diezmilesimas) → significativo.</li></ul>\n\n" +
            "Escribir 7,0000 indica que la medida tiene precisión hasta la diezmilesima.\n\n" +
            "∴ <strong>7,0000</strong> tiene <strong>5 cifras significativas</strong>."),

        new SigFigScenario("2,30 × 10⁻³", 3,
            "En la <strong>notación científica</strong> \\(a \\times 10^n\\), " +
            "todos los dígitos de la <strong>mantisa</strong> son significativos.\n\n" +
            "Para <strong>2,30 × 10⁻³</strong>, la mantisa es <strong>2,30</strong>:\n\n" +
            "<ul><li>2 → significativo (dígito no nulo).</li>" +
            "<li>3 → significativo (dígito no nulo).</li>" +
            "<li>0 final → <strong>significativo</strong> (cero final con decimal).</li></ul>\n\n" +
            "La potencia de 10 (×10⁻³) solo indica el orden de magnitud; no añade " +
            "ni quita cifras significativas.\n\n" +
            "∴ <strong>2,30 × 10⁻³</strong> tiene <strong>3 cifras significativas</strong>."),

        new SigFigScenario("5,30 × 10⁴", 3,
            "En la <strong>notación científica</strong> \\(a \\times 10^n\\), " +
            "solo importa la mantisa para contar cifras significativas.\n\n" +
            "Para <strong>5,30 × 10⁴</strong>, la mantisa es <strong>5,30</strong>:\n\n" +
            "<ul><li>5 → significativo.</li>" +
            "<li>3 → significativo.</li>" +
            "<li>0 final → <strong>significativo</strong> (cero final con decimal).</li></ul>\n\n" +
            "∴ <strong>5,30 × 10⁴</strong> tiene <strong>3 cifras significativas</strong>."),

        new SigFigScenario("9,81 × 10²", 3,
            "Para <strong>9,81 × 10²</strong> (la aceleración de la gravedad " +
            "en notación científica), la mantisa es <strong>9,81</strong>:\n\n" +
            "<ul><li>9 → significativo.</li>" +
            "<li>8 → significativo.</li>" +
            "<li>1 → significativo.</li></ul>\n\n" +
            "La potencia ×10² situa el valor en el orden de las centenas " +
            "pero no afecta al recuento de cifras significativas.\n\n" +
            "∴ <strong>9,81 × 10²</strong> tiene <strong>3 cifras significativas</strong>."),

        new SigFigScenario("1,2 × 10⁻³", 2,
            "Para <strong>1,2 × 10⁻³</strong>, la mantisa es <strong>1,2</strong>:\n\n" +
            "<ul><li>1 → significativo.</li>" +
            "<li>2 → significativo.</li></ul>\n\n" +
            "No hay ceros finales ni ceros intercalados en la mantisa.\n\n" +
            "∴ <strong>1,2 × 10⁻³</strong> tiene <strong>2 cifras significativas</strong>."),

        new SigFigScenario("4,050 × 10⁵", 4,
            "Para <strong>4,050 × 10⁵</strong>, la mantisa es <strong>4,050</strong>:\n\n" +
            "<ul><li>4 → significativo.</li>" +
            "<li>0 intercalado (entre 4 y 5) → <strong>significativo</strong>.</li>" +
            "<li>5 → significativo.</li>" +
            "<li>0 final con decimal → <strong>significativo</strong>.</li></ul>\n\n" +
            "∴ <strong>4,050 × 10⁵</strong> tiene <strong>4 cifras significativas</strong>.")
    );

    // =========================================================================
    // API pública
    // =========================================================================

    public FourthEsoScientificActivityExercise generateAndSave() {
        FourthEsoScientificActivityExercise ex = new FourthEsoScientificActivityExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);

        // Distribución: 33 % SCALAR_VECTORIAL, 34 % VECTOR_MATH, 33 % SIGNIFICANT_FIGURES
        int roll = random.nextInt(9);
        if (roll < 3) {
            buildScalarVectorial(ex);
        } else if (roll < 6) {
            buildVectorMath(ex);
        } else {
            buildSignificantFigures(ex);
        }

        log.debug("4ESO BL1 generado: type={} mode={}", ex.getActivityType(), ex.getExerciseMode());
        return repository.save(ex);
    }

    public FourthEsoScientificActivityExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ejercicio 4ESO BL1 no encontrado: " + id));
    }

    // =========================================================================
    // Constructores internos — SCALAR_VECTORIAL
    // =========================================================================

    private void buildScalarVectorial(FourthEsoScientificActivityExercise ex) {
        ex.setActivityType(FourthEsoActivityType.SCALAR_VECTORIAL);
        ex.setExerciseMode("MULTIPLE_CHOICE");

        McQuestion q = SCALAR_VECTORIAL_MC.get(random.nextInt(SCALAR_VECTORIAL_MC.size()));
        ex.setStatement(q.statement());
        ex.setOption0(q.opt0());
        ex.setOption1(q.opt1());
        ex.setOption2(q.opt2());
        ex.setOption3(q.opt3());
        ex.setCorrectIndex(q.correct());
        ex.setExplanation(q.explanation());
    }

    // =========================================================================
    // Constructores internos — VECTOR_MATH
    // =========================================================================

    private void buildVectorMath(FourthEsoScientificActivityExercise ex) {
        ex.setActivityType(FourthEsoActivityType.VECTOR_MATH);
        ex.setExerciseMode("NUMERICAL");
        ex.setUnknownVariable("modulo_resultante");
        ex.setTolerancePercent(2.0);

        // Distribución: 30 % SAME_SAME, 30 % SAME_OPPOSITE, 40 % PERPENDICULAR
        int roll = random.nextInt(10);
        if (roll < 3) {
            buildSameSame(ex);
        } else if (roll < 6) {
            buildSameOpposite(ex);
        } else {
            buildPerpendicular(ex);
        }
    }

    private void buildSameSame(FourthEsoScientificActivityExercise ex) {
        VectorScenario sc = SAME_SAME_SCENARIOS.get(random.nextInt(SAME_SAME_SCENARIOS.size()));
        ex.setStatement(sc.statement());
        ex.setCorrectAnswerValue(sc.result());
        ex.setCorrectAnswerDisplay(fmtNum(sc.result()) + " " + sc.unit());
        ex.setAnswerUnit(sc.unit());
        ex.setExplanation(
            "Los vectores tienen la <strong>misma dirección y el mismo sentido</strong>. " +
            "Los módulos se suman algebraicamente:\n\n" +
            "\\[R = A + B\\]\n\n" +
            "Sustituyendo \\(A = " + fmtNum(sc.vecA()) + "\\,\\text{" + sc.unit() + "}\\) " +
            "y \\(B = " + fmtNum(sc.vecB()) + "\\,\\text{" + sc.unit() + "}\\):\n\n" +
            "\\[R = " + fmtNum(sc.vecA()) + " + " + fmtNum(sc.vecB()) +
            " = " + fmtNum(sc.result()) + "\\,\\text{" + sc.unit() + "}\\]\n\n" +
            "∴  R⃗ = " + fmtNum(sc.result()) + " " + sc.unit()
        );
    }

    private void buildSameOpposite(FourthEsoScientificActivityExercise ex) {
        VectorScenario sc = SAME_OPPOSITE_SCENARIOS.get(random.nextInt(SAME_OPPOSITE_SCENARIOS.size()));
        ex.setStatement(sc.statement());
        ex.setCorrectAnswerValue(sc.result());
        ex.setCorrectAnswerDisplay(fmtNum(sc.result()) + " " + sc.unit());
        ex.setAnswerUnit(sc.unit());
        ex.setExplanation(
            "Los vectores tienen la <strong>misma dirección pero sentidos opuestos</strong>. " +
            "El módulo de la resultante es la diferencia entre los módulos:\n\n" +
            "\\[R = |A - B|\\]\n\n" +
            "Sustituyendo \\(A = " + fmtNum(sc.vecA()) + "\\,\\text{" + sc.unit() + "}\\) " +
            "y \\(B = " + fmtNum(sc.vecB()) + "\\,\\text{" + sc.unit() + "}\\):\n\n" +
            "\\[R = |" + fmtNum(sc.vecA()) + " - " + fmtNum(sc.vecB()) + "| = " +
            fmtNum(sc.result()) + "\\,\\text{" + sc.unit() + "}\\]\n\n" +
            "La dirección del vector resultante coincide con la del vector de mayor módulo " +
            "(" + fmtNum(sc.vecA()) + " " + sc.unit() + ").\n\n" +
            "∴  R⃗ = " + fmtNum(sc.result()) + " " + sc.unit()
        );
    }

    private void buildPerpendicular(FourthEsoScientificActivityExercise ex) {
        VectorScenario sc = PERPENDICULAR_SCENARIOS.get(random.nextInt(PERPENDICULAR_SCENARIOS.size()));
        ex.setStatement(sc.statement());
        ex.setCorrectAnswerValue(sc.result());
        ex.setCorrectAnswerDisplay(fmtNum(sc.result()) + " " + sc.unit());
        ex.setAnswerUnit(sc.unit());

        double a2 = sc.vecA() * sc.vecA();
        double b2 = sc.vecB() * sc.vecB();
        double r2 = a2 + b2;

        ex.setExplanation(
            "Para dos vectores <strong>perpendiculares</strong> (90°), el módulo del " +
            "vector resultante se calcula con el <strong>Teorema de Pitágoras</strong>:\n\n" +
            "\\[R = \\sqrt{A^2 + B^2}\\]\n\n" +
            "Sustituyendo \\(A = " + fmtNum(sc.vecA()) + "\\,\\text{" + sc.unit() + "}\\) " +
            "y \\(B = " + fmtNum(sc.vecB()) + "\\,\\text{" + sc.unit() + "}\\):\n\n" +
            "\\[R = \\sqrt{(" + fmtNum(sc.vecA()) + ")^2 + (" + fmtNum(sc.vecB()) + ")^2} " +
            "= \\sqrt{" + fmtNum(a2) + " + " + fmtNum(b2) + "} " +
            "= \\sqrt{" + fmtNum(r2) + "} = " + fmtNum(sc.result()) + "\\,\\text{" + sc.unit() + "}\\]\n\n" +
            "∴  R⃗ = " + fmtNum(sc.result()) + " " + sc.unit()
        );
    }

    // =========================================================================
    // Constructores internos — SIGNIFICANT_FIGURES
    // =========================================================================

    private void buildSignificantFigures(FourthEsoScientificActivityExercise ex) {
        ex.setActivityType(FourthEsoActivityType.SIGNIFICANT_FIGURES);
        ex.setExerciseMode("NUMERICAL");
        ex.setUnknownVariable("cifras_significativas");
        ex.setAnswerUnit("");
        ex.setTolerancePercent(0.1);

        SigFigScenario sc = SIG_FIG_SCENARIOS.get(random.nextInt(SIG_FIG_SCENARIOS.size()));
        ex.setStatement("Determina el número de cifras significativas del siguiente número: "
            + sc.numberDisplay());
        ex.setCorrectAnswerValue((double) sc.sigFigs());
        ex.setCorrectAnswerDisplay(String.valueOf(sc.sigFigs()));
        ex.setExplanation(sc.explanation());
    }

    // =========================================================================
    // Utilidades de formato numérico
    // =========================================================================

    private String fmtNum(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return new BigDecimal(Double.toString(value))
            .setScale(2, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
            .replace(".", ",");
    }
}
