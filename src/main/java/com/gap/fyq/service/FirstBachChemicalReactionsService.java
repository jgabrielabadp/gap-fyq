package com.gap.fyq.service;

import com.gap.fyq.model.firstbach.chemicalreactions.ChemicalReactionsType;
import com.gap.fyq.model.firstbach.chemicalreactions.FirstBachChemicalReactionsExercise;
import com.gap.fyq.repository.FirstBachChemicalReactionsRepository;
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
public class FirstBachChemicalReactionsService {

    private final FirstBachChemicalReactionsRepository repository;
    private final Random random = new Random();

    private static final String COURSE  = "1BACH";
    private static final String BLOCK   = "BL3";
    private static final double VM_STP  = 22.4; // L/mol a condiciones normales (c.n.)

    // =========================================================================
    // DATA RECORDS
    // =========================================================================

    /**
     * Escenario con un solo reactivo impuro y rendimiento.
     * totalMass en massUnit ("g" o "kg"). productIsGas=true → producto gaseoso (22,4 L/mol).
     */
    private record SimpleScenario(
        String context,
        String equation,
        String reactantName, String reactantFormula, double molarMassReact, int coeffReact,
        String productName,  String productFormula,
        double molarMassProduct, int coeffProduct, boolean productIsGas,
        double totalMass, String massUnit, double purityPct, double yieldPct
    ) {}

    /** Escenario de reactivo limitante con impurezas en uno o ambos reactivos. */
    private record LimitingScenario(
        String context, String equation,
        String nameA, String formulaA, double molarMassA, int coeffA,
        String nameB, String formulaB, double molarMassB, int coeffB,
        String productName, String productFormula, double molarMassProduct, int coeffProduct,
        double totalMassA, double purityA,
        double totalMassB, double purityB,
        double yieldPct
    ) {}

    // =========================================================================
    // PURITY_AND_YIELD — 8 escenarios de laboratorio (masa en g)
    // =========================================================================

