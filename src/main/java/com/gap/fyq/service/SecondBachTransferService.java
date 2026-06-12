package com.gap.fyq.service;

import com.gap.fyq.model.secondbach.transfer.SecondBachTransferExercise;
import com.gap.fyq.model.secondbach.transfer.TransferType;
import com.gap.fyq.repository.SecondBachTransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecondBachTransferService {

    private final SecondBachTransferRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "2BACH_Q";
    private static final String BLOCK  = "BL4";

    // ── Constantes universales ────────────────────────────────────────────────
    public static final double KW        = 1.0e-14;   // producto iónico del agua a 25 °C
    public static final double FARADAY   = 96485.0;   // C/mol e⁻

    // =========================================================================
    // DATOS — Ácido-Base
    // =========================================================================

    private record StrongAcidScenario(
        String name, String formula, int protons,   // protons = H⁺ por mol
        double concentration, double pH
    ) {}

    private record StrongBaseScenario(
        String name, String formula, int hydroxyls,
        double concentration, double pH
    ) {}

    private record WeakAcidScenario(
        String name, String formula,
        double ka, double concentration,
        boolean useApprox,  // true si C0/Ka > 100 → aproximación válida
        double pH, double alpha
    ) {}

    private record WeakBaseScenario(
        String name, String formula,
        double kb, double concentration,
        boolean useApprox,
        double pH, double alpha
    ) {}

    private static final List<StrongAcidScenario> STRONG_ACIDS = buildStrongAcids();
    private static final List<StrongBaseScenario> STRONG_BASES = buildStrongBases();
    private static final List<WeakAcidScenario>   WEAK_ACIDS   = buildWeakAcids();
    private static final List<WeakBaseScenario>   WEAK_BASES   = buildWeakBases();

    private static List<StrongAcidScenario> buildStrongAcids() {
        return List.of(
            build_SA("ácido clorhídrico",    "HCl",   1, 0.10),
            build_SA("ácido clorhídrico",    "HCl",   1, 0.050),
            build_SA("ácido nítrico",        "HNO₃",  1, 0.20),
            build_SA("ácido nítrico",        "HNO₃",  1, 0.010),
            build_SA("ácido sulfúrico",      "H₂SO₄", 2, 0.050),
            build_SA("ácido perclórico",     "HClO₄", 1, 0.0010)
        );
    }

    private static StrongAcidScenario build_SA(String name, String formula,
                                                int protons, double c) {
        double pH = -Math.log10(protons * c);
        return new StrongAcidScenario(name, formula, protons, c, round2(pH));
    }

    private static List<StrongBaseScenario> buildStrongBases() {
        return List.of(
            build_SB("hidróxido de sodio",    "NaOH", 1, 0.10),
            build_SB("hidróxido de sodio",    "NaOH", 1, 0.050),
            build_SB("hidróxido de potasio",  "KOH",  1, 0.20),
            build_SB("hidróxido de bario",    "Ba(OH)₂", 2, 0.050),
            build_SB("hidróxido de calcio",   "Ca(OH)₂", 2, 0.010),
            build_SB("hidróxido de litio",    "LiOH", 1, 0.0010)
        );
    }

    private static StrongBaseScenario build_SB(String name, String formula,
                                                 int oh, double c) {
        double pOH = -Math.log10(oh * c);
        double pH  = 14.0 - pOH;
        return new StrongBaseScenario(name, formula, oh, c, round2(pH));
    }

    private static List<WeakAcidScenario> buildWeakAcids() {
        return List.of(
            build_WA("ácido acético",      "CH₃COOH",  1.8e-5, 0.10),
            build_WA("ácido acético",      "CH₃COOH",  1.8e-5, 0.50),
            build_WA("ácido fórmico",      "HCOOH",    1.77e-4, 0.10),
            build_WA("ácido fluorhídrico", "HF",       6.8e-4,  0.10),
            build_WA("ácido nitroso",      "HNO₂",     4.5e-4,  0.20),
            build_WA("ácido benzoico",     "C₆H₅COOH", 6.3e-5,  0.050),
            build_WA("ácido hipocloroso",  "HClO",     3.0e-8,  0.10),
            build_WA("ácido cianhídrico",  "HCN",      6.2e-10, 0.050)
        );
    }

    private static WeakAcidScenario build_WA(String name, String formula,
                                               double ka, double c0) {
        boolean approx = (c0 / ka) > 100.0;
        double x;
        if (approx) {
            x = Math.sqrt(ka * c0);
        } else {
            // cuadrática: x² + Ka·x - Ka·C0 = 0
            x = (-ka + Math.sqrt(ka * ka + 4 * ka * c0)) / 2.0;
        }
        double pH   = round2(-Math.log10(x));
        double alpha = round4(x / c0);
        return new WeakAcidScenario(name, formula, ka, c0, approx, pH, alpha);
    }

    private static List<WeakBaseScenario> buildWeakBases() {
        return List.of(
            build_WB("amoníaco",              "NH₃",        1.8e-5,  0.10),
            build_WB("amoníaco",              "NH₃",        1.8e-5,  0.50),
            build_WB("metilamina",            "CH₃NH₂",     4.4e-4,  0.10),
            build_WB("dimetilamina",          "(CH₃)₂NH",   5.1e-4,  0.050),
            build_WB("anilina",               "C₆H₅NH₂",   4.3e-10, 0.10),
            build_WB("piridina",              "C₅H₅N",      1.7e-9,  0.20)
        );
    }

    private static WeakBaseScenario build_WB(String name, String formula,
                                               double kb, double c0) {
        boolean approx = (c0 / kb) > 100.0;
        double x;
        if (approx) {
            x = Math.sqrt(kb * c0);
        } else {
            x = (-kb + Math.sqrt(kb * kb + 4 * kb * c0)) / 2.0;
        }
        double pOH  = -Math.log10(x);
        double pH   = round2(14.0 - pOH);
        double alpha = round4(x / c0);
        return new WeakBaseScenario(name, formula, kb, c0, approx, pH, alpha);
    }

    // =========================================================================
    // DATOS — Redox
    // =========================================================================

    private record OxStateScenario(
        String compound, String element, String context,
        int oxState, String explanation
    ) {}

    private record RedoxReactionScenario(
        String reactionName,
        String reactionUnbalanced,
        String medium,                  // "ácido" o "básico"
        String oxidant, String reductant,
        String[] coefficients,          // coeficientes clave en el mismo orden que la pregunta
        String coeffQuestion,           // pregunta sobre qué coeficientes pedir
        String optionA, String optionB, String optionC,
        String correctOxRedLetter,      // para MCQ oxidante/reductor
        String explanationHtml
    ) {}

    private static final List<OxStateScenario> OX_STATE_SCENARIOS = buildOxStateScenarios();
    private static final List<RedoxReactionScenario> REDOX_SCENARIOS = buildRedoxScenarios();

    private static List<OxStateScenario> buildOxStateScenarios() {
        return List.of(
            new OxStateScenario("KMnO₄", "Mn", "permanganato de potasio", +7,
                buildOxExpl("KMnO₄", "Mn", +7,
                    "K es +1 (metal alcalino), O es −2. "
                    + "\\[+1 + x + 4(-2) = 0 \\Rightarrow x = +7\\]")),
            new OxStateScenario("K₂Cr₂O₇", "Cr", "dicromato de potasio", +6,
                buildOxExpl("K₂Cr₂O₇", "Cr", +6,
                    "K es +1, O es −2. "
                    + "\\[2(+1) + 2x + 7(-2) = 0 \\Rightarrow 2x = 12 \\Rightarrow x = +6\\]")),
            new OxStateScenario("H₂SO₄", "S", "ácido sulfúrico", +6,
                buildOxExpl("H₂SO₄", "S", +6,
                    "H es +1, O es −2. "
                    + "\\[2(+1) + x + 4(-2) = 0 \\Rightarrow x = +6\\]")),
            new OxStateScenario("HNO₃", "N", "ácido nítrico", +5,
                buildOxExpl("HNO₃", "N", +5,
                    "H es +1, O es −2. "
                    + "\\[+1 + x + 3(-2) = 0 \\Rightarrow x = +5\\]")),
            new OxStateScenario("SO₂", "S", "dióxido de azufre", +4,
                buildOxExpl("SO₂", "S", +4,
                    "O es −2. "
                    + "\\[x + 2(-2) = 0 \\Rightarrow x = +4\\]")),
            new OxStateScenario("Fe₂O₃", "Fe", "óxido de hierro(III)", +3,
                buildOxExpl("Fe₂O₃", "Fe", +3,
                    "O es −2. "
                    + "\\[2x + 3(-2) = 0 \\Rightarrow x = +3\\]")),
            new OxStateScenario("Cr₂O₃", "Cr", "óxido de cromo(III)", +3,
                buildOxExpl("Cr₂O₃", "Cr", +3,
                    "O es −2. "
                    + "\\[2x + 3(-2) = 0 \\Rightarrow x = +3\\]")),
            new OxStateScenario("MnO₂", "Mn", "dióxido de manganeso", +4,
                buildOxExpl("MnO₂", "Mn", +4,
                    "O es −2. "
                    + "\\[x + 2(-2) = 0 \\Rightarrow x = +4\\]")),
            new OxStateScenario("Na₂S₂O₃", "S", "tiosulfato de sodio", +2,
                buildOxExpl("Na₂S₂O₃", "S", +2,
                    "Na es +1, O es −2. "
                    + "\\[2(+1) + 2x + 3(-2) = 0 \\Rightarrow 2x = 4 \\Rightarrow x = +2\\]")),
            new OxStateScenario("ClO₃⁻", "Cl", "ion clorato", +5,
                buildOxExpl("ClO₃⁻", "Cl", +5,
                    "O es −2, carga total −1. "
                    + "\\[x + 3(-2) = -1 \\Rightarrow x = +5\\]"))
        );
    }

    private static List<RedoxReactionScenario> buildRedoxScenarios() {
        return List.of(

            // MnO₄⁻ + Fe²⁺ → Mn²⁺ + Fe³⁺  (medio ácido)
            // Ajustada: MnO₄⁻ + 5Fe²⁺ + 8H⁺ → Mn²⁺ + 5Fe³⁺ + 4H₂O
            new RedoxReactionScenario(
                "Permanganato con hierro(II)",
                "MnO₄⁻ + Fe²⁺ → Mn²⁺ + Fe³⁺",
                "ácido",
                "MnO₄⁻ (Mn pasa de +7 a +2)", "Fe²⁺ (Fe pasa de +2 a +3)",
                new String[]{"1","5","8","1","5","4"},
                "Tras ajustar, introduce los coeficientes de: "
                + "<code>MnO₄⁻ | Fe²⁺ | H⁺ | Mn²⁺ | Fe³⁺ | H₂O</code> "
                + "separados por <code>|</code>.",
                "A) Oxidante: Fe²⁺; reductor: MnO₄⁻",
                "B) Oxidante: MnO₄⁻; reductor: Fe²⁺",
                "C) Oxidante: H⁺; reductor: Fe²⁺",
                "B",
                buildRedoxExpl(
                    "MnO₄⁻ + 5 Fe²⁺ + 8 H⁺ → Mn²⁺ + 5 Fe³⁺ + 4 H₂O",
                    "ácido",
                    new String[]{
                        "\\text{Reducción: } \\text{MnO}_4^- + 8\\text{H}^+ + 5e^- \\rightarrow \\text{Mn}^{2+} + 4\\text{H}_2\\text{O}",
                        "\\text{Oxidación: } \\text{Fe}^{2+} \\rightarrow \\text{Fe}^{3+} + e^-"
                    },
                    "MnO₄⁻ (Mn: +7 → +2, gana 5 e⁻)", "Fe²⁺ (Fe: +2 → +3, pierde 1 e⁻)",
                    "Se multiplica la oxidación ×5 para igualar electrones transferidos."
                )
            ),

            // Cr₂O₇²⁻ + I⁻ → Cr³⁺ + I₂  (medio ácido)
            // Ajustada: Cr₂O₇²⁻ + 6I⁻ + 14H⁺ → 2Cr³⁺ + 3I₂ + 7H₂O
            new RedoxReactionScenario(
                "Dicromato con yoduro",
                "Cr₂O₇²⁻ + I⁻ → Cr³⁺ + I₂",
                "ácido",
                "Cr₂O₇²⁻ (Cr pasa de +6 a +3)", "I⁻ (I pasa de −1 a 0)",
                new String[]{"1","6","14","2","3","7"},
                "Introduce los coeficientes de: "
                + "<code>Cr₂O₇²⁻ | I⁻ | H⁺ | Cr³⁺ | I₂ | H₂O</code> "
                + "separados por <code>|</code>.",
                "A) Oxidante: I⁻; reductor: Cr₂O₇²⁻",
                "B) Oxidante: H⁺; reductor: I⁻",
                "C) Oxidante: Cr₂O₇²⁻; reductor: I⁻",
                "C",
                buildRedoxExpl(
                    "Cr₂O₇²⁻ + 6 I⁻ + 14 H⁺ → 2 Cr³⁺ + 3 I₂ + 7 H₂O",
                    "ácido",
                    new String[]{
                        "\\text{Reducción: } \\text{Cr}_2\\text{O}_7^{2-} + 14\\text{H}^+ + 6e^- \\rightarrow 2\\text{Cr}^{3+} + 7\\text{H}_2\\text{O}",
                        "\\text{Oxidación: } 2\\text{I}^- \\rightarrow \\text{I}_2 + 2e^-"
                    },
                    "Cr₂O₇²⁻ (Cr: +6 → +3, gana 6 e⁻ en total)", "I⁻ (I: −1 → 0, pierde 1 e⁻ por átomo)",
                    "Oxidación ×3 para igualar 6 electrones. Cada Cr gana 3e⁻; hay 2 Cr → 6e⁻ totales."
                )
            ),

            // MnO₄⁻ + SO₂ → Mn²⁺ + SO₄²⁻  (medio ácido)
            // Ajustada: 2MnO₄⁻ + 5SO₂ + 2H₂O → 2Mn²⁺ + 5SO₄²⁻ + 4H⁺
            new RedoxReactionScenario(
                "Permanganato con SO₂",
                "MnO₄⁻ + SO₂ → Mn²⁺ + SO₄²⁻",
                "ácido",
                "MnO₄⁻ (Mn: +7 → +2)", "SO₂ (S: +4 → +6)",
                new String[]{"2","5","2","2","5","4"},
                "Introduce los coeficientes de: "
                + "<code>MnO₄⁻ | SO₂ | H₂O | Mn²⁺ | SO₄²⁻ | H⁺</code> "
                + "separados por <code>|</code>.",
                "A) Oxidante: SO₂; reductor: MnO₄⁻",
                "B) Oxidante: MnO₄⁻; reductor: SO₂",
                "C) Oxidante: MnO₄⁻; reductor: H₂O",
                "B",
                buildRedoxExpl(
                    "2 MnO₄⁻ + 5 SO₂ + 2 H₂O → 2 Mn²⁺ + 5 SO₄²⁻ + 4 H⁺",
                    "ácido",
                    new String[]{
                        "\\text{Reducción: } \\text{MnO}_4^- + 8\\text{H}^+ + 5e^- \\rightarrow \\text{Mn}^{2+} + 4\\text{H}_2\\text{O}",
                        "\\text{Oxidación: } \\text{SO}_2 + 2\\text{H}_2\\text{O} \\rightarrow \\text{SO}_4^{2-} + 4\\text{H}^+ + 2e^-"
                    },
                    "MnO₄⁻ (Mn: +7 → +2, gana 5 e⁻)", "SO₂ (S: +4 → +6, pierde 2 e⁻)",
                    "Reducción ×2, oxidación ×5 → 10 e⁻ transferidos."
                )
            ),

            // Cl₂ + NaOH → NaCl + NaClO₃ (dismutación en medio básico)
            // Ajustada: 3Cl₂ + 6OH⁻ → 5Cl⁻ + ClO₃⁻ + 3H₂O
            new RedoxReactionScenario(
                "Dismutación del Cl₂ en medio básico",
                "Cl₂ + OH⁻ → Cl⁻ + ClO₃⁻ + H₂O",
                "básico",
                "Cl₂ actúa simultáneamente como oxidante (→ ClO₃⁻) y reductor (→ Cl⁻)",
                "Cl₂ (dismutación)",
                new String[]{"3","6","5","1","3"},
                "Introduce los coeficientes de: "
                + "<code>Cl₂ | OH⁻ | Cl⁻ | ClO₃⁻ | H₂O</code> "
                + "separados por <code>|</code>.",
                "A) Oxidante: OH⁻; reductor: Cl₂",
                "B) Oxidante: Cl₂; reductor: OH⁻",
                "C) Cl₂ actúa como oxidante y reductor simultáneamente (dismutación)",
                "C",
                buildRedoxExpl(
                    "3 Cl₂ + 6 OH⁻ → 5 Cl⁻ + ClO₃⁻ + 3 H₂O",
                    "básico",
                    new String[]{
                        "\\text{Reducción: } \\text{Cl}_2 + 2e^- \\rightarrow 2\\text{Cl}^-",
                        "\\text{Oxidación: } \\text{Cl}_2 + 12\\text{OH}^- \\rightarrow 2\\text{ClO}_3^- + 6\\text{H}_2\\text{O} + 10e^-"
                    },
                    "Cl₂ (Cl: 0 → −1 en Cl⁻, gana e⁻)", "Cl₂ (Cl: 0 → +5 en ClO₃⁻, pierde e⁻)",
                    "Reducción ×5, oxidación ×1 → 10 e⁻. Reacción de dismutación."
                )
            )
        );
    }

    // =========================================================================
    // DATOS — Electroquímica / Faraday
    // =========================================================================

    private record GalvanicCellScenario(
        String cellName,
        String anodeHalfReaction, String cathodeHalfReaction,
        String anode, String cathode,
        double e_anode, double e_cathode,   // potenciales estándar de REDUCCIÓN
        double emf                          // E° = E°cátodo − E°ánodo
    ) {}

    private record ElectrolysisScenario(
        String cathodeReaction,
        String depositedSpecies,
        double molarMass,            // g/mol
        int    electronsPerIon,      // n en la semirreacción
        double currentA,             // intensidad en A
        double timeS,                // tiempo en segundos
        double massG                 // masa depositada en g
    ) {}

    private static final List<GalvanicCellScenario>  GALVANIC_SCENARIOS    = buildGalvanicScenarios();
    private static final List<ElectrolysisScenario>  ELECTROLYSIS_SCENARIOS = buildElectrolysisScenarios();

    private static List<GalvanicCellScenario> buildGalvanicScenarios() {
        return List.of(
            buildCell("Daniell (Zn/Cu)",
                "Zn²⁺ + 2e⁻ → Zn", "Cu²⁺ + 2e⁻ → Cu",
                "Zn / Zn²⁺", "Cu²⁺ / Cu", -0.76, +0.34),
            buildCell("Zn/Ag",
                "Zn²⁺ + 2e⁻ → Zn", "Ag⁺ + e⁻ → Ag",
                "Zn / Zn²⁺", "Ag⁺ / Ag", -0.76, +0.80),
            buildCell("Fe/Cu",
                "Fe²⁺ + 2e⁻ → Fe", "Cu²⁺ + 2e⁻ → Cu",
                "Fe / Fe²⁺", "Cu²⁺ / Cu", -0.44, +0.34),
            buildCell("Zn/Sn",
                "Zn²⁺ + 2e⁻ → Zn", "Sn²⁺ + 2e⁻ → Sn",
                "Zn / Zn²⁺", "Sn²⁺ / Sn", -0.76, -0.14),
            buildCell("Ni/Ag",
                "Ni²⁺ + 2e⁻ → Ni", "Ag⁺ + e⁻ → Ag",
                "Ni / Ni²⁺", "Ag⁺ / Ag", -0.25, +0.80),
            buildCell("Fe/Ag",
                "Fe²⁺ + 2e⁻ → Fe", "Ag⁺ + e⁻ → Ag",
                "Fe / Fe²⁺", "Ag⁺ / Ag", -0.44, +0.80),
            buildCell("Al/Cu",
                "Al³⁺ + 3e⁻ → Al", "Cu²⁺ + 2e⁻ → Cu",
                "Al / Al³⁺", "Cu²⁺ / Cu", -1.66, +0.34),
            buildCell("Mg/Fe",
                "Mg²⁺ + 2e⁻ → Mg", "Fe²⁺ + 2e⁻ → Fe",
                "Mg / Mg²⁺", "Fe²⁺ / Fe", -2.37, -0.44)
        );
    }

    private static GalvanicCellScenario buildCell(String name,
            String anodeHR, String cathodeHR, String anode, String cathode,
            double eA, double eC) {
        double emf = round2(eC - eA);
        return new GalvanicCellScenario(name, anodeHR, cathodeHR, anode, cathode, eA, eC, emf);
    }

    private static List<ElectrolysisScenario> buildElectrolysisScenarios() {
        return List.of(
            buildElec("Cu²⁺ + 2e⁻ → Cu", "Cu",   63.55, 2, 2.0,  30 * 60),
            buildElec("Ag⁺ + e⁻ → Ag",   "Ag",  107.87, 1, 1.5,  45 * 60),
            buildElec("Ag⁺ + e⁻ → Ag",   "Ag",  107.87, 1, 3.0,  20 * 60),
            buildElec("Cu²⁺ + 2e⁻ → Cu", "Cu",   63.55, 2, 5.0,  15 * 60),
            buildElec("Au³⁺ + 3e⁻ → Au", "Au",  196.97, 3, 1.0,  60 * 60),
            buildElec("Ni²⁺ + 2e⁻ → Ni", "Ni",   58.69, 2, 4.0,  25 * 60),
            buildElec("Zn²⁺ + 2e⁻ → Zn", "Zn",   65.38, 2, 3.0,  40 * 60),
            buildElec("Cr³⁺ + 3e⁻ → Cr", "Cr",   52.00, 3, 2.5,  50 * 60)
        );
    }

    private static ElectrolysisScenario buildElec(String reaction, String species,
            double mm, int n, double I, double t) {
        // m = (M · I · t) / (n · F)
        double mass = round2((mm * I * t) / (n * FARADAY));
        return new ElectrolysisScenario(reaction, species, mm, n, I, t, mass);
    }

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public SecondBachTransferExercise generateAndSave() {
        var ex = new SecondBachTransferExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);

        int roll = random.nextInt(10);
        switch (roll) {
            case 0 -> buildStrongAcid(ex);
            case 1 -> buildStrongBase(ex);
            case 2 -> buildWeakAcidPH(ex);
            case 3 -> buildWeakBasePH(ex);
            case 4 -> buildWeakAcidAlpha(ex);
            case 5 -> buildOxidationState(ex);
            case 6 -> buildRedoxCoefficients(ex);
            case 7 -> buildRedoxOxidantMCQ(ex);
            case 8 -> buildGalvanicEMF(ex);
            default -> buildElectrolysisMass(ex);
        }

        log.debug("2BACH_Q BL4 generado: type={} mode={}",
            ex.getTransferType(), ex.getExerciseMode());
        return repository.save(ex);
    }

    public SecondBachTransferExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ejercicio 2BACH_Q BL4 no encontrado: " + id));
    }

    // =========================================================================
    // BUILDERS — ACID_BASE_PH
    // =========================================================================

    private void buildStrongAcid(SecondBachTransferExercise ex) {
        ex.setTransferType(TransferType.ACID_BASE_PH);
        ex.setExerciseMode("PH_STRONG_ACID");

        StrongAcidScenario sc = STRONG_ACIDS.get(random.nextInt(STRONG_ACIDS.size()));

        ex.setStatement(String.format(
            "Calcula el pH de una disolución de <strong>%s (%s)</strong> "
            + "de concentración <em>C</em><sub>0</sub> = %.4f M. "
            + "Considera que el ácido se disocia completamente. (dos decimales)",
            sc.name(), sc.formula(), sc.concentration()));
        ex.setCorrectAnswer(String.valueOf(sc.pH()));
        ex.setCorrectAnswerDisplay("pH = " + sc.pH());
        ex.setUnit("adimensional");
        ex.setExplanation(buildStrongAcidExpl(sc));
    }

    private void buildStrongBase(SecondBachTransferExercise ex) {
        ex.setTransferType(TransferType.ACID_BASE_PH);
        ex.setExerciseMode("PH_STRONG_BASE");

        StrongBaseScenario sc = STRONG_BASES.get(random.nextInt(STRONG_BASES.size()));

        ex.setStatement(String.format(
            "Calcula el pH de una disolución de <strong>%s (%s)</strong> "
            + "de concentración <em>C</em><sub>0</sub> = %.4f M. "
            + "Base fuerte: disociación completa. (dos decimales)",
            sc.name(), sc.formula(), sc.concentration()));
        ex.setCorrectAnswer(String.valueOf(sc.pH()));
        ex.setCorrectAnswerDisplay("pH = " + sc.pH());
        ex.setUnit("adimensional");
        ex.setExplanation(buildStrongBaseExpl(sc));
    }

    private void buildWeakAcidPH(SecondBachTransferExercise ex) {
        ex.setTransferType(TransferType.ACID_BASE_PH);
        ex.setExerciseMode("PH_WEAK_ACID");

        WeakAcidScenario sc = WEAK_ACIDS.get(random.nextInt(WEAK_ACIDS.size()));

        ex.setStatement(String.format(
            "Calcula el pH de una disolución de <strong>%s (%s)</strong> "
            + "de concentración <em>C</em><sub>0</sub> = %.3f M. "
            + "<em>K</em><sub>a</sub> = %.2e. (dos decimales)",
            sc.name(), sc.formula(), sc.concentration(), sc.ka()));
        ex.setCorrectAnswer(String.valueOf(sc.pH()));
        ex.setCorrectAnswerDisplay("pH = " + sc.pH());
        ex.setUnit("adimensional");
        ex.setExplanation(buildWeakAcidExpl(sc));
    }

    private void buildWeakBasePH(SecondBachTransferExercise ex) {
        ex.setTransferType(TransferType.ACID_BASE_PH);
        ex.setExerciseMode("PH_WEAK_BASE");

        WeakBaseScenario sc = WEAK_BASES.get(random.nextInt(WEAK_BASES.size()));

        ex.setStatement(String.format(
            "Calcula el pH de una disolución de <strong>%s (%s)</strong> "
            + "de concentración <em>C</em><sub>0</sub> = %.3f M. "
            + "<em>K</em><sub>b</sub> = %.2e. (dos decimales)",
            sc.name(), sc.formula(), sc.concentration(), sc.kb()));
        ex.setCorrectAnswer(String.valueOf(sc.pH()));
        ex.setCorrectAnswerDisplay("pH = " + sc.pH());
        ex.setUnit("adimensional");
        ex.setExplanation(buildWeakBaseExpl(sc));
    }

    private void buildWeakAcidAlpha(SecondBachTransferExercise ex) {
        ex.setTransferType(TransferType.ACID_BASE_PH);
        ex.setExerciseMode("ALPHA_WEAK_ACID");

        WeakAcidScenario sc = WEAK_ACIDS.get(random.nextInt(WEAK_ACIDS.size()));

        ex.setStatement(String.format(
            "Calcula el grado de ionización α del <strong>%s (%s)</strong> "
            + "a concentración <em>C</em><sub>0</sub> = %.3f M. "
            + "<em>K</em><sub>a</sub> = %.2e. "
            + "Expresa α en tanto por uno con dos decimales.",
            sc.name(), sc.formula(), sc.concentration(), sc.ka()));
        ex.setCorrectAnswer(String.valueOf(sc.alpha()));
        ex.setCorrectAnswerDisplay("α = " + sc.alpha()
            + " (" + round2(sc.alpha() * 100) + " %)");
        ex.setUnit("adimensional (0–1)");
        ex.setExplanation(buildWeakAcidAlphaExpl(sc));
    }

    // =========================================================================
    // BUILDERS — REDOX_ION_ELECTRON
    // =========================================================================

    private void buildOxidationState(SecondBachTransferExercise ex) {
        ex.setTransferType(TransferType.REDOX_ION_ELECTRON);
        ex.setExerciseMode("REDOX_OXIDATION_STATE");

        OxStateScenario sc = OX_STATE_SCENARIOS.get(random.nextInt(OX_STATE_SCENARIOS.size()));

        ex.setStatement(String.format(
            "Determina el número de oxidación del <strong>%s</strong> en el compuesto "
            + "<strong>%s</strong> (%s). Introduce el valor con su signo (p.ej. <code>+6</code> o <code>-2</code>).",
            sc.element(), sc.compound(), sc.context()));
        ex.setCorrectAnswer(String.valueOf(sc.oxState()));
        ex.setCorrectAnswerDisplay((sc.oxState() > 0 ? "+" : "") + sc.oxState());
        ex.setUnit("entero con signo");
        ex.setExplanation(sc.explanation());
    }

    private void buildRedoxCoefficients(SecondBachTransferExercise ex) {
        ex.setTransferType(TransferType.REDOX_ION_ELECTRON);
        ex.setExerciseMode("REDOX_COEFFICIENTS");

        RedoxReactionScenario sc = REDOX_SCENARIOS.get(random.nextInt(REDOX_SCENARIOS.size()));

        ex.setStatement(String.format(
            "Ajusta por el <strong>método del ion-electrón</strong> en medio <em>%s</em> "
            + "la siguiente reacción: <strong>%s</strong>.<br><br>%s",
            sc.medium(), sc.reactionUnbalanced(), sc.coeffQuestion()));
        ex.setCorrectAnswer(String.join("|", sc.coefficients()));
        ex.setCorrectAnswerDisplay(String.join(" | ", sc.coefficients()));
        ex.setUnit("coeficientes enteros");
        ex.setExplanation(sc.explanationHtml());
    }

    private void buildRedoxOxidantMCQ(SecondBachTransferExercise ex) {
        ex.setTransferType(TransferType.REDOX_ION_ELECTRON);
        ex.setExerciseMode("REDOX_OXIDANT_TEXT");

        RedoxReactionScenario sc = REDOX_SCENARIOS.get(random.nextInt(REDOX_SCENARIOS.size()));

        ex.setStatement(String.format(
            "Para la reacción ajustada en medio <em>%s</em>: "
            + "<strong>%s</strong>.<br>"
            + "¿Cuál es el <strong>agente oxidante</strong> y el <strong>agente reductor</strong>?",
            sc.medium(), sc.reactionUnbalanced()));
        ex.setOptionA(sc.optionA());
        ex.setOptionB(sc.optionB());
        ex.setOptionC(sc.optionC());
        ex.setCorrectAnswer(sc.correctOxRedLetter());
        ex.setCorrectAnswerDisplay(sc.correctOxRedLetter() + ": " + sc.oxidant());
        ex.setUnit("—");
        ex.setExplanation(sc.explanationHtml());
    }

    // =========================================================================
    // BUILDERS — ELECTROCHEMISTRY_FARADAY
    // =========================================================================

    private void buildGalvanicEMF(SecondBachTransferExercise ex) {
        ex.setTransferType(TransferType.ELECTROCHEMISTRY_FARADAY);
        ex.setExerciseMode("FARADAY_EMF");

        GalvanicCellScenario sc = GALVANIC_SCENARIOS.get(
            random.nextInt(GALVANIC_SCENARIOS.size()));

        ex.setStatement(String.format(
            "Dada la siguiente pila galvánica: "
            + "<strong>%s || %s</strong> (%s). "
            + "Potenciales estándar de reducción: "
            + "<em>E°</em>(<code>%s</code>) = %+.2f V; "
            + "<em>E°</em>(<code>%s</code>) = %+.2f V. "
            + "Calcula la fuerza electromotriz estándar <em>E°</em><sub>celda</sub> (en V, dos decimales).",
            sc.anode(), sc.cathode(), sc.cellName(),
            sc.anodeHalfReaction(), sc.e_anode(),
            sc.cathodeHalfReaction(), sc.e_cathode()));
        ex.setCorrectAnswer(String.valueOf(sc.emf()));
        ex.setCorrectAnswerDisplay("E° = " + sc.emf() + " V");
        ex.setUnit("V");
        ex.setExplanation(buildEMFExpl(sc));
    }

    private void buildElectrolysisMass(SecondBachTransferExercise ex) {
        ex.setTransferType(TransferType.ELECTROCHEMISTRY_FARADAY);
        ex.setExerciseMode("FARADAY_MASS");

        ElectrolysisScenario sc = ELECTROLYSIS_SCENARIOS.get(
            random.nextInt(ELECTROLYSIS_SCENARIOS.size()));

        double minutos = sc.timeS() / 60.0;
        ex.setStatement(String.format(
            "En un proceso de electrólisis, la semirreacción catódica es "
            + "<strong>%s</strong>. Se hace pasar una corriente de "
            + "<em>I</em> = %.1f A durante <em>t</em> = %.0f min. "
            + "Masa molar del <strong>%s</strong>: <em>M</em> = %.2f g/mol. "
            + "Calcula la masa depositada (en g, dos decimales). "
            + "Usa <em>F</em> = 96 485 C/mol e⁻.",
            sc.cathodeReaction(), sc.currentA(), minutos,
            sc.depositedSpecies(), sc.molarMass()));
        ex.setCorrectAnswer(String.valueOf(sc.massG()));
        ex.setCorrectAnswerDisplay("m = " + sc.massG() + " g");
        ex.setUnit("g");
        ex.setExplanation(buildFaradayMassExpl(sc));
    }

    // =========================================================================
    // EXPLICACIONES — ACID-BASE
    // =========================================================================

    private static String buildStrongAcidExpl(StrongAcidScenario sc) {
        double cH = sc.protons() * sc.concentration();
        var sb = new StringBuilder();
        sb.append("<strong>pH de ácido fuerte — disociación completa</strong>\n\n");
        sb.append("\\[").append(sc.formula())
          .append(" \\xrightarrow{\\text{total}} ")
          .append(sc.protons() > 1 ? sc.protons() : "")
          .append("\\text{H}^+ + \\text{A}^{n-}\\]\n\n");
        sb.append("\\[[\\text{H}^+] = ")
          .append(sc.protons() > 1 ? sc.protons() + " \\times " : "")
          .append(sc.concentration()).append(" = ").append(cH).append("\\text{ M}\\]\n\n");
        sb.append("\\[\\text{pH} = -\\log[\\text{H}^+] = -\\log(").append(cH)
          .append(") = ").append(sc.pH()).append("\\]\n\n");
        sb.append("<strong>Criterio:</strong> ácido fuerte → [H⁺] = n × C₀. "
            + "No se plantea equilibrio (α = 1 por definición).\n\n");
        sb.append("∴ <strong>pH = ").append(sc.pH()).append("</strong>");
        return sb.toString();
    }

    private static String buildStrongBaseExpl(StrongBaseScenario sc) {
        double cOH = sc.hydroxyls() * sc.concentration();
        double pOH = -Math.log10(cOH);
        var sb = new StringBuilder();
        sb.append("<strong>pH de base fuerte — vía pOH y K<sub>w</sub></strong>\n\n");
        sb.append("\\[").append(sc.formula())
          .append(" \\xrightarrow{\\text{total}} \\text{M}^{n+} + ")
          .append(sc.hydroxyls() > 1 ? sc.hydroxyls() : "")
          .append("\\text{OH}^-\\]\n\n");
        sb.append("\\[[\\text{OH}^-] = ")
          .append(sc.hydroxyls() > 1 ? sc.hydroxyls() + " \\times " : "")
          .append(sc.concentration()).append(" = ").append(cOH).append("\\text{ M}\\]\n\n");
        sb.append("\\[\\text{pOH} = -\\log[\\text{OH}^-] = -\\log(")
          .append(cOH).append(") = ").append(round2(pOH)).append("\\]\n\n");
        sb.append("\\[\\text{pH} = 14 - \\text{pOH} = 14 - ")
          .append(round2(pOH)).append(" = ").append(sc.pH()).append("\\]\n\n");
        sb.append("<strong>Relación K<sub>w</sub>:</strong>\n\n");
        sb.append("\\[K_w = [\\text{H}^+][\\text{OH}^-] = 1{,}0 \\times 10^{-14} "
            + "\\Rightarrow \\text{pH} + \\text{pOH} = 14\\]\n\n");
        sb.append("∴ <strong>pH = ").append(sc.pH()).append("</strong>");
        return sb.toString();
    }

    private static String buildWeakAcidExpl(WeakAcidScenario sc) {
        double c0 = sc.concentration();
        double ka = sc.ka();
        var sb = new StringBuilder();
        sb.append("<strong>pH de ácido débil — equilibrio protónico</strong>\n\n");
        sb.append("\\[\\text{HA} \\rightleftharpoons \\text{H}^+ + \\text{A}^- "
            + "\\qquad K_a = ").append(String.format("%.2e", ka)).append("\\]\n\n");

        // Tabla ICE
        sb.append(buildPhIceTable("HA", "H⁺", "A⁻", c0)).append("\n\n");

        sb.append("\\[K_a = \\frac{x^2}{C_0 - x} = ").append(String.format("%.2e", ka)).append("\\]\n\n");

        if (sc.useApprox()) {
            sb.append("<strong>Comprobación de la aproximación</strong> "
                + "(C₀/Kₐ = ").append(round2(c0 / ka)).append(" > 100 → válida):\n\n");
            sb.append("\\[x \\approx \\sqrt{K_a \\cdot C_0} = "
                + "\\sqrt{").append(String.format("%.2e", ka)).append(" \\times ")
              .append(c0).append("} = ").append(String.format("%.4e", Math.sqrt(ka * c0)))
              .append("\\text{ M}\\]\n\n");
        } else {
            sb.append("<strong>C₀/Kₐ = ").append(round2(c0 / ka))
              .append(" ≤ 100 → resolución exacta por ecuación cuadrática</strong>\n\n");
            sb.append("\\[x^2 + K_a x - K_a C_0 = 0\\]\n\n");
            double disc = ka * ka + 4 * ka * c0;
            sb.append("\\[x = \\frac{-").append(String.format("%.2e", ka))
              .append(" + \\sqrt{").append(String.format("%.4e", disc)).append("}}{2} = ")
              .append(String.format("%.4e", (-ka + Math.sqrt(disc)) / 2)).append("\\text{ M}\\]\n\n");
        }

        double x = sc.useApprox() ? Math.sqrt(ka * c0) : (-ka + Math.sqrt(ka * ka + 4 * ka * c0)) / 2;
        sb.append("\\[\\text{pH} = -\\log x = -\\log(")
          .append(String.format("%.4e", x)).append(") = ").append(sc.pH()).append("\\]\n\n");
        sb.append("∴ <strong>pH = ").append(sc.pH()).append("</strong>");
        return sb.toString();
    }

    private static String buildWeakBaseExpl(WeakBaseScenario sc) {
        double c0 = sc.concentration();
        double kb = sc.kb();
        var sb = new StringBuilder();
        sb.append("<strong>pH de base débil — vía pOH y K<sub>w</sub></strong>\n\n");
        sb.append("\\[\\text{B} + \\text{H}_2\\text{O} \\rightleftharpoons "
            + "\\text{BH}^+ + \\text{OH}^- "
            + "\\qquad K_b = ").append(String.format("%.2e", kb)).append("\\]\n\n");

        sb.append(buildPhIceTable("B", "BH⁺", "OH⁻", c0)).append("\n\n");

        sb.append("\\[K_b = \\frac{x^2}{C_0 - x} = ").append(String.format("%.2e", kb)).append("\\]\n\n");

        double x;
        if (sc.useApprox()) {
            x = Math.sqrt(kb * c0);
            sb.append("<strong>Aproximación válida</strong> (C₀/K<sub>b</sub> = ")
              .append(round2(c0 / kb)).append(" > 100):\n\n");
            sb.append("\\[x \\approx \\sqrt{K_b \\cdot C_0} = ")
              .append(String.format("%.4e", x)).append("\\text{ M}\\]\n\n");
        } else {
            x = (-kb + Math.sqrt(kb * kb + 4 * kb * c0)) / 2;
            sb.append("<strong>Ecuación cuadrática</strong> (C₀/K<sub>b</sub> = ")
              .append(round2(c0 / kb)).append(" ≤ 100):\n\n");
            sb.append("\\[x = \\frac{-K_b + \\sqrt{K_b^2 + 4K_b C_0}}{2} = ")
              .append(String.format("%.4e", x)).append("\\text{ M}\\]\n\n");
        }

        double pOH = -Math.log10(x);
        sb.append("\\[\\text{pOH} = -\\log x = ").append(round2(pOH)).append("\\]\n\n");
        sb.append("\\[\\text{pH} = 14 - \\text{pOH} = 14 - ")
          .append(round2(pOH)).append(" = ").append(sc.pH()).append("\\]\n\n");
        sb.append("∴ <strong>pH = ").append(sc.pH()).append("</strong>");
        return sb.toString();
    }

    private static String buildWeakAcidAlphaExpl(WeakAcidScenario sc) {
        var sb = new StringBuilder();
        sb.append("<strong>Grado de ionización α del ácido débil</strong>\n\n");
        sb.append("\\[\\alpha = \\frac{[\\text{H}^+]_{\\text{eq}}}{C_0}\\]\n\n");
        double x = sc.useApprox()
            ? Math.sqrt(sc.ka() * sc.concentration())
            : (-sc.ka() + Math.sqrt(sc.ka() * sc.ka() + 4 * sc.ka() * sc.concentration())) / 2;
        sb.append("\\[[\\text{H}^+] = ").append(String.format("%.4e", x))
          .append("\\text{ M}\\]\n\n");
        sb.append("\\[\\alpha = \\frac{").append(String.format("%.4e", x))
          .append("}{").append(sc.concentration()).append("} = ")
          .append(sc.alpha()).append("\\]\n\n");
        sb.append("<strong>Interpretación:</strong> el ")
          .append(round2(sc.alpha() * 100)).append(" % de las moléculas de ")
          .append(sc.formula()).append(" se han ionizado. "
              + "Cuanto mayor es C₀ o menor es Kₐ, menor es α (efecto de dilución inverso).\n\n");
        sb.append("∴ <strong>α = ").append(sc.alpha()).append("</strong>");
        return sb.toString();
    }

    private static String buildPhIceTable(String acid, String hplus, String conj,
                                           double c0) {
        var sb = new StringBuilder();
        sb.append("<strong>Tabla ICE:</strong>\n\n")
          .append("<table class=\"table table-sm table-bordered\">")
          .append("<thead><tr><th>Especie</th><th>Inicio</th><th>Reacciona</th>"
              + "<th>Equilibrio</th></tr></thead><tbody>");
        appendRow(sb, acid,  String.valueOf(c0), "−x", "C₀ − x");
        appendRow(sb, hplus, "≈ 0",              "+x", "x");
        appendRow(sb, conj,  "0",                "+x", "x");
        sb.append("</tbody></table>");
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIONES — REDOX
    // =========================================================================

    private static String buildOxExpl(String compound, String element,
                                       int oxState, String reasoning) {
        var sb = new StringBuilder();
        sb.append("<strong>Número de oxidación del ").append(element)
          .append(" en ").append(compound).append("</strong>\n\n");
        sb.append("<strong>Reglas para asignar números de oxidación:</strong>\n")
          .append("<ul>")
          .append("<li>Metales alcalinos (Grupo 1): siempre <strong>+1</strong></li>")
          .append("<li>Metales alcalinotérreos (Grupo 2): siempre <strong>+2</strong></li>")
          .append("<li>Oxígeno: <strong>−2</strong> (excepto en peróxidos −1 y OF₂ +2)</li>")
          .append("<li>Hidrógeno: <strong>+1</strong> (−1 en hidruros metálicos)</li>")
          .append("<li>Suma de todos los N.O. = carga total de la especie</li>")
          .append("</ul>\n\n");
        sb.append("<strong>Aplicación a ").append(compound).append(":</strong>\n\n");
        sb.append(reasoning).append("\n\n");
        sb.append("∴ N.O. del ").append(element).append(" en ").append(compound)
          .append(": <strong>").append(oxState > 0 ? "+" : "").append(oxState)
          .append("</strong>");
        return sb.toString();
    }

    private static String buildRedoxExpl(String balanced, String medium,
                                          String[] halfReactions,
                                          String oxidantRole, String reductantRole,
                                          String equalNote) {
        var sb = new StringBuilder();
        sb.append("<strong>Ajuste por el método del ion-electrón en medio ")
          .append(medium).append("</strong>\n\n");

        sb.append("<strong>Semirreacciones parciales:</strong>\n\n");
        for (String hr : halfReactions) {
            sb.append("\\[").append(hr).append("\\]\n\n");
        }

        if ("ácido".equals(medium)) {
            sb.append("<strong>Ajuste en medio ácido:</strong>\n")
              .append("<ol>")
              .append("<li>Igualar átomos distintos de H y O.</li>")
              .append("<li>Igualar O añadiendo H₂O al lado que falta.</li>")
              .append("<li>Igualar H añadiendo H⁺ al lado que falta.</li>")
              .append("<li>Igualar cargas añadiendo e⁻ al lado más positivo.</li>")
              .append("<li>Multiplicar semirreacciones para igualar e⁻ transferidos.</li>")
              .append("<li>Sumar y simplificar.</li>")
              .append("</ol>\n\n");
        } else {
            sb.append("<strong>Ajuste en medio básico:</strong>\n")
              .append("<ol>")
              .append("<li>Ajustar como en medio ácido.</li>")
              .append("<li>Añadir OH⁻ a ambos lados para neutralizar cada H⁺ "
                  + "(H⁺ + OH⁻ → H₂O).</li>")
              .append("<li>Simplificar H₂O que aparezca en ambos lados.</li>")
              .append("</ol>\n\n");
        }

        sb.append("<strong>Reacción ajustada:</strong>\n\n");
        sb.append("\\[\\text{").append(balanced.replace("→", "\\rightarrow")).append("}\\]\n\n");

        sb.append("<strong>Identificación:</strong>\n")
          .append("<ul>")
          .append("<li><strong>Agente oxidante</strong> (se reduce, gana e⁻): ")
          .append(oxidantRole).append("</li>")
          .append("<li><strong>Agente reductor</strong> (se oxida, pierde e⁻): ")
          .append(reductantRole).append("</li>")
          .append("</ul>\n\n");

        sb.append(equalNote);
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIONES — ELECTROQUÍMICA
    // =========================================================================

    private static String buildEMFExpl(GalvanicCellScenario sc) {
        var sb = new StringBuilder();
        sb.append("<strong>Fuerza electromotriz estándar de la celda galvánica</strong>\n\n");
        sb.append("\\[E^\\circ_{\\text{celda}} = E^\\circ_{\\text{cátodo}} - E^\\circ_{\\text{ánodo}}\\]\n\n");

        sb.append("<strong>Identificación de electrodos:</strong>\n")
          .append("<ul>")
          .append("<li><strong>Cátodo</strong> (reducción, E° más positivo): ")
          .append(sc.cathode()).append(", <em>E°</em> = ").append(String.format("%+.2f", sc.e_cathode()))
          .append(" V</li>")
          .append("<li><strong>Ánodo</strong> (oxidación, E° más negativo): ")
          .append(sc.anode()).append(", <em>E°</em> = ").append(String.format("%+.2f", sc.e_anode()))
          .append(" V</li>")
          .append("</ul>\n\n");

        sb.append("<strong>Semirreacciones:</strong>\n\n");
        sb.append("\\[\\text{Cátodo: }").append(sc.cathodeHalfReaction().replace("→", "\\rightarrow"))
          .append("\\]\n");
        sb.append("\\[\\text{Ánodo: }").append(sc.anodeHalfReaction().replace("→", "\\rightarrow")
            .replace("→", "\\rightarrow (\\text{invertida})"))
          .append("\\]\n\n");

        sb.append("<strong>Cálculo de E°:</strong>\n\n");
        sb.append("\\[E^\\circ = ").append(String.format("%+.2f", sc.e_cathode()))
          .append(" - (").append(String.format("%+.2f", sc.e_anode())).append(") = ")
          .append(sc.emf()).append("\\text{ V}\\]\n\n");

        sb.append(sc.emf() > 0
            ? "<strong>E° > 0 → reacción espontánea</strong> (pila galvánica viable)."
            : "<strong>E° < 0 → reacción no espontánea</strong> (requiere energía externa).");
        sb.append("\n\n∴ <strong>E°<sub>celda</sub> = ").append(sc.emf()).append(" V</strong>");
        return sb.toString();
    }

    private static String buildFaradayMassExpl(ElectrolysisScenario sc) {
        double q = sc.currentA() * sc.timeS();
        var sb = new StringBuilder();
        sb.append("<strong>Ley de Faraday — masa depositada en electrólisis</strong>\n\n");
        sb.append("\\[m = \\frac{M \\cdot I \\cdot t}{n \\cdot F}\\]\n\n");

        sb.append("donde:\n")
          .append("<ul>")
          .append("<li><em>M</em> = ").append(sc.molarMass()).append(" g/mol (masa molar)</li>")
          .append("<li><em>I</em> = ").append(sc.currentA()).append(" A (intensidad)</li>")
          .append("<li><em>t</em> = ").append((int) sc.timeS())
          .append(" s = ").append((int)(sc.timeS()/60)).append(" min (tiempo)</li>")
          .append("<li><em>n</em> = ").append(sc.electronsPerIon())
          .append(" (electrones por ion en la semirreacción)</li>")
          .append("<li><em>F</em> = 96 485 C/mol e⁻ (constante de Faraday)</li>")
          .append("</ul>\n\n");

        sb.append("<strong>Paso 1 — Carga total transferida:</strong>\n\n");
        sb.append("\\[Q = I \\cdot t = ").append(sc.currentA()).append(" \\times ")
          .append((int) sc.timeS()).append(" = ").append(round2(q)).append("\\text{ C}\\]\n\n");

        sb.append("<strong>Paso 2 — Moles de electrones:</strong>\n\n");
        sb.append("\\[n_{e^-} = \\frac{Q}{F} = \\frac{").append(round2(q))
          .append("}{96\\,485} = ").append(String.format("%.5f", q / FARADAY))
          .append("\\text{ mol}\\]\n\n");

        sb.append("<strong>Paso 3 — Moles de metal depositado:</strong>\n\n");
        sb.append("\\[n_{\\text{metal}} = \\frac{n_{e^-}}{").append(sc.electronsPerIon())
          .append("} = ").append(String.format("%.5f", q / (FARADAY * sc.electronsPerIon())))
          .append("\\text{ mol}\\]\n\n");

        sb.append("<strong>Paso 4 — Masa depositada:</strong>\n\n");
        sb.append("\\[m = n_{\\text{metal}} \\times M = ")
          .append(String.format("%.5f", q / (FARADAY * sc.electronsPerIon())))
          .append(" \\times ").append(sc.molarMass())
          .append(" = ").append(sc.massG()).append("\\text{ g}\\]\n\n");

        sb.append("∴ <strong>m(").append(sc.depositedSpecies()).append(") = ")
          .append(sc.massG()).append(" g</strong>");
        return sb.toString();
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static void appendRow(StringBuilder sb, String name,
                                   Object init, String change, Object eq) {
        sb.append("<tr><td>").append(name).append("</td>")
          .append("<td>").append(init).append("</td>")
          .append("<td>").append(change).append("</td>")
          .append("<td>").append(eq).append("</td></tr>");
    }

    public static double round2(double v)  { return Math.round(v * 100.0) / 100.0; }
    private static double round4(double v) { return Math.round(v * 10000.0) / 10000.0; }
}
