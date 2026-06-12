package com.gap.fyq.service;

import com.gap.fyq.model.secondbach.kineticsthermal.KineticsThermalType;
import com.gap.fyq.model.secondbach.kineticsthermal.SecondBachKineticsThermalExercise;
import com.gap.fyq.repository.SecondBachKineticsThermalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecondBachKineticsThermalService {

    private final SecondBachKineticsThermalRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "2BACH_Q";
    private static final String BLOCK  = "BL2";

    // ── Constantes universales ────────────────────────────────────────────────
    private static final double R          = 8.314;   // J/(mol·K)
    private static final double ABS_ZERO   = 273.15;  // K = 0 °C

    // =========================================================================
    // DATOS — Ciclos de Hess / Born-Haber
    // =========================================================================

    /**
     * Reacción representada como suma de pasos intermedios.
     * deltaH en kJ/mol; los valores de los pasos se dan directamente en kJ.
     * netDeltaH = Σ coeff_i * step_i
     */
    private record HessScenario(
        String reactionName,
        String reactionEq,
        List<HessStep> steps,
        double netDeltaHkJ,
        String explanationHtml
    ) {}

    private record HessStep(
        String label,
        String equation,
        double deltaHkJ,
        double coefficient   // +1 o -1 según si se invierte
    ) {}

    private static final List<HessScenario> HESS_SCENARIOS = buildHessScenarios();

    private static List<HessScenario> buildHessScenarios() {
        return List.of(

            // ── Combustión del carbono → CO₂ a partir de CO₂ y CO ──────────────
            new HessScenario(
                "Formación de CO₂ a partir de CO",
                "CO(g) + ½O₂(g) → CO₂(g)",
                List.of(
                    new HessStep("Combustión completa del C",
                        "C(s) + O₂(g) → CO₂(g)", -393.5, +1.0),
                    new HessStep("Combustión parcial del C (invertida)",
                        "CO(g) → C(s) + ½O₂(g)", +110.5, +1.0)
                ),
                -283.0,
                buildHessExpl(
                    "CO(g) + ½O₂(g) → CO₂(g)",
                    List.of(
                        new HessStep("C(s) + O₂(g) → CO₂(g)", "C(s) + O₂(g) → CO₂(g)", -393.5, +1.0),
                        new HessStep("CO(g) → C(s) + ½O₂(g) [invertida]",
                            "CO(g) → C(s) + ½O₂(g)", +110.5, +1.0)
                    ),
                    -283.0, "exotérmico", "La oxidación del monóxido de carbono a dióxido libera energía."
                )
            ),

            // ── Formación del NH₃ (Haber-Bosch) ─────────────────────────────────
            new HessScenario(
                "Síntesis del amoniaco (Haber-Bosch)",
                "½N₂(g) + 3/2 H₂(g) → NH₃(g)",
                List.of(
                    new HessStep("Formación directa de NH₃",
                        "½N₂(g) + 3/2 H₂(g) → NH₃(g)", -46.1, +1.0)
                ),
                -46.1,
                buildHessExpl(
                    "½N₂(g) + 3/2 H₂(g) → NH₃(g)",
                    List.of(new HessStep("ΔH°f(NH₃)", "½N₂(g) + 3/2 H₂(g) → NH₃(g)", -46.1, +1.0)),
                    -46.1, "exotérmico", "La formación del amoniaco es exotérmica; favorecida entálpicamente."
                )
            ),

            // ── Born-Haber: NaCl ─────────────────────────────────────────────────
            new HessScenario(
                "Energía reticular del NaCl (ciclo de Born-Haber)",
                "Na(s) + ½Cl₂(g) → NaCl(s)",
                List.of(
                    new HessStep("Entalpía de formación NaCl",
                        "Na(s) + ½Cl₂(g) → NaCl(s)",           -411.0, +1.0),
                    new HessStep("Sublimación del Na (invertida)",
                        "Na(g) → Na(s)",                          -108.0, +1.0),
                    new HessStep("Disociación del Cl₂ (invertida)",
                        "Cl(g) → ½Cl₂(g)",                       -121.5, +1.0),
                    new HessStep("1ª energía ionización Na (invertida)",
                        "Na(g) → Na⁺(g) + e⁻, invertida",       -496.0, +1.0),
                    new HessStep("Afinidad electrónica del Cl (invertida)",
                        "Cl⁻(g) → Cl(g) + e⁻",                  +349.0, +1.0)
                ),
                -788.0,
                buildBornHaberExpl(-411.0, +108.0, +121.5, +496.0, -349.0, -788.0)
            ),

            // ── Born-Haber: KF ───────────────────────────────────────────────────
            new HessScenario(
                "Energía reticular del KF (ciclo de Born-Haber)",
                "K(s) + ½F₂(g) → KF(s)",
                List.of(
                    new HessStep("ΔH°f(KF)", "K(s) + ½F₂(g) → KF(s)", -567.0, +1.0),
                    new HessStep("Sublimación K (inv.)", "K(g) → K(s)", -89.0,  +1.0),
                    new HessStep("Disociación ½F₂ (inv.)", "F(g) → ½F₂(g)", -79.0, +1.0),
                    new HessStep("IE₁ K (inv.)", "K⁺(g) + e⁻ → K(g)", -419.0, +1.0),
                    new HessStep("AE F (inv.)", "F⁻(g) → F(g) + e⁻",  +328.0, +1.0)
                ),
                -826.0,
                buildBornHaberExpl(-567.0, +89.0, +79.0, +419.0, -328.0, -826.0)
            ),

            // ── Combustión del etanol ────────────────────────────────────────────
            new HessScenario(
                "Combustión del etanol",
                "C₂H₅OH(l) + 3 O₂(g) → 2 CO₂(g) + 3 H₂O(l)",
                List.of(
                    new HessStep("ΔHf° CO₂ × 2",
                        "2 C(s) + 2 O₂(g) → 2 CO₂(g)", -787.0, +1.0),
                    new HessStep("ΔHf° H₂O × 3",
                        "3 H₂(g) + 3/2 O₂(g) → 3 H₂O(l)", -857.4, +1.0),
                    new HessStep("ΔHf° C₂H₅OH (invertida)",
                        "C₂H₅OH(l) → 2 C(s) + 3 H₂(g) + ½ O₂(g)", +277.7, +1.0)
                ),
                -1366.7,
                buildHessExpl(
                    "C₂H₅OH(l) + 3 O₂(g) → 2 CO₂(g) + 3 H₂O(l)",
                    List.of(
                        new HessStep("2×ΔHf°(CO₂)", "2 C(s) + 2 O₂(g) → 2 CO₂(g)", -787.0, +1.0),
                        new HessStep("3×ΔHf°(H₂O)", "3 H₂(g) + 3/2 O₂(g) → 3 H₂O(l)", -857.4, +1.0),
                        new HessStep("-ΔHf°(C₂H₅OH)", "C₂H₅OH → ..., invertida", +277.7, +1.0)
                    ),
                    -1366.7, "exotérmico",
                    "Combustión completa: todos los pasos elemental→producto son exotérmicos."
                )
            )
        );
    }

    // =========================================================================
    // DATOS — Gibbs / Espontaneidad
    // =========================================================================

    private record GibbsScenario(
        String reactionName,
        String reactionEq,
        double deltaHkJ,
        double deltaSJperK,   // en J/(mol·K)
        double tempK,
        boolean askDeltaG,    // true → calcular ΔG; false → calcular T_límite
        double expectedAnswer,
        String unit,
        String explanationHtml
    ) {}

    private static final List<GibbsScenario> GIBBS_SCENARIOS = buildGibbsScenarios();

    private static List<GibbsScenario> buildGibbsScenarios() {
        return List.of(

            // ── 1. Síntesis NH₃ — calcular ΔG a 298 K ───────────────────────────
            new GibbsScenario(
                "Síntesis del amoniaco a 298 K",
                "N₂(g) + 3 H₂(g) ⇌ 2 NH₃(g)",
                -92.4, -198.0, 298.15, true,
                round2(-92.4 - 298.15 * (-198.0 / 1000.0)), "kJ",
                buildGibbsExplDeltaG("N₂(g) + 3 H₂(g) ⇌ 2 NH₃(g)",
                    -92.4, -198.0, 298.15,
                    round2(-92.4 - 298.15 * (-198.0 / 1000.0)),
                    "La reacción es espontánea a 298 K (ΔG < 0) a pesar de la disminución de entropía, "
                    + "gracias a la fuerte contribución entálpica negativa."
                )
            ),

            // ── 2. Fusión del hielo a 310 K — calcular ΔG ───────────────────────
            new GibbsScenario(
                "Fusión del hielo a 310 K",
                "H₂O(s) → H₂O(l)",
                6.01, 22.1, 310.0, true,
                round2(6.01 - 310.0 * (22.1 / 1000.0)), "kJ",
                buildGibbsExplDeltaG("H₂O(s) → H₂O(l)",
                    6.01, 22.1, 310.0,
                    round2(6.01 - 310.0 * (22.1 / 1000.0)),
                    "Por encima de 273,15 K el proceso de fusión es espontáneo (ΔG < 0)."
                )
            ),

            // ── 3. Descomposición del CaCO₃ — temperatura límite ─────────────────
            new GibbsScenario(
                "Descomposición térmica del CaCO₃ — temperatura límite",
                "CaCO₃(s) → CaO(s) + CO₂(g)",
                177.9, 160.5, 0.0, false,
                round2(177.9 * 1000.0 / 160.5), "K",
                buildGibbsExplTLimit("CaCO₃(s) → CaO(s) + CO₂(g)",
                    177.9, 160.5,
                    round2(177.9 * 1000.0 / 160.5),
                    "Por encima de esa temperatura la generación de CO₂ hace el proceso espontáneo (+ΔH, +ΔS → espontáneo a T alta)."
                )
            ),

            // ── 4. Oxidación del SO₂ a SO₃ — calcular ΔG a 700 K ───────────────
            new GibbsScenario(
                "Oxidación SO₂ → SO₃ a 700 K",
                "2 SO₂(g) + O₂(g) → 2 SO₃(g)",
                -198.0, -187.9, 700.0, true,
                round2(-198.0 - 700.0 * (-187.9 / 1000.0)), "kJ",
                buildGibbsExplDeltaG("2 SO₂(g) + O₂(g) → 2 SO₃(g)",
                    -198.0, -187.9, 700.0,
                    round2(-198.0 - 700.0 * (-187.9 / 1000.0)),
                    "A 700 K el término entrópico penaliza la espontaneidad; existe T límite por encima de la cual la reacción se vuelve no espontánea."
                )
            ),

            // ── 5. Formación del NO — temperatura límite ─────────────────────────
            new GibbsScenario(
                "Formación del NO — temperatura límite",
                "N₂(g) + O₂(g) → 2 NO(g)",
                180.5, 24.8, 0.0, false,
                round2(180.5 * 1000.0 / 24.8), "K",
                buildGibbsExplTLimit("N₂(g) + O₂(g) → 2 NO(g)",
                    180.5, 24.8,
                    round2(180.5 * 1000.0 / 24.8),
                    "La reacción es endotérmica y aumenta levemente la entropía; solo es espontánea a temperaturas muy altas (descargas eléctricas, motores)."
                )
            ),

            // ── 6. Combustión del hidrógeno — ΔG a 298 K ────────────────────────
            new GibbsScenario(
                "Combustión del hidrógeno a 298 K",
                "H₂(g) + ½O₂(g) → H₂O(l)",
                -285.8, -163.2, 298.15, true,
                round2(-285.8 - 298.15 * (-163.2 / 1000.0)), "kJ",
                buildGibbsExplDeltaG("H₂(g) + ½O₂(g) → H₂O(l)",
                    -285.8, -163.2, 298.15,
                    round2(-285.8 - 298.15 * (-163.2 / 1000.0)),
                    "Reacción muy exotérmica y con disminución de entropía: ΔG < 0 a 298 K, espontánea en todo rango de T habitual."
                )
            )
        );
    }

    // =========================================================================
    // DATOS — Arrhenius
    // =========================================================================

    private record ArrheniusEaScenario(
        String reactionName,
        double k1, double T1K,
        double k2, double T2K,
        double eaKJPerMol,
        String explanationHtml
    ) {}

    private record ArrheniusOrderScenario(
        String reactionName,
        String reactionEq,
        String tableHtml,
        int orderA, int orderB,
        String explanationHtml
    ) {}

    private static final List<ArrheniusEaScenario> ARRHENIUS_EA_SCENARIOS = buildArrheniusEaScenarios();
    private static final List<ArrheniusOrderScenario> ARRHENIUS_ORDER_SCENARIOS = buildArrheniusOrderScenarios();

    private static List<ArrheniusEaScenario> buildArrheniusEaScenarios() {
        double ea1 = 50.0;
        double ea2 = 75.0;
        double ea3 = 100.0;
        double ea4 = 30.0;
        double ea5 = 120.0;

        double T1a = 300.0, T2a = 400.0;
        double k1a = 1.50e-3, k2a = k1a * Math.exp(-ea1 * 1000.0 / R * (1.0/T2a - 1.0/T1a));

        double T1b = 350.0, T2b = 500.0;
        double k1b = 2.80e-4, k2b = k1b * Math.exp(-ea2 * 1000.0 / R * (1.0/T2b - 1.0/T1b));

        double T1c = 400.0, T2c = 600.0;
        double k1c = 5.00e-5, k2c = k1c * Math.exp(-ea3 * 1000.0 / R * (1.0/T2c - 1.0/T1c));

        double T1d = 298.0, T2d = 323.0;
        double k1d = 3.20e-2, k2d = k1d * Math.exp(-ea4 * 1000.0 / R * (1.0/T2d - 1.0/T1d));

        double T1e = 500.0, T2e = 700.0;
        double k1e = 1.00e-6, k2e = k1e * Math.exp(-ea5 * 1000.0 / R * (1.0/T2e - 1.0/T1e));

        return List.of(
            new ArrheniusEaScenario("Descomposición del N₂O₅",
                k1a, T1a, k2a, T2a, ea1,
                buildArrheniusEaExpl("N₂O₅", k1a, T1a, k2a, T2a, ea1)),
            new ArrheniusEaScenario("Saponificación del acetato de etilo",
                k1b, T1b, k2b, T2b, ea2,
                buildArrheniusEaExpl("Saponificación", k1b, T1b, k2b, T2b, ea2)),
            new ArrheniusEaScenario("Pirolisis del etano",
                k1c, T1c, k2c, T2c, ea3,
                buildArrheniusEaExpl("Pirolisis C₂H₆", k1c, T1c, k2c, T2c, ea3)),
            new ArrheniusEaScenario("Isomerización del butano",
                k1d, T1d, k2d, T2d, ea4,
                buildArrheniusEaExpl("Isomerización butano", k1d, T1d, k2d, T2d, ea4)),
            new ArrheniusEaScenario("Oxidación del CO",
                k1e, T1e, k2e, T2e, ea5,
                buildArrheniusEaExpl("Oxidación CO", k1e, T1e, k2e, T2e, ea5))
        );
    }

    private static List<ArrheniusOrderScenario> buildArrheniusOrderScenarios() {
        return List.of(

            // A + B → P : orden 1 en A, orden 2 en B
            new ArrheniusOrderScenario(
                "Reacción A + B → P",
                "A + B → P",
                buildOrderTable(
                    new double[]{0.10, 0.20, 0.10},
                    new double[]{0.10, 0.10, 0.20},
                    new double[]{5.0e-4, 1.0e-3, 2.0e-3}
                ),
                1, 2,
                buildArrheniusOrderExpl("A + B → P",
                    new double[]{0.10, 0.20, 0.10},
                    new double[]{0.10, 0.10, 0.20},
                    new double[]{5.0e-4, 1.0e-3, 2.0e-3},
                    1, 2)
            ),

            // A + B → P : orden 2 en A, orden 1 en B
            new ArrheniusOrderScenario(
                "Reacción A + B → P (2,1)",
                "A + B → P",
                buildOrderTable(
                    new double[]{0.10, 0.20, 0.10},
                    new double[]{0.10, 0.10, 0.20},
                    new double[]{2.0e-3, 8.0e-3, 4.0e-3}
                ),
                2, 1,
                buildArrheniusOrderExpl("A + B → P",
                    new double[]{0.10, 0.20, 0.10},
                    new double[]{0.10, 0.10, 0.20},
                    new double[]{2.0e-3, 8.0e-3, 4.0e-3},
                    2, 1)
            ),

            // A + B → P : orden 1 en A, orden 1 en B
            new ArrheniusOrderScenario(
                "Reacción A + B → P (1,1)",
                "A + B → P",
                buildOrderTable(
                    new double[]{0.10, 0.20, 0.10},
                    new double[]{0.10, 0.10, 0.30},
                    new double[]{6.0e-4, 1.2e-3, 1.8e-3}
                ),
                1, 1,
                buildArrheniusOrderExpl("A + B → P",
                    new double[]{0.10, 0.20, 0.10},
                    new double[]{0.10, 0.10, 0.30},
                    new double[]{6.0e-4, 1.2e-3, 1.8e-3},
                    1, 1)
            )
        );
    }

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public SecondBachKineticsThermalExercise generateAndSave() {
        var ex = new SecondBachKineticsThermalExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);

        int roll = random.nextInt(5);
        switch (roll) {
            case 0 -> buildHess(ex);
            case 1 -> buildGibbsDeltaG(ex);
            case 2 -> buildGibbsTLimit(ex);
            case 3 -> buildArrheniusEa(ex);
            default -> buildArrheniusOrder(ex);
        }

        log.debug("2BACH_Q BL2 generado: type={} mode={}",
            ex.getKineticsThermalType(), ex.getExerciseMode());
        return repository.save(ex);
    }

    public SecondBachKineticsThermalExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ejercicio 2BACH_Q BL2 no encontrado: " + id));
    }

    // =========================================================================
    // BUILDER — HESS / BORN-HABER
    // =========================================================================

    private void buildHess(SecondBachKineticsThermalExercise ex) {
        ex.setKineticsThermalType(KineticsThermalType.HESS_BORN_HABER);
        ex.setExerciseMode("HESS_NUMERIC");

        HessScenario sc = HESS_SCENARIOS.get(random.nextInt(HESS_SCENARIOS.size()));
        ex.setStatement(String.format(
            "Usando la Ley de Hess, calcula la entalpía neta (ΔH, en kJ) para la reacción: "
            + "<strong>%s</strong>. Indica el resultado con su signo (positivo si endotérmica, "
            + "negativo si exotérmica).", sc.reactionEq()));
        ex.setCorrectAnswer(String.valueOf(round2(sc.netDeltaHkJ())));
        ex.setCorrectAnswerDisplay(round2(sc.netDeltaHkJ()) + " kJ");
        ex.setUnit("kJ");
        ex.setExplanation(sc.explanationHtml());
    }

    // =========================================================================
    // BUILDER — GIBBS ΔG
    // =========================================================================

    private void buildGibbsDeltaG(SecondBachKineticsThermalExercise ex) {
        ex.setKineticsThermalType(KineticsThermalType.GIBBS_SPONTANEITY);
        ex.setExerciseMode("GIBBS_DELTA_G");

        List<GibbsScenario> pool = GIBBS_SCENARIOS.stream()
            .filter(GibbsScenario::askDeltaG).toList();
        GibbsScenario sc = pool.get(random.nextInt(pool.size()));

        double deltaSkJ = sc.deltaSJperK() / 1000.0;
        String sign     = sc.deltaHkJ() < 0 ? "negativo (exotérmica)" : "positivo (endotérmica)";
        ex.setStatement(String.format(
            "Para la reacción <strong>%s</strong>, se conocen: "
            + "ΔH = %.1f kJ/mol; ΔS = %.1f J/(mol·K); T = %.2f K. "
            + "Calcula ΔG (en kJ, dos decimales, con signo). "
            + "Nota: ΔH es %s.",
            sc.reactionEq(), sc.deltaHkJ(), sc.deltaSJperK(), sc.tempK(), sign));
        ex.setCorrectAnswer(String.valueOf(sc.expectedAnswer()));
        ex.setCorrectAnswerDisplay(sc.expectedAnswer() + " kJ");
        ex.setUnit("kJ");
        ex.setExplanation(sc.explanationHtml());
    }

    // =========================================================================
    // BUILDER — GIBBS T LÍMITE
    // =========================================================================

    private void buildGibbsTLimit(SecondBachKineticsThermalExercise ex) {
        ex.setKineticsThermalType(KineticsThermalType.GIBBS_SPONTANEITY);
        ex.setExerciseMode("GIBBS_T_LIMIT");

        List<GibbsScenario> pool = GIBBS_SCENARIOS.stream()
            .filter(s -> !s.askDeltaG()).toList();
        GibbsScenario sc = pool.get(random.nextInt(pool.size()));

        ex.setStatement(String.format(
            "Para la reacción <strong>%s</strong> se conocen: "
            + "ΔH = %.1f kJ/mol; ΔS = %.1f J/(mol·K). "
            + "Calcula la temperatura límite de espontaneidad (T en Kelvin, dos decimales) "
            + "por encima de la cual ΔG < 0.",
            sc.reactionEq(), sc.deltaHkJ(), sc.deltaSJperK()));
        ex.setCorrectAnswer(String.valueOf(sc.expectedAnswer()));
        ex.setCorrectAnswerDisplay(sc.expectedAnswer() + " K");
        ex.setUnit("K");
        ex.setExplanation(sc.explanationHtml());
    }

    // =========================================================================
    // BUILDER — ARRHENIUS Ea
    // =========================================================================

    private void buildArrheniusEa(SecondBachKineticsThermalExercise ex) {
        ex.setKineticsThermalType(KineticsThermalType.ARRHENIUS_REACTION_RATE);
        ex.setExerciseMode("ARRHENIUS_EA");

        ArrheniusEaScenario sc = ARRHENIUS_EA_SCENARIOS.get(
            random.nextInt(ARRHENIUS_EA_SCENARIOS.size()));

        ex.setStatement(String.format(
            "Para la reacción «%s», la constante de velocidad vale "
            + "<em>k</em>₁ = %.3e s⁻¹ a T₁ = %.1f K y "
            + "<em>k</em>₂ = %.3e s⁻¹ a T₂ = %.1f K. "
            + "Calcula la energía de activación <em>E</em>ₐ (en kJ/mol, dos decimales). "
            + "Usa R = 8,314 J/(mol·K).",
            sc.reactionName(), sc.k1(), sc.T1K(), sc.k2(), sc.T2K()));
        ex.setCorrectAnswer(String.valueOf(round2(sc.eaKJPerMol())));
        ex.setCorrectAnswerDisplay(round2(sc.eaKJPerMol()) + " kJ/mol");
        ex.setUnit("kJ/mol");
        ex.setExplanation(sc.explanationHtml());
    }

    // =========================================================================
    // BUILDER — ARRHENIUS órdenes parciales
    // =========================================================================

    private void buildArrheniusOrder(SecondBachKineticsThermalExercise ex) {
        ex.setKineticsThermalType(KineticsThermalType.ARRHENIUS_REACTION_RATE);
        ex.setExerciseMode("ARRHENIUS_ORDER");

        ArrheniusOrderScenario sc = ARRHENIUS_ORDER_SCENARIOS.get(
            random.nextInt(ARRHENIUS_ORDER_SCENARIOS.size()));

        ex.setStatement(String.format(
            "Para la reacción <strong>%s</strong>, se dispone de la siguiente tabla de "
            + "velocidades iniciales:\n\n%s\n\n"
            + "Determina el orden parcial respecto a A (α), el orden parcial respecto a B (β) "
            + "y el orden global (n = α + β). "
            + "Introduce la respuesta como: <code>α|β|n</code> (p. ej. <code>1|2|3</code>).",
            sc.reactionEq(), sc.tableHtml()));
        int global = sc.orderA() + sc.orderB();
        ex.setCorrectAnswer(sc.orderA() + "|" + sc.orderB() + "|" + global);
        ex.setCorrectAnswerDisplay("α = " + sc.orderA() + ", β = " + sc.orderB()
            + ", orden global = " + global);
        ex.setUnit("—");
        ex.setExplanation(sc.explanationHtml());
    }

    // =========================================================================
    // CONSTRUCTORES DE EXPLICACIONES
    // =========================================================================

    private static String buildHessExpl(String reactionEq, List<HessStep> steps,
                                         double netKJ, String tipo, String context) {
        var sb = new StringBuilder();
        sb.append("<strong>Resolución por la Ley de Hess — ")
          .append(reactionEq).append("</strong>\n\n");
        sb.append("La <em>Ley de Hess</em> establece que la entalpía de reacción es independiente "
            + "del camino seguido. Se puede obtener combinando linealmente reacciones conocidas:\n\n");

        sb.append("\\[\\Delta H^\\circ_{\\text{neta}} = \\sum_i c_i\\,\\Delta H^\\circ_i\\]\n\n");

        sb.append("<strong>Pasos y sus entalpías:</strong>\n<ul>");
        double sum = 0.0;
        for (HessStep step : steps) {
            sb.append("<li>").append(step.label()).append(": ")
              .append(step.deltaHkJ()).append(" kJ</li>");
            sum += step.coefficient() * step.deltaHkJ();
        }
        sb.append("</ul>\n\n");

        sb.append("<strong>Suma:</strong>\n\n")
          .append("\\[\\Delta H^\\circ = ");
        for (int i = 0; i < steps.size(); i++) {
            HessStep s = steps.get(i);
            if (i > 0) sb.append(s.deltaHkJ() >= 0 ? " + " : " ");
            sb.append("(").append(s.deltaHkJ()).append(")");
        }
        sb.append(" = ").append(round2(netKJ)).append("\\text{ kJ}\\]\n\n");

        sb.append("<strong>Criterio de signos:</strong> ΔH <strong>").append(tipo)
          .append("</strong> (").append(tipo.equals("exotérmico")
            ? "se libera energía al entorno, ΔH < 0"
            : "el sistema absorbe energía del entorno, ΔH > 0").append(").\n\n");
        sb.append(context).append("\n\n");
        sb.append("∴ <strong>ΔH°<sub>neta</sub> = ").append(round2(netKJ)).append(" kJ</strong>");
        return sb.toString();
    }

    private static String buildBornHaberExpl(double hf, double sub, double diss,
                                              double ie, double ae, double uRed) {
        var sb = new StringBuilder();
        sb.append("<strong>Ciclo de Born-Haber para la sal iónica</strong>\n\n");
        sb.append("El ciclo de Born-Haber descompone la formación de un cristal iónico en "
            + "etapas termodinámicas mensurables:\n\n");
        sb.append("<ol>")
          .append("<li><strong>Entalpía de formación</strong> ΔH°<sub>f</sub> = ")
          .append(hf).append(" kJ (dato de partida)</li>")
          .append("<li><strong>Sublimación</strong> del metal: ΔH°<sub>sub</sub> = +")
          .append(sub).append(" kJ (+, endotérmico)</li>")
          .append("<li><strong>Disociación</strong> del halógeno (½X₂ → X·): ΔH°<sub>dis</sub> = +")
          .append(diss).append(" kJ (+, endotérmico)</li>")
          .append("<li><strong>1ª Energía de ionización</strong> del metal: IE₁ = +")
          .append(ie).append(" kJ (+, endotérmico)</li>")
          .append("<li><strong>Afinidad electrónica</strong> del halógeno: AE = ")
          .append(ae).append(" kJ (−, exotérmica)</li>")
          .append("<li><strong>Energía reticular</strong> U = ? (lo que queremos)</li>")
          .append("</ol>\n\n");
        sb.append("Aplicando Hess:\n\n")
          .append("\\[\\Delta H^\\circ_f = \\Delta H^\\circ_{\\text{sub}} + ")
          .append("\\Delta H^\\circ_{\\text{dis}} + IE_1 + AE + U\\]\n\n")
          .append("\\[U = \\Delta H^\\circ_f - (\\Delta H^\\circ_{\\text{sub}} + ")
          .append("\\Delta H^\\circ_{\\text{dis}} + IE_1 + AE)\\]\n\n")
          .append("\\[U = ").append(hf).append(" - (").append(sub).append(" + ")
          .append(diss).append(" + ").append(ie).append(" + (").append(ae).append("))\\]\n\n")
          .append("\\[U = ").append(round2(uRed)).append("\\text{ kJ/mol}\\]\n\n");
        sb.append("La energía reticular <strong>U < 0</strong>: la formación del cristal "
            + "iónico desde iones gaseosos <strong>libera energía</strong> (proceso exotérmico). "
            + "Valores muy negativos indican redes cristalinas muy estables.\n\n");
        sb.append("∴ <strong>U = ").append(round2(uRed)).append(" kJ/mol</strong>");
        return sb.toString();
    }

    private static String buildGibbsExplDeltaG(String eq, double dHkJ, double dSJperK,
                                                 double TK, double deltaGkJ, String context) {
        double dSkJ = dSJperK / 1000.0;
        var sb = new StringBuilder();
        sb.append("<strong>Energía de Gibbs — ΔG = ΔH − T·ΔS</strong>\n\n");
        sb.append("La ecuación de Gibbs permite predecir la <em>espontaneidad</em> de un proceso:\n\n")
          .append("\\[\\Delta G = \\Delta H - T \\cdot \\Delta S\\]\n\n");

        sb.append("<strong>Conversión de unidades de entropía:</strong>\n\n")
          .append("\\[\\Delta S = ").append(dSJperK).append("\\text{ J/(mol·K)} = ")
          .append(dSJperK).append(" \\div 1000 = ").append(dSkJ)
          .append("\\text{ kJ/(mol·K)}\\]\n\n");

        sb.append("<strong>Sustitución numérica:</strong>\n\n")
          .append("\\[\\Delta G = ").append(dHkJ).append(" - ").append(TK)
          .append(" \\times (").append(dSkJ).append(")\\]\n\n")
          .append("\\[\\Delta G = ").append(dHkJ).append(" - (")
          .append(round2(TK * dSkJ)).append(")\\]\n\n")
          .append("\\[\\Delta G = ").append(deltaGkJ).append("\\text{ kJ}\\]\n\n");

        sb.append("<strong>Criterio de espontaneidad:</strong>\n")
          .append("<ul>")
          .append("<li>ΔG < 0 → proceso <strong>espontáneo</strong></li>")
          .append("<li>ΔG = 0 → <strong>equilibrio</strong></li>")
          .append("<li>ΔG > 0 → proceso <strong>no espontáneo</strong> (requiere trabajo externo)</li>")
          .append("</ul>\n\n");

        sb.append(context).append("\n\n");
        sb.append("∴ <strong>ΔG = ").append(deltaGkJ).append(" kJ</strong> → ")
          .append(deltaGkJ < 0 ? "proceso <strong>espontáneo</strong>" : "proceso <strong>no espontáneo</strong>")
          .append(" a esa temperatura.");
        return sb.toString();
    }

    private static String buildGibbsExplTLimit(String eq, double dHkJ, double dSJperK,
                                                 double TlimK, String context) {
        double dSkJ = dSJperK / 1000.0;
        var sb = new StringBuilder();
        sb.append("<strong>Temperatura límite de espontaneidad (T tal que ΔG = 0)</strong>\n\n");
        sb.append("La condición de frontera entre espontáneo y no espontáneo es ΔG = 0:\n\n")
          .append("\\[0 = \\Delta H - T_{\\text{lím}} \\cdot \\Delta S\\]\n\n")
          .append("\\[T_{\\text{lím}} = \\frac{\\Delta H}{\\Delta S}\\]\n\n");

        sb.append("<strong>Conversión de entropía a kJ:</strong>\n\n")
          .append("\\[\\Delta S = ").append(dSJperK).append("\\text{ J/(mol·K)} = ")
          .append(dSkJ).append("\\text{ kJ/(mol·K)}\\]\n\n");

        sb.append("<strong>Sustitución:</strong>\n\n")
          .append("\\[T_{\\text{lím}} = \\frac{").append(dHkJ).append("}{")
          .append(dSkJ).append("} = ").append(TlimK).append("\\text{ K}\\]\n\n");

        sb.append("<strong>Análisis de signos:</strong>\n")
          .append("<ul>")
          .append("<li>ΔH > 0, ΔS > 0: espontáneo solo por encima de T<sub>lím</sub> (entropía favorece a alta T)</li>")
          .append("<li>ΔH < 0, ΔS < 0: espontáneo solo por debajo de T<sub>lím</sub> (entalpía favorece a baja T)</li>")
          .append("</ul>\n\n");

        sb.append(context).append("\n\n");
        sb.append("∴ <strong>T<sub>lím</sub> = ").append(TlimK).append(" K</strong>");
        return sb.toString();
    }

    private static String buildArrheniusEaExpl(String name, double k1, double T1,
                                                 double k2, double T2, double eaKJ) {
        double lnRatio  = Math.log(k2 / k1);
        double invDiff  = (1.0 / T1) - (1.0 / T2);
        double eaJCalc  = R * lnRatio / invDiff;
        var sb = new StringBuilder();
        sb.append("<strong>Ecuación de Arrhenius — cálculo de Eₐ</strong>\n\n");
        sb.append("La ecuación de Arrhenius en su forma diferencial relaciona dos pares (k, T):\n\n")
          .append("\\[\\ln\\frac{k_2}{k_1} = -\\frac{E_a}{R}\\left(\\frac{1}{T_2}-\\frac{1}{T_1}\\right)\\]\n\n");

        sb.append("Despejando Eₐ:\n\n")
          .append("\\[E_a = -R \\cdot \\frac{\\ln(k_2/k_1)}{\\dfrac{1}{T_2} - \\dfrac{1}{T_1}}\\]\n\n");

        sb.append("<strong>Sustitución numérica (datos):</strong>\n\n")
          .append("\\[k_1 = ").append(String.format("%.3e", k1))
          .append("\\text{ s}^{-1},\\quad T_1 = ").append(T1).append("\\text{ K}\\]\n")
          .append("\\[k_2 = ").append(String.format("%.3e", k2))
          .append("\\text{ s}^{-1},\\quad T_2 = ").append(T2).append("\\text{ K}\\]\n\n");

        sb.append("<strong>Paso 1 — Logaritmo neperiano del cociente de constantes:</strong>\n\n")
          .append("\\[\\ln\\frac{k_2}{k_1} = \\ln\\frac{")
          .append(String.format("%.3e", k2)).append("}{")
          .append(String.format("%.3e", k1)).append("} = ")
          .append(String.format("%.4f", lnRatio)).append("\\]\n\n");

        sb.append("<strong>Paso 2 — Diferencia de inversos de temperatura:</strong>\n\n")
          .append("\\[\\frac{1}{T_1} - \\frac{1}{T_2} = \\frac{1}{").append(T1)
          .append("} - \\frac{1}{").append(T2).append("} = ")
          .append(String.format("%.6e", invDiff)).append("\\text{ K}^{-1}\\]\n\n");

        sb.append("<strong>Paso 3 — Cálculo de Eₐ (en J/mol):</strong>\n\n")
          .append("\\[E_a = \\frac{").append(String.format("%.4f", lnRatio))
          .append("}{").append(String.format("%.6e", invDiff / R))
          .append("\\times 10^{-1}} = ").append(String.format("%.1f", eaJCalc))
          .append("\\text{ J/mol}\\]\n\n");

        sb.append("<strong>Paso 4 — Conversión a kJ/mol:</strong>\n\n")
          .append("\\[E_a = \\frac{").append(String.format("%.1f", eaJCalc))
          .append("}{1000} = ").append(round2(eaKJ)).append("\\text{ kJ/mol}\\]\n\n");

        sb.append("<strong>Nota sobre R:</strong> R = 8,314 J/(mol·K). Es imprescindible usar "
            + "Eₐ en <em>Julios</em> mientras se opera con R; convertir a kJ solo al final.\n\n");
        sb.append("∴ <strong>Eₐ = ").append(round2(eaKJ)).append(" kJ/mol</strong>");
        return sb.toString();
    }

    private static String buildArrheniusOrderExpl(String eq, double[] concA, double[] concB,
                                                    double[] rates, int alpha, int beta) {
        int global = alpha + beta;
        var sb = new StringBuilder();
        sb.append("<strong>Determinación de órdenes parciales de reacción</strong>\n\n");
        sb.append("La ecuación de velocidad tiene la forma:\n\n")
          .append("\\[v = k[A]^\\alpha [B]^\\beta\\]\n\n");

        sb.append("Comparamos experimentos variando una concentración cada vez:\n\n");

        // Razón α
        sb.append("<strong>Orden respecto a A (α): comparar experimentos 1 y 2</strong>\n\n");
        double ratioA = rates[1] / rates[0];
        double ratioCA = concA[1] / concA[0];
        sb.append("\\[\\frac{v_2}{v_1} = \\left(\\frac{[A]_2}{[A]_1}\\right)^\\alpha "
            + "\\Rightarrow \\frac{").append(String.format("%.1e", rates[1]))
          .append("}{").append(String.format("%.1e", rates[0])).append("} = \\left(\\frac{")
          .append(concA[1]).append("}{").append(concA[0]).append("}\\right)^\\alpha\\]\n\n")
          .append("\\[").append(String.format("%.1f", ratioA)).append(" = ")
          .append(String.format("%.1f", ratioCA)).append("^\\alpha "
          + "\\Rightarrow \\alpha = ").append(alpha).append("\\]\n\n");

        // Razón β
        sb.append("<strong>Orden respecto a B (β): comparar experimentos 1 y 3</strong>\n\n");
        double ratioBv = rates[2] / rates[0];
        double ratioCB = concB[2] / concB[0];
        sb.append("\\[\\frac{v_3}{v_1} = \\left(\\frac{[B]_3}{[B]_1}\\right)^\\beta "
            + "\\Rightarrow \\frac{").append(String.format("%.1e", rates[2]))
          .append("}{").append(String.format("%.1e", rates[0])).append("} = \\left(\\frac{")
          .append(concB[2]).append("}{").append(concB[0]).append("}\\right)^\\beta\\]\n\n")
          .append("\\[").append(String.format("%.1f", ratioBv)).append(" = ")
          .append(String.format("%.1f", ratioCB)).append("^\\beta "
          + "\\Rightarrow \\beta = ").append(beta).append("\\]\n\n");

        sb.append("<strong>Orden global:</strong> n = α + β = ")
          .append(alpha).append(" + ").append(beta).append(" = ").append(global).append("\n\n");

        sb.append("∴ <strong>α = ").append(alpha).append(", β = ").append(beta)
          .append(", orden global = ").append(global).append("</strong>");
        return sb.toString();
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static String buildOrderTable(double[] concA, double[] concB, double[] rates) {
        var sb = new StringBuilder();
        sb.append("<table class=\"table table-sm table-bordered table-striped\">")
          .append("<thead><tr>")
          .append("<th>Exp.</th><th>[A] (mol/L)</th><th>[B] (mol/L)</th>")
          .append("<th>v₀ (mol·L⁻¹·s⁻¹)</th></tr></thead><tbody>");
        for (int i = 0; i < concA.length; i++) {
            sb.append("<tr><td>").append(i + 1).append("</td>")
              .append("<td>").append(concA[i]).append("</td>")
              .append("<td>").append(concB[i]).append("</td>")
              .append("<td>").append(String.format("%.2e", rates[i])).append("</td></tr>");
        }
        sb.append("</tbody></table>");
        return sb.toString();
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