    private static final List<SimpleScenario> PURITY_YIELD = List.of(

        // S1: CaCO3 → CaO + CO2  (caliza → cal viva, producto masa)
        // m_pura=40g, n=0,3996, n_CaO=0,3996, m_teo=22,41g, m_real=20,17g
        new SimpleScenario(
            "descomposición de la caliza para obtención de cal viva",
            "CaCO₃ → CaO + CO₂",
            "caliza (mineral con CaCO₃)", "CaCO3", 100.09, 1,
            "óxido de calcio (cal viva)", "CaO", 56.08, 1, false,
            50.0, "g", 80.0, 90.0),

        // S2: CaCO3 → CaO + CO2  (gas CO2 a c.n.)
        // m_pura=75g, n=0,7493, V_teo=16,78L, V_real=14,27L
        new SimpleScenario(
            "desprendimiento de CO₂ en la descomposición de caliza",
            "CaCO₃ → CaO + CO₂",
            "caliza (mineral con CaCO₃)", "CaCO3", 100.09, 1,
            "dióxido de carbono (gas)", "CO2", 0, 1, true,
            100.0, "g", 75.0, 85.0),

        // S3: 2Fe2O3 + 3C → 4Fe + 3CO2  (hematites → hierro)
        // m_pura=140g, n_Fe2O3=0,8767, n_Fe=1,7534, m_teo=97,93g, m_real=83,24g
        new SimpleScenario(
            "reducción de hematites con carbono de coque",
            "2 Fe₂O₃ + 3 C → 4 Fe + 3 CO₂",
            "hematites (mineral de Fe₂O₃)", "Fe2O3", 159.69, 2,
            "hierro metálico", "Fe", 55.845, 4, false,
            200.0, "g", 70.0, 85.0),

        // S4: 4FeS2 + 11O2 → 2Fe2O3 + 8SO2  (tostación de piritas → SO2)
        // m_pura=360g, n_FeS2=3,0008, n_SO2=6,0015, m_teo=384,58g, m_real=365,35g
        new SimpleScenario(
            "tostación de pirita (FeS₂) en exceso de oxígeno",
            "4 FeS₂ + 11 O₂ → 2 Fe₂O₃ + 8 SO₂",
            "pirita (mineral con FeS₂)", "FeS2", 119.97, 4,
            "dióxido de azufre", "SO2", 64.06, 8, false,
            400.0, "g", 90.0, 95.0),

        // S5: Cu2S + O2 → 2Cu + SO2  (calcocinita → cobre)
        // m_pura=195g, n_Cu2S=1,2252, n_Cu=2,4504, m_teo=155,71g, m_real=124,57g
        new SimpleScenario(
            "obtención de cobre a partir de calcocinita (Cu₂S)",
            "Cu₂S + O₂ → 2 Cu + SO₂",
            "calcocinita (mineral con Cu₂S)", "Cu2S", 159.16, 1,
            "cobre metálico", "Cu", 63.546, 2, false,
            300.0, "g", 65.0, 80.0),

        // S6: N2 + 3H2 → 2NH3  (Haber-Bosch, pase único)
        // m_pura=47,5g, n_N2=1,6955, n_NH3=3,3910, m_teo=57,75g, m_real=11,55g
        new SimpleScenario(
            "síntesis de amoníaco (proceso Haber-Bosch, pase único)",
            "N₂ + 3 H₂ → 2 NH₃",
            "corriente gaseosa con N₂", "N2", 28.014, 1,
            "amoníaco (NH₃)", "NH3", 17.031, 2, false,
            50.0, "g", 95.0, 20.0),

        // S7: 2KClO3 → 2KCl + 3O2  (clorato potásico → O2 gas)
        // m_pura=102g, n_KClO3=0,8323, n_O2=1,2484, V_teo=27,96L, V_real=26,57L
        new SimpleScenario(
            "obtención de oxígeno por descomposición de clorato potásico",
            "2 KClO₃ → 2 KCl + 3 O₂",
            "clorato de potasio (KClO₃)", "KClO3", 122.55, 2,
            "oxígeno (gas)", "O2", 0, 3, true,
            120.0, "g", 85.0, 95.0),

        // S8: 2ZnS + 3O2 → 2ZnO + 2SO2  (tostación de esfalerita → ZnO)
        // m_pura=176g, n_ZnS=1,8070, n_ZnO=1,8070, m_teo=147,04g, m_real=135,28g
        new SimpleScenario(
            "tostación de esfalerita (ZnS) para obtener ZnO",
            "2 ZnS + 3 O₂ → 2 ZnO + 2 SO₂",
            "esfalerita (mineral con ZnS)", "ZnS", 97.44, 2,
            "óxido de zinc", "ZnO", 81.38, 2, false,
            200.0, "g", 88.0, 92.0)
    );

    // =========================================================================
    // LIMITING_WITH_IMPURITIES — 6 escenarios
    // =========================================================================

