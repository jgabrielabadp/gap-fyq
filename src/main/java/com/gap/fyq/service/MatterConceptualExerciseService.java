package com.gap.fyq.service;

import com.gap.fyq.model.matter.MatterConceptualExercise;
import com.gap.fyq.model.matter.MatterConceptualVariant;
import com.gap.fyq.repository.MatterConceptualExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MatterConceptualExerciseService {

    private final MatterConceptualExerciseRepository repository;
    private final Random random = new Random();

    // ── Opciones fijas para ejercicios de clasificación ────────────────────
    private static final String CLASIF_0 = "Sustancia pura — Elemento";
    private static final String CLASIF_1 = "Sustancia pura — Compuesto";
    private static final String CLASIF_2 = "Mezcla homogénea (disolución o aleación)";
    private static final String CLASIF_3 = "Mezcla heterogénea (suspensión o coloide)";

    // ── Registros internos ─────────────────────────────────────────────────
    private record QuestionDef(
            String statement,
            String opt0, String opt1, String opt2, String opt3,
            int correct,
            String explanation) {}

    private record ClassificationDef(
            String material,
            int correct,
            String explanation) {}

    // ── Banco de preguntas de opción múltiple (12) ─────────────────────────
    private static final List<QuestionDef> QUESTIONS = List.of(

        new QuestionDef(
            "Según el modelo cinético-molecular, ¿qué caracteriza a las partículas de un gas?",
            "Están muy unidas y vibran en posiciones fijas",
            "Están próximas entre sí y se deslizan unas sobre otras",
            "Están muy separadas y se mueven a gran velocidad en todas direcciones",
            "Permanecen estáticas salvo cuando se les aplica calor",
            2,
            "En el modelo cinético-molecular, las partículas de un gas tienen mucha energía cinética: " +
            "están muy separadas entre sí (por eso los gases son compresibles) y se mueven de forma " +
            "rápida y desordenada en todas las direcciones. Esto explica que los gases no tengan forma " +
            "propia y que llenen todo el volumen del recipiente que los contiene."
        ),

        new QuestionDef(
            "¿En qué estado de la materia tienen las partículas mayor energía cinética media?",
            "Sólido",
            "Líquido",
            "Gas",
            "La energía cinética es igual en todos los estados a la misma temperatura",
            2,
            "La energía cinética media de las partículas aumenta con la temperatura y es máxima en el " +
            "estado gaseoso. En los sólidos, las partículas solo vibran alrededor de posiciones fijas; " +
            "en los líquidos se mueven con más libertad; y en los gases se desplazan rápidamente sin " +
            "restricciones. Por eso los gases ejercen mayor presión y tienden a expandirse."
        ),

        new QuestionDef(
            "¿Cuál de los siguientes cambios de estado libera energía al entorno (proceso exotérmico)?",
            "Fusión (sólido → líquido)",
            "Sublimación (sólido → gas)",
            "Vaporización (líquido → gas)",
            "Solidificación (líquido → sólido)",
            3,
            "Durante la solidificación, las partículas del líquido se ordenan formando la red rígida " +
            "del sólido y liberan energía en forma de calor al entorno (proceso exotérmico). " +
            "También son exotérmicos la condensación y la deposición (sublimación inversa). " +
            "Por el contrario, fusión, vaporización y sublimación son endotérmicos: absorben calor " +
            "del entorno para vencer las fuerzas de cohesión entre las partículas."
        ),

        new QuestionDef(
            "¿Qué ocurre con la temperatura de una sustancia pura mientras se está fundiendo?",
            "Aumenta progresivamente hasta que todo el sólido se convierte en líquido",
            "Disminuye ligeramente durante la fusión",
            "Permanece constante mientras coexisten las fases sólida y líquida",
            "Varía de forma irregular según la cantidad de sustancia",
            2,
            "Durante la fusión de una sustancia pura, toda la energía suministrada se invierte en " +
            "romper las fuerzas de cohesión de la red sólida (calor latente de fusión) sin que " +
            "aumente la temperatura. Ésta permanece constante en el punto de fusión mientras " +
            "coexisten sólido y líquido. Las mezclas, en cambio, se funden en un intervalo de " +
            "temperatura, lo que sirve para distinguirlas experimentalmente de las sustancias puras."
        ),

        new QuestionDef(
            "¿Cuál es la diferencia principal entre evaporación y ebullición?",
            "La evaporación ocurre solo en la superficie a cualquier temperatura; " +
                "la ebullición ocurre en toda la masa a una temperatura fija",
            "La evaporación absorbe calor y la ebullición lo libera",
            "La ebullición es siempre más lenta que la evaporación",
            "Son el mismo proceso con distinto nombre",
            0,
            "La evaporación es un proceso lento que ocurre en la superficie del líquido a cualquier " +
            "temperatura. La ebullición ocurre cuando se alcanza la temperatura de ebullición " +
            "característica de cada sustancia (100 °C para el agua a 1 atm), se produce en toda la " +
            "masa del líquido y genera burbujas de vapor en su interior. Ambos son procesos de " +
            "vaporización, pero en condiciones distintas."
        ),

        new QuestionDef(
            "¿Cuál de estas afirmaciones sobre las sustancias puras es correcta?",
            "Siempre están formadas por un único tipo de átomo",
            "Tienen composición química fija y propiedades características constantes",
            "No pueden descomponerse por ningún medio físico ni químico",
            "El agua con azúcar es un ejemplo de sustancia pura",
            1,
            "Una sustancia pura tiene composición química definida y constante, y unas propiedades " +
            "físicas características (punto de fusión, de ebullición, densidad...) que son siempre " +
            "las mismas. Pueden ser elementos —un solo tipo de átomo, como el oro (Au)— o compuestos " +
            "—átomos de distintos elementos enlazados químicamente, como el agua (H₂O)—. " +
            "El agua con azúcar es una mezcla de composición variable."
        ),

        new QuestionDef(
            "Para separar el alcohol del agua, ¿qué técnica de separación es la más adecuada?",
            "Filtración",
            "Decantación",
            "Destilación",
            "Imantación",
            2,
            "El alcohol y el agua forman una mezcla homogénea (disolución). La destilación aprovecha " +
            "la diferencia en sus puntos de ebullición (etanol ≈ 78 °C, agua = 100 °C): al calentar, " +
            "el alcohol se evapora antes y se recoge al condensarse en el refrigerante. " +
            "La filtración y la decantación solo separan mezclas heterogéneas; la imantación se " +
            "usa cuando uno de los componentes es magnético."
        ),

        new QuestionDef(
            "¿Qué método de separación usarías para obtener agua limpia de una mezcla de agua y arena?",
            "Destilación",
            "Filtración",
            "Cristalización",
            "Cromatografía",
            1,
            "La arena no se disuelve en el agua y forma una mezcla heterogénea. La filtración " +
            "hace pasar la mezcla por un papel de filtro: la arena queda retenida (residuo) y el " +
            "agua pasa al filtrado. La destilación sería innecesariamente compleja para este caso; " +
            "la cristalización se usa para recuperar un sólido disuelto; la cromatografía separa " +
            "sustancias con propiedades similares."
        ),

        new QuestionDef(
            "La leche es un ejemplo de:",
            "Sustancia pura compuesta",
            "Disolución verdadera (mezcla homogénea a escala molecular)",
            "Coloide (mezcla coloidal)",
            "Mezcla heterogénea con fases visibles a simple vista",
            2,
            "La leche es una emulsión coloidal: contiene gotitas de grasa y proteínas dispersas en " +
            "agua con un tamaño de partícula entre 1 nm y 1 µm. Aunque parece homogénea a simple " +
            "vista, presenta el efecto Tyndall (dispersa un haz de luz), confirmando su naturaleza " +
            "coloidal. En una disolución verdadera (p. ej. agua con sal), las partículas tienen " +
            "tamaño inferior a 1 nm y no dispersan la luz."
        ),

        new QuestionDef(
            "¿Para qué sirve la técnica de cromatografía?",
            "Separar mezclas heterogéneas sólido-líquido mediante un filtro",
            "Separar líquidos miscibles aprovechando sus distintos puntos de ebullición",
            "Separar y analizar los componentes de una mezcla según su distinta afinidad " +
                "por una fase estacionaria",
            "Separar sólidos de distinto tamaño mediante tamizado",
            2,
            "La cromatografía separa los componentes de una mezcla en función de su distinta " +
            "afinidad por una fase estacionaria (papel, gel de sílice) y una fase móvil (disolvente). " +
            "Cada componente avanza a diferente velocidad por el soporte y queda en una posición " +
            "distinta (mancha diferenciada). Es muy útil para separar e identificar pigmentos, " +
            "tintas, aminoácidos o principios activos."
        ),

        new QuestionDef(
            "Según el modelo cinético-molecular, ¿a qué se debe la presión que ejerce " +
                "un gas sobre las paredes del recipiente?",
            "Al peso de las partículas sobre la base del recipiente",
            "A los continuos choques de las partículas contra las paredes",
            "A las fuerzas de atracción entre las partículas y las paredes",
            "Al calor que desprenden las partículas al moverse",
            1,
            "La presión de un gas se debe a los choques continuos de sus partículas contra las " +
            "paredes del recipiente. Cada choque ejerce una pequeña fuerza; la suma de estas fuerzas " +
            "por unidad de área es la presión. Si aumentamos la temperatura (partículas más rápidas) " +
            "o reducimos el volumen (más choques por segundo), la presión aumenta. Esto es " +
            "consistente con las leyes de los gases ideales."
        ),

        new QuestionDef(
            "El bronce es una aleación de cobre y estaño. ¿Cómo se clasifica el bronce?",
            "Sustancia pura — Compuesto",
            "Mezcla heterogénea",
            "Mezcla homogénea",
            "Sustancia pura — Elemento",
            2,
            "El bronce es una aleación metálica, un tipo de mezcla homogénea sólida. Los átomos de " +
            "cobre y estaño se distribuyen de forma uniforme en la red metálica, dando propiedades " +
            "uniformes en toda su masa. Las aleaciones tienen composición variable (distintos " +
            "porcentajes de cada metal), lo que las diferencia de los compuestos puros. Otras " +
            "aleaciones comunes son el latón (Cu + Zn) y el acero (Fe + C)."
        )
    );

    // ── Banco de sistemas materiales para clasificación (10) ───────────────
    private static final List<ClassificationDef> CLASSIFICATIONS = List.of(

        new ClassificationDef(
            "agua pura (H₂O)",
            1,
            "El agua (H₂O) es una sustancia pura compuesta: cada molécula tiene exactamente " +
            "dos átomos de hidrógeno enlazados covalentemente a uno de oxígeno. Su composición " +
            "es siempre la misma (88,9 % O y 11,1 % H en masa) y sus propiedades son características " +
            "y constantes: funde a 0 °C y hierve a 100 °C a presión normal."
        ),

        new ClassificationDef(
            "hierro (Fe)",
            0,
            "El hierro (Fe) es una sustancia pura elemental: solo contiene átomos de hierro " +
            "(número atómico Z = 26). Los elementos aparecen en la tabla periódica y no pueden " +
            "descomponerse en sustancias más simples por medios químicos ordinarios. " +
            "Sus propiedades (densidad 7,87 g/cm³, fusión a 1538 °C) son características y constantes."
        ),

        new ClassificationDef(
            "agua con sal",
            2,
            "El agua con sal (disolución salina) es una mezcla homogénea: los iones de Na⁺ y Cl⁻ " +
            "se distribuyen uniformemente entre las moléculas de agua. No se distinguen sus " +
            "componentes a simple vista ni con microscopio. Su composición es variable según la " +
            "cantidad de sal añadida y la sal puede recuperarse por evaporación o cristalización."
        ),

        new ClassificationDef(
            "granito",
            3,
            "El granito es una roca plutónica formada por una mezcla heterogénea de minerales: " +
            "cuarzo (gris-transparente), feldespato (rosa o blanco) y mica (negra o plateada). " +
            "A simple vista se distinguen las distintas fases por su diferente color y textura. " +
            "Su composición varía de un yacimiento a otro."
        ),

        new ClassificationDef(
            "aire seco",
            2,
            "El aire seco es una mezcla homogénea de gases: nitrógeno (~78 %), oxígeno (~21 %), " +
            "argón (~0,9 %) y dióxido de carbono (~0,04 %), entre otros. Sus componentes se " +
            "mezclan a escala molecular dando lugar a una mezcla de composición uniforme en " +
            "cualquier punto. La composición del aire puede variar ligeramente según la altitud."
        ),

        new ClassificationDef(
            "dióxido de carbono (CO₂)",
            1,
            "El dióxido de carbono (CO₂) es una sustancia pura compuesta: cada molécula tiene " +
            "exactamente un átomo de carbono enlazado a dos átomos de oxígeno. Su composición " +
            "es siempre la misma (27,3 % C y 72,7 % O en masa) y tiene propiedades características " +
            "constantes (sublima a -78,5 °C a presión normal)."
        ),

        new ClassificationDef(
            "sangre",
            3,
            "La sangre es una mezcla heterogénea coloidal: el plasma (líquido acuoso) contiene " +
            "glóbulos rojos, glóbulos blancos y plaquetas en suspensión. Al centrifugarla, sus " +
            "componentes se separan en capas (hematocrito), confirmando su naturaleza heterogénea. " +
            "Los glóbulos rojos tienen un tamaño de unos 7-8 µm, muy superior al de partículas " +
            "de una disolución verdadera."
        ),

        new ClassificationDef(
            "vinagre",
            2,
            "El vinagre es una mezcla homogénea: contiene agua y ácido acético (CH₃COOH) como " +
            "componentes principales, con pequeñas cantidades de otras sustancias disueltas. " +
            "Es una disolución de aspecto uniforme y composición variable según el tipo " +
            "(habitualmente entre 4 % y 8 % de ácido acético en masa)."
        ),

        new ClassificationDef(
            "oxígeno (O₂)",
            0,
            "El oxígeno (O₂) es una sustancia pura elemental: aunque sus átomos forman moléculas " +
            "diatómicas (O₂), solo contiene un tipo de elemento (oxígeno, Z = 8). Los elementos " +
            "son sustancias puras que no pueden descomponerse en sustancias más simples por medios " +
            "químicos ordinarios. El oxígeno aparece en la tabla periódica con símbolo O."
        ),

        new ClassificationDef(
            "humo",
            3,
            "El humo es una mezcla heterogénea de tipo aerosol: contiene partículas sólidas de " +
            "hollín y cenizas dispersas en gases calientes. Sus partículas son lo bastante grandes " +
            "para dispersar la luz visible (efecto Tyndall, que explica su aspecto visible) y " +
            "precipitan lentamente por gravedad. Con el tiempo, las partículas más pesadas sedimentan."
        )
    );

    // ── API pública ────────────────────────────────────────────────────────

    public MatterConceptualExercise generateAndSave() {
        MatterConceptualExercise e = new MatterConceptualExercise();
        e.setCourse("2ESO");
        e.setBlock("BL2C");
        if (random.nextBoolean()) {
            buildMultipleChoice(e);
        } else {
            buildClassification(e);
        }
        return repository.save(e);
    }

    public MatterConceptualExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ejercicio conceptual no encontrado: " + id));
    }

    // ── Constructores internos ─────────────────────────────────────────────

    private void buildMultipleChoice(MatterConceptualExercise e) {
        QuestionDef q = QUESTIONS.get(random.nextInt(QUESTIONS.size()));
        e.setVariant(MatterConceptualVariant.MULTIPLE_CHOICE);
        e.setStatement(q.statement());
        e.setOption0(q.opt0());
        e.setOption1(q.opt1());
        e.setOption2(q.opt2());
        e.setOption3(q.opt3());
        e.setCorrectIndex(q.correct());
        e.setExplanation(q.explanation());
    }

    private void buildClassification(MatterConceptualExercise e) {
        ClassificationDef c = CLASSIFICATIONS.get(random.nextInt(CLASSIFICATIONS.size()));
        e.setVariant(MatterConceptualVariant.CLASSIFICATION);
        e.setStatement("Clasifica el siguiente sistema material: " + c.material());
        e.setOption0(CLASIF_0);
        e.setOption1(CLASIF_1);
        e.setOption2(CLASIF_2);
        e.setOption3(CLASIF_3);
        e.setCorrectIndex(c.correct());
        e.setExplanation(c.explanation());
    }
}
