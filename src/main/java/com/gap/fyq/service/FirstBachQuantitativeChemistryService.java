package com.gap.fyq.service;

import com.gap.fyq.model.firstbach.quantitativechemistry.FirstBachQuantitativeChemistryExercise;
import com.gap.fyq.model.firstbach.quantitativechemistry.QuantitativeChemistryType;
import com.gap.fyq.repository.FirstBachQuantitativeChemistryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirstBachQuantitativeChemistryService {

    private final FirstBachQuantitativeChemistryRepository repository;
    private final Random random = new Random();

    private static final String COURSE  = "1BACH";
    private static final String BLOCK   = "BL2";
    private static final double R_GAS   = 0.08206; // L·atm/(mol·K)

    // =========================================================================
    // DATA RECORDS
    // =========================================================================

    private record ElemComp(String symbol, String nameEs, int count, double atomicMass) {}

    private record CompoundScenario(
        String name,
        String molecularFormulaCanon,  // ASCII: "C6H12O6"
        String molecularFormulaDisp,   // Unicode subscripts: "C₆H₁₂O₆"
        double molarMass,
        List<ElemComp> elements,
        String empiricalFormulaDisp    // "CH₂O"
    ) {}

    private record SolutionScenario(
        String soluteName, String soluteFormula, String soluteFormulaDisp,
        double soluteMolarMass,
        String solventName, String solventFormula, String solventFormulaDisp,
        double solventMolarMass,
        double soluteMassG, double solventMassG, double densitySoln,
        String solveFor   // "molalidad" | "fraccion_molar"
    ) {}

    private record GasScenario(
        String nameA, String formulaA, String formulaDispA, double molarMassA, double massA,
        String nameB, String formulaB, String formulaDispB, double molarMassB, double massB,
        double volumeL, double tempK
    ) {}

    // =========================================================================
    // COMPOUNDS — EMPIRICAL_MOLECULAR_FORMULA
    // Masa molar calculada con: C=12,011 H=1,008 N=14,007 O=15,999
    // =========================================================================

    private static final List<CompoundScenario> COMPOUNDS = List.of(

        // C6H12O6  —  MM=180,156  —  emp CH2O (n=6)
        new CompoundScenario("glucosa", "C6H12O6", "C₆H₁₂O₆",
            180.156,
            List.of(new ElemComp("C","carbono",6,12.011),
                    new ElemComp("H","hidrógeno",12,1.008),
                    new ElemComp("O","oxígeno",6,15.999)),
            "CH₂O"),

        // C2H4O2  —  MM=60,052  —  emp CH2O (n=2)
        new CompoundScenario("ácido acético", "C2H4O2", "C₂H₄O₂",
            60.052,
            List.of(new ElemComp("C","carbono",2,12.011),
                    new ElemComp("H","hidrógeno",4,1.008),
                    new ElemComp("O","oxígeno",2,15.999)),
            "CH₂O"),

        // C6H6  —  MM=78,114  —  emp CH (n=6)
        new CompoundScenario("benceno", "C6H6", "C₆H₆",
            78.114,
            List.of(new ElemComp("C","carbono",6,12.011),
                    new ElemComp("H","hidrógeno",6,1.008)),
            "CH"),

        // C2H2  —  MM=26,038  —  emp CH (n=2)
        new CompoundScenario("acetileno", "C2H2", "C₂H₂",
            26.038,
            List.of(new ElemComp("C","carbono",2,12.011),
                    new ElemComp("H","hidrógeno",2,1.008)),
            "CH"),

        // C2H4  —  MM=28,054  —  emp CH2 (n=2)
        new CompoundScenario("etileno", "C2H4", "C₂H₄",
            28.054,
            List.of(new ElemComp("C","carbono",2,12.011),
                    new ElemComp("H","hidrógeno",4,1.008)),
            "CH₂"),

        // C4H10  —  MM=58,124  —  emp C2H5 (n=2)
        new CompoundScenario("butano", "C4H10", "C₄H₁₀",
            58.124,
            List.of(new ElemComp("C","carbono",4,12.011),
                    new ElemComp("H","hidrógeno",10,1.008)),
            "C₂H₅"),

        // H2O2  —  MM=34,014  —  emp HO (n=2)
        new CompoundScenario("peróxido de hidrógeno", "H2O2", "H₂O₂",
            34.014,
            List.of(new ElemComp("H","hidrógeno",2,1.008),
                    new ElemComp("O","oxígeno",2,15.999)),
            "HO"),

        // C2H2O4  —  MM=90,034  —  emp CHO2 (n=2)
        new CompoundScenario("ácido oxálico", "C2H2O4", "C₂H₂O₄",
            90.034,
            List.of(new ElemComp("C","carbono",2,12.011),
                    new ElemComp("H","hidrógeno",2,1.008),
                    new ElemComp("O","oxígeno",4,15.999)),
            "CHO₂")
    );

    // =========================================================================
    // SOLUTIONS — ADVANCED_SOLUTIONS
    // d = densidad de la disolución (g/mL) — dato distractor
    // =========================================================================

    private static final List<SolutionScenario> SOLUTIONS = List.of(

        // NaCl en H2O — MOLALIDAD
        new SolutionScenario(
            "cloruro de sodio", "NaCl", "NaCl", 58.44,
            "agua", "H2O", "H₂O", 18.015,
            5.84, 200.0, 1.026, "molalidad"),

        // C6H12O6 en H2O — MOLALIDAD
        new SolutionScenario(
            "glucosa", "C6H12O6", "C₆H₁₂O₆", 180.16,
            "agua", "H2O", "H₂O", 18.015,
            18.02, 100.0, 1.083, "molalidad"),

        // Urea CH4N2O en H2O — MOLALIDAD
        new SolutionScenario(
            "urea", "CH4N2O", "CH₄N₂O", 60.06,
            "agua", "H2O", "H₂O", 18.015,
            6.00, 250.0, 1.010, "molalidad"),

        // NaOH en H2O — MOLALIDAD
        new SolutionScenario(
            "hidróxido de sodio", "NaOH", "NaOH", 40.00,
            "agua", "H2O", "H₂O", 18.015,
            9.00, 500.0, 1.018, "molalidad"),

        // Etanol C2H6O en H2O — FRACCIÓN MOLAR
        new SolutionScenario(
            "etanol", "C2H6O", "C₂H₅OH", 46.07,
            "agua", "H2O", "H₂O", 18.015,
            46.07, 200.0, 0.924, "fraccion_molar"),

        // Glucosa en H2O — FRACCIÓN MOLAR
        new SolutionScenario(
            "glucosa", "C6H12O6", "C₆H₁₂O₆", 180.16,
            "agua", "H2O", "H₂O", 18.015,
            90.08, 180.2, 1.241, "fraccion_molar"),

        // NH3 en H2O — FRACCIÓN MOLAR
        new SolutionScenario(
            "amoníaco", "NH3", "NH₃", 17.031,
            "agua", "H2O", "H₂O", 18.015,
            17.03, 500.0, 0.997, "fraccion_molar"),

        // Metanol CH4O en H2O — FRACCIÓN MOLAR
        new SolutionScenario(
            "metanol", "CH4O", "CH₃OH", 32.042,
            "agua", "H2O", "H₂O", 18.015,
            16.02, 200.0, 0.986, "fraccion_molar")
    );

    // =========================================================================
    // GAS MIXTURES — GAS_MIXTURES_DALTON
    // Masas elegidas para n ≈ 1,000 o 0,500 mol (cálculos limpios).
    // R = 0,08206 L·atm/(mol·K)
    // =========================================================================

    private static final List<GasScenario> GAS_SCENARIOS = List.of(

        // N2 + O2, V=10 L, T=273 K  →  P_N2=2,24 atm  P_O2=1,12 atm  Ptot=3,36 atm
        new GasScenario(
            "nitrógeno", "N2", "N₂", 28.014, 28.01,
            "oxígeno",   "O2", "O₂", 32.000, 16.00,
            10.0, 273.0),

        // H2 + N2, V=5 L, T=300 K  →  P_H2=4,92 atm  P_N2=4,92 atm  Ptot=9,85 atm
        new GasScenario(
            "hidrógeno", "H2", "H₂",  2.016, 2.016,
            "nitrógeno", "N2", "N₂", 28.014, 28.01,
            5.0, 300.0),

        // O2 + CO2, V=8 L, T=350 K  →  P_O2=1,80 atm  P_CO2=3,59 atm  Ptot=5,39 atm
        new GasScenario(
            "oxígeno",          "O2",  "O₂",   32.000, 16.00,
            "dióxido de carbono","CO2", "CO₂",  44.009, 44.01,
            8.0, 350.0),

        // He + Ar, V=6 L, T=400 K  →  P_He=5,47 atm  P_Ar=5,47 atm  Ptot=10,94 atm
        new GasScenario(
            "helio", "He", "He",  4.003,  4.003,
            "argón", "Ar", "Ar", 39.948, 39.95,
            6.0, 400.0),

        // N2 + CH4, V=5 L, T=320 K  →  P_N2=5,25 atm  P_CH4=5,25 atm  Ptot=10,50 atm
        new GasScenario(
            "nitrógeno", "N2",  "N₂",  28.014, 28.01,
            "metano",         "CH4", "CH₄",  16.043, 16.04,
            5.0, 320.0),

        // H2 + O2, V=4 L, T=250 K  →  P_H2=2,56 atm  P_O2=7,69 atm  Ptot=10,26 atm
        new GasScenario(
            "hidrógeno", "H2", "H₂",  2.016,  1.008,
            "oxígeno",   "O2", "O₂", 32.000, 48.00,
            4.0, 250.0)
    );

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public FirstBachQuantitativeChemistryExercise generateAndSave() {
        FirstBachQuantitativeChemistryExercise ex = new FirstBachQuantitativeChemistryExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);

        int roll = random.nextInt(3);
        if      (roll == 0) buildEmpiricalMolecularFormula(ex);
        else if (roll == 1) buildAdvancedSolutions(ex);
        else                buildGasMixtures(ex);

        log.debug("1BACH BL2 generado: type={} mode={}", ex.getChemistryType(), ex.getExerciseMode());
        return repository.save(ex);
    }

    public FirstBachQuantitativeChemistryExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ejercicio 1BACH BL2 no encontrado: " + id));
    }

    // =========================================================================
    // CONSTRUCTOR — EMPIRICAL_MOLECULAR_FORMULA
    // Dado el % en masa de cada elemento y la MM total, hallar la fórmula molecular.
    // =========================================================================

    private void buildEmpiricalMolecularFormula(FirstBachQuantitativeChemistryExercise ex) {
        ex.setChemistryType(QuantitativeChemistryType.EMPIRICAL_MOLECULAR_FORMULA);
        ex.setExerciseMode("FORMULA");
        ex.setTolerancePercent(0);

        CompoundScenario sc = COMPOUNDS.get(random.nextInt(COMPOUNDS.size()));

        // Statement — percentages computed from exact atomic masses
        double totalMass = sc.elements().stream()
            .mapToDouble(e -> e.count() * e.atomicMass()).sum();
        var sb = new StringBuilder();
        sb.append("Un compuesto ").append(sc.name()).append(" tiene la siguiente composición en masa: ");
        for (int i = 0; i < sc.elements().size(); i++) {
            ElemComp e = sc.elements().get(i);
            double pct = (e.count() * e.atomicMass() / totalMass) * 100.0;
            if (i > 0) sb.append(", ");
            sb.append(e.symbol()).append(": ").append(fmtPct(pct)).append(" %");
        }
        sb.append(". La masa molar del compuesto es ").append(fmt2(sc.molarMass()))
          .append(" g/mol. Determina su fórmula molecular.");
        ex.setStatement(sb.toString());

        ex.setCorrectFormulaCanonical(sc.molecularFormulaCanon());
        ex.setCorrectFormulaDisplay(sc.molecularFormulaDisp());
        ex.setExplanation(buildFormulaExplanation(sc));
    }

    // =========================================================================
    // CONSTRUCTOR — ADVANCED_SOLUTIONS
    // Molalidad (b) o fracción molar (χ₁); densidad como dato distractor.
    // =========================================================================

    private void buildAdvancedSolutions(FirstBachQuantitativeChemistryExercise ex) {
        ex.setChemistryType(QuantitativeChemistryType.ADVANCED_SOLUTIONS);
        ex.setExerciseMode("NUMERICAL");
        ex.setTolerancePercent(2.0);

        SolutionScenario sc = SOLUTIONS.get(random.nextInt(SOLUTIONS.size()));
        ex.setUnknownVariable(sc.solveFor());

        double nSolute  = sc.soluteMassG()  / sc.soluteMolarMass();
        double nSolvent = sc.solventMassG() / sc.solventMolarMass();

        final double answer;
        final String unit;
        final String askVerb;

        if ("molalidad".equals(sc.solveFor())) {
            double kgSolvent = sc.solventMassG() / 1000.0;
            answer   = nSolute / kgSolvent;
            unit     = "mol/kg";
            askVerb  = "Calcula la molalidad (b) de la disolución.";
        } else {
            answer   = nSolute / (nSolute + nSolvent);
            unit     = "";
            askVerb  = "Calcula la fracción molar del soluto (χ₁).";
        }

        ex.setCorrectAnswerValue(answer);
        ex.setAnswerUnit(unit.isEmpty() ? "—" : unit);
        ex.setCorrectAnswerDisplay(fmt3Sig(answer) + (unit.isEmpty() ? "" : " " + unit));

        ex.setStatement(String.format(
            "Se disuelven %s g de %s (%s, M = %s g/mol) en %s g de %s (%s, M = %s g/mol). " +
            "La densidad de la disolución resultante es %s g/mL. %s",
            fmt2(sc.soluteMassG()),  sc.soluteName(),  sc.soluteFormulaDisp(),
            fmt3(sc.soluteMolarMass()),
            fmt2(sc.solventMassG()), sc.solventName(), sc.solventFormulaDisp(),
            fmt3(sc.solventMolarMass()),
            fmt3(sc.densitySoln()), askVerb));

        ex.setExplanation(buildSolutionExplanation(sc, nSolute, nSolvent, answer, unit));
    }

    // =========================================================================
    // CONSTRUCTOR — GAS_MIXTURES_DALTON
    // PV=nRT por cada gas; P_total = Σ Pᵢ (Ley de Dalton).
    // =========================================================================

    private void buildGasMixtures(FirstBachQuantitativeChemistryExercise ex) {
        ex.setChemistryType(QuantitativeChemistryType.GAS_MIXTURES_DALTON);
        ex.setExerciseMode("NUMERICAL");
        ex.setTolerancePercent(2.0);

        GasScenario sc = GAS_SCENARIOS.get(random.nextInt(GAS_SCENARIOS.size()));

        double nA = sc.massA() / sc.molarMassA();
        double nB = sc.massB() / sc.molarMassB();
        double pA = nA * R_GAS * sc.tempK() / sc.volumeL();
        double pB = nB * R_GAS * sc.tempK() / sc.volumeL();
        double pTotal = pA + pB;

        int tempC = (int) Math.round(sc.tempK() - 273.0);
        String tempStr = fmt2(sc.volumeL()) + " L a " +
            (int) sc.tempK() + " K (" + tempC + " °C)";

        String[] unknowns  = {"presion_parcial_A", "presion_parcial_B", "presion_total"};
        String solveFor = unknowns[random.nextInt(3)];
        ex.setUnknownVariable(solveFor);

        String askVerb = switch (solveFor) {
            case "presion_parcial_A" ->
                "Calcula la presión parcial del " + sc.nameA() +
                " (" + sc.formulaDispA() + ") en la mezcla.";
            case "presion_parcial_B" ->
                "Calcula la presión parcial del " + sc.nameB() +
                " (" + sc.formulaDispB() + ") en la mezcla.";
            default ->
                "Calcula la presión total de la mezcla gaseosa.";
        };

        double answer = switch (solveFor) {
            case "presion_parcial_A" -> pA;
            case "presion_parcial_B" -> pB;
            default                  -> pTotal;
        };

        ex.setCorrectAnswerValue(answer);
        ex.setAnswerUnit("atm");
        ex.setCorrectAnswerDisplay(fmt3Sig(answer) + " atm");

        ex.setStatement(String.format(
            "En un recipiente de %s se introduce una mezcla de %s g de %s " +
            "(%s, M = %s g/mol) y %s g de %s (%s, M = %s g/mol). " +
            "Suponiendo comportamiento ideal, %s",
            tempStr,
            fmt2(sc.massA()), sc.nameA(), sc.formulaDispA(), fmt3(sc.molarMassA()),
            fmt2(sc.massB()), sc.nameB(), sc.formulaDispB(), fmt3(sc.molarMassB()),
            askVerb));

        ex.setExplanation(buildGasExplanation(sc, nA, nB, pA, pB, pTotal, solveFor));
    }

    // =========================================================================
    // EXPLICACIÓN — EMPIRICAL_MOLECULAR_FORMULA
    // =========================================================================

    private String buildFormulaExplanation(CompoundScenario sc) {
        double totalMass = sc.elements().stream()
            .mapToDouble(e -> e.count() * e.atomicMass()).sum();

        var sb = new StringBuilder();

        // Paso 1 — tabla %→g
        sb.append("<strong>Paso 1 — De porcentaje a gramos</strong>")
          .append(" (base: 100 g de compuesto):\n\n")
          .append("<table class=\"data-table\"><thead>")
          .append("<tr><th>Elemento</th><th>% en masa</th>")
          .append("<th>Gramos en 100 g</th><th>Masa atómica (g/mol)</th></tr>")
          .append("</thead><tbody>");

        double minMoles = Double.MAX_VALUE;
        for (ElemComp e : sc.elements()) {
            double pct   = e.count() * e.atomicMass() / totalMass * 100.0;
            double moles = pct / e.atomicMass();
            if (moles < minMoles) minMoles = moles;
            sb.append("<tr><td>").append(e.symbol()).append("</td>")
              .append("<td>").append(fmtPct(pct)).append(" %</td>")
              .append("<td>").append(fmtPct(pct)).append(" g</td>")
              .append("<td>").append(fmt3(e.atomicMass())).append("</td></tr>");
        }
        sb.append("</tbody></table>\n\n");

        // Paso 2 — g → mol
        sb.append("<strong>Paso 2 — De gramos a moles</strong>")
          .append(" (dividir por la masa atómica):\n\n");
        for (ElemComp e : sc.elements()) {
            double pct   = e.count() * e.atomicMass() / totalMass * 100.0;
            double moles = pct / e.atomicMass();
            sb.append("\\[n_{\\text{").append(e.symbol()).append("}} = ")
              .append("\\frac{").append(fmtK2(pct)).append("}{").append(fmtK3(e.atomicMass())).append("}")
              .append(" = ").append(fmtK3(moles)).append("\\,\\text{mol}\\]\n\n");
        }

        // Paso 3 — dividir por el mínimo
        sb.append("<strong>Paso 3 — Dividir por el menor número de moles")
          .append(" (").append(fmtK3(minMoles)).append(" mol):</strong>\n\n");
        for (ElemComp e : sc.elements()) {
            double pct   = e.count() * e.atomicMass() / totalMass * 100.0;
            double moles = pct / e.atomicMass();
            double ratio = moles / minMoles;
            sb.append("\\[\\text{").append(e.symbol()).append("}: ")
              .append("\\frac{").append(fmtK3(moles)).append("}{").append(fmtK3(minMoles)).append("}")
              .append(" = ").append(fmtK2(ratio)).append(" \\approx ")
              .append((long) Math.round(ratio)).append("\\]\n\n");
        }
        sb.append("→ <strong>Fórmula empírica: ").append(sc.empiricalFormulaDisp())
          .append("</strong>\n\n");

        // M empírica: suma de (ratio redondeado × masa atómica)
        double mEmpClean = 0;
        for (ElemComp e : sc.elements()) {
            double pct   = e.count() * e.atomicMass() / totalMass * 100.0;
            double moles = pct / e.atomicMass();
            long   ratio = Math.round(moles / minMoles);
            mEmpClean += ratio * e.atomicMass();
        }

        sb.append("\\[M(\\text{empírica}) = ");
        boolean first = true;
        for (ElemComp e : sc.elements()) {
            double pct   = e.count() * e.atomicMass() / totalMass * 100.0;
            double moles = pct / e.atomicMass();
            long   ratio = Math.round(moles / minMoles);
            if (!first) sb.append(" + ");
            if (ratio > 1) sb.append(ratio).append(" \\times ");
            sb.append(fmtK3(e.atomicMass()));
            first = false;
        }
        sb.append(" = ").append(fmtK3(mEmpClean)).append("\\,\\text{g/mol}\\]\n\n");

        // Paso 4 — n multiplicador
        double n = sc.molarMass() / mEmpClean;
        long nInt = Math.round(n);
        sb.append("<strong>Paso 4 — Calcular el multiplicador n:</strong>\n\n")
          .append("\\[n = \\frac{M_{\\text{molecular}}}{M_{\\text{empírica}}} = ")
          .append("\\frac{").append(fmtK2(sc.molarMass())).append("}{").append(fmtK3(mEmpClean)).append("}")
          .append(" = ").append(fmtK2(n)).append(" \\approx ").append(nInt).append("\\]\n\n");

        sb.append("∴ <strong>Fórmula molecular: ")
          .append(sc.molecularFormulaDisp()).append("</strong>");

        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — ADVANCED_SOLUTIONS
    // =========================================================================

    private String buildSolutionExplanation(SolutionScenario sc,
                                            double nSolute, double nSolvent,
                                            double answer, String unit) {
        boolean isMolality = "molalidad".equals(sc.solveFor());
        var sb = new StringBuilder();

        sb.append("<strong>ℹ️ Dato distractor:</strong> ")
          .append("La densidad de la disolución (").append(fmt3(sc.densitySoln()))
          .append(" g/mL) <strong>no es necesaria</strong> para calcular la ")
          .append(isMolality ? "molalidad" : "fracción molar")
          .append(". ")
          .append(isMolality
              ? "La molalidad se refiere exclusivamente a la masa del <em>disolvente puro</em>."
              : "La fracción molar se calcula directamente a partir de los moles.")
          .append("\n\n");

        // Paso 1 — moles de soluto
        sb.append("<strong>Paso 1 — Moles de soluto (")
          .append(sc.soluteFormulaDisp()).append("):</strong>\n\n")
          .append("\\[n_{\\text{soluto}} = \\frac{m}{M} = ")
          .append("\\frac{").append(fmtK2(sc.soluteMassG())).append("\\,\\text{g}}")
          .append("{").append(fmtK3(sc.soluteMolarMass())).append("\\,\\text{g/mol}}")
          .append(" = ").append(fmtK3Sig(nSolute)).append("\\,\\text{mol}\\]\n\n");

        if (isMolality) {
            // Paso 2 — kg disolvente
            double kgSolvent = sc.solventMassG() / 1000.0;
            sb.append("<strong>Paso 2 — Masa del disolvente en kg:</strong>\n\n")
              .append("\\[m_{\\text{disolvente}} = ")
              .append(fmtK2(sc.solventMassG())).append("\\,\\text{g} = ")
              .append(fmtK3(kgSolvent)).append("\\,\\text{kg}\\]\n\n");

            // Paso 3 — molalidad
            sb.append("<strong>Paso 3 — Molalidad:</strong>\n\n")
              .append("\\[b = \\frac{n_{\\text{soluto}}}{m_{\\text{disolvente}}\\,(\\text{kg})} = ")
              .append("\\frac{").append(fmtK3Sig(nSolute)).append("\\,\\text{mol}}")
              .append("{").append(fmtK3(kgSolvent)).append("\\,\\text{kg}}")
              .append(" = ").append(fmtK3Sig(answer)).append("\\,\\text{mol\\cdot kg}^{-1}\\]\n\n")
              .append("∴ \\(b = \\boxed{").append(fmtK3Sig(answer))
              .append("\\,\\text{mol/kg}}\\)");
        } else {
            // Paso 2 — moles de disolvente
            sb.append("<strong>Paso 2 — Moles de disolvente (")
              .append(sc.solventFormulaDisp()).append("):</strong>\n\n")
              .append("\\[n_{\\text{disolvente}} = \\frac{m}{M} = ")
              .append("\\frac{").append(fmtK2(sc.solventMassG())).append("\\,\\text{g}}")
              .append("{").append(fmtK3(sc.solventMolarMass())).append("\\,\\text{g/mol}}")
              .append(" = ").append(fmtK3Sig(nSolvent)).append("\\,\\text{mol}\\]\n\n");

            // Paso 3 — fracción molar
            sb.append("<strong>Paso 3 — Fracción molar del soluto:</strong>\n\n")
              .append("\\[\\chi_1 = \\frac{n_{\\text{soluto}}}{n_{\\text{soluto}} + n_{\\text{disolvente}}} = ")
              .append("\\frac{").append(fmtK3Sig(nSolute)).append("}")
              .append("{").append(fmtK3Sig(nSolute)).append(" + ").append(fmtK3Sig(nSolvent)).append("}")
              .append(" = \\frac{").append(fmtK3Sig(nSolute)).append("}")
              .append("{").append(fmtK3Sig(nSolute + nSolvent)).append("}")
              .append(" = ").append(fmtK3Sig(answer)).append("\\]\n\n")
              .append("∴ \\(\\chi_1 = \\boxed{").append(fmtK3Sig(answer)).append("}\\)");
        }

        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — GAS_MIXTURES_DALTON
    // =========================================================================

    private String buildGasExplanation(GasScenario sc,
                                       double nA, double nB,
                                       double pA, double pB, double pTotal,
                                       String solveFor) {
        int tempC = (int) Math.round(sc.tempK() - 273.0);
        var sb = new StringBuilder();

        sb.append("<strong>Ley de los gases ideales y Ley de Dalton:</strong>\n\n")
          .append("Cada gas ocupa todo el volumen del recipiente; su presión parcial obedece:\n\n")
          .append("\\[P_i = \\frac{n_i \\cdot R \\cdot T}{V},\\quad ")
          .append("R = 0{,}08206\\,\\text{L\\cdot atm/(mol\\cdot K)}\\]\n\n");

        // Datos del sistema
        sb.append("<strong>Datos del sistema:</strong> ")
          .append("V = ").append(fmt2(sc.volumeL())).append(" L, ")
          .append("T = ").append((int) sc.tempK()).append(" K (")
          .append(tempC).append(" °C)\n\n");

        // Paso 1 — moles de cada gas
        sb.append("<strong>Paso 1 — Moles de cada gas:</strong>\n\n")
          .append("\\[n_{\\text{").append(sc.formulaA()).append("}} = ")
          .append("\\frac{").append(fmtK2(sc.massA())).append("\\,\\text{g}}")
          .append("{").append(fmtK3(sc.molarMassA())).append("\\,\\text{g/mol}}")
          .append(" = ").append(fmtK3Sig(nA)).append("\\,\\text{mol}\\]\n\n")
          .append("\\[n_{\\text{").append(sc.formulaB()).append("}} = ")
          .append("\\frac{").append(fmtK2(sc.massB())).append("\\,\\text{g}}")
          .append("{").append(fmtK3(sc.molarMassB())).append("\\,\\text{g/mol}}")
          .append(" = ").append(fmtK3Sig(nB)).append("\\,\\text{mol}\\]\n\n");

        // Paso 2 — presiones parciales
        sb.append("<strong>Paso 2 — Presiones parciales:</strong>\n\n")
          .append("\\[P_{\\text{").append(sc.formulaA()).append("}} = ")
          .append("\\frac{").append(fmtK3Sig(nA)).append(" \\times 0{,}08206 \\times ")
          .append((int) sc.tempK()).append("}{").append(fmtK2(sc.volumeL())).append("}")
          .append(" = ").append(fmtK3Sig(pA)).append("\\,\\text{atm}\\]\n\n")
          .append("\\[P_{\\text{").append(sc.formulaB()).append("}} = ")
          .append("\\frac{").append(fmtK3Sig(nB)).append(" \\times 0{,}08206 \\times ")
          .append((int) sc.tempK()).append("}{").append(fmtK2(sc.volumeL())).append("}")
          .append(" = ").append(fmtK3Sig(pB)).append("\\,\\text{atm}\\]\n\n");

        // Paso 3 — presión total (Ley de Dalton)
        sb.append("<strong>Paso 3 — Presión total (Ley de Dalton):</strong>\n\n")
          .append("\\[P_{\\text{total}} = P_{\\text{").append(sc.formulaA()).append("}} + P_{\\text{")
          .append(sc.formulaB()).append("}} = ")
          .append(fmtK3Sig(pA)).append(" + ").append(fmtK3Sig(pB))
          .append(" = ").append(fmtK3Sig(pTotal)).append("\\,\\text{atm}\\]\n\n");

        String boxedLabel = switch (solveFor) {
            case "presion_parcial_A" ->
                "P_{\\text{" + sc.formulaA() + "}} = \\boxed{" + fmtK3Sig(pA) + "\\,\\text{atm}}";
            case "presion_parcial_B" ->
                "P_{\\text{" + sc.formulaB() + "}} = \\boxed{" + fmtK3Sig(pB) + "\\,\\text{atm}}";
            default ->
                "P_{\\text{total}} = \\boxed{" + fmtK3Sig(pTotal) + "\\,\\text{atm}}";
        };
        sb.append("∴ \\(").append(boxedLabel).append("\\)");

        return sb.toString();
    }

    // =========================================================================
    // FORMATEADORES
    // =========================================================================

    /** 2 decimales, coma española. Para masas y densidades. */
    private String fmt2(double v) {
        return String.format("%.2f", v).replace(".", ",");
    }

    /** 3 decimales, coma española, recorta ceros finales. Para masas molares. */
    private String fmt3(double v) {
        String s = String.format("%.3f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
        return s.replace(".", ",");
    }

    /** 2 decimales para porcentajes. */
    private String fmtPct(double v) {
        return String.format("%.2f", v).replace(".", ",");
    }

    /** 3 cifras significativas, coma española. Para resultados finales. */
    private String fmt3Sig(double v) {
        if (v == 0) return "0";
        double absV = Math.abs(v);
        int exp = (int) Math.floor(Math.log10(absV));
        int decimals = Math.max(0, Math.min(8, 2 - exp));
        return String.format("%." + decimals + "f", v).replace(".", ",");
    }

    /** KaTeX: 2 decimales con {,}. */
    private String fmtK2(double v) {
        return fmt2(v).replace(",", "{,}");
    }

    /** KaTeX: 3 decimales, recorta ceros, con {,}. */
    private String fmtK3(double v) {
        return fmt3(v).replace(",", "{,}");
    }

    /** KaTeX: 3 cifras significativas con {,}. */
    private String fmtK3Sig(double v) {
        return fmt3Sig(v).replace(",", "{,}");
    }
}