    private static final List<LimitingScenario> LIMITING = List.of(

        // L1: N2 + 3H2 → 2NH3  —  N2 limitante
        // nA=1,799(N2) nB=8,482(H2) port=1,799 vs 2,827 → N2 lim
        // n_NH3=3,598 m_teo=61,28g m_real=49,03g
        new LimitingScenario(
            "síntesis de amoníaco (Haber-Bosch)",
            "N₂ + 3 H₂ → 2 NH₃",
            "nitrógeno", "N2", 28.014, 1,
            "hidrógeno", "H2",  2.016, 3,
            "amoníaco", "NH3", 17.031, 2,
            56.0, 90.0,
            18.0, 95.0,
            80.0),

        // L2: Fe2O3 + 3CO → 2Fe + 3CO2  —  CO limitante
        // nA=1,703(Fe2O3) nB=4,284(CO) port=1,703 vs 1,428 → CO lim
        // n_Fe=2,856 m_teo=159,52g m_real=143,57g
        new LimitingScenario(
            "reducción de óxido de hierro con monóxido de carbono",
            "Fe₂O₃ + 3 CO → 2 Fe + 3 CO₂",
            "hematites (Fe₂O₃)", "Fe2O3", 159.69, 1,
            "monóxido de carbono", "CO", 28.010, 3,
            "hierro metálico", "Fe", 55.845, 2,
            320.0, 85.0,
            120.0, 100.0,
            90.0),

        // L3: 2Al + Fe2O3 → Al2O3 + 2Fe  (termita)  —  Fe2O3 limitante
        // nA=2,817(Al) nB=1,102(Fe2O3) port=1,408 vs 1,102 → Fe2O3 lim
        // n_Fe=2,204 m_teo=123,07g m_real=116,92g
        new LimitingScenario(
            "reacción termita (aluminotermia)",
            "2 Al + Fe₂O₃ → Al₂O₃ + 2 Fe",
            "polvo de aluminio", "Al",    26.982, 2,
            "óxido de hierro(III)", "Fe2O3", 159.69, 1,
            "hierro metálico", "Fe", 55.845, 2,
            80.0, 95.0,
            200.0, 88.0,
            95.0),

        // L4: CH4 + 2O2 → CO2 + 2H2O  —  O2 limitante
        // nA=8,976(CH4) nB=9,282(O2) port=8,976 vs 4,641 → O2 lim
        // n_CO2=4,641 m_teo=204,24g m_real=194,03g
        new LimitingScenario(
            "combustión de gas natural (metano)",
            "CH₄ + 2 O₂ → CO₂ + 2 H₂O",
            "gas natural (CH₄)", "CH4", 16.043, 1,
            "oxígeno",           "O2",  31.998, 2,
            "dióxido de carbono", "CO2", 44.009, 1,
            160.0, 90.0,
            300.0, 99.0,
            95.0),

        // L5: 2SO2 + O2 → 2SO3  —  SO2 limitante
        // nA=3,746(SO2) nB=3,094(O2) port=1,873 vs 3,094 → SO2 lim
        // n_SO3=3,746 m_teo=299,94g m_real=290,95g
        new LimitingScenario(
            "oxidación catalítica de SO₂ (proceso de contacto)",
            "2 SO₂ + O₂ → 2 SO₃",
            "dióxido de azufre", "SO2", 64.06, 2,
            "oxígeno",           "O2",  31.998, 1,
            "trióxido de azufre", "SO3", 80.06, 2,
            250.0, 96.0,
            100.0, 99.0,
            97.0),

        // L6: 2Ca + O2 → 2CaO  —  Ca limitante
        // nA=3,513(Ca) nB=1,866(O2) port=1,756 vs 1,866 → Ca lim
        // n_CaO=3,513 m_teo=197,01g m_real=181,25g
        new LimitingScenario(
            "oxidación de calcio para obtener cal viva (CaO)",
            "2 Ca + O₂ → 2 CaO",
            "calcio metálico", "Ca", 40.078, 2,
            "oxígeno",         "O2", 31.998, 1,
            "óxido de calcio", "CaO", 56.077, 2,
            160.0, 88.0,
            60.0, 99.5,
            92.0)
    );

    // =========================================================================
    // INDUSTRIAL_SIDERURGY — 6 escenarios a escala industrial (masa en kg)
    // =========================================================================

