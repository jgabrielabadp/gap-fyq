package com.gap.fyq.service;

import com.gap.fyq.model.secondbach.chemicalequilibrium.EquilibriumType;
import com.gap.fyq.model.secondbach.chemicalequilibrium.SecondBachChemicalEquilibriumExercise;
import com.gap.fyq.repository.SecondBachChemicalEquilibriumRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecondBachChemicalEquilibriumService {

    private final SecondBachChemicalEquilibriumRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "2BACH_Q";
    private static final String BLOCK  = "BL3";

    // R en atm·L/(mol·K) — para la conversión Kc ↔ Kp
    private static final double R_ATM = 0.082;

    // =========================================================================
    // DATOS — Equilibrios homogéneos gaseosos (Kc/Kp/α)
    // =========================================================================

    /**
     * Reacción aA + bB ⇌ cC + dD.
     * Se proporcionan moles iniciales de reactivos, volumen V (L) y temperatura T (K).
     * La variable x representa los moles que reaccionan por unidad de volumen.
     *
     * askMode: "KC", "KP" o "ALPHA"
     */
    private record HomogeneousScenario(
        String reactionName,
        String reactionEq,
        int coeffA, int coeffB, int coeffC, int coeffD,
        String nameA, String nameB, String nameC, String nameD,
        double initMolesA, double initMolesB,   // moles iniciales de reactivos
        double volL,                             // volumen en litros
        double tempK,                            // temperatura en K
        double kc,                               // Kc calculado
        String askMode                           // "KC", "KP" o "ALPHA"
    ) {}

    private static final List<HomogeneousScenario> HOMO_SCENARIOS = buildHomoScenarios();

    private static List<HomogeneousScenario> buildHomoScenarios() {
        // N₂ + 3H₂ ⇌ 2NH₃ — síntesis Haber-Bosch, calcular Kc
        // Inicio: 1 mol N₂, 3 mol H₂, V=1 L, T=500 K, x=0.50 mol/L
        double x1 = 0.50;
        double kcHaber = Math.pow(2 * x1, 2) / ((1 - x1) * Math.pow(3 - 3 * x1, 3));

        // H₂ + I₂ ⇌ 2HI — calcular α (grado de disociación del HI inverso, planteado como síntesis)
        // Inicio: 1 mol H₂, 1 mol I₂, V=1 L, T=700 K, Kc=54.0
        // xEq tal que Kc = (2x)²/((1-x)*(1-x)) = 54 → 2x/(1-x)=√54 → x=√54/(2+√54)
        double kc_HI = 54.0;
        double sqrtKc_HI = Math.sqrt(kc_HI);
        double xHI = sqrtKc_HI / (2.0 + sqrtKc_HI);

        // PCl₅ ⇌ PCl₃ + Cl₂ — calcular α
        // Inicio: 1 mol PCl₅, V=2 L, T=523 K, Kc=0.045
        // [PCl₅]₀ = 0.5 M → Kc = (α·0.5)(α·0.5) / ((0.5 - α·0.5)) = α²·0.5/(1-α) = 0.045
        // → 0.5α² + 0.045α - 0.045 = 0 → discriminante
        double c0_pcl5 = 0.5;
        double kc_pcl5 = 0.045;
        // a=c0, b=kc_pcl5, c=-kc_pcl5 con variable α: c0*α² + kc_pcl5*α - kc_pcl5 = 0
        double alphaPCl5 = solveQuadraticPositive(c0_pcl5, kc_pcl5, -kc_pcl5);

        // SO₂ + ½O₂ ⇌ SO₃ (×2: 2SO₂+O₂⇌2SO₃) — calcular Kp dado Kc
        // Kc=280 a T=1000 K; Δn = 2-3 = -1
        double kc_so3 = 280.0;
        double kp_so3 = kc_so3 * Math.pow(R_ATM * 1000.0, -1);

        // CO + H₂O ⇌ CO₂ + H₂ (desplazamiento agua-gas) — calcular Kc
        // Inicio: 1 mol CO + 1 mol H₂O, V=1 L, T=800 K, x=0.80
        double xCO = 0.80;
        double kcCO = (xCO * xCO) / ((1 - xCO) * (1 - xCO));

        return List.of(
            // ── 1. Haber-Bosch — Kc ──────────────────────────────────────────────
            new HomogeneousScenario(
                "Síntesis del amoniaco (Haber-Bosch)",
                "N₂(g) + 3 H₂(g) ⇌ 2 NH₃(g)",
                1, 3, 2, 0,
                "N₂", "H₂", "NH₃", "—",
                1.0, 3.0, 1.0, 500.0,
                round2(kcHaber), "KC"
            ),
            // ── 2. H₂ + I₂ ⇌ 2HI — α ────────────────────────────────────────────
            new HomogeneousScenario(
                "Formación de HI",
                "H₂(g) + I₂(g) ⇌ 2 HI(g)",
                1, 1, 2, 0,
                "H₂", "I₂", "HI", "—",
                1.0, 1.0, 1.0, 700.0,
                kc_HI, "ALPHA"
            ),
            // ── 3. PCl₅ ⇌ PCl₃ + Cl₂ — α ────────────────────────────────────────
            new HomogeneousScenario(
                "Disociación del PCl₅",
                "PCl₅(g) ⇌ PCl₃(g) + Cl₂(g)",
                1, 0, 1, 1,
                "PCl₅", "—", "PCl₃", "Cl₂",
                1.0, 0.0, 2.0, 523.0,
                kc_pcl5, "ALPHA"
            ),
            // ── 4. 2SO₂ + O₂ ⇌ 2SO₃ — Kp dado Kc ───────────────────────────────
            new HomogeneousScenario(
                "Oxidación del SO₂ a SO₃",
                "2 SO₂(g) + O₂(g) ⇌ 2 SO₃(g)",
                2, 1, 2, 0,
                "SO₂", "O₂", "SO₃", "—",
                2.0, 1.0, 1.0, 1000.0,
                kc_so3, "KP"
            ),
            // ── 5. CO + H₂O ⇌ CO₂ + H₂ — Kc ────────────────────────────────────
            new HomogeneousScenario(
                "Desplazamiento del agua-gas",
                "CO(g) + H₂O(g) ⇌ CO₂(g) + H₂(g)",
                1, 1, 1, 1,
                "CO", "H₂O", "CO₂", "H₂",
                1.0, 1.0, 1.0, 800.0,
                round2(kcCO), "KC"
            )
        );
    }

    // =========================================================================
    // DATOS — Le Chatelier (MCQ cualitativo)
    // =========================================================================

    private record LeChatelierScenario(
        String reactionName,
        String reactionEq,
        String perturbation,
        String optionA, String optionB, String optionC,
        String correctLetter, String correctDisplay,
        String explanation
    ) {}

    private static final List<LeChatelierScenario> LECHAT_SCENARIOS = buildLeChatScenarios();

    private static List<LeChatelierScenario> buildLeChatScenarios() {
        return List.of(

            new LeChatelierScenario(
                "Síntesis del amoniaco",
                "N₂(g) + 3 H₂(g) ⇌ 2 NH₃(g)   ΔH = −92 kJ/mol",
                "Se aumenta la presión total del sistema a temperatura constante.",
                "A) El equilibrio se desplaza hacia los reactivos (hacia la izquierda).",
                "B) El equilibrio se desplaza hacia los productos (hacia la derecha).",
                "C) El equilibrio no se altera.",
                "B", "Desplazamiento hacia la derecha (↑ presión favorece el lado con menos moles de gas)",
                buildLeChatExpl(
                    "N₂(g) + 3 H₂(g) ⇌ 2 NH₃(g)",
                    "aumento de presión",
                    "La presión aumenta → el sistema reduce el volumen desplazándose "
                    + "hacia el lado con <strong>menos moles de gas</strong>. "
                    + "Reactivos: 1 + 3 = 4 mol gas. Productos: 2 mol gas. "
                    + "Como 2 < 4, el equilibrio se desplaza a la <strong>derecha</strong>.",
                    "B"
                )
            ),

            new LeChatelierScenario(
                "Síntesis del amoniaco — temperatura",
                "N₂(g) + 3 H₂(g) ⇌ 2 NH₃(g)   ΔH = −92 kJ/mol",
                "Se aumenta la temperatura del sistema a volumen constante.",
                "A) El equilibrio se desplaza hacia los productos (derecha).",
                "B) El equilibrio se desplaza hacia los reactivos (izquierda).",
                "C) El equilibrio no varía porque Kc no depende de la temperatura.",
                "B", "Desplazamiento hacia la izquierda (↑ T desplaza hacia la reacción endotérmica)",
                buildLeChatExpl(
                    "N₂(g) + 3 H₂(g) ⇌ 2 NH₃(g)",
                    "aumento de temperatura",
                    "La reacción directa es <strong>exotérmica</strong> (ΔH < 0). "
                    + "Al aumentar T, el sistema consume energía desplazándose en el sentido "
                    + "<strong>endotérmico</strong>, es decir, hacia los reactivos. "
                    + "Además, Kc <em>disminuye</em> al subir T en reacciones exotérmicas.",
                    "B"
                )
            ),

            new LeChatelierScenario(
                "Disociación del N₂O₄",
                "N₂O₄(g) ⇌ 2 NO₂(g)   ΔH = +57 kJ/mol",
                "Se añade N₂O₄ adicional al sistema en equilibrio a T constante.",
                "A) El equilibrio no se altera; Q = Kc sigue siendo válido.",
                "B) El equilibrio se desplaza hacia la izquierda.",
                "C) El equilibrio se desplaza hacia la derecha (↑ productos).",
                "C", "Desplazamiento hacia la derecha (Q < Kc al añadir reactivo)",
                buildLeChatExpl(
                    "N₂O₄(g) ⇌ 2 NO₂(g)",
                    "adición de N₂O₄",
                    "Al añadir más N₂O₄, [N₂O₄] aumenta. El cociente de reacción "
                    + "\\(Q = [\\text{NO}_2]^2/[\\text{N}_2\\text{O}_4]\\) cae por debajo de Kc. "
                    + "Para restaurar el equilibrio (Q → Kc), la reacción avanza hacia la "
                    + "<strong>derecha</strong>, consumiendo N₂O₄ y produciendo más NO₂.",
                    "C"
                )
            ),

            new LeChatelierScenario(
                "Oxidación del SO₂",
                "2 SO₂(g) + O₂(g) ⇌ 2 SO₃(g)   ΔH = −198 kJ/mol",
                "Se extrae SO₃ del sistema manteniendo T y V constantes.",
                "A) El equilibrio se desplaza hacia la izquierda.",
                "B) El equilibrio no varía porque Kc es constante a T fija.",
                "C) El equilibrio se desplaza hacia la derecha.",
                "C", "Desplazamiento hacia la derecha (Q > Kc al disminuir [SO₃])",
                buildLeChatExpl(
                    "2 SO₂(g) + O₂(g) ⇌ 2 SO₃(g)",
                    "extracción de SO₃",
                    "Reducir [SO₃] hace que \\(Q < K_c\\). El sistema produce más SO₃ "
                    + "desplazándose hacia la <strong>derecha</strong>, consumiendo SO₂ y O₂.",
                    "C"
                )
            ),

            new LeChatelierScenario(
                "Síntesis del HI — volumen",
                "H₂(g) + I₂(g) ⇌ 2 HI(g)   Δn = 0",
                "Se duplica el volumen del recipiente a temperatura constante.",
                "A) El equilibrio se desplaza hacia la derecha.",
                "B) El equilibrio se desplaza hacia la izquierda.",
                "C) El equilibrio no se altera.",
                "C", "Sin desplazamiento (Δn = 0, la presión no afecta)",
                buildLeChatExpl(
                    "H₂(g) + I₂(g) ⇌ 2 HI(g)",
                    "duplicación del volumen",
                    "El cambio de volumen/presión solo desplaza el equilibrio si "
                    + "\\(\\Delta n \\neq 0\\). Aquí: 1 + 1 = 2 mol reactivos, "
                    + "2 mol productos → \\(\\Delta n = 0\\). "
                    + "Al cambiar V, Q = Kc se mantiene y el sistema <strong>no se desplaza</strong>.",
                    "C"
                )
            ),

            new LeChatelierScenario(
                "Formación de NH₃ — catalizador",
                "N₂(g) + 3 H₂(g) ⇌ 2 NH₃(g)   ΔH = −92 kJ/mol",
                "Se añade un catalizador (Fe) al sistema en equilibrio.",
                "A) El equilibrio se desplaza hacia los productos.",
                "B) El equilibrio no se desplaza; solo aumenta la velocidad de ambas reacciones.",
                "C) El equilibrio se desplaza hacia los reactivos.",
                "B", "Sin desplazamiento (el catalizador no altera Kc)",
                buildLeChatExpl(
                    "N₂(g) + 3 H₂(g) ⇌ 2 NH₃(g)",
                    "adición de catalizador",
                    "Un catalizador reduce la energía de activación de la reacción directa "
                    + "<em>e inversa</em> en la misma proporción. Las velocidades de ambas "
                    + "reacciones aumentan por igual, por lo que K no cambia y el sistema "
                    + "<strong>alcanza el equilibrio más rápido sin desplazarse</strong>.",
                    "B"
                )
            )
        );
    }

    // =========================================================================
    // DATOS — Solubilidad y Ks (equilibrios heterogéneos)
    // =========================================================================

    /**
     * Sal AB₂ ⇌ A^(m+) + 2 B^(-) → Ks = [A][B]²
     * formulaType: "AB", "AB2", "A2B", "AB3"
     * ionCommonConc: concentración del ion común (0.0 si agua pura)
     * ionCommonName: nombre del ion común ("—" si agua pura)
     * askMode: "SOLUBILITY_PURE" o "SOLUBILITY_COMMON_ION"
     */
    private record SolubilityScenario(
        String saltName,
        String saltFormula,
        String cationName,
        String anionName,
        String formulaType,    // "AB", "AB2", "A2B", "AB3", "A3B2"
        double ks,
        double ionCommonConc,
        String ionCommonName,
        String askMode,
        double solubility      // respuesta esperada en mol/L
    ) {}

    private static final List<SolubilityScenario> SOLUBILITY_SCENARIOS = buildSolubilityScenarios();

    private static List<SolubilityScenario> buildSolubilityScenarios() {

        // AgCl ⇌ Ag⁺ + Cl⁻     Ks = 1.8e-10   s=√Ks  (agua pura)
        double ks_agcl = 1.8e-10;
        double s_agcl  = Math.sqrt(ks_agcl);

        // AgCl en NaCl 0.10 M → Cl⁻ común: Ks = s·(s + 0.10) ≈ s·0.10 → s = Ks/0.10
        double c_nacl   = 0.10;
        double s_agcl_ci = ks_agcl / c_nacl;

        // BaSO₄ ⇌ Ba²⁺ + SO₄²⁻  Ks = 1.1e-10  s=√Ks  (agua pura)
        double ks_baso4 = 1.1e-10;
        double s_baso4  = Math.sqrt(ks_baso4);

        // PbI₂ ⇌ Pb²⁺ + 2 I⁻    Ks = 9.8e-9   s³·4 = Ks → s = ∛(Ks/4)
        double ks_pbi2 = 9.8e-9;
        double s_pbi2  = Math.cbrt(ks_pbi2 / 4.0);

        // PbI₂ en KI 0.050 M → I⁻ común: Ks = s·(2s+0.050)² ≈ s·0.050² → s = Ks/0.0025
        double c_ki     = 0.050;
        double s_pbi2_ci = ks_pbi2 / (c_ki * c_ki);

        // Ag₂CrO₄ ⇌ 2 Ag⁺ + CrO₄²⁻  Ks = 1.12e-12  4s³ = Ks → s = ∛(Ks/4)
        double ks_ag2cro4 = 1.12e-12;
        double s_ag2cro4  = Math.cbrt(ks_ag2cro4 / 4.0);

        // Mg(OH)₂ ⇌ Mg²⁺ + 2 OH⁻   Ks = 5.61e-12  4s³ = Ks → s = ∛(Ks/4)
        double ks_mgoh2 = 5.61e-12;
        double s_mgoh2  = Math.cbrt(ks_mgoh2 / 4.0);

        // Ca₃(PO₄)₂ ⇌ 3 Ca²⁺ + 2 PO₄³⁻  Ks = 2.07e-33  108s⁵ = Ks → s = ⁵√(Ks/108)
        double ks_ca3po4 = 2.07e-33;
        double s_ca3po4  = Math.pow(ks_ca3po4 / 108.0, 0.2);

        return List.of(
            new SolubilityScenario("cloruro de plata", "AgCl",
                "Ag⁺", "Cl⁻", "AB", ks_agcl, 0.0, "—",
                "SOLUBILITY_PURE", s_agcl),

            new SolubilityScenario("cloruro de plata", "AgCl",
                "Ag⁺", "Cl⁻", "AB", ks_agcl, c_nacl, "Cl⁻ (NaCl " + c_nacl + " M)",
                "SOLUBILITY_COMMON_ION", s_agcl_ci),

            new SolubilityScenario("sulfato de bario", "BaSO₄",
                "Ba²⁺", "SO₄²⁻", "AB", ks_baso4, 0.0, "—",
                "SOLUBILITY_PURE", s_baso4),

            new SolubilityScenario("yoduro de plomo(II)", "PbI₂",
                "Pb²⁺", "I⁻", "AB2", ks_pbi2, 0.0, "—",
                "SOLUBILITY_PURE", s_pbi2),

            new SolubilityScenario("yoduro de plomo(II)", "PbI₂",
                "Pb²⁺", "I⁻", "AB2", ks_pbi2, c_ki, "I⁻ (KI " + c_ki + " M)",
                "SOLUBILITY_COMMON_ION", s_pbi2_ci),

            new SolubilityScenario("cromato de plata(I)", "Ag₂CrO₄",
                "Ag⁺", "CrO₄²⁻", "A2B", ks_ag2cro4, 0.0, "—",
                "SOLUBILITY_PURE", s_ag2cro4),

            new SolubilityScenario("hidróxido de magnesio", "Mg(OH)₂",
                "Mg²⁺", "OH⁻", "AB2", ks_mgoh2, 0.0, "—",
                "SOLUBILITY_PURE", s_mgoh2),

            new SolubilityScenario("fosfato de calcio", "Ca₃(PO₄)₂",
                "Ca²⁺", "PO₄³⁻", "A3B2", ks_ca3po4, 0.0, "—",
                "SOLUBILITY_PURE", s_ca3po4)
        );
    }

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public SecondBachChemicalEquilibriumExercise generateAndSave() {
        var ex = new SecondBachChemicalEquilibriumExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);

        int roll = random.nextInt(6);
        switch (roll) {
            case 0, 1 -> buildHomogeneous(ex);
            case 2    -> buildLeChatelier(ex);
            default   -> buildSolubility(ex);
        }

        log.debug("2BACH_Q BL3 generado: type={} mode={}",
            ex.getEquilibriumType(), ex.getExerciseMode());
        return repository.save(ex);
    }

    public SecondBachChemicalEquilibriumExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ejercicio 2BACH_Q BL3 no encontrado: " + id));
    }

    // =========================================================================
    // BUILDER — HOMOGENEOUS_KC_KP
    // =========================================================================

    private void buildHomogeneous(SecondBachChemicalEquilibriumExercise ex) {
        ex.setEquilibriumType(EquilibriumType.HOMOGENEOUS_KC_KP);

        HomogeneousScenario sc = HOMO_SCENARIOS.get(random.nextInt(HOMO_SCENARIOS.size()));

        switch (sc.askMode()) {
            case "KC"    -> buildKcExercise(ex, sc);
            case "KP"    -> buildKpExercise(ex, sc);
            default      -> buildAlphaExercise(ex, sc);
        }
    }

    private void buildKcExercise(SecondBachChemicalEquilibriumExercise ex,
                                   HomogeneousScenario sc) {
        ex.setExerciseMode("KC_VALUE");

        // Para escenario N₂+3H₂⇌2NH₃: x=0.50 es fijo; para CO+H₂O: x=0.80
        double xEq = sc.reactionName().contains("agua-gas") ? 0.80 : 0.50;
        double kc  = sc.kc();

        ex.setStatement(String.format(
            "Para la reacción <strong>%s</strong> en un recipiente de <em>V</em> = %.1f L "
            + "a <em>T</em> = %.0f K, se parte de %.1f mol de %s y %.1f mol de %s. "
            + "En el equilibrio se han formado %.2f mol de %s. "
            + "Calcula <em>K</em><sub>c</sub> (dos decimales).",
            sc.reactionEq(), sc.volL(), sc.tempK(),
            sc.initMolesA(), sc.nameA(), sc.initMolesB(), sc.nameB(),
            xEq * sc.coeffC(), sc.nameC()));

        ex.setCorrectAnswer(String.valueOf(round2(kc)));
        ex.setCorrectAnswerDisplay("Kc = " + round2(kc));
        ex.setUnit("adimensional");
        ex.setExplanation(buildKcExplanation(sc, xEq));
    }

    private void buildKpExercise(SecondBachChemicalEquilibriumExercise ex,
                                   HomogeneousScenario sc) {
        ex.setExerciseMode("KP_VALUE");
        int deltaN = sc.coeffC() + sc.coeffD() - sc.coeffA() - sc.coeffB();
        double kp  = sc.kc() * Math.pow(R_ATM * sc.tempK(), deltaN);

        ex.setStatement(String.format(
            "Para la reacción <strong>%s</strong> a <em>T</em> = %.0f K, "
            + "el valor de <em>K</em><sub>c</sub> = %.1f. "
            + "Calcula <em>K</em><sub>p</sub> sabiendo que <em>R</em> = 0,082 atm·L/(mol·K). "
            + "(dos decimales).",
            sc.reactionEq(), sc.tempK(), sc.kc()));

        ex.setCorrectAnswer(String.valueOf(round2(kp)));
        ex.setCorrectAnswerDisplay("Kp = " + round2(kp));
        ex.setUnit("adimensional");
        ex.setExplanation(buildKpExplanation(sc, kp, deltaN));
    }

    private void buildAlphaExercise(SecondBachChemicalEquilibriumExercise ex,
                                     HomogeneousScenario sc) {
        ex.setExerciseMode("ALPHA_VALUE");

        double alpha;
        if (sc.reactionName().contains("HI")) {
            // H₂+I₂⇌2HI: α = √Kc/(2+√Kc)
            double sq = Math.sqrt(sc.kc());
            alpha = sq / (2.0 + sq);
        } else {
            // PCl₅⇌PCl₃+Cl₂: cuadrática en α: c0·α²+Kc·α−Kc=0
            double c0 = sc.initMolesA() / sc.volL();
            alpha = solveQuadraticPositive(c0, sc.kc(), -sc.kc());
        }

        ex.setStatement(String.format(
            "Para la reacción <strong>%s</strong> en un recipiente de <em>V</em> = %.1f L "
            + "a <em>T</em> = %.0f K, se parte de %.1f mol de %s. "
            + "Sabiendo que <em>K</em><sub>c</sub> = %.3f, "
            + "calcula el grado de disociación α (tanto por uno, dos decimales).",
            sc.reactionEq(), sc.volL(), sc.tempK(),
            sc.initMolesA(), sc.nameA(), sc.kc()));

        ex.setCorrectAnswer(String.valueOf(round2(alpha)));
        ex.setCorrectAnswerDisplay("α = " + round2(alpha) + " (" + round2(alpha * 100) + " %)");
        ex.setUnit("adimensional (0–1)");
        ex.setExplanation(buildAlphaExplanation(sc, alpha));
    }

    // =========================================================================
    // BUILDER — LE_CHATELIER_PERTURBATION
    // =========================================================================

    private void buildLeChatelier(SecondBachChemicalEquilibriumExercise ex) {
        ex.setEquilibriumType(EquilibriumType.LE_CHATELIER_PERTURBATION);
        ex.setExerciseMode("LE_CHATELIER_MCQ");

        LeChatelierScenario sc = LECHAT_SCENARIOS.get(random.nextInt(LECHAT_SCENARIOS.size()));

        ex.setStatement(String.format(
            "Considera el equilibrio: <strong>%s</strong>.<br>"
            + "Perturbación aplicada: <em>%s</em><br>"
            + "¿En qué sentido se desplaza el equilibrio?",
            sc.reactionEq(), sc.perturbation()));

        ex.setOptionA(sc.optionA());
        ex.setOptionB(sc.optionB());
        ex.setOptionC(sc.optionC());
        ex.setCorrectAnswer(sc.correctLetter());
        ex.setCorrectAnswerDisplay(sc.correctDisplay());
        ex.setUnit("—");
        ex.setExplanation(sc.explanation());
    }

    // =========================================================================
    // BUILDER — SOLUBILITY_ION_COMMON
    // =========================================================================

    private void buildSolubility(SecondBachChemicalEquilibriumExercise ex) {
        ex.setEquilibriumType(EquilibriumType.SOLUBILITY_ION_COMMON);

        SolubilityScenario sc = SOLUBILITY_SCENARIOS.get(
            random.nextInt(SOLUBILITY_SCENARIOS.size()));

        ex.setExerciseMode(sc.askMode());

        String ksFormatted = formatSci(sc.ks());
        if ("SOLUBILITY_PURE".equals(sc.askMode())) {
            ex.setStatement(String.format(
                "El producto de solubilidad del <strong>%s (%s)</strong> es "
                + "<em>K</em><sub>s</sub> = %s. "
                + "Calcula la solubilidad <em>s</em> en agua pura (en mol/L, dos decimales "
                + "en notación científica).",
                sc.saltName(), sc.saltFormula(), ksFormatted));
        } else {
            ex.setStatement(String.format(
                "El producto de solubilidad del <strong>%s (%s)</strong> es "
                + "<em>K</em><sub>s</sub> = %s. "
                + "Calcula la solubilidad <em>s</em> en una disolución que contiene "
                + "<strong>%s</strong> (efecto del ion común). "
                + "Expresa el resultado en mol/L con dos decimales en notación científica.",
                sc.saltName(), sc.saltFormula(), ksFormatted, sc.ionCommonName()));
        }

        double s = sc.solubility();
        ex.setCorrectAnswer(String.valueOf(s));
        ex.setCorrectAnswerDisplay("s = " + formatSci(s) + " mol/L");
        ex.setUnit("mol/L");
        ex.setExplanation(buildSolubilityExplanation(sc));
    }

    // =========================================================================
    // CONSTRUCTORES DE EXPLICACIONES
    // =========================================================================

    private String buildKcExplanation(HomogeneousScenario sc, double xEq) {
        double v    = sc.volL();
        double cA0  = sc.initMolesA() / v;
        double cB0  = sc.initMolesB() / v;
        double cCeq = sc.coeffC() * xEq / v;
        double cAeq = cA0 - sc.coeffA() * xEq / v;
        double cBeq = (sc.coeffB() > 0) ? cB0 - sc.coeffB() * xEq / v : 0;

        var sb = new StringBuilder();
        sb.append("<strong>Resolución: cálculo de K<sub>c</sub></strong>\n\n");
        sb.append("\\[\\text{Reacción: }").append(latexReaction(sc)).append("\\]\n\n");

        // Tabla de evolución ICE
        sb.append(buildIceTable(sc, xEq, v));

        // Ley de acción de masas
        sb.append("\n\n<strong>Ley de acción de masas:</strong>\n\n");
        sb.append("\\[K_c = \\frac{[").append(sc.nameC()).append("]^").append(sc.coeffC())
          .append("}{[").append(sc.nameA()).append("]^").append(sc.coeffA());
        if (sc.coeffB() > 0) sb.append("[").append(sc.nameB()).append("]^").append(sc.coeffB());
        sb.append("}\\]\n\n");

        sb.append("\\[K_c = \\frac{(").append(round4(cCeq)).append(")^").append(sc.coeffC())
          .append("}{(").append(round4(cAeq)).append(")^").append(sc.coeffA());
        if (sc.coeffB() > 0)
            sb.append("\\cdot(").append(round4(cBeq)).append(")^").append(sc.coeffB());
        sb.append("} = ").append(round2(sc.kc())).append("\\]\n\n");

        sb.append("∴ <strong>K<sub>c</sub> = ").append(round2(sc.kc())).append("</strong>");
        return sb.toString();
    }

    private String buildKpExplanation(HomogeneousScenario sc, double kp, int deltaN) {
        var sb = new StringBuilder();
        sb.append("<strong>Conversión K<sub>c</sub> → K<sub>p</sub></strong>\n\n");
        sb.append("La relación entre K<sub>c</sub> y K<sub>p</sub> se obtiene de la "
            + "ley de gases ideales:\n\n");
        sb.append("\\[K_p = K_c \\cdot (RT)^{\\Delta n}\\]\n\n");
        sb.append("donde \\(\\Delta n = \\text{moles de gas productos} - "
            + "\\text{moles de gas reactivos}\\).\n\n");

        sb.append("<strong>Cálculo de Δn:</strong>\n\n");
        int prodMoles = sc.coeffC() + sc.coeffD();
        int reactMoles = sc.coeffA() + sc.coeffB();
        sb.append("\\[\\Delta n = ").append(prodMoles).append(" - ")
          .append(reactMoles).append(" = ").append(deltaN).append("\\]\n\n");

        sb.append("<strong>Sustitución:</strong>\n\n");
        sb.append("\\[K_p = ").append(sc.kc())
          .append(" \\cdot (0{,}082 \\times ").append((int) sc.tempK())
          .append(")^{").append(deltaN).append("}\\]\n\n");
        sb.append("\\[K_p = ").append(sc.kc())
          .append(" \\cdot (").append(round4(R_ATM * sc.tempK()))
          .append(")^{").append(deltaN).append("} = ").append(round2(kp)).append("\\]\n\n");

        sb.append("∴ <strong>K<sub>p</sub> = ").append(round2(kp)).append("</strong>");
        return sb.toString();
    }

    private String buildAlphaExplanation(HomogeneousScenario sc, double alpha) {
        double v   = sc.volL();
        double c0  = sc.initMolesA() / v;
        var sb     = new StringBuilder();

        sb.append("<strong>Resolución: grado de disociación α</strong>\n\n");
        sb.append("\\[\\text{Reacción: }").append(latexReaction(sc)).append("\\]\n\n");

        sb.append("Definimos α como la fracción de reactivo que ha reaccionado "
            + "(\\(0 < \\alpha < 1\\)).\n\n");

        if (sc.reactionName().contains("HI")) {
            // H₂ + I₂ ⇌ 2HI: ambos reactivos con mismos coeficientes
            sb.append(buildIceTableAlpha_sym(sc, alpha, v, c0));
            sb.append("\n\n<strong>Ley de acción de masas:</strong>\n\n");
            sb.append("\\[K_c = \\frac{(2\\alpha c_0)^2}{(c_0-\\alpha c_0)^2} "
                + "= \\frac{(2\\alpha)^2}{(1-\\alpha)^2} = \\left(\\frac{2\\alpha}{1-\\alpha}\\right)^2\\]\n\n");
            sb.append("\\[\\sqrt{K_c} = \\frac{2\\alpha}{1-\\alpha} "
                + "\\Rightarrow \\alpha = \\frac{\\sqrt{K_c}}{2+\\sqrt{K_c}}\\]\n\n");
            double sq = Math.sqrt(sc.kc());
            sb.append("\\[\\alpha = \\frac{\\sqrt{").append(sc.kc()).append("}}{2+\\sqrt{")
              .append(sc.kc()).append("}} = \\frac{").append(round4(sq))
              .append("}{2+").append(round4(sq)).append("} = ")
              .append(round2(alpha)).append("\\]\n\n");
        } else {
            // PCl₅ ⇌ PCl₃ + Cl₂
            sb.append(buildIceTableAlpha_pcl5(sc, alpha, v, c0));
            sb.append("\n\n<strong>Ley de acción de masas:</strong>\n\n");
            sb.append("\\[K_c = \\frac{(\\alpha c_0)(\\alpha c_0)}{c_0(1-\\alpha)} "
                + "= \\frac{\\alpha^2 c_0}{1-\\alpha}\\]\n\n");
            sb.append("\\[").append(sc.kc()).append(" = \\frac{\\alpha^2 \\cdot ")
              .append(c0).append("}{1-\\alpha}\\]\n\n");
            sb.append("<strong>Ecuación de 2º grado en α:</strong>\n\n");
            sb.append("\\[").append(c0).append("\\alpha^2 + ").append(sc.kc())
              .append("\\alpha - ").append(sc.kc()).append(" = 0\\]\n\n");
            double disc = sc.kc() * sc.kc() + 4 * c0 * sc.kc();
            sb.append("\\[\\alpha = \\frac{-").append(sc.kc()).append(" + \\sqrt{")
              .append(round4(disc)).append("}}{2 \\cdot ").append(c0)
              .append("} = ").append(round2(alpha)).append("\\]\n\n");
            sb.append("(Se descarta la raíz negativa por carecer de significado físico.)\n\n");
        }

        sb.append("∴ <strong>α = ").append(round2(alpha))
          .append(" → ").append(round2(alpha * 100)).append(" %</strong>");
        return sb.toString();
    }

    private static String buildLeChatExpl(String reaction, String perturbation,
                                           String reasoning, String correct) {
        var sb = new StringBuilder();
        sb.append("<strong>Principio de Le Chatelier y cociente de reacción Q</strong>\n\n");
        sb.append("Reacción: <strong>").append(reaction).append("</strong>\n\n");
        sb.append("Perturbación: <em>").append(perturbation).append("</em>\n\n");
        sb.append("<strong>Análisis:</strong>\n\n").append(reasoning).append("\n\n");
        sb.append("<strong>Criterio Q vs K:</strong>\n")
          .append("<ul>")
          .append("<li>Q < K → reacción avanza hacia la <strong>derecha</strong> "
              + "(forma más productos)</li>")
          .append("<li>Q > K → reacción avanza hacia la <strong>izquierda</strong> "
              + "(forma más reactivos)</li>")
          .append("<li>Q = K → sistema en <strong>equilibrio</strong></li>")
          .append("</ul>\n\n");
        sb.append("∴ Respuesta correcta: <strong>").append(correct).append("</strong>");
        return sb.toString();
    }

    private String buildSolubilityExplanation(SolubilityScenario sc) {
        var sb = new StringBuilder();
        sb.append("<strong>Equilibrio de solubilidad — ")
          .append(sc.saltFormula()).append("</strong>\n\n");

        // Ecuación de disociación y expresión de Ks
        String ksExpr = buildKsExpression(sc);
        sb.append(buildDissociationEquation(sc)).append("\n\n");
        sb.append("<strong>Expresión del producto de solubilidad:</strong>\n\n");
        sb.append("\\[K_s = ").append(ksExpr).append(" = ").append(formatSci(sc.ks())).append("\\]\n\n");

        if ("SOLUBILITY_PURE".equals(sc.askMode())) {
            sb.append("<strong>Tabla de equilibrio en agua pura:</strong>\n\n");
            sb.append(buildSolIceTable(sc, 0.0)).append("\n\n");
            sb.append("<strong>Sustitución en K<sub>s</sub>:</strong>\n\n");
            sb.append(buildSolSubstitution(sc, 0.0)).append("\n\n");
            sb.append("∴ <strong>s = ").append(formatSci(sc.solubility()))
              .append(" mol/L</strong>");
        } else {
            double ci = sc.ionCommonConc();
            sb.append("<strong>Efecto del ion común:</strong>\n\n");
            sb.append("La presencia de <strong>").append(sc.ionCommonName())
              .append("</strong> en la solución aumenta la concentración del ion común, "
                  + "desplazando el equilibrio hacia la izquierda "
                  + "(<em>principio de Le Chatelier</em>).\n\n");
            sb.append("<strong>Tabla de equilibrio con ion común:</strong>\n\n");
            sb.append(buildSolIceTable(sc, ci)).append("\n\n");
            sb.append("<strong>Aproximación de ion común</strong> (s ≪ ")
              .append(ci).append(" M):\n\n");
            sb.append(buildSolSubstitution(sc, ci)).append("\n\n");
            sb.append("∴ <strong>s = ").append(formatSci(sc.solubility()))
              .append(" mol/L</strong> "
                  + "(varias órdenes de magnitud menor que en agua pura → "
                  + "el ion común <em>reduce drásticamente</em> la solubilidad)");
        }
        return sb.toString();
    }

    // =========================================================================
    // HELPERS DE TABLAS ICE
    // =========================================================================

    private String buildIceTable(HomogeneousScenario sc, double xEq, double v) {
        double cA0 = sc.initMolesA() / v;
        double cB0 = (sc.coeffB() > 0) ? sc.initMolesB() / v : 0;

        var sb = new StringBuilder();
        sb.append("<strong>Tabla de evolución ICE (mol/L):</strong>\n\n");
        sb.append("<table class=\"table table-sm table-bordered\">")
          .append("<thead><tr><th>Especie</th><th>Inicio</th><th>Reacciona</th>"
              + "<th>Equilibrio</th></tr></thead><tbody>");

        // Reactivo A
        appendIceRow(sb, sc.nameA(), round4(cA0),
            "−" + sc.coeffA() + "x",
            round4(cA0 - sc.coeffA() * xEq / v));

        // Reactivo B (si existe)
        if (sc.coeffB() > 0) {
            appendIceRow(sb, sc.nameB(), round4(cB0),
                "−" + sc.coeffB() + "x",
                round4(cB0 - sc.coeffB() * xEq / v));
        }

        // Producto C
        appendIceRow(sb, sc.nameC(), "0",
            "+" + sc.coeffC() + "x",
            round4(sc.coeffC() * xEq / v));

        // Producto D (si existe)
        if (sc.coeffD() > 0) {
            appendIceRow(sb, sc.nameD(), "0",
                "+" + sc.coeffD() + "x",
                round4(sc.coeffD() * xEq / v));
        }

        sb.append("</tbody></table>\n")
          .append("Con x = ").append(round4(xEq / v)).append(" mol/L (concentración transformada).");
        return sb.toString();
    }

    private String buildIceTableAlpha_sym(HomogeneousScenario sc, double alpha,
                                           double v, double c0) {
        var sb = new StringBuilder();
        sb.append("<strong>Tabla ICE con variable α (H₂ + I₂ ⇌ 2 HI):</strong>\n\n");
        sb.append("<table class=\"table table-sm table-bordered\">")
          .append("<thead><tr><th>Especie</th><th>Inicio (mol/L)</th>"
              + "<th>Reacciona</th><th>Equilibrio</th></tr></thead><tbody>");
        appendIceRow(sb, sc.nameA(),  fmtC(c0), "−αc₀", fmtC(c0 * (1 - alpha)));
        appendIceRow(sb, sc.nameB(),  fmtC(c0), "−αc₀", fmtC(c0 * (1 - alpha)));
        appendIceRow(sb, sc.nameC(),  "0",       "+2αc₀", fmtC(2 * alpha * c0));
        sb.append("</tbody></table>");
        return sb.toString();
    }

    private String buildIceTableAlpha_pcl5(HomogeneousScenario sc, double alpha,
                                             double v, double c0) {
        var sb = new StringBuilder();
        sb.append("<strong>Tabla ICE con variable α (PCl₅ ⇌ PCl₃ + Cl₂):</strong>\n\n");
        sb.append("<table class=\"table table-sm table-bordered\">")
          .append("<thead><tr><th>Especie</th><th>Inicio (mol/L)</th>"
              + "<th>Reacciona</th><th>Equilibrio</th></tr></thead><tbody>");
        appendIceRow(sb, "PCl₅", fmtC(c0), "−αc₀", fmtC(c0 * (1 - alpha)));
        appendIceRow(sb, "PCl₃", "0", "+αc₀", fmtC(alpha * c0));
        appendIceRow(sb, "Cl₂",  "0", "+αc₀", fmtC(alpha * c0));
        sb.append("</tbody></table>");
        return sb.toString();
    }

    private String buildSolIceTable(SolubilityScenario sc, double commonIonConc) {
        String cation = sc.cationName();
        String anion  = sc.anionName();
        String[] stoich = getStoich(sc.formulaType());
        int nA = Integer.parseInt(stoich[0]);
        int nB = Integer.parseInt(stoich[1]);

        double initAnion = commonIonConc;  // ion común si existe

        var sb = new StringBuilder();
        sb.append("<table class=\"table table-sm table-bordered\">")
          .append("<thead><tr><th>Especie</th><th>Inicio (mol/L)</th>"
              + "<th>Disuelve</th><th>Equilibrio</th></tr></thead><tbody>");

        appendIceRow(sb, sc.saltFormula() + "(s)", "sólido", "+s", "disminuye");
        appendIceRow(sb, cation,
            "0",
            "+" + (nA > 1 ? nA : "") + "s",
            nA > 1 ? nA + "s" : "s");
        if (commonIonConc > 0) {
            appendIceRow(sb, anion,
                formatSci(initAnion),
                "+" + (nB > 1 ? nB : "") + "s",
                formatSci(initAnion) + (nB > 1 ? " + " + nB + "s" : " + s") + " ≈ " + formatSci(initAnion));
        } else {
            appendIceRow(sb, anion, "0",
                "+" + (nB > 1 ? nB : "") + "s",
                nB > 1 ? nB + "s" : "s");
        }

        sb.append("</tbody></table>");
        return sb.toString();
    }

    private String buildDissociationEquation(SolubilityScenario sc) {
        String[] stoich = getStoich(sc.formulaType());
        int nA = Integer.parseInt(stoich[0]);
        int nB = Integer.parseInt(stoich[1]);
        return "\\[" + sc.saltFormula() + "(s) \\rightleftharpoons "
            + (nA > 1 ? nA : "") + sc.cationName() + "(aq) + "
            + (nB > 1 ? nB : "") + sc.anionName() + "(aq)\\]";
    }

    private String buildKsExpression(SolubilityScenario sc) {
        String[] stoich = getStoich(sc.formulaType());
        int nA = Integer.parseInt(stoich[0]);
        int nB = Integer.parseInt(stoich[1]);
        String cE = "[" + sc.cationName() + "]" + (nA > 1 ? "^" + nA : "");
        String aE = "[" + sc.anionName()  + "]" + (nB > 1 ? "^" + nB : "");
        return cE + " \\cdot " + aE;
    }

    private String buildSolSubstitution(SolubilityScenario sc, double ci) {
        String[] stoich = getStoich(sc.formulaType());
        int nA = Integer.parseInt(stoich[0]);
        int nB = Integer.parseInt(stoich[1]);
        double s = sc.solubility();
        var sb = new StringBuilder();

        if (ci == 0) {
            sb.append("\\[K_s = (").append(nA > 1 ? nA + "s)" : "s)");
            if (nA > 1) sb.append("^").append(nA);
            sb.append(" \\cdot (").append(nB > 1 ? nB + "s)" : "s)");
            if (nB > 1) sb.append("^").append(nB);
            sb.append(" = ").append(formatSci(sc.ks())).append("\\]\n\n");
            // Desglosar la expresión de s
            int exp = nA + nB;
            double coeff = Math.pow(nA, nA) * Math.pow(nB, nB);
            sb.append("\\[").append(coeff > 1 ? round4(coeff) + " \\cdot " : "")
              .append("s^").append(exp).append(" = ").append(formatSci(sc.ks())).append("\\]\n\n");
            sb.append("\\[s = \\sqrt[").append(exp).append("]{\\frac{")
              .append(formatSci(sc.ks())).append("}{")
              .append(coeff > 1 ? round4(coeff) : "1")
              .append("}} = ").append(formatSci(s)).append("\\text{ mol/L}\\]");
        } else {
            // Aproximación: concentración ion común ≫ s
            sb.append("Como \\(s \\ll ").append(ci).append("\\) M, se aproxima:\n\n");
            sb.append("\\[K_s \\approx s \\cdot ").append(nB > 1 ? "(" + nB + " \\cdot " + ci + ")^" + nB : ci)
              .append(" = ").append(formatSci(sc.ks())).append("\\]\n\n");
            sb.append("\\[s = \\frac{").append(formatSci(sc.ks())).append("}{")
              .append(nB > 1 ? round4(Math.pow(nB * ci, nB)) : ci)
              .append("} = ").append(formatSci(s)).append("\\text{ mol/L}\\]");
        }
        return sb.toString();
    }

    private void appendIceRow(StringBuilder sb, String name,
                               Object init, String change, Object eq) {
        sb.append("<tr><td>").append(name).append("</td>")
          .append("<td>").append(init).append("</td>")
          .append("<td>").append(change).append("</td>")
          .append("<td>").append(eq).append("</td></tr>");
    }

    // =========================================================================
    // HELPERS MATEMÁTICOS
    // =========================================================================

    /** Resuelve ax²+bx+c=0 y devuelve la raíz positiva físicamente válida (0<x<máx). */
    public static double solveQuadraticPositive(double a, double b, double c) {
        double disc = b * b - 4 * a * c;
        if (disc < 0) throw new ArithmeticException("Discriminante negativo");
        double x1 = (-b + Math.sqrt(disc)) / (2 * a);
        double x2 = (-b - Math.sqrt(disc)) / (2 * a);
        if (x1 > 0 && x1 <= 1.0) return x1;
        if (x2 > 0 && x2 <= 1.0) return x2;
        // Si ninguna cae en (0,1), devolvemos la positiva más cercana a 0
        return (x1 > 0) ? x1 : x2;
    }

    private static String[] getStoich(String formulaType) {
        return switch (formulaType) {
            case "AB"   -> new String[]{"1", "1"};
            case "AB2"  -> new String[]{"1", "2"};
            case "A2B"  -> new String[]{"2", "1"};
            case "AB3"  -> new String[]{"1", "3"};
            case "A3B2" -> new String[]{"3", "2"};
            default     -> new String[]{"1", "1"};
        };
    }

    private static String latexReaction(HomogeneousScenario sc) {
        var sb = new StringBuilder();
        sb.append(sc.coeffA()).append("\\,\\text{").append(sc.nameA()).append("}");
        if (sc.coeffB() > 0)
            sb.append(" + ").append(sc.coeffB()).append("\\,\\text{").append(sc.nameB()).append("}");
        sb.append(" \\rightleftharpoons ");
        sb.append(sc.coeffC()).append("\\,\\text{").append(sc.nameC()).append("}");
        if (sc.coeffD() > 0)
            sb.append(" + ").append(sc.coeffD()).append("\\,\\text{").append(sc.nameD()).append("}");
        return sb.toString();
    }

    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
    private static double round4(double v) { return Math.round(v * 10000.0) / 10000.0; }

    private static String fmtC(double v)   { return String.valueOf(round4(v)); }

    private static String formatSci(double v) {
        if (v == 0) return "0";
        if (Math.abs(v) >= 0.01 && Math.abs(v) < 1000) return String.valueOf(round4(v));
        int exp = (int) Math.floor(Math.log10(Math.abs(v)));
        double mant = v / Math.pow(10, exp);
        return String.format("%.2f·10<sup>%d</sup>", mant, exp);
    }
}
