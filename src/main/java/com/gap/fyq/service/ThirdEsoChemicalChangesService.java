package com.gap.fyq.service;

import com.gap.fyq.model.thirdeso.chemicalchanges.ChemicalChangeType;
import com.gap.fyq.model.thirdeso.chemicalchanges.ThirdEsoChemicalChangesExercise;
import com.gap.fyq.repository.ThirdEsoChemicalChangesRepository;
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
public class ThirdEsoChemicalChangesService {

    private final ThirdEsoChemicalChangesRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "3ESO";
    private static final String BLOCK  = "BL3";

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public ThirdEsoChemicalChangesExercise generateAndSave() {
        int r = random.nextInt(3);
        ThirdEsoChemicalChangesExercise ex = switch (r) {
            case 0 -> buildEquationBalancing();
            case 1 -> buildLavoisierLaw();
            default -> buildBasicStoichiometry();
        };
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);
        return repository.save(ex);
    }

    public ThirdEsoChemicalChangesExercise findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ejercicio no encontrado: " + id));
    }

    // =========================================================================
    // AJUSTE DE ECUACIONES
    // =========================================================================

    private record BalancingScenario(
        String statement,
        String coefficients,  // coeficientes correctos separados por comas
        String labels,        // etiquetas KaTeX de cada hueco, separadas por comas
        String balanced,      // ecuación ajustada en KaTeX (para explicación)
        String stepByStep     // HTML + LaTeX de la explicación
    ) {}

    private static final List<BalancingScenario> BALANCING_SCENARIOS = List.of(

        new BalancingScenario(
            "Ajusta la ecuación de síntesis del amoníaco: N₂ + H₂ → NH₃",
            "1,3,2",
            "N_{2},H_{2},NH_{3}",
            "\\text{N}_2 + 3\\,\\text{H}_2 \\rightarrow 2\\,\\text{NH}_3",
            "<p><strong>Ecuación sin ajustar:</strong> \\(\\text{N}_2 + \\text{H}_2 \\rightarrow \\text{NH}_3\\)</p>" +
            "<p>Contamos átomos en cada lado e igualamos:</p>" +
            "<ul><li>Nitrógeno (N): 1 molécula de N₂ aporta 2 N → necesitamos 2 NH₃.</li>" +
            "<li>Hidrógeno (H): 2 NH₃ necesitan 6 H → necesitamos 3 H₂.</li></ul>" +
            "<p><strong>Ecuación ajustada:</strong>" +
            "\\[\\text{N}_2 + 3\\,\\text{H}_2 \\rightarrow 2\\,\\text{NH}_3\\]" +
            "Verificación: N: 2 = 2 ✓ &nbsp; H: 6 = 6 ✓</p>"
        ),

        new BalancingScenario(
            "Ajusta la combustión del metano: CH₄ + O₂ → CO₂ + H₂O",
            "1,2,1,2",
            "CH_{4},O_{2},CO_{2},H_{2}O",
            "\\text{CH}_4 + 2\\,\\text{O}_2 \\rightarrow \\text{CO}_2 + 2\\,\\text{H}_2\\text{O}",
            "<p><strong>Ecuación sin ajustar:</strong> " +
            "\\(\\text{CH}_4 + \\text{O}_2 \\rightarrow \\text{CO}_2 + \\text{H}_2\\text{O}\\)</p>" +
            "<ol><li>C: 1 CH₄ → 1 CO₂ → coeficiente CO₂ = 1.</li>" +
            "<li>H: 4 H en CH₄ → 2 H₂O → coeficiente H₂O = 2.</li>" +
            "<li>O: 1 CO₂ + 2 H₂O = 4 O → necesitamos 2 O₂.</li></ol>" +
            "<p><strong>Ecuación ajustada:</strong>" +
            "\\[\\text{CH}_4 + 2\\,\\text{O}_2 \\rightarrow \\text{CO}_2 + 2\\,\\text{H}_2\\text{O}\\]" +
            "Verificación: C: 1=1 ✓ &nbsp; H: 4=4 ✓ &nbsp; O: 4=4 ✓</p>"
        ),

        new BalancingScenario(
            "Ajusta la formación del agua: H₂ + O₂ → H₂O",
            "2,1,2",
            "H_{2},O_{2},H_{2}O",
            "2\\,\\text{H}_2 + \\text{O}_2 \\rightarrow 2\\,\\text{H}_2\\text{O}",
            "<p><strong>Ecuación sin ajustar:</strong> " +
            "\\(\\text{H}_2 + \\text{O}_2 \\rightarrow \\text{H}_2\\text{O}\\)</p>" +
            "<p>O₂ aporta 2 O, pero H₂O solo lleva 1 O: necesitamos 2 H₂O. " +
            "Con 2 H₂O tenemos 4 H → necesitamos 2 H₂.</p>" +
            "<p><strong>Ecuación ajustada:</strong>" +
            "\\[2\\,\\text{H}_2 + \\text{O}_2 \\rightarrow 2\\,\\text{H}_2\\text{O}\\]" +
            "Verificación: H: 4=4 ✓ &nbsp; O: 2=2 ✓</p>"
        ),

        new BalancingScenario(
            "Ajusta la combustión del carbono: C + O₂ → CO₂",
            "1,1,1",
            "C,O_{2},CO_{2}",
            "\\text{C} + \\text{O}_2 \\rightarrow \\text{CO}_2",
            "<p><strong>Ecuación sin ajustar:</strong> " +
            "\\(\\text{C} + \\text{O}_2 \\rightarrow \\text{CO}_2\\)</p>" +
            "<p>Cada molécula de CO₂ contiene 1 C y 2 O; O₂ aporta exactamente 2 O. " +
            "Los coeficientes son todos 1 — la ecuación ya está ajustada.</p>" +
            "<p><strong>Ecuación ajustada:</strong>" +
            "\\[\\text{C} + \\text{O}_2 \\rightarrow \\text{CO}_2\\]" +
            "Verificación: C: 1=1 ✓ &nbsp; O: 2=2 ✓</p>"
        ),

        new BalancingScenario(
            "Ajusta la formación del óxido de hierro (III): Fe + O₂ → Fe₂O₃",
            "4,3,2",
            "Fe,O_{2},Fe_{2}O_{3}",
            "4\\,\\text{Fe} + 3\\,\\text{O}_2 \\rightarrow 2\\,\\text{Fe}_2\\text{O}_3",
            "<p><strong>Ecuación sin ajustar:</strong> " +
            "\\(\\text{Fe} + \\text{O}_2 \\rightarrow \\text{Fe}_2\\text{O}_3\\)</p>" +
            "<p>El mínimo común múltiplo de 2 (O₂) y 3 (O en Fe₂O₃) es 6:</p>" +
            "<ul><li>3 O₂ aportan 6 O → necesitamos 2 Fe₂O₃ (cada uno con 3 O).</li>" +
            "<li>2 Fe₂O₃ contienen 4 Fe → necesitamos 4 Fe.</li></ul>" +
            "<p><strong>Ecuación ajustada:</strong>" +
            "\\[4\\,\\text{Fe} + 3\\,\\text{O}_2 \\rightarrow 2\\,\\text{Fe}_2\\text{O}_3\\]" +
            "Verificación: Fe: 4=4 ✓ &nbsp; O: 6=6 ✓</p>"
        ),

        new BalancingScenario(
            "Ajusta la descomposición del agua oxigenada: H₂O₂ → H₂O + O₂",
            "2,2,1",
            "H_{2}O_{2},H_{2}O,O_{2}",
            "2\\,\\text{H}_2\\text{O}_2 \\rightarrow 2\\,\\text{H}_2\\text{O} + \\text{O}_2",
            "<p><strong>Ecuación sin ajustar:</strong> " +
            "\\(\\text{H}_2\\text{O}_2 \\rightarrow \\text{H}_2\\text{O} + \\text{O}_2\\)</p>" +
            "<p>Cada H₂O₂ tiene 2 O; la H₂O solo lleva 1 O. Necesitamos 2 H₂O₂ para " +
            "liberar 1 O₂ y conservar 2 H₂O.</p>" +
            "<p><strong>Ecuación ajustada:</strong>" +
            "\\[2\\,\\text{H}_2\\text{O}_2 \\rightarrow 2\\,\\text{H}_2\\text{O} + \\text{O}_2\\]" +
            "Verificación: H: 4=4 ✓ &nbsp; O: 4=4 ✓</p>"
        )
    );

    private ThirdEsoChemicalChangesExercise buildEquationBalancing() {
        BalancingScenario sc = BALANCING_SCENARIOS.get(random.nextInt(BALANCING_SCENARIOS.size()));
        ThirdEsoChemicalChangesExercise ex = new ThirdEsoChemicalChangesExercise();
        ex.setChangeType(ChemicalChangeType.EQUATION_BALANCING);
        ex.setStatement(sc.statement());
        ex.setCorrectCoefficients(sc.coefficients());
        ex.setCoefficientCount(sc.coefficients().split(",").length);
        ex.setCoefficientLabels(sc.labels());
        ex.setExplanation(sc.stepByStep());
        return ex;
    }

    // =========================================================================
    // LEY DE LAVOISIER
    // Masa reactivos = Masa productos
    // Generamos cuatro valores (A, B, C, D) con A+B = C+D.
    // Ocultamos aleatoriamente uno de los cuatro.
    // =========================================================================

    private record LavoisierScenario(
        String reactant1, String reactant2,
        String product1,  String product2
    ) {}

    private static final List<LavoisierScenario> LAVOISIER_SCENARIOS = List.of(
        new LavoisierScenario("hierro (Fe)",    "azufre (S)",
                              "sulfuro de hierro (FeS)",    "exceso sin reaccionar"),
        new LavoisierScenario("magnesio (Mg)",  "oxígeno (O₂)",
                              "óxido de magnesio (MgO)",   "exceso sin reaccionar"),
        new LavoisierScenario("sodio (Na)",     "cloro (Cl₂)",
                              "cloruro de sodio (NaCl)",    "exceso sin reaccionar"),
        new LavoisierScenario("cobre (Cu)",     "azufre (S)",
                              "sulfuro de cobre (CuS)",     "exceso sin reaccionar"),
        new LavoisierScenario("calcio (Ca)",    "oxígeno (O₂)",
                              "óxido de calcio (CaO)",     "exceso sin reaccionar"),
        new LavoisierScenario("zinc (Zn)",      "ácido clorhídrico (HCl)",
                              "cloruro de zinc (ZnCl₂)", "hidrógeno (H₂)")
    );

    private ThirdEsoChemicalChangesExercise buildLavoisierLaw() {
        LavoisierScenario sc = LAVOISIER_SCENARIOS.get(random.nextInt(LAVOISIER_SCENARIOS.size()));

        double mA = 10 + random.nextInt(41);
        double mB = 10 + random.nextInt(41);
        double total = mA + mB;
        double mC = round2(total * (0.4 + random.nextDouble() * 0.3));
        double mD = round2(total - mC);

        int hidden = random.nextInt(4);
        double[] masses = {mA, mB, mC, mD};
        String[] names  = {sc.reactant1(), sc.reactant2(), sc.product1(), sc.product2()};
        String[] roles  = {"reactivo 1", "reactivo 2", "producto 1", "producto 2"};

        double answer     = masses[hidden];
        String unknown    = names[hidden];
        String unknownRole = roles[hidden];

        // Construir enunciado
        StringBuilder stmt = new StringBuilder();
        if (hidden < 2) {
            // incógnita en reactivos
            int knownReact = hidden == 0 ? 1 : 0;
            stmt.append("En una reacción química se mezclan ")
                .append(fmt(masses[knownReact])).append(" g de ").append(names[knownReact])
                .append(" con una cantidad desconocida de ").append(unknown)
                .append(". La reacción produce ").append(fmt(masses[2])).append(" g de ").append(names[2])
                .append(" y ").append(fmt(masses[3])).append(" g de ").append(names[3])
                .append(". Calcula la masa de ").append(unknown)
                .append(" (").append(unknownRole).append(") usando la ley de Lavoisier.");
        } else {
            // incógnita en productos
            int knownProd = hidden == 2 ? 3 : 2;
            stmt.append("Se mezclan ").append(fmt(masses[0])).append(" g de ").append(names[0])
                .append(" con ").append(fmt(masses[1])).append(" g de ").append(names[1])
                .append(". La reacción produce ").append(fmt(masses[knownProd]))
                .append(" g de ").append(names[knownProd])
                .append(" y una cantidad desconocida de ").append(unknown)
                .append(". Calcula la masa de ").append(unknown)
                .append(" (").append(unknownRole).append(") usando la ley de Lavoisier.");
        }

        String expl = buildLavoisierExplanation(names, masses, hidden);

        ThirdEsoChemicalChangesExercise ex = new ThirdEsoChemicalChangesExercise();
        ex.setChangeType(ChemicalChangeType.LAVOISIER_LAW);
        ex.setStatement(stmt.toString());
        ex.setCorrectAnswerValue(answer);
        ex.setCorrectAnswerDisplay(fmt(answer) + " g");
        ex.setAnswerUnit("g");
        ex.setExplanation(expl);
        return ex;
    }

    private String buildLavoisierExplanation(String[] names, double[] masses, int hidden) {
        String mR1 = hidden == 0 ? "x" : fmt(masses[0]);
        String mR2 = hidden == 1 ? "x" : fmt(masses[1]);
        String mP1 = hidden == 2 ? "x" : fmt(masses[2]);
        String mP2 = hidden == 3 ? "x" : fmt(masses[3]);

        double answer = masses[hidden];

        String equation;
        if (hidden < 2) {
            double knownOtherReact = hidden == 0 ? masses[1] : masses[0];
            equation = String.format(
                "x = %s\\,\\text{g} + %s\\,\\text{g} - %s\\,\\text{g} = %s\\,\\text{g}",
                fmt(masses[2]), fmt(masses[3]), fmt(knownOtherReact), fmt(answer));
        } else {
            double knownOtherProd = hidden == 2 ? masses[3] : masses[2];
            equation = String.format(
                "x = %s\\,\\text{g} + %s\\,\\text{g} - %s\\,\\text{g} = %s\\,\\text{g}",
                fmt(masses[0]), fmt(masses[1]), fmt(knownOtherProd), fmt(answer));
        }

        return "<p><strong>Ley de Lavoisier (conservación de la masa):</strong></p>" +
               "\\[m_{\\text{reactivos}} = m_{\\text{productos}}\\]" +
               "<p>Sustituyendo los datos conocidos:</p>" +
               String.format("\\[%s + %s = %s + %s\\]", mR1, mR2, mP1, mP2) +
               "<p>Despejando la incógnita (" + names[hidden] + "):</p>" +
               "\\[" + equation + "\\]" +
               "<p><strong>Resultado:</strong> la masa de " + names[hidden] +
               " es <strong>" + fmt(answer) + " g</strong>.</p>";
    }

    // =========================================================================
    // ESTEQUIOMETRÍA BÁSICA (masa a masa)
    // =========================================================================

    private record StoichiometryScenario(
        String name,
        String balancedEq,
        String balancedKatex,
        String givenSubstance,
        double givenMolarMass,
        int    givenCoeff,
        String soughtSubstance,
        double soughtMolarMass,
        int    soughtCoeff
    ) {}

    private static final List<StoichiometryScenario> STOICH_SCENARIOS = List.of(

        new StoichiometryScenario(
            "Combustión del metano",
            "CH₄ + 2 O₂ → CO₂ + 2 H₂O",
            "\\text{CH}_4 + 2\\,\\text{O}_2 \\rightarrow \\text{CO}_2 + 2\\,\\text{H}_2\\text{O}",
            "metano (CH₄)", 16.0, 1,
            "dióxido de carbono (CO₂)", 44.0, 1
        ),

        new StoichiometryScenario(
            "Combustión del metano",
            "CH₄ + 2 O₂ → CO₂ + 2 H₂O",
            "\\text{CH}_4 + 2\\,\\text{O}_2 \\rightarrow \\text{CO}_2 + 2\\,\\text{H}_2\\text{O}",
            "metano (CH₄)", 16.0, 1,
            "agua (H₂O)", 18.0, 2
        ),

        new StoichiometryScenario(
            "Formación del agua",
            "2 H₂ + O₂ → 2 H₂O",
            "2\\,\\text{H}_2 + \\text{O}_2 \\rightarrow 2\\,\\text{H}_2\\text{O}",
            "hidrógeno (H₂)", 2.0, 2,
            "agua (H₂O)", 18.0, 2
        ),

        new StoichiometryScenario(
            "Síntesis del amoníaco",
            "N₂ + 3 H₂ → 2 NH₃",
            "\\text{N}_2 + 3\\,\\text{H}_2 \\rightarrow 2\\,\\text{NH}_3",
            "nitrógeno (N₂)", 28.0, 1,
            "amoníaco (NH₃)", 17.0, 2
        ),

        new StoichiometryScenario(
            "Síntesis del amoníaco",
            "N₂ + 3 H₂ → 2 NH₃",
            "\\text{N}_2 + 3\\,\\text{H}_2 \\rightarrow 2\\,\\text{NH}_3",
            "hidrógeno (H₂)", 2.0, 3,
            "amoníaco (NH₃)", 17.0, 2
        ),

        new StoichiometryScenario(
            "Óxido de magnesio",
            "2 Mg + O₂ → 2 MgO",
            "2\\,\\text{Mg} + \\text{O}_2 \\rightarrow 2\\,\\text{MgO}",
            "magnesio (Mg)", 24.0, 2,
            "óxido de magnesio (MgO)", 40.0, 2
        ),

        new StoichiometryScenario(
            "Oxidación del hierro (herrumbre)",
            "4 Fe + 3 O₂ → 2 Fe₂O₃",
            "4\\,\\text{Fe} + 3\\,\\text{O}_2 \\rightarrow 2\\,\\text{Fe}_2\\text{O}_3",
            "hierro (Fe)", 56.0, 4,
            "óxido de hierro (III) (Fe₂O₃)", 160.0, 2
        )
    );

    private ThirdEsoChemicalChangesExercise buildBasicStoichiometry() {
        StoichiometryScenario sc = STOICH_SCENARIOS.get(random.nextInt(STOICH_SCENARIOS.size()));

        int multiplier = 1 + random.nextInt(8);
        double givenGrams = round2(sc.givenMolarMass() * multiplier);

        double factor = ((double) sc.soughtCoeff() * sc.soughtMolarMass())
                      / ((double) sc.givenCoeff() * sc.givenMolarMass());
        double answer = round2(givenGrams * factor);

        String stmt = sc.name() + ". Ecuación ajustada: " + sc.balancedEq()
                    + ". Si se utilizan " + fmt(givenGrams) + " g de " + sc.givenSubstance()
                    + ", ¿cuántos gramos de " + sc.soughtSubstance() + " se producen?";

        String expl = buildStoichExplanation(sc, givenGrams, answer);

        ThirdEsoChemicalChangesExercise ex = new ThirdEsoChemicalChangesExercise();
        ex.setChangeType(ChemicalChangeType.BASIC_STOICHIOMETRY);
        ex.setStatement(stmt);
        ex.setCorrectAnswerValue(answer);
        ex.setCorrectAnswerDisplay(fmt(answer) + " g");
        ex.setAnswerUnit("g");
        ex.setExplanation(expl);
        ex.setTolerancePercent(2.0);
        return ex;
    }

    private String buildStoichExplanation(StoichiometryScenario sc,
                                           double givenGrams, double answer) {
        double mmGiven  = sc.givenMolarMass();
        double mmSought = sc.soughtMolarMass();
        int    cGiven   = sc.givenCoeff();
        int    cSought  = sc.soughtCoeff();

        double moles       = round2(givenGrams / mmGiven);
        double molesSought = round2(moles * cSought / (double) cGiven);

        return "<p><strong>Reacción:</strong></p>" +
               "\\[" + sc.balancedKatex() + "\\]" +
               "<p><strong>Paso 1 — Masas molares:</strong></p>" +
               "<ul>" +
               "<li>M(" + sc.givenSubstance() + ") = " + fmt(mmGiven) + " g/mol</li>" +
               "<li>M(" + sc.soughtSubstance() + ") = " + fmt(mmSought) + " g/mol</li>" +
               "</ul>" +
               "<p><strong>Paso 2 — Moles de la sustancia dada:</strong></p>" +
               String.format("\\[n = \\frac{%s\\,\\text{g}}{%s\\,\\text{g/mol}} = %s\\,\\text{mol}\\]",
                   fmt(givenGrams), fmt(mmGiven), fmt(moles)) +
               "<p><strong>Paso 3 — Relación estequiométrica</strong> " +
               "(por la ecuación, " + cGiven + " mol de " + sc.givenSubstance() +
               " producen " + cSought + " mol de " + sc.soughtSubstance() + "):</p>" +
               String.format("\\[n_{\\text{buscado}} = %s\\,\\text{mol} \\times \\frac{%d \\times %s}{%d \\times %s} = %s\\,\\text{mol}\\]",
                   fmt(moles), cSought, fmt(mmSought), cGiven, fmt(mmGiven), fmt(molesSought)) +
               "<p><strong>Paso 4 — Gramos buscados:</strong></p>" +
               String.format("\\[m = %s\\,\\text{mol} \\times %s\\,\\text{g/mol} = \\boxed{%s\\,\\text{g}}\\]",
                   fmt(molesSought), fmt(mmSought), fmt(answer));
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
}