    private static final List<SimpleScenario> INDUSTRIAL = List.of(

        // I1: Fe2O3 + 3CO → 2Fe + 3CO2  (alto horno, 1000 kg mineral)
        // m_pura=620 kg=620000g, n_Fe2O3=3882, n_Fe=7764, m_teo=433584g=433,58kg, m_real=403,23kg
        new SimpleScenario(
            "reducción de hematites en el alto horno (siderurgia)",
            "Fe₂O₃ + 3 CO → 2 Fe + 3 CO₂",
            "mineral de hierro (hematites, Fe₂O₃)", "Fe2O3", 159.69, 1,
            "hierro (arrabio)", "Fe", 55.845, 2, false,
            1000.0, "kg", 62.0, 93.0),

        // I2: 4FeS2 + 11O2 → 2Fe2O3 + 8SO2  (tostación industrial, 500 kg pirita)
        // m_pura=440 kg, n_FeS2=3668, n_SO2=7335, m_teo=470010g=470,01kg, m_real=451,21kg
        new SimpleScenario(
            "tostación industrial de pirita (obtención de SO₂ para H₂SO₄)",
            "4 FeS₂ + 11 O₂ → 2 Fe₂O₃ + 8 SO₂",
            "pirita industrial (FeS₂)", "FeS2", 119.97, 4,
            "dióxido de azufre", "SO2", 64.06, 8, false,
            500.0, "kg", 88.0, 96.0),

        // I3: N2 + 3H2 → 2NH3  (Haber-Bosch industrial, 280 kg)
        // m_pura=266 kg, n_N2=9495, n_NH3=18990, m_teo=323430g=323,43kg, m_real=64,69kg
        new SimpleScenario(
            "proceso Haber-Bosch (producción industrial de amoníaco)",
            "N₂ + 3 H₂ → 2 NH₃",
            "corriente de N₂ (alimentación del reactor)", "N2", 28.014, 1,
            "amoníaco (NH₃)", "NH3", 17.031, 2, false,
            280.0, "kg", 95.0, 20.0),

        // I4: 2SO2 + O2 → 2SO3  (proceso contacto, 800 kg gas)
        // m_pura=704 kg, n_SO2=10992, n_SO3=10992, m_teo=880006g=880,01kg, m_real=853,61kg
        new SimpleScenario(
            "proceso de contacto (obtención industrial de SO₃)",
            "2 SO₂ + O₂ → 2 SO₃",
            "gas industrial con SO₂", "SO2", 64.06, 2,
            "trióxido de azufre (SO₃)", "SO3", 80.06, 2, false,
            800.0, "kg", 88.0, 97.0),

        // I5: Fe2O3 + 3CO → 2Fe + 3CO2  (alto horno, 5000 kg mineral)
        // m_pura=3550 kg, n_Fe2O3=22230, n_Fe=44460, m_teo=2483500g=2483,50kg, m_real=2260,0kg
        new SimpleScenario(
            "producción de arrabio en alto horno (escala de toneladas)",
            "Fe₂O₃ + 3 CO → 2 Fe + 3 CO₂",
            "mineral de hierro (hematites)", "Fe2O3", 159.69, 1,
            "hierro (arrabio)", "Fe", 55.845, 2, false,
            5000.0, "kg", 71.0, 91.0),

        // I6: Cu2S + O2 → 2Cu + SO2  (metalurgia del cobre, 2000 kg)
        // m_pura=1100 kg, n_Cu2S=6912, n_Cu=13823, m_teo=878343g=878,34kg, m_real=764,16kg
        new SimpleScenario(
            "obtención industrial de cobre por tostación de calcocinita (Cu₂S)",
            "Cu₂S + O₂ → 2 Cu + SO₂",
            "mineral de cobre (calcocinita, Cu₂S)", "Cu2S", 159.16, 1,
            "cobre metálico", "Cu", 63.546, 2, false,
            2000.0, "kg", 55.0, 87.0)
    );

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public FirstBachChemicalReactionsExercise generateAndSave() {
        FirstBachChemicalReactionsExercise ex = new FirstBachChemicalReactionsExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);
        ex.setExerciseMode("NUMERICAL");

        int roll = random.nextInt(3);
        if      (roll == 0) buildPurityYield(ex);
        else if (roll == 1) buildLimiting(ex);
        else                buildIndustrial(ex);

