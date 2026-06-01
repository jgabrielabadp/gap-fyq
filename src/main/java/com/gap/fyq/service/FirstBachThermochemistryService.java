package com.gap.fyq.service;

import com.gap.fyq.model.firstbach.thermochemistry.FirstBachThermochemistryExercise;
import com.gap.fyq.model.firstbach.thermochemistry.ThermochemistryType;
import com.gap.fyq.repository.FirstBachThermochemistryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirstBachThermochemistryService {

    private final FirstBachThermochemistryRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "1BACH";
    private static final String BLOCK  = "BL4";

    // =========================================================================
    // DATA RECORDS
    // =========================================================================

    /** Una sustancia en la ecuación con su ΔH°f y coeficiente estequiométrico. */
    private record ReactionTerm(
        String formulaAscii,  // "Fe2O3", "H2O"
        String state,         // "g", "l", "s"
        int    coeff,
        double deltaHf        // kJ/mol (0 para elementos en estado estándar)
    ) {}

    private record ReactionScenario(
        String name,
        String equationDisp,      // con Unicode y espacios para mostrar al alumno
        List<ReactionTerm> reactants,
        List<ReactionTerm> products
    ) {}

    private record GibbsScenario(
        String context,      // descripción del proceso
        double deltaHkJ,     // kJ   (puede ser negativo)
        double deltaSJK,     // J/K  (puede ser negativo)
        double tempCelsius   // °C
    ) {}

    private record ConceptualQuestion(
        String question,
        String opt0, String opt1, String opt2, String opt3,
        int correct,
        String explanation
    ) {}

    // =========================================================================
    // REACTION_ENTHALPY — 8 reacciones (ΔH°f verificados con NIST 298 K)
    // ΔH°r = Σ[n·ΔH°f(prod)] − Σ[m·ΔH°f(react)]
    // =========================================================================

    private static final List<ReactionScenario> REACTIONS = List.of(

        // R1: CH4 + 2O2 → CO2 + 2H2O(l)  ΔH°r = -890,36 kJ/mol
        new ReactionScenario(
            "combustión del metano",
            "CH₄(g) + 2 O₂(g) → CO₂(g) + 2 H₂O(l)",
            List.of(new ReactionTerm("CH4","g",1,-74.81),
                    new ReactionTerm("O2", "g",2,  0.00)),
            List.of(new ReactionTerm("CO2","g",1,-393.51),
                    new ReactionTerm("H2O","l",2,-285.83))),

        // R2: C2H5OH(l) + 3O2 → 2CO2 + 3H2O(l)  ΔH°r = -1366,82 kJ/mol
        new ReactionScenario(
            "combustión del etanol",
            "C₂H₅OH(l) + 3 O₂(g) → 2 CO₂(g) + 3 H₂O(l)",
            List.of(new ReactionTerm("C2H5OH","l",1,-277.69),
                    new ReactionTerm("O2",    "g",3,   0.00)),
            List.of(new ReactionTerm("CO2","g",2,-393.51),
                    new ReactionTerm("H2O","l",3,-285.83))),

        // R3: N2 + 3H2 → 2NH3(g)  ΔH°r = -92,22 kJ/mol
        new ReactionScenario(
            "síntesis del amoníaco (Haber-Bosch)",
            "N₂(g) + 3 H₂(g) → 2 NH₃(g)",
            List.of(new ReactionTerm("N2","g",1,  0.00),
                    new ReactionTerm("H2","g",3,  0.00)),
            List.of(new ReactionTerm("NH3","g",2,-46.11))),

        // R4: CaCO3(s) → CaO(s) + CO2(g)  ΔH°r = +178,32 kJ/mol
        new ReactionScenario(
            "descomposición del carbonato de calcio (calcinación)",
            "CaCO₃(s) → CaO(s) + CO₂(g)",
            List.of(new ReactionTerm("CaCO3","s",1,-1206.92)),
            List.of(new ReactionTerm("CaO", "s",1, -635.09),
                    new ReactionTerm("CO2", "g",1, -393.51))),

        // R5: 2SO2 + O2 → 2SO3(g)  ΔH°r = -197,78 kJ/mol
        new ReactionScenario(
            "oxidación del SO₂ a SO₃ (proceso de contacto)",
            "2 SO₂(g) + O₂(g) → 2 SO₃(g)",
            List.of(new ReactionTerm("SO2","g",2,-296.83),
                    new ReactionTerm("O2", "g",1,   0.00)),
            List.of(new ReactionTerm("SO3","g",2,-395.72))),

        // R6: H2 + Cl2 → 2HCl(g)  ΔH°r = -184,62 kJ/mol
        new ReactionScenario(
            "síntesis del cloruro de hidrógeno",
            "H₂(g) + Cl₂(g) → 2 HCl(g)",
            List.of(new ReactionTerm("H2",  "g",1,  0.00),
                    new ReactionTerm("Cl2", "g",1,  0.00)),
            List.of(new ReactionTerm("HCl","g",2,-92.31))),

        // R7: Fe2O3(s) + 3H2 → 2Fe(s) + 3H2O(l)  ΔH°r = -33,29 kJ/mol
        new ReactionScenario(
            "reducción del óxido de hierro(III) con hidrógeno",
            "Fe₂O₃(s) + 3 H₂(g) → 2 Fe(s) + 3 H₂O(l)",
            List.of(new ReactionTerm("Fe2O3","s",1,-824.20),
                    new ReactionTerm("H2",   "g",3,   0.00)),
            List.of(new ReactionTerm("Fe",   "s",2,   0.00),
                    new ReactionTerm("H2O",  "l",3,-285.83))),

        // R8: 2NO + O2 → 2NO2(g)  ΔH°r = -114,14 kJ/mol
        new ReactionScenario(
            "oxidación del monóxido de nitrógeno a dióxido",
            "2 NO(g) + O₂(g) → 2 NO₂(g)",
            List.of(new ReactionTerm("NO", "g",2,+90.25),
                    new ReactionTerm("O2", "g",1,  0.00)),
            List.of(new ReactionTerm("NO2","g",2,+33.18)))
    );

    // =========================================================================
    // GIBBS_SPONTANEITY_CALC — 8 escenarios
    // ΔG = ΔH − T·ΔS   (ΔS en J/K → dividir entre 1000; T en °C → +273,15)
    // =========================================================================

    private static final List<GibbsScenario> GIBBS_SCENARIOS = List.of(

        // G1: ΔG = -150 − (298,15×0,200) = -209,63 kJ  → espontánea
        new GibbsScenario(
            "combustión de un hidrocarburo (proceso exotérmico con aumento de entropía)",
            -150.00, +200.0, 25.0),

        // G2: ΔG = 120 − (373,15×(−0,080)) = +149,85 kJ  → no espontánea
        new GibbsScenario(
            "síntesis de un compuesto a partir de sus elementos a 100 °C",
            +120.00, -80.0, 100.0),

        // G3: ΔG = -40 − (773,15×(−0,150)) = +75,97 kJ  → no espontánea
        new GibbsScenario(
            "reducción de un óxido metálico a alta temperatura",
            -40.00, -150.0, 500.0),

        // G4: ΔG = 60 − (573,15×0,250) = -83,29 kJ  → espontánea
        new GibbsScenario(
            "disociación de un compuesto a temperatura moderada-alta",
            +60.00, +250.0, 300.0),

        // G5: ΔG = -200 − (293,15×(−0,100)) = -170,69 kJ  → espontánea
        new GibbsScenario(
            "precipitación de una sal inorgánica a temperatura ambiente",
            -200.00, -100.0, 20.0),

        // G6: ΔG = 90 − (673,15×0,120) = +9,22 kJ  → no espontánea
        new GibbsScenario(
            "síntesis a temperatura moderada-alta con entropía favorable",
            +90.00, +120.0, 400.0),

        // G7: ΔG = -30 − (298,15×0,050) = -44,91 kJ  → espontánea
        new GibbsScenario(
            "disolución de una sal con ligero efecto endotérmico de entropía",
            -30.00, +50.0, 25.0),

        // G8: ΔG = 160 − (873,15×0,400) = -189,26 kJ  → espontánea
        new GibbsScenario(
            "descomposición de un óxido a temperatura muy alta",
            +160.00, +400.0, 600.0)
    );

    // =========================================================================
    // CONCEPTUAL_SPONTANEITY — 8 preguntas de opción múltiple
    // =========================================================================

    private static final List<ConceptualQuestion> MC_QUESTIONS = List.of(

        // C1
        new ConceptualQuestion(
            "Para una reacción con ΔH° < 0 y ΔS° > 0, ¿bajo qué condiciones de temperatura es espontánea (ΔG < 0)?",
            "Solo a temperaturas bajas",
            "Solo a temperaturas altas",
            "A cualquier temperatura",
            "Nunca es espontánea",
            2,
            "Cuando ΔH° < 0 y ΔS° > 0, la función de Gibbs vale:\n\n" +
            "\\[\\Delta G = \\underbrace{\\Delta H°}_{<\\,0} - T \\cdot \\underbrace{\\Delta S°}_{>\\,0}\\]\n\n" +
            "El primer término ya es negativo y el segundo término \\(-T\\cdot\\Delta S°\\) también es negativo " +
            "(signo negativo por signo positivo). Por tanto \\(\\Delta G < 0\\) <strong>para cualquier temperatura " +
            "\\(T > 0\\)</strong>. Esta es la combinación más favorable: la reacción es espontánea a cualquier T."),

        // C2
        new ConceptualQuestion(
            "Para una reacción con ΔH° > 0 y ΔS° < 0, ¿bajo qué condiciones de temperatura es espontánea?",
            "A cualquier temperatura",
            "Solo a temperaturas bajas",
            "Solo a temperaturas altas",
            "Nunca es espontánea",
            3,
            "Cuando ΔH° > 0 y ΔS° < 0:\n\n" +
            "\\[\\Delta G = \\underbrace{\\Delta H°}_{>\\,0} - T \\cdot \\underbrace{\\Delta S°}_{<\\,0} = " +
            "\\Delta H° + T \\cdot |\\Delta S°|\\]\n\n" +
            "Ambos sumandos son positivos para cualquier \\(T > 0\\), por lo que \\(\\Delta G > 0\\) siempre. " +
            "Esta reacción <strong>nunca es espontánea</strong> en condiciones estándar: " +
            "la entalpía y la entropía se oponen al proceso."),

        // C3
        new ConceptualQuestion(
            "Una reacción tiene ΔH° < 0 y ΔS° < 0. ¿Cuándo será espontánea?",
            "A cualquier temperatura",
            "A temperaturas bajas, cuando |ΔH°| > T·|ΔS°|",
            "A temperaturas altas, cuando T·|ΔS°| > |ΔH°|",
            "Nunca es espontánea",
            1,
            "Con ΔH° < 0 y ΔS° < 0, la ecuación de Gibbs queda:\n\n" +
            "\\[\\Delta G = \\Delta H° - T \\cdot \\Delta S° = \\Delta H° + T \\cdot |\\Delta S°|\\]\n\n" +
            "A temperatura baja, el término \\(|\\Delta H°|\\) domina y \\(\\Delta G < 0\\) " +
            "(la reacción es espontánea). A temperatura alta, el término entróPico \\(T|\\Delta S°|\\) " +
            "se vuelve mayor en magnitud que \\(|\\Delta H°|\\) y \\(\\Delta G\\) pasa a ser positivo. " +
            "La temperatura de cruce es \\(T = |\\Delta H°|/|\\Delta S°|\\)."),

        // C4
        new ConceptualQuestion(
            "Una reacción tiene ΔH° > 0 y ΔS° > 0. ¿Cuándo será espontánea?",
            "A cualquier temperatura",
            "A temperaturas bajas",
            "A temperaturas altas, cuando T·ΔS° > ΔH°",
            "Nunca es espontánea",
            2,
            "Con ΔH° > 0 y ΔS° > 0:\n\n" +
            "\\[\\Delta G = \\Delta H° - T \\cdot \\Delta S°\\]\n\n" +
            "La reacción es espontánea cuando \\(\\Delta G < 0\\), es decir cuando:\n\n" +
            "\\[T \\cdot \\Delta S° > \\Delta H° \\implies T > \\frac{\\Delta H°}{\\Delta S°}\\]\n\n" +
            "Solo a <strong>temperaturas suficientemente altas</strong> el término entróPico supera al " +
            "entálpico y la reacción se vuelve espontánea."),

        // C5
        new ConceptualQuestion(
            "¿Cuál de los siguientes procesos tiene ΔS > 0 (la entropía del sistema aumenta)?",
            "Solidificación del hierro fundido: Fe(l) → Fe(s)",
            "Sublimación del yodo sólido: I₂(s) → I₂(g)",
            "Condensación del vapor de agua: H₂O(g) → H₂O(l)",
            "Precipitación de AgCl: Ag⁺(aq) + Cl⁻(aq) → AgCl(s)",
            1,
            "La entropía aumenta cuando los sistemas pasan a estados con mayor desorden molecular:\n\n" +
            "<ul><li><strong>Sólido → Líquido → Gas</strong>: aumenta la entropía.</li>" +
            "<li><strong>Gas → Líquido → Sólido</strong>: disminuye la entropía.</li>" +
            "<li>Precipitación: dos iones dispersos (aq) forman un sólido → " +
            "\\(\\Delta S < 0\\).</li></ul>\n\n" +
            "La sublimación del yodo <strong>I₂(s) → I₂(g)</strong> convierte un sólido ordenado " +
            "en un gas de alta entropía: \\(\\Delta S \\gg 0\\)."),

        // C6
        new ConceptualQuestion(
            "¿Qué combinación de signos de ΔH° y ΔS° garantiza ΔG° < 0 a cualquier temperatura?",
            "ΔH° > 0 y ΔS° < 0",
            "ΔH° < 0 y ΔS° < 0",
            "ΔH° > 0 y ΔS° > 0",
            "ΔH° < 0 y ΔS° > 0",
            3,
            "\\[\\Delta G = \\underbrace{\\Delta H°}_{?} - T \\cdot \\underbrace{\\Delta S°}{?}\\]\n\n" +
            "Para que \\(\\Delta G < 0\\) para cualquier \\(T > 0\\) se necesita que:\n\n" +
            "<ul><li>\\(\\Delta H° < 0\\) (contribución negativa siempre).</li>" +
            "<li>\\(-T\\cdot\\Delta S° < 0\\) siempre, lo que exige \\(\\Delta S° > 0\\).</li></ul>\n\n" +
            "Con <strong>ΔH° < 0 y ΔS° > 0</strong>, ambos términos contribuyen negativamente " +
            "y \\(\\Delta G < 0\\) para cualquier temperatura positiva."),

        // C7
        new ConceptualQuestion(
            "Para una reacción con ΔH° = +40 kJ/mol y ΔS° = +160 J/(mol·K), ¿a partir de qué temperatura aproximada es espontánea?",
            "T > 100 K",
            "T > 250 K",
            "T > 500 K",
            "T > 1000 K",
            1,
            "La temperatura umbral es la que hace \\(\\Delta G = 0\\):\n\n" +
            "\\[\\Delta G = 0 \\implies \\Delta H° = T \\cdot \\Delta S°\\]\n\n" +
            "\\[T = \\frac{\\Delta H°}{\\Delta S°} = \\frac{40{,}000\\,\\text{J/mol}}" +
            "{160\\,\\text{J/(mol·K)}} = 250\\,\\text{K}\\]\n\n" +
            "Para \\(T > 250\\,\\text{K}\\) el término \\(T\\cdot\\Delta S°\\) supera a " +
            "\\(\\Delta H°\\) y la reacción es espontánea. " +
            "Nota: 250 K = −23 °C, una temperatura bastante baja."),

        // C8
        new ConceptualQuestion(
            "¿Cuál es la expresión correcta de la energía libre de Gibbs a presión y temperatura constantes?",
            "ΔG = ΔH + T·ΔS",
            "ΔG = ΔH − T·ΔS",
            "ΔG = ΔS − T·ΔH",
            "ΔG = T·(ΔH − ΔS)",
            1,
            "La función de energía libre de Gibbs se define como:\n\n" +
            "\\[G = H - T \\cdot S\\]\n\n" +
            "Para un proceso a T y P constantes, tomando variaciones:\n\n" +
            "\\[\\boxed{\\Delta G = \\Delta H - T \\cdot \\Delta S}\\]\n\n" +
            "El criterio de espontaneidad es: si \\(\\Delta G < 0\\) la reacción es espontánea; " +
            "si \\(\\Delta G > 0\\) no lo es; si \\(\\Delta G = 0\\) el sistema está en equilibrio. " +
            "Esta ecuación fue desarrollada por Josiah Willard Gibbs en 1876.")
    );

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public FirstBachThermochemistryExercise generateAndSave() {
        FirstBachThermochemistryExercise ex = new FirstBachThermochemistryExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);

        int roll = random.nextInt(3);
        if      (roll == 0) buildReactionEnthalpy(ex);
        else if (roll == 1) buildGibbsSpontaneity(ex);
        else                buildConceptualSpontaneity(ex);

        log.debug("1BACH BL4 generado: type={} mode={}",
            ex.getThermochemistryType(), ex.getExerciseMode());
        return repository.save(ex);
    }

    public FirstBachThermochemistryExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ejercicio 1BACH BL4 no encontrado: " + id));
    }

    // =========================================================================
    // CONSTRUCTOR — REACTION_ENTHALPY
    // =========================================================================

    private void buildReactionEnthalpy(FirstBachThermochemistryExercise ex) {
        ex.setThermochemistryType(ThermochemistryType.REACTION_ENTHALPY);
        ex.setExerciseMode("NUMERICAL");
        ex.setAnswerUnit("kJ/mol");
        ex.setTolerancePercent(2.0);

        ReactionScenario sc = REACTIONS.get(random.nextInt(REACTIONS.size()));

        // Calcular ΔH°r = Σ[n·ΔH°f(prod)] − Σ[m·ΔH°f(react)]
        double sumProd = sc.products().stream()
            .mapToDouble(t -> t.coeff() * t.deltaHf()).sum();
        double sumReact = sc.reactants().stream()
            .mapToDouble(t -> t.coeff() * t.deltaHf()).sum();
        double deltaHr = sumProd - sumReact;

        ex.setCorrectAnswerValue(deltaHr);
        ex.setCorrectAnswerDisplay(fmt2(deltaHr) + " kJ/mol");

        ex.setStatement(
            "Calcula la entalpía estándar de reacción (ΔH°r, en kJ/mol) a 298 K " +
            "para la reacción de " + sc.name() + " utilizando los valores de " +
            "ΔH°f de la tabla adjunta: " + sc.equationDisp());

        ex.setDataTableHtml(buildDataTable(sc));
        ex.setExplanation(buildEnthalpyExplanation(sc, sumProd, sumReact, deltaHr));
    }

    // =========================================================================
    // CONSTRUCTOR — GIBBS_SPONTANEITY_CALC
    // =========================================================================

    private void buildGibbsSpontaneity(FirstBachThermochemistryExercise ex) {
        ex.setThermochemistryType(ThermochemistryType.GIBBS_SPONTANEITY_CALC);
        ex.setExerciseMode("GIBBS_COMBINED");
        ex.setAnswerUnit("kJ");
        ex.setTolerancePercent(2.0);

        GibbsScenario sc = GIBBS_SCENARIOS.get(random.nextInt(GIBBS_SCENARIOS.size()));

        double T_K      = sc.tempCelsius() + 273.15;
        double deltaS_k = sc.deltaSJK() / 1000.0;     // J/K → kJ/K
        double deltaG   = sc.deltaHkJ() - T_K * deltaS_k;

        ex.setCorrectAnswerValue(deltaG);
        ex.setCorrectAnswerDisplay(fmt2(deltaG) + " kJ");

        int spontIndex = deltaG < -0.01 ? 0 : (deltaG > 0.01 ? 1 : 2);
        ex.setCorrectSpontaneityIndex(spontIndex);

        String deltaSSign = sc.deltaSJK() >= 0 ? "+" : "";
        String deltaHSign = sc.deltaHkJ() >= 0 ? "+" : "";

        ex.setStatement(String.format(
            "Para un proceso químico (%s) se conocen los siguientes datos: " +
            "ΔH = %s%s kJ  |  ΔS = %s%s J/K  |  T = %s °C. " +
            "Calcula ΔG (en kJ, con 2 decimales) e indica si la reacción es " +
            "espontánea, no espontánea o si está en equilibrio.",
            sc.context(),
            deltaHSign, fmt2(Math.abs(sc.deltaHkJ())),
            deltaSSign, fmt1(sc.deltaSJK()),
            fmt1(sc.tempCelsius())));

        ex.setExplanation(buildGibbsExplanation(sc, T_K, deltaS_k, deltaG, spontIndex));
    }

    // =========================================================================
    // CONSTRUCTOR — CONCEPTUAL_SPONTANEITY
    // =========================================================================

    private void buildConceptualSpontaneity(FirstBachThermochemistryExercise ex) {
        ex.setThermochemistryType(ThermochemistryType.CONCEPTUAL_SPONTANEITY);
        ex.setExerciseMode("MULTIPLE_CHOICE");

        ConceptualQuestion q = MC_QUESTIONS.get(random.nextInt(MC_QUESTIONS.size()));

        ex.setStatement(q.question());
        ex.setOption0(q.opt0());
        ex.setOption1(q.opt1());
        ex.setOption2(q.opt2());
        ex.setOption3(q.opt3());
        ex.setCorrectIndex(q.correct());
        ex.setExplanation(q.explanation());
    }

    // =========================================================================
    // EXPLICACIÓN — REACTION_ENTHALPY
    // =========================================================================

    private String buildEnthalpyExplanation(ReactionScenario sc,
                                             double sumProd, double sumReact,
                                             double deltaHr) {
        var sb = new StringBuilder();

        sb.append("<strong>Ley de Hess — fórmula general:</strong>\n\n")
          .append("\\[\\Delta H°_r = \\sum_i n_i \\cdot \\Delta H°_f(\\text{productos}) - ")
          .append("\\sum_j m_j \\cdot \\Delta H°_f(\\text{reactivos})\\]\n\n");

        // Productos
        sb.append("<strong>Término de productos:</strong>\n\n\\[");
        boolean first = true;
        for (ReactionTerm t : sc.products()) {
            if (!first) sb.append(" + ");
            if (t.coeff() > 1) sb.append(t.coeff()).append("\\times(");
            sb.append(fmtK2(t.deltaHf()));
            if (t.coeff() > 1) sb.append(")");
            first = false;
        }
        sb.append(" = ").append(fmtK2(sumProd)).append("\\,\\text{kJ/mol}\\]\n\n");

        // Reactivos
        sb.append("<strong>Término de reactivos:</strong>\n\n\\[");
        first = true;
        for (ReactionTerm t : sc.reactants()) {
            if (!first) sb.append(" + ");
            if (t.coeff() > 1) sb.append(t.coeff()).append("\\times(");
            sb.append(fmtK2(t.deltaHf()));
            if (t.coeff() > 1) sb.append(")");
            first = false;
        }
        sb.append(" = ").append(fmtK2(sumReact)).append("\\,\\text{kJ/mol}\\]\n\n");

        // Resultado
        sb.append("<strong>Entalpía de reacción:</strong>\n\n")
          .append("\\[\\Delta H°_r = ").append(fmtK2(sumProd))
          .append(" - (").append(fmtK2(sumReact)).append(") = ")
          .append(fmtK2(sumProd)).append(" - (").append(fmtK2(sumReact)).append(") = ")
          .append(fmtK2(deltaHr)).append("\\,\\text{kJ/mol}\\]\n\n");

        String thermal = deltaHr < 0
            ? "<strong>exotérmica</strong> (el sistema cede calor al entorno, ΔH°r < 0)"
            : "<strong>endotérmica</strong> (el sistema absorbe calor del entorno, ΔH°r > 0)";
        sb.append("∴ La reacción de ").append(sc.name()).append(" es ")
          .append(thermal).append(".");

        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — GIBBS_SPONTANEITY_CALC
    // =========================================================================

    private String buildGibbsExplanation(GibbsScenario sc,
                                          double T_K, double deltaS_k,
                                          double deltaG, int spontIndex) {
        String spontText = switch (spontIndex) {
            case 0  -> "<strong>espontánea</strong> (\\(\\Delta G < 0\\))";
            case 1  -> "<strong>no espontánea</strong> (\\(\\Delta G > 0\\))";
            default -> "<strong>en equilibrio</strong> (\\(\\Delta G \\approx 0\\))";
        };
        var sb = new StringBuilder();

        sb.append("<strong>Ecuación de Gibbs-Helmholtz:</strong>\n\n")
          .append("\\[\\Delta G = \\Delta H - T \\cdot \\Delta S\\]\n\n");

        sb.append("<strong>Conversión de unidades (pasos críticos):</strong>\n\n")
          .append("<ul>")
          .append("<li>\\(\\Delta H = ").append(fmtK2(sc.deltaHkJ()))
          .append("\\,\\text{kJ}\\) (ya en kJ, no requiere conversión)</li>")
          .append("<li>\\(\\Delta S = ").append(fmtK1(sc.deltaSJK()))
          .append("\\,\\text{J/K} \\div 1000 = ")
          .append(fmtK4(deltaS_k)).append("\\,\\text{kJ/K}\\) ")
          .append("&larr; <strong>dividir entre 1000 para homogeneizar unidades</strong></li>")
          .append("<li>\\(T = ").append(fmtK1(sc.tempCelsius()))
          .append("\\,°C + 273{,}15 = ").append(fmtK2(T_K))
          .append("\\,\\text{K}\\) &larr; <strong>sumar 273,15 para pasar a Kelvin</strong></li>")
          .append("</ul>\n\n");

        sb.append("<strong>Sustitución en la fórmula:</strong>\n\n")
          .append("\\[\\Delta G = ").append(fmtK2(sc.deltaHkJ()))
          .append("\\,\\text{kJ} - ").append(fmtK2(T_K)).append("\\,\\text{K} \\times ")
          .append(fmtK4(deltaS_k)).append("\\,\\text{kJ/K}\\]\n\n")
          .append("\\[\\Delta G = ").append(fmtK2(sc.deltaHkJ()))
          .append(" - (").append(fmtK2(T_K * deltaS_k)).append(") = ")
          .append(fmtK2(deltaG)).append("\\,\\text{kJ}\\]\n\n");

        sb.append("<strong>Criterio de espontaneidad de Gibbs:</strong>\n\n")
          .append("Como \\(\\Delta G = ").append(fmtK2(deltaG))
          .append("\\,\\text{kJ}\\), la reacción es ").append(spontText)
          .append(" en estas condiciones (T = ").append(fmt1(sc.tempCelsius()))
          .append(" °C, P constante).");

        return sb.toString();
    }

    // =========================================================================
    // HELPER — tabla HTML de ΔH°f
    // =========================================================================

    private String buildDataTable(ReactionScenario sc) {
        var sb = new StringBuilder();
        sb.append("<table class=\"data-table\">")
          .append("<thead><tr>")
          .append("<th>Sustancia</th><th>Estado</th><th>ΔH°f (kJ/mol)</th>")
          .append("</tr></thead><tbody>");

        for (ReactionTerm t : sc.reactants()) {
            sb.append("<tr><td>").append(toDisplayFormula(t.formulaAscii())).append("</td>")
              .append("<td>(").append(t.state()).append(")</td>")
              .append("<td>").append(fmt2(t.deltaHf())).append("</td></tr>");
        }
        for (ReactionTerm t : sc.products()) {
            sb.append("<tr><td>").append(toDisplayFormula(t.formulaAscii())).append("</td>")
              .append("<td>(").append(t.state()).append(")</td>")
              .append("<td>").append(fmt2(t.deltaHf())).append("</td></tr>");
        }
        sb.append("</tbody></table>");
        return sb.toString();
    }

    // =========================================================================
    // FORMATEADORES Y HELPERS
    // =========================================================================

    /** 1 decimal, coma española. */
    private String fmt1(double v) {
        return String.format("%.1f", v).replace(".", ",");
    }

    /** 2 decimales, coma española. */
    private String fmt2(double v) {
        return String.format("%.2f", v).replace(".", ",");
    }

    /** 4 decimales, coma española. */
    private String fmt4(double v) {
        return String.format("%.4f", v).replace(".", ",");
    }

    /** KaTeX 2 decimales con {,}. */
    private String fmtK2(double v) { return fmt2(v).replace(",", "{,}"); }

    /** KaTeX 1 decimal con {,}. */
    private String fmtK1(double v) { return fmt1(v).replace(",", "{,}"); }

    /** KaTeX 4 decimales con {,}. */
    private String fmtK4(double v) { return fmt4(v).replace(",", "{,}"); }

    /**
     * Convierte una fórmula ASCII en formato de visualización HTML con subíndices Unicode.
     * "Fe2O3" → "Fe₂O₃",  "CH4" → "CH₄",  "H2O" → "H₂O"
     */
    private String toDisplayFormula(String ascii) {
        return ascii
            .replace("0","₀").replace("1","₁").replace("2","₂").replace("3","₃")
            .replace("4","₄").replace("5","₅").replace("6","₆").replace("7","₇")
            .replace("8","₈").replace("9","₉");
    }

    /**
     * Convierte una fórmula ASCII a notación KaTeX con \text{} para evitar italización.
     * "Fe2O3" → "\text{Fe}_2\text{O}_3"
     */
    @SuppressWarnings("unused")
    private String formulaToKatex(String formula) {
        var sb = new StringBuilder();
        Matcher m = Pattern.compile("([A-Z][a-z]?)(\\d*)").matcher(formula);
        while (m.find()) {
            sb.append("\\text{").append(m.group(1)).append("}");
            String d = m.group(2);
            if (!d.isEmpty() && !d.equals("1")) sb.append("_").append(d);
        }
        return sb.toString();
    }
}