        log.debug("1BACH BL3 generado: type={} id={}",
            ex.getReactionsType(), ex.getId());
        return repository.save(ex);
    }

    public FirstBachChemicalReactionsExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ejercicio 1BACH BL3 no encontrado: " + id));
    }

    // =========================================================================
    // CONSTRUCTOR — PURITY_AND_YIELD
    // Pureza → moles → estequiometría → rendimiento
    // =========================================================================

    private void buildPurityYield(FirstBachChemicalReactionsExercise ex) {
        ex.setReactionsType(ChemicalReactionsType.PURITY_AND_YIELD);
        SimpleScenario sc = PURITY_YIELD.get(random.nextInt(PURITY_YIELD.size()));
        fillSimpleExercise(ex, sc);
    }

    // =========================================================================
    // CONSTRUCTOR — INDUSTRIAL_SIDERURGY
    // Misma matemática que PURITY_AND_YIELD pero a escala industrial (kg)
    // =========================================================================

    private void buildIndustrial(FirstBachChemicalReactionsExercise ex) {
        ex.setReactionsType(ChemicalReactionsType.INDUSTRIAL_SIDERURGY);
        SimpleScenario sc = INDUSTRIAL.get(random.nextInt(INDUSTRIAL.size()));
        fillSimpleExercise(ex, sc);
    }

    /** Lógica compartida por PURITY_AND_YIELD e INDUSTRIAL_SIDERURGY. */
    private void fillSimpleExercise(FirstBachChemicalReactionsExercise ex,
                                    SimpleScenario sc) {
        ex.setTolerancePercent(2.0);

        boolean isKg   = "kg".equals(sc.massUnit());
        double massG   = sc.totalMass() * (isKg ? 1000.0 : 1.0);
        double mPureG  = massG * sc.purityPct() / 100.0;
        double nReact  = mPureG / sc.molarMassReact();
        double nProd   = nReact * sc.coeffProduct() / sc.coeffReact();

        final double answer;
        final String unit;

        if (sc.productIsGas()) {
            double vTheo = nProd * VM_STP;
            answer = vTheo * sc.yieldPct() / 100.0;
            unit = "L";
        } else {
            double mTheoG = nProd * sc.molarMassProduct();
            double mRealG = mTheoG * sc.yieldPct() / 100.0;
            answer = isKg ? mRealG / 1000.0 : mRealG;
            unit = sc.massUnit();
        }

        ex.setUnknownVariable(sc.productIsGas() ? "volumen_gas" : "masa_producto");
        ex.setCorrectAnswerValue(answer);
        ex.setAnswerUnit(unit);
        ex.setCorrectAnswerDisplay(fmt2(answer) + " " + unit);

        String massStr   = fmt2(sc.totalMass()) + " " + sc.massUnit();
        String askVerb   = sc.productIsGas()
            ? "¿Qué volumen de " + sc.productName() + " (en L a c.n.) se obtiene?"
            : "¿Qué masa de " + sc.productName() + " (en " + sc.massUnit() + ") se obtiene?";

        ex.setStatement(String.format(
            "Se dispone de %s de %s (pureza: %s %%). " +
            "La reacción es: %s. " +
            "El rendimiento de la reacción es del %s %%. %s",
            massStr, sc.reactantName(),
            fmt1(sc.purityPct()),
            sc.equation(),
            fmt1(sc.yieldPct()),
            askVerb));

        ex.setExplanation(buildSimpleExplanation(sc, mPureG, nReact, nProd, answer, unit, isKg));
    }

    // =========================================================================
    // CONSTRUCTOR — LIMITING_WITH_IMPURITIES
    // =========================================================================

    private void buildLimiting(FirstBachChemicalReactionsExercise ex) {
        ex.setReactionsType(ChemicalReactionsType.LIMITING_WITH_IMPURITIES);
        ex.setUnknownVariable("masa_producto");
        ex.setTolerancePercent(2.0);

        LimitingScenario sc = LIMITING.get(random.nextInt(LIMITING.size()));

        double mPureAg = sc.totalMassA() * sc.purityA() / 100.0;
        double mPureBg = sc.totalMassB() * sc.purityB() / 100.0;
        double nA      = mPureAg / sc.molarMassA();
        double nB      = mPureBg / sc.molarMassB();

        // Reactivo limitante: menor cociente n_i / coeff_i
        double portA = nA / sc.coeffA();
        double portB = nB / sc.coeffB();
        boolean aIsLimiting = portA <= portB;

        double nLimiting    = aIsLimiting ? nA : nB;
        int    coeffLimiting = aIsLimiting ? sc.coeffA() : sc.coeffB();
        double nProduct     = nLimiting * sc.coeffProduct() / coeffLimiting;
        double mTheoG       = nProduct * sc.molarMassProduct();
        double answer       = mTheoG * sc.yieldPct() / 100.0;

        ex.setCorrectAnswerValue(answer);
        ex.setAnswerUnit("g");
        ex.setCorrectAnswerDisplay(fmt2(answer) + " g");

        ex.setStatement(String.format(
            "Se mezclan %s g de %s (pureza: %s %%) con %s g de %s (pureza: %s %%). " +
            "La reacción que tiene lugar es: %s. " +
            "El rendimiento es del %s %%. ¿Qué masa de %s (en g) se obtiene?",
            fmt2(sc.totalMassA()), sc.nameA(), fmt1(sc.purityA()),
            fmt2(sc.totalMassB()), sc.nameB(), fmt1(sc.purityB()),
            sc.equation(),
            fmt1(sc.yieldPct()),
            sc.productName()));

        ex.setExplanation(buildLimitingExplanation(
            sc, mPureAg, mPureBg, nA, nB, portA, portB,
            aIsLimiting, nProduct, mTheoG, answer));
    }

    // =========================================================================
    // EXPLICACIÓN — SimpleScenario (PURITY_AND_YIELD / INDUSTRIAL_SIDERURGY)
    // =========================================================================

    private String buildSimpleExplanation(SimpleScenario sc,
                                          double mPureG, double nReact, double nProd,
                                          double answer, String unit, boolean isKg) {
        String fReact   = formulaToKatex(sc.reactantFormula());
        String fProduct = formulaToKatex(sc.productFormula());
        var sb = new StringBuilder();

        sb.append("<strong>Ecuación ajustada:</strong> ").append(sc.equation())
          .append("\n\n");

        if (isKg) {
            sb.append("<em>Nota: los cálculos se realizan internamente en gramos. ")
              .append("La masa de entrada en kg se convierte multiplicando por 1000.</em>\n\n");
        }

        // Paso 1 — masa pura
        String massIn = isKg
            ? fmtK2(sc.totalMass()) + "\\,\\text{kg} \\times 1000 = " + fmtK2(sc.totalMass() * 1000) + "\\,\\text{g}"
            : fmtK2(sc.totalMass()) + "\\,\\text{" + sc.massUnit() + "}";

        sb.append("<strong>Paso 1 — Masa de reactivo puro (").append(sc.reactantFormula())
          .append("):</strong>\n\n")
          .append("\\[m_{\\text{puro}} = m_{\\text{total}} \\times \\frac{\\eta_{\\text{pureza}}}{100} = ")
          .append(massIn)
          .append(" \\times \\frac{").append(fmtK1(sc.purityPct())).append("}{100} = ")
          .append(fmtK2(mPureG)).append("\\,\\text{g}\\]\n\n");

        // Paso 2 — moles de reactivo
        sb.append("<strong>Paso 2 — Moles de reactivo:</strong>\n\n")
          .append("\\[n_{").append(fReact).append("} = \\frac{m_{\\text{puro}}}{M_{").append(fReact).append("}} = ")
          .append("\\frac{").append(fmtK2(mPureG)).append("\\,\\text{g}}")
          .append("{").append(fmtK2(sc.molarMassReact())).append("\\,\\text{g/mol}} = ")
          .append(fmtK4(nReact)).append("\\,\\text{mol}\\]\n\n");

        // Paso 3 — estequiometría
        sb.append("<strong>Paso 3 — Proporción estequiométrica</strong>")
          .append(" (").append(sc.coeffReact()).append(" mol ").append(sc.reactantFormula())
          .append(" → ").append(sc.coeffProduct()).append(" mol ").append(sc.productFormula())
          .append("):\n\n")
          .append("\\[n_{").append(fProduct).append("} = ")
          .append(fmtK4(nReact)).append(" \\times \\frac{").append(sc.coeffProduct())
          .append("}{").append(sc.coeffReact()).append("} = ")
          .append(fmtK4(nProd)).append("\\,\\text{mol}\\]\n\n");

        // Paso 4 — masa/volumen teórico
        if (sc.productIsGas()) {
            double vTheo = nProd * VM_STP;
            sb.append("<strong>Paso 4 — Volumen teórico a c.n. (22,4 L/mol):</strong>\n\n")
              .append("\\[V_{\\text{teórico}} = ")
              .append(fmtK4(nProd)).append("\\,\\text{mol} \\times 22{,}4\\,\\text{L/mol} = ")
              .append(fmtK2(vTheo)).append("\\,\\text{L}\\]\n\n");
        } else {
            double mTheoG = nProd * sc.molarMassProduct();
            sb.append("<strong>Paso 4 — Masa teórica de producto:</strong>\n\n")
              .append("\\[m_{\\text{teórico}} = ")
              .append(fmtK4(nProd)).append("\\,\\text{mol} \\times ")
              .append(fmtK2(sc.molarMassProduct())).append("\\,\\text{g/mol} = ")
              .append(fmtK2(mTheoG)).append("\\,\\text{g}");
            if (isKg) {
                sb.append(" = ").append(fmtK2(mTheoG / 1000.0)).append("\\,\\text{kg}");
            }
            sb.append("\\]\n\n");
        }

        // Paso 5 — rendimiento
        sb.append("<strong>Paso 5 — Masa/volumen real (rendimiento η = ")
          .append(fmt1(sc.yieldPct())).append(" %):</strong>\n\n")
          .append("\\[")
          .append(sc.productIsGas() ? "V" : "m")
          .append("_{\\text{real}} = ")
          .append(sc.productIsGas() ? "V_{\\text{teórico}}" : "m_{\\text{teórico}}")
          .append(" \\times \\frac{\\eta}{100} = ")
          .append(sc.productIsGas()
              ? fmtK2(nProd * VM_STP)
              : (isKg ? fmtK2(nProd * sc.molarMassProduct() / 1000.0) + "\\,\\text{kg}" : fmtK2(nProd * sc.molarMassProduct()) + "\\,\\text{g}"))
          .append(" \\times \\frac{").append(fmtK1(sc.yieldPct())).append("}{100} = ")
          .append(fmtK2(answer)).append("\\,\\text{").append(unit).append("}\\]\n\n");

        sb.append("∴ Se obtienen \\(\\boxed{").append(fmtK2(answer))
          .append("\\,\\text{").append(unit).append("}}\\) de ")
          .append(sc.productName()).append(".");

        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — LimitingScenario
    // =========================================================================

    private String buildLimitingExplanation(LimitingScenario sc,
                                            double mPureAg, double mPureBg,
                                            double nA, double nB,
                                            double portA, double portB,
                                            boolean aIsLimiting,
                                            double nProduct, double mTheoG, double answer) {
        String fA       = formulaToKatex(sc.formulaA());
        String fB       = formulaToKatex(sc.formulaB());
        String fProduct = formulaToKatex(sc.productFormula());
        String limName  = aIsLimiting ? sc.nameA() : sc.nameB();
        String limForm  = aIsLimiting ? sc.formulaA() : sc.formulaB();
        var sb = new StringBuilder();

        sb.append("<strong>Ecuación ajustada:</strong> ").append(sc.equation())
          .append("\n\n");

        // Paso 1 — masas puras y moles
        sb.append("<strong>Paso 1 — Masas puras y moles iniciales:</strong>\n\n")
          .append("\\[m_{\\text{puro}\\,(").append(sc.formulaA()).append(")} = ")
          .append(fmtK2(sc.totalMassA())).append(" \\times \\frac{").append(fmtK1(sc.purityA())).append("}{100} = ")
          .append(fmtK2(mPureAg)).append("\\,\\text{g}\\]\n\n")
          .append("\\[n_{").append(fA).append("} = \\frac{")
          .append(fmtK2(mPureAg)).append("}{").append(fmtK2(sc.molarMassA())).append("} = ")
          .append(fmtK4(nA)).append("\\,\\text{mol}\\]\n\n")
          .append("\\[m_{\\text{puro}\\,(").append(sc.formulaB()).append(")} = ")
          .append(fmtK2(sc.totalMassB())).append(" \\times \\frac{").append(fmtK1(sc.purityB())).append("}{100} = ")
          .append(fmtK2(mPureBg)).append("\\,\\text{g}\\]\n\n")
          .append("\\[n_{").append(fB).append("} = \\frac{")
          .append(fmtK2(mPureBg)).append("}{").append(fmtK2(sc.molarMassB())).append("} = ")
          .append(fmtK4(nB)).append("\\,\\text{mol}\\]\n\n");

        // Paso 2 — reactivo limitante
        sb.append("<strong>Paso 2 — Determinación del reactivo limitante</strong>")
          .append(" (dividir moles entre el coeficiente estequiométrico):\n\n")
          .append("\\[\\frac{n_{").append(fA).append("}}{").append(sc.coeffA()).append("} = ")
          .append("\\frac{").append(fmtK4(nA)).append("}{").append(sc.coeffA()).append("} = ")
          .append(fmtK4(portA)).append("\\quad\\text{u.e.}\\]\n\n")
          .append("\\[\\frac{n_{").append(fB).append("}}{").append(sc.coeffB()).append("} = ")
          .append("\\frac{").append(fmtK4(nB)).append("}{").append(sc.coeffB()).append("} = ")
          .append(fmtK4(portB)).append("\\quad\\text{u.e.}\\]\n\n");

        double minPortion = Math.min(portA, portB);
        double maxPortion = Math.max(portA, portB);
        sb.append("Como \\(").append(fmtK4(minPortion)).append(" < ").append(fmtK4(maxPortion))
          .append("\\), el <strong>reactivo limitante es ").append(limName)
          .append(" (").append(limForm).append(")</strong>.\n\n");

        // Paso 3 — moles y masa teórica de producto
        double nLimiting    = aIsLimiting ? nA : nB;
        int    coeffLimiting = aIsLimiting ? sc.coeffA() : sc.coeffB();

        sb.append("<strong>Paso 3 — Moles y masa teórica de ").append(sc.productFormula()).append(":</strong>\n\n")
          .append("\\[n_{").append(fProduct).append("} = n_{\\text{limitante}} \\times \\frac{")
          .append(sc.coeffProduct()).append("}{").append(coeffLimiting).append("} = ")
          .append(fmtK4(nLimiting)).append(" \\times \\frac{").append(sc.coeffProduct())
          .append("}{").append(coeffLimiting).append("} = ").append(fmtK4(nProduct))
          .append("\\,\\text{mol}\\]\n\n")
          .append("\\[m_{\\text{teórico}} = ")
          .append(fmtK4(nProduct)).append("\\,\\text{mol} \\times ")
          .append(fmtK2(sc.molarMassProduct())).append("\\,\\text{g/mol} = ")
          .append(fmtK2(mTheoG)).append("\\,\\text{g}\\]\n\n");

        // Paso 4 — rendimiento
        sb.append("<strong>Paso 4 — Masa real (rendimiento η = ")
          .append(fmt1(sc.yieldPct())).append(" %):</strong>\n\n")
          .append("\\[m_{\\text{real}} = ").append(fmtK2(mTheoG))
          .append("\\,\\text{g} \\times \\frac{").append(fmtK1(sc.yieldPct())).append("}{100} = ")
          .append(fmtK2(answer)).append("\\,\\text{g}\\]\n\n");

        sb.append("∴ Se obtienen \\(\\boxed{").append(fmtK2(answer))
          .append("\\,\\text{g}}\\) de ").append(sc.productName()).append(".");

        return sb.toString();
    }

    // =========================================================================
    // FORMATEADORES
    // =========================================================================

    /** 1 decimal, coma española. Para porcentajes. */
    private String fmt1(double v) {
        return String.format("%.1f", v).replace(".", ",");
    }

    /** 2 decimales, coma española. Para masas, volúmenes y respuestas finales. */
    private String fmt2(double v) {
        return String.format("%.2f", v).replace(".", ",");
    }

    /** 4 decimales, coma española. Para moles en pasos intermedios. */
    private String fmt4(double v) {
        return String.format("%.4f", v).replace(".", ",");
    }

    /** KaTeX: 1 decimal con {,}. */
    private String fmtK1(double v) {
        return fmt1(v).replace(",", "{,}");
    }

    /** KaTeX: 2 decimales con {,}. */
    private String fmtK2(double v) {
        return fmt2(v).replace(",", "{,}");
    }

    /** KaTeX: 4 decimales con {,}. */
    private String fmtK4(double v) {
        return fmt4(v).replace(",", "{,}");
    }

    // =========================================================================
    // HELPER — convierte fórmula ASCII a KaTeX (ej: Fe2O3 → \text{Fe}_2\text{O}_3)
    // =========================================================================

    private String formulaToKatex(String formula) {
        var sb = new StringBuilder();
        Matcher m = Pattern.compile("([A-Z][a-z]?)(\\d*)").matcher(formula);
        while (m.find()) {
            sb.append("\\text{").append(m.group(1)).append("}");
            String digits = m.group(2);
            if (!digits.isEmpty() && !digits.equals("1")) {
                sb.append("_").append(digits);
            }
        }
        return sb.toString();
    }
}
