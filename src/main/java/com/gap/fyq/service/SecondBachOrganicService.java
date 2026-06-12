package com.gap.fyq.service;

import com.gap.fyq.model.secondbach.organic.OrganicType;
import com.gap.fyq.model.secondbach.organic.SecondBachOrganicExercise;
import com.gap.fyq.repository.SecondBachOrganicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecondBachOrganicService {

    private final SecondBachOrganicRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "2BACH_Q";
    private static final String BLOCK  = "BL5";

    // =========================================================================
    // DATOS — Reacciones orgánicas
    // =========================================================================

    private record ReactionScenario(
        String name, String statement,
        String reactionType,               // "Adición", "Eliminación", "Sustitución"
        String optionA, String optionB, String optionC,
        String correctLetter,
        String explanation
    ) {}

    private record MarkovnikovScenario(
        String alkene, String reagent,
        String majorProduct,
        String optionA, String optionB, String optionC,
        String correctLetter,
        String explanation
    ) {}

    private record SaytzeffScenario(
        String haloalkane, String conditions,
        String majorAlkene, String minorAlkene,
        String optionA, String optionB, String optionC,
        String correctLetter,
        String explanation
    ) {}

    private static final List<ReactionScenario>   REACTION_SCENARIOS    = buildReactionScenarios();
    private static final List<MarkovnikovScenario> MARKOVNIKOV_SCENARIOS = buildMarkovnikovScenarios();
    private static final List<SaytzeffScenario>   SAYTZEFF_SCENARIOS    = buildSaytzeffScenarios();

    private static List<ReactionScenario> buildReactionScenarios() {
        return List.of(
            new ReactionScenario(
                "Eteno + HBr",
                "El <strong>eteno</strong> (CH₂=CH₂) reacciona con <strong>HBr</strong>. "
                + "¿Qué tipo de reacción tiene lugar?",
                "Adición",
                "A) Adición electrófila", "B) Eliminación", "C) Sustitución nucleófila",
                "A",
                rxnTypeExpl("Adición electrófila",
                    "\\text{CH}_2{=}\\text{CH}_2 + \\text{HBr} \\rightarrow "
                    + "\\text{CH}_3\\text{CH}_2\\text{Br}",
                    "El H⁺ del HBr ataca el doble enlace (π) actuando como electrófilo. "
                    + "El enlace π se rompe de forma heterolítica: un carbono gana H⁺ formando "
                    + "un carbocatión primario, que inmediatamente es atacado por Br⁻ (nucleófilo). "
                    + "El doble enlace desaparece → <strong>adición electrófila</strong>. "
                    + "Producto: <em>bromoetano</em> (CH₃CH₂Br).")
            ),
            new ReactionScenario(
                "2-bromobutano + KOH/etanol",
                "El <strong>2-bromobutano</strong> (CH₃CHBrCH₂CH₃) se calienta con <strong>KOH/etanol</strong>. "
                + "¿Qué tipo de reacción tiene lugar?",
                "Eliminación",
                "A) Sustitución nucleófila S_N2",
                "B) Adición electrófila",
                "C) Eliminación E2 (β-eliminación)",
                "C",
                rxnTypeExpl("Eliminación E2 (β-eliminación)",
                    "\\text{CH}_3\\text{CHBrCH}_2\\text{CH}_3 "
                    + "\\xrightarrow{\\text{KOH/EtOH}} "
                    + "\\text{CH}_3\\text{CH}{=}\\text{CH}\\text{CH}_3 + \\text{KBr} + \\text{H}_2\\text{O}",
                    "En presencia de una base fuerte en disolvente aprótico (etanol), "
                    + "el OH⁻ actúa como base y arranca un protón β (adyacente al C–Br). "
                    + "Simultáneamente el Br⁻ abandona → mecanismo concertado E2. "
                    + "Se forma un doble enlace C=C (<strong>eliminación</strong>). "
                    + "Producto mayoritario: <em>but-2-eno</em> (regla de Saytzeff).")
            ),
            new ReactionScenario(
                "Metano + Cl₂/hν",
                "El <strong>metano</strong> (CH₄) reacciona con <strong>Cl₂</strong> bajo irradiación ultravioleta (hν). "
                + "¿Qué tipo de reacción tiene lugar?",
                "Sustitución",
                "A) Adición", "B) Condensación",
                "C) Sustitución homolítica radical",
                "C",
                rxnTypeExpl("Sustitución radical (SR)",
                    "\\text{CH}_4 + \\text{Cl}_2 \\xrightarrow{h\\nu} "
                    + "\\text{CH}_3\\text{Cl} + \\text{HCl}",
                    "La luz ultravioleta rompe el enlace Cl–Cl de forma homolítica: "
                    + "Cl₂ → 2 Cl· (radicales). El Cl· arranca un H del CH₄ generando "
                    + "el radical metilo CH₃·, que a su vez ataca otra molécula de Cl₂. "
                    + "Un H de la molécula es reemplazado por Cl → <strong>sustitución</strong>. "
                    + "Producto: <em>clorometano</em> (CH₃Cl).")
            ),
            new ReactionScenario(
                "Propeno + H₂O/H⁺",
                "El <strong>prop-1-eno</strong> (CH₃CH=CH₂) reacciona con agua en presencia de ácido "
                + "(H₂O/H⁺). ¿Qué tipo de reacción tiene lugar?",
                "Adición",
                "A) Adición electrófila (hidratación)",
                "B) Eliminación",
                "C) Sustitución electrófila aromática",
                "A",
                rxnTypeExpl("Adición electrófila (hidratación de alqueno)",
                    "\\text{CH}_3\\text{CH}{=}\\text{CH}_2 + \\text{H}_2\\text{O} "
                    + "\\xrightarrow{\\text{H}^+} "
                    + "\\text{CH}_3\\text{CH(OH)CH}_3",
                    "El H⁺ protona el doble enlace generando un carbocatión. "
                    + "Por Markovnikov, el H⁺ se añade al carbono con más H, "
                    + "formando el carbocatión 2° (más estable). "
                    + "El agua actúa como nucleófilo y ataca el carbocatión. "
                    + "El H₂O reacciona con el enlace π → <strong>adición</strong>. "
                    + "Producto mayoritario: <em>propan-2-ol</em>.")
            ),
            new ReactionScenario(
                "Benceno + HNO₃/H₂SO₄",
                "El <strong>benceno</strong> (C₆H₆) reacciona con <strong>HNO₃/H₂SO₄</strong> (mezcla nitrante). "
                + "¿Qué tipo de reacción tiene lugar?",
                "Sustitución",
                "A) Adición electrófila",
                "B) Sustitución electrófila aromática (nitración)",
                "C) Eliminación E1",
                "B",
                rxnTypeExpl("Sustitución electrófila aromática (SEA) — nitración",
                    "\\text{C}_6\\text{H}_6 + \\text{HNO}_3 "
                    + "\\xrightarrow{\\text{H}_2\\text{SO}_4} "
                    + "\\text{C}_6\\text{H}_5\\text{NO}_2 + \\text{H}_2\\text{O}",
                    "El H₂SO₄ concentrado protona al HNO₃ generando el ión nitronio "
                    + "NO₂⁺ (electrófilo fuerte). El NO₂⁺ ataca el anillo aromático. "
                    + "Se forma un complejo σ (σ-complejo o carbocatión de Wheland). "
                    + "Un protón abandona el anillo restaurando la aromaticidad → "
                    + "se <em>sustituye</em> un H por NO₂ sin adicionar al anillo. "
                    + "Producto: <em>nitrobenceno</em>.")
            ),
            new ReactionScenario(
                "2-cloropropano + NaOH(ac)",
                "El <strong>2-cloropropano</strong> ((CH₃)₂CHCl) reacciona con <strong>NaOH acuoso</strong>. "
                + "¿Qué tipo de reacción tiene lugar?",
                "Sustitución",
                "A) Eliminación E2",
                "B) Sustitución nucleófila (hidrólisis básica)",
                "C) Adición nucleófila",
                "B",
                rxnTypeExpl("Sustitución nucleófila S_N1",
                    "(\\text{CH}_3)_2\\text{CHCl} + \\text{NaOH(ac)} \\rightarrow "
                    + "(\\text{CH}_3)_2\\text{CHOH} + \\text{NaCl}",
                    "En disolvente acuoso polar (medio nucleófilo moderado), "
                    + "predomina la sustitución sobre la eliminación. "
                    + "El OH⁻ desplaza al Cl⁻ a través de un carbocatión secundario "
                    + "(mecanismo S_N1) o por ataque concertado S_N2. "
                    + "El Cl es <em>reemplazado</em> por OH → <strong>sustitución</strong>. "
                    + "Producto: <em>propan-2-ol</em>.")
            ),
            new ReactionScenario(
                "Eteno + Br₂/CCl₄",
                "El <strong>eteno</strong> (CH₂=CH₂) reacciona con <strong>Br₂</strong> disuelto "
                + "en CCl₄ (tetracloruro de carbono). ¿Qué tipo de reacción tiene lugar?",
                "Adición",
                "A) Adición electrófila (halogenación)",
                "B) Sustitución radical",
                "C) Eliminación",
                "A",
                rxnTypeExpl("Adición electrófila — halogenación",
                    "\\text{CH}_2{=}\\text{CH}_2 + \\text{Br}_2 "
                    + "\\xrightarrow{\\text{CCl}_4} "
                    + "\\text{CH}_2\\text{BrCH}_2\\text{Br}",
                    "La nube π del alqueno polariza la molécula de Br₂ (δ⁺–Br–Brδ⁻). "
                    + "El Brδ⁺ ataca el doble enlace formando un ión bromonio cíclico "
                    + "(intermedio de tres miembros). "
                    + "El Br⁻ ataca por la cara opuesta (adición anti). "
                    + "Los dos Br se añaden al doble enlace → <strong>adición</strong>. "
                    + "Producto: <em>1,2-dibromoetano</em>. "
                    + "Obsérvese: la disolución de Br₂ (naranja) se decolora → ensayo clásico.")
            )
        );
    }

    private static List<MarkovnikovScenario> buildMarkovnikovScenarios() {
        return List.of(
            new MarkovnikovScenario(
                "prop-1-eno", "HBr",
                "2-bromopropano",
                "A) 1-bromopropano", "B) 2-bromopropano", "C) 1,2-dibromopropano",
                "B",
                markovExpl("CH₃CH=CH₂", "HBr", "2-bromopropano", "1-bromopropano",
                    "C2 (central)", "C1 (terminal)",
                    "CH₃(CHBr)CH₃", "CH₂BrCH₂CH₃",
                    "2° (más estable)", "1° (menos estable)",
                    "El H⁺ protona C1 (más sustituido con H) → carbocatión 2° en C2. "
                    + "Br⁻ ataca C2. Producto mayoritario: <em>2-bromopropano</em>.")
            ),
            new MarkovnikovScenario(
                "but-1-eno", "HCl",
                "2-clorobutano",
                "A) 2-clorobutano", "B) 1-clorobutano", "C) 1,2-diclorobutano",
                "A",
                markovExpl("CH₃CH₂CH=CH₂", "HCl", "2-clorobutano", "1-clorobutano",
                    "C1 (terminal)", "C2 (interno)",
                    "CH₃CH₂CH(Cl)CH₃", "CH₃CH₂CH₂CH₂Cl",
                    "2° (más estable)", "1° (menos estable)",
                    "El H⁺ protona C1 (más sustituido con H) → carbocatión 2° en C2. "
                    + "Cl⁻ ataca C2. Producto mayoritario: <em>2-clorobutano</em>.")
            ),
            new MarkovnikovScenario(
                "2-metilpropeno", "HBr",
                "2-bromo-2-metilpropano",
                "A) 1-bromo-2-metilpropano",
                "B) 2-bromo-2-metilpropano",
                "C) 1,2-dibromo-2-metilpropano",
                "B",
                markovExpl("(CH₃)₂C=CH₂", "HBr", "2-bromo-2-metilpropano", "1-bromo-2-metilpropano",
                    "C3 (=CH₂, más H)", "C2 ((CH₃)₂C=, más sustituido)",
                    "(CH₃)₃CBr", "(CH₃)₂CHCH₂Br",
                    "3° (muy estable)", "1° (inestable)",
                    "El H⁺ protona CH₂ → carbocatión terciario extremadamente estable en C2. "
                    + "Br⁻ ataca C2. Producto: <em>2-bromo-2-metilpropano</em>.")
            ),
            new MarkovnikovScenario(
                "prop-1-eno", "H₂O (H⁺ catalizador)",
                "propan-2-ol",
                "A) propan-1-ol", "B) glicerol", "C) propan-2-ol",
                "C",
                markovExpl("CH₃CH=CH₂", "H₂O", "propan-2-ol", "propan-1-ol",
                    "C1 (=CH₂)", "C2 (=CH–)",
                    "CH₃CH(OH)CH₃", "CH₃CH₂CH₂OH",
                    "2° (más estable)", "1° (menos estable)",
                    "El H⁺ protona C1 → carbocatión 2° en C2. "
                    + "El agua (nucleófilo) ataca C2. "
                    + "Producto: <em>propan-2-ol</em>.")
            ),
            new MarkovnikovScenario(
                "but-1-eno", "H₂O (H⁺ catalizador)",
                "butan-2-ol",
                "A) butan-2-ol", "B) butan-1-ol", "C) butanal",
                "A",
                markovExpl("CH₃CH₂CH=CH₂", "H₂O", "butan-2-ol", "butan-1-ol",
                    "C1 (=CH₂)", "C2 (=CH–)",
                    "CH₃CH₂CH(OH)CH₃", "CH₃CH₂CH₂CH₂OH",
                    "2° (estable)", "1° (inestable)",
                    "El H⁺ protona C1 → carbocatión 2° en C2. "
                    + "Agua ataca C2. Producto: <em>butan-2-ol</em>.")
            ),
            new MarkovnikovScenario(
                "2-metilbut-1-eno", "HBr",
                "2-bromo-2-metilbutano",
                "A) 2-bromo-2-metilbutano",
                "B) 1-bromo-2-metilbutano",
                "C) 3-bromo-2-metilbutano",
                "A",
                markovExpl("CH₃CH₂C(CH₃)=CH₂", "HBr",
                    "2-bromo-2-metilbutano", "1-bromo-2-metilbutano",
                    "C1 (=CH₂)", "C2 (=C–, terciario)",
                    "CH₃CH₂C(Br)(CH₃)₂... simplificado: tert-bromo",
                    "CH₂BrCH(CH₃)CH₂CH₃",
                    "3° (el más estable)", "1° (inestable)",
                    "El H⁺ protona CH₂ → carbocatión 3° en C2. "
                    + "Br⁻ ataca C2 terciario. Producto: <em>2-bromo-2-metilbutano</em>.")
            )
        );
    }

    private static List<SaytzeffScenario> buildSaytzeffScenarios() {
        return List.of(
            new SaytzeffScenario(
                "2-bromobutano", "KOH/etanol, Δ",
                "but-2-eno", "but-1-eno",
                "A) but-1-eno", "B) but-2-eno", "C) butano",
                "B",
                saytzeffExpl("CH₃CHBrCH₂CH₃", "but-2-eno", "but-1-eno",
                    "internal",
                    "But-2-eno tiene el doble enlace entre C2 y C3 (2 sustituyentes a cada lado). "
                    + "But-1-eno tiene el doble enlace terminal (solo 1 sustituyente en C2). "
                    + "El alqueno más sustituido es más estable → Saytzeff → mayoritario: "
                    + "<em>but-2-eno</em>.")
            ),
            new SaytzeffScenario(
                "2-bromopentano", "KOH/etanol, Δ",
                "pent-2-eno", "pent-1-eno",
                "A) pent-1-eno", "B) pentano", "C) pent-2-eno",
                "C",
                saytzeffExpl("CH₃CHBrCH₂CH₂CH₃", "pent-2-eno", "pent-1-eno",
                    "internal",
                    "La base OH⁻ puede abstraer H de C1 (→ pent-1-eno) o de C3 (→ pent-2-eno). "
                    + "Pent-2-eno (disubstituido) es más estable que pent-1-eno (monosubstituido). "
                    + "Regla de Saytzeff → producto mayoritario: <em>pent-2-eno</em>.")
            ),
            new SaytzeffScenario(
                "2-bromo-2-metilbutano", "KOH/etanol, Δ",
                "2-metilbut-2-eno", "2-metilbut-1-eno",
                "A) 2-metilbut-1-eno",
                "B) 3-metilbut-1-eno",
                "C) 2-metilbut-2-eno",
                "C",
                saytzeffExpl("CH₃C(CH₃)(Br)CH₂CH₃", "2-metilbut-2-eno", "2-metilbut-1-eno",
                    "trisustituted",
                    "Hay dos β-H disponibles: los del CH₃ en C3 (→ 2-metilbut-2-eno, trisubstituido) "
                    + "y los del CH₂ en C4 (→ 2-metilbut-1-eno, bisubstituido). "
                    + "El alqueno trisustituido es el más estable → mayoritario: "
                    + "<em>2-metilbut-2-eno</em>.")
            ),
            new SaytzeffScenario(
                "2-clorobutano", "KOH/etanol, Δ",
                "but-2-eno", "but-1-eno",
                "A) but-2-eno", "B) but-1-eno", "C) 1-clorobutano",
                "A",
                saytzeffExpl("CH₃CHClCH₂CH₃", "but-2-eno", "but-1-eno",
                    "internal",
                    "Eliminación del Cl en C2: extracción de H en C1 → but-1-eno (monosustituto); "
                    + "extracción de H en C3 → but-2-eno (disustituto). "
                    + "Saytzeff predice but-2-eno como producto mayoritario.")
            ),
            new SaytzeffScenario(
                "3-bromohexano", "KOH/etanol, Δ",
                "hex-3-eno", "hex-2-eno",
                "A) hex-2-eno", "B) hex-1-eno", "C) hex-3-eno",
                "C",
                saytzeffExpl("CH₃CH₂CHBrCH₂CH₂CH₃", "hex-3-eno", "hex-2-eno",
                    "internal",
                    "Desde C3-Br, se puede abstraer H en C2 (→ hex-2-eno) o H en C4 (→ hex-3-eno). "
                    + "Hex-3-eno es disustituto igual que hex-2-eno, pero forma la cadena central. "
                    + "En equilibrio cinético (base fuerte), el isómero interno hex-3-eno "
                    + "predomina ligeramente. Regla de Saytzeff: <em>hex-3-eno</em>.")
            )
        );
    }

    // =========================================================================
    // DATOS — Isomería espacial
    // =========================================================================

    private record GeometricScenario(
        String compound, String formula,
        boolean hasGeometric,
        String explanation
    ) {}

    private record ChiralScenario(
        String compound, String formula,
        int chiralCount,
        String explanation
    ) {}

    private static final List<GeometricScenario> GEOMETRIC_SCENARIOS = buildGeometricScenarios();
    private static final List<ChiralScenario>    CHIRAL_SCENARIOS    = buildChiralScenarios();

    private static List<GeometricScenario> buildGeometricScenarios() {
        return List.of(
            new GeometricScenario("but-2-eno", "CH₃-CH=CH-CH₃", true,
                geomExpl("but-2-eno", true,
                    "C2: H y CH₃ (diferentes). C3: H y CH₃ (diferentes). "
                    + "Los dos carbonos del doble enlace tienen sustituyentes distintos → "
                    + "existen isómeros <em>cis</em> (CH₃ al mismo lado) y <em>trans</em> "
                    + "(CH₃ en lados opuestos).")),
            new GeometricScenario("but-1-eno", "CH₂=CH-CH₂-CH₃", false,
                geomExpl("but-1-eno", false,
                    "C1 tiene <strong>dos H iguales</strong> (CH₂=). Si uno de los carbonos "
                    + "del doble enlace porta dos grupos idénticos, no puede haber isomería "
                    + "geométrica (no hay forma de distinguir cis/trans en ese extremo).")),
            new GeometricScenario("pent-2-eno", "CH₃-CH=CH-CH₂-CH₃", true,
                geomExpl("pent-2-eno", true,
                    "C2: H y CH₃ (diferentes). C3: H y CH₂CH₃ (diferentes). "
                    + "Condición cumplida en ambos carbonos → existen <em>cis-pent-2-eno</em> "
                    + "y <em>trans-pent-2-eno</em>.")),
            new GeometricScenario("2-metilbut-2-eno", "(CH₃)₂C=CH-CH₃", false,
                geomExpl("2-metilbut-2-eno", false,
                    "C2 tiene <strong>dos grupos CH₃ iguales</strong> ((CH₃)₂C=). "
                    + "Dado que un carbono del doble enlace lleva sustituyentes idénticos, "
                    + "no puede existir isomería cis/trans.")),
            new GeometricScenario("1,2-dicloroeteno", "CHCl=CHCl", true,
                geomExpl("1,2-dicloroeteno", true,
                    "C1: H y Cl (diferentes). C2: H y Cl (diferentes). "
                    + "Existen <em>cis-1,2-dicloroeteno</em> (Cl al mismo lado, dipolar) "
                    + "y <em>trans-1,2-dicloroeteno</em> (Cl en lados opuestos, apolar).")),
            new GeometricScenario("propeno", "CH₃-CH=CH₂", false,
                geomExpl("propeno", false,
                    "C1 (=CH₂) tiene <strong>dos H iguales</strong>. "
                    + "No hay posibilidad de isomería geométrica.")),
            new GeometricScenario("cis/trans-2-butenodioico", "HOOC-CH=CH-COOH", true,
                geomExpl("ácido butenodioico (maleico/fumárico)", true,
                    "C2: H y COOH (diferentes). C3: H y COOH (diferentes). "
                    + "El isómero <em>cis</em> es el ácido maleico (cicliza a anhídrido); "
                    + "el <em>trans</em> es el ácido fumárico (más estable, punto de fusión mayor).")),
            new GeometricScenario("hex-3-eno", "CH₃-CH₂-CH=CH-CH₂-CH₃", true,
                geomExpl("hex-3-eno", true,
                    "C3: H y CH₂CH₃ (diferentes). C4: H y CH₂CH₃ (diferentes). "
                    + "Condición cumplida → existen <em>cis-hex-3-eno</em> "
                    + "y <em>trans-hex-3-eno</em>."))
        );
    }

    private static List<ChiralScenario> buildChiralScenarios() {
        return List.of(
            new ChiralScenario("2-bromobutano", "CH₃CHBrCH₂CH₃", 1,
                chiralExpl("2-bromobutano", 1,
                    new String[]{"C2"},
                    new String[]{"C2: {Br, H, CH₃, CH₂CH₃} — cuatro sustituyentes distintos → C* quiral"})),
            new ChiralScenario("ácido láctico (ác. 2-hidroxipropanoico)", "CH₃CH(OH)COOH", 1,
                chiralExpl("ácido láctico", 1,
                    new String[]{"C2"},
                    new String[]{"C2: {OH, H, CH₃, COOH} — cuatro diferentes → C* quiral. "
                        + "Existe como L-ácido láctico (músculo) y D-ácido láctico (fermentación)."})),
            new ChiralScenario("glicina (ác. aminoacético)", "H₂N-CH₂-COOH", 0,
                chiralExpl("glicina", 0,
                    new String[]{},
                    new String[]{"C2: {NH₂, H, H, COOH} — lleva <strong>dos H iguales</strong> → NO es quiral. "
                        + "La glicina es el único aminoácido natural sin actividad óptica."})),
            new ChiralScenario("alanina (ác. 2-aminopropanoico)", "CH₃CH(NH₂)COOH", 1,
                chiralExpl("alanina", 1,
                    new String[]{"C2"},
                    new String[]{"C2: {NH₂, H, CH₃, COOH} — cuatro diferentes → C* quiral. "
                        + "El enantiómero L-alanina es el más abundante en proteínas."})),
            new ChiralScenario("3-metilhexano", "CH₃CH₂CH(CH₃)CH₂CH₂CH₃", 1,
                chiralExpl("3-metilhexano", 1,
                    new String[]{"C3"},
                    new String[]{"C3: {H, CH₃, CH₂CH₃, CH₂CH₂CH₃} — cuatro grupos distintos → C* quiral."})),
            new ChiralScenario("2-clorobutano", "CH₃CHClCH₂CH₃", 1,
                chiralExpl("2-clorobutano", 1,
                    new String[]{"C2"},
                    new String[]{"C2: {Cl, H, CH₃, CH₂CH₃} — cuatro distintos → C* quiral."})),
            new ChiralScenario("glicerol (propano-1,2,3-triol)", "HOCH₂CH(OH)CH₂OH", 0,
                chiralExpl("glicerol", 0,
                    new String[]{},
                    new String[]{"C2: {OH, H, CH₂OH, CH₂OH} — los grupos C1 y C3 son <strong>idénticos</strong> "
                        + "(ambos CH₂OH). Al haber dos sustituyentes iguales, C2 <strong>no es quiral</strong>. "
                        + "El glicerol es aquiral (moléculas superponibles con su imagen especular)."})),
            new ChiralScenario("tartrato (ác. 2,3-dihidroxibutanodioico)", "HOOC-CHOH-CHOH-COOH", 2,
                chiralExpl("ácido tartárico", 2,
                    new String[]{"C2", "C3"},
                    new String[]{
                        "C2: {OH, H, COOH, CHOHCOOH} — cuatro distintos → C* quiral.",
                        "C3: {OH, H, COOH, CHOHCOOH} — cuatro distintos → C* quiral.",
                        "Nota: el ácido meso-tartárico tiene 2 C* pero es aquiral por plano de simetría."
                    }))
        );
    }

    // =========================================================================
    // DATOS — Polímeros
    // =========================================================================

    private record PolymerMonomerScenario(
        String polymer, String use,
        String monomer,
        String optionA, String optionB, String optionC,
        String correctLetter,
        String explanation
    ) {}

    private record PolymerTypeScenario(
        String polymer, String monomer,
        String type,               // "Adición" o "Condensación"
        String optionA, String optionB, String optionC,
        String correctLetter,
        String explanation
    ) {}

    private static final List<PolymerMonomerScenario> POLYMER_MONOMER_SCENARIOS = buildPolymerMonomerScenarios();
    private static final List<PolymerTypeScenario>    POLYMER_TYPE_SCENARIOS    = buildPolymerTypeScenarios();

    private static List<PolymerMonomerScenario> buildPolymerMonomerScenarios() {
        return List.of(
            new PolymerMonomerScenario("Polietileno (PE)", "bolsas, tuberías, envases",
                "Eteno (CH₂=CH₂)",
                "A) Eteno (CH₂=CH₂)", "B) Propeno (CH₃CH=CH₂)", "C) Cloruro de vinilo",
                "A",
                polymerMonomerExpl("Polietileno (PE)", "Eteno (CH₂=CH₂)", "Adición",
                    "El doble enlace del eteno se abre en presencia de un iniciador radical. "
                    + "Las unidades –CH₂–CH₂– se enlazan indefinidamente. "
                    + "Es el plástico más producido del mundo. "
                    + "HDPE (alta densidad): envases rígidos. LDPE (baja densidad): film.")),
            new PolymerMonomerScenario("PVC (policloruro de vinilo)", "tuberías, perfiles, suelos",
                "Cloruro de vinilo (CH₂=CHCl)",
                "A) Eteno", "B) Cloruro de vinilo (CH₂=CHCl)", "C) Estireno",
                "B",
                polymerMonomerExpl("PVC", "Cloruro de vinilo (cloroeteno, CH₂=CHCl)", "Adición",
                    "Polimerización radical del cloroeteno. La unidad repetida es –CH₂–CHCl–. "
                    + "El Cl confiere dureza y resistencia al fuego. "
                    + "Con plastificantes (ftalatos) se vuelve flexible (mangueras, cuero artificial).")),
            new PolymerMonomerScenario("Polipropileno (PP)", "fibras, envases, automóvil",
                "Propeno (CH₃CH=CH₂)",
                "A) Buteno", "B) Estireno", "C) Propeno (CH₃CH=CH₂)",
                "C",
                polymerMonomerExpl("Polipropileno (PP)", "Propeno (CH₃CH=CH₂)", "Adición",
                    "Polimerización de coordinación (catalizadores Ziegler–Natta). "
                    + "La unidad repetida es –CH₂–CH(CH₃)–. "
                    + "El PP isotáctico (grupos CH₃ alineados) tiene alta cristalinidad y Tf ≈ 165 °C.")),
            new PolymerMonomerScenario("Poliestireno (PS)", "envases de espuma (EPS), carcasas",
                "Estireno (C₆H₅CH=CH₂)",
                "A) Estireno (C₆H₅CH=CH₂)", "B) Isopreno", "C) Acrilonitrilo",
                "A",
                polymerMonomerExpl("Poliestireno (PS)", "Estireno (vinilbenceno, C₆H₅CH=CH₂)", "Adición",
                    "Polimerización radical o aniónica del estireno. "
                    + "La unidad repetida es –CH₂–CH(C₆H₅)–. "
                    + "El grupo fenilo aporta rigidez. EPS expandido (poliestireno expandido): embalajes.")),
            new PolymerMonomerScenario("Nylon-6,6 (poliamida 6,6)", "fibras textiles, engranajes",
                "Hexametilendiamina + ácido adípico",
                "A) Hexametilendiamina + ácido adípico",
                "B) Etilenglicol + ácido tereftálico",
                "C) Caprolactama",
                "A",
                polymerMonomerExpl("Nylon-6,6", "Hexametilendiamina + ácido adípico", "Condensación",
                    "El grupo –NH₂ de la diamina reacciona con el –COOH del diacido "
                    + "formando un enlace amida (–CO–NH–) y liberando H₂O. "
                    + "La nomenclatura '6,6' indica 6 C en la diamina y 6 C en el diacido. "
                    + "Resistencia mecánica y térmica excepcionales.")),
            new PolymerMonomerScenario("PET (politereftalato de etileno)", "botellas, fibras poliéster",
                "Etilenglicol + ácido tereftálico",
                "A) Caprolactama",
                "B) Etilenglicol + ácido tereftálico",
                "C) Eteno + ácido acético",
                "B",
                polymerMonomerExpl("PET", "Etilenglicol + ácido tereftálico", "Condensación",
                    "El –OH del etilenglicol reacciona con el –COOH del ácido tereftálico "
                    + "formando un enlace éster (–COO–) y liberando H₂O. "
                    + "Alta claridad óptica, barrera a gases → botellas de refresco (Plástico '1' / PETE). "
                    + "Reciclable.")),
            new PolymerMonomerScenario("Caucho natural (poli-cis-isopreno)", "neumáticos, guantes",
                "Isopreno (CH₂=C(CH₃)–CH=CH₂)",
                "A) Isopreno (CH₂=C(CH₃)–CH=CH₂)",
                "B) Butadieno",
                "C) Estireno",
                "A",
                polymerMonomerExpl("Caucho natural", "Isopreno (2-metilbuta-1,3-dieno)", "Adición",
                    "Biosíntesis enzimática en el árbol Hevea brasiliensis. "
                    + "La polimerización da lugar a la configuración cis (geometría Z) "
                    + "en el doble enlace residual → elasticidad. "
                    + "La vulcanización (Goodyear) crea puentes disulfuro entre cadenas "
                    + "mejorando la resistencia mecánica.")),
            new PolymerMonomerScenario("Teflon (PTFE)", "sartenes antiadherentes, juntas industriales",
                "Tetrafluoroetileno (CF₂=CF₂)",
                "A) Difluoroetileno", "B) Clorotrifluoroetileno",
                "C) Tetrafluoroetileno (CF₂=CF₂)",
                "C",
                polymerMonomerExpl("Teflon (PTFE)", "Tetrafluoroetileno (CF₂=CF₂)", "Adición",
                    "La sustitución total de H por F crea los enlaces C–F más fuertes en la naturaleza. "
                    + "La cadena –CF₂–CF₂– es completamente inerte a ácidos, bases y calor. "
                    + "Coeficiente de fricción extremadamente bajo. "
                    + "Descubierto accidentalmente por Roy Plunkett (DuPont, 1938)."))
        );
    }

    private static List<PolymerTypeScenario> buildPolymerTypeScenarios() {
        return List.of(
            new PolymerTypeScenario("Polietileno (PE)", "eteno",
                "Adición",
                "A) Adición", "B) Condensación", "C) Copolimerización en bloque",
                "A",
                polymerTypeExpl("Polietileno", "eteno", "Adición",
                    "No se libera ningún subproducto. El doble enlace del eteno se abre "
                    + "y los monómeros se encadenan directamente → <strong>polimerización de adición</strong>. "
                    + "Masa molar del polímero = n × masa molar del monómero.")),
            new PolymerTypeScenario("PVC", "cloruro de vinilo",
                "Adición",
                "A) Condensación", "B) Adición", "C) Coordinación (Ziegler-Natta)",
                "B",
                polymerTypeExpl("PVC", "cloruro de vinilo (CH₂=CHCl)", "Adición",
                    "No se pierde ningún átomo. La adición de n monómeros de cloroeteno "
                    + "genera la cadena –(CH₂–CHCl)ₙ–. "
                    + "Sin subproducto → <strong>adición</strong>.")),
            new PolymerTypeScenario("Nylon-6,6", "hexametilendiamina + ácido adípico",
                "Condensación",
                "A) Adición radical", "B) Adición aniónica",
                "C) Condensación (poliamidación)",
                "C",
                polymerTypeExpl("Nylon-6,6", "diamina + diacido", "Condensación",
                    "Cada unión entre monómeros libera una molécula de <strong>agua (H₂O)</strong>. "
                    + "Esto es característico de la polimerización de condensación. "
                    + "Masa del polímero < n × masa monómero.")),
            new PolymerTypeScenario("PET (poliéster)", "etilenglicol + ácido tereftálico",
                "Condensación",
                "A) Condensación (poliesterificación)",
                "B) Adición radical",
                "C) Apertura de anillo",
                "A",
                polymerTypeExpl("PET", "diol + diacido", "Condensación",
                    "Cada enlace éster –COO– se forma con liberación de H₂O. "
                    + "Reacción de esterificación repetida n veces → condensación. "
                    + "La reacción es reversible: en condiciones adecuadas el PET se hidroliza "
                    + "(reciclaje químico).")),
            new PolymerTypeScenario("Baquelita (resina fenol-formaldehído)", "fenol + formaldehído",
                "Condensación",
                "A) Adición electrófila", "B) Condensación",
                "C) Copolimerización aleatoria",
                "B",
                polymerTypeExpl("Baquelita", "fenol + formaldehído (HCHO)", "Condensación",
                    "El formaldehído reacciona con el fenol en posición orto y para. "
                    + "Cada enlace –CH₂– entre anillos fenólicos libera H₂O. "
                    + "Resultado: red tridimensional termoestable → no funde ni se disuelve. "
                    + "Primer plástico sintético de la historia (Baekeland, 1907).")),
            new PolymerTypeScenario("Polipropileno (PP)", "propeno",
                "Adición",
                "A) Condensación", "B) Adición",
                "C) Polimerización por inserción",
                "B",
                polymerTypeExpl("Polipropileno", "propeno (CH₃CH=CH₂)", "Adición",
                    "El doble enlace del propeno se abre (catalizador Ziegler–Natta). "
                    + "Los monómeros se enlazan sin liberar subproductos → <strong>adición</strong>.")),
            new PolymerTypeScenario("Caucho natural", "isopreno",
                "Adición",
                "A) Adición", "B) Condensación", "C) Condensación con vulcanización",
                "A",
                polymerTypeExpl("Caucho natural", "isopreno (2-metilbuta-1,3-dieno)", "Adición",
                    "Poli-cis-isopreno: los dobles enlaces del dieno se reorganizan "
                    + "en la cadena sin pérdida de átomos → <strong>adición</strong>. "
                    + "Tras la vulcanización los puentes S–S NO cambian el tipo de polimerización."))
        );
    }

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public SecondBachOrganicExercise generateAndSave() {
        var ex = new SecondBachOrganicExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);

        int roll = random.nextInt(7);
        switch (roll) {
            case 0 -> buildReactionType(ex);
            case 1 -> buildMarkovnikov(ex);
            case 2 -> buildSaytzeff(ex);
            case 3 -> buildGeometricIsomers(ex);
            case 4 -> buildChiralCarbons(ex);
            case 5 -> buildPolymerMonomer(ex);
            default -> buildPolymerType(ex);
        }

        log.debug("2BACH_Q BL5 generado: type={} mode={}",
            ex.getOrganicType(), ex.getExerciseMode());
        return repository.save(ex);
    }

    public SecondBachOrganicExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ejercicio 2BACH_Q BL5 no encontrado: " + id));
    }

    // =========================================================================
    // BUILDERS
    // =========================================================================

    private void buildReactionType(SecondBachOrganicExercise ex) {
        ex.setOrganicType(OrganicType.ORGANIC_REACTIONS_RULES);
        ex.setExerciseMode("REACTION_TYPE_MCQ");
        ReactionScenario sc = REACTION_SCENARIOS.get(random.nextInt(REACTION_SCENARIOS.size()));
        ex.setStatement(sc.statement());
        ex.setOptionA(sc.optionA()); ex.setOptionB(sc.optionB()); ex.setOptionC(sc.optionC());
        ex.setCorrectAnswer(sc.correctLetter());
        ex.setCorrectAnswerDisplay(sc.correctLetter() + ": " + sc.reactionType());
        ex.setUnit("—");
        ex.setExplanation(sc.explanation());
    }

    private void buildMarkovnikov(SecondBachOrganicExercise ex) {
        ex.setOrganicType(OrganicType.ORGANIC_REACTIONS_RULES);
        ex.setExerciseMode("MARKOVNIKOV_PRODUCT");
        MarkovnikovScenario sc = MARKOVNIKOV_SCENARIOS.get(random.nextInt(MARKOVNIKOV_SCENARIOS.size()));
        ex.setStatement(String.format(
            "Aplica la <strong>regla de Markovnikov</strong> a la adición de "
            + "<strong>%s</strong> sobre el <strong>%s</strong>. "
            + "¿Cuál es el <em>producto mayoritario</em>?",
            sc.reagent(), sc.alkene()));
        ex.setOptionA(sc.optionA()); ex.setOptionB(sc.optionB()); ex.setOptionC(sc.optionC());
        ex.setCorrectAnswer(sc.correctLetter());
        ex.setCorrectAnswerDisplay(sc.correctLetter() + ": " + sc.majorProduct());
        ex.setUnit("—");
        ex.setExplanation(sc.explanation());
    }

    private void buildSaytzeff(SecondBachOrganicExercise ex) {
        ex.setOrganicType(OrganicType.ORGANIC_REACTIONS_RULES);
        ex.setExerciseMode("SAYTZEFF_PRODUCT");
        SaytzeffScenario sc = SAYTZEFF_SCENARIOS.get(random.nextInt(SAYTZEFF_SCENARIOS.size()));
        ex.setStatement(String.format(
            "El <strong>%s</strong> se somete a eliminación con <strong>%s</strong>. "
            + "Aplica la <strong>regla de Saytzeff</strong> y selecciona el "
            + "<em>alqueno mayoritario</em>.",
            sc.haloalkane(), sc.conditions()));
        ex.setOptionA(sc.optionA()); ex.setOptionB(sc.optionB()); ex.setOptionC(sc.optionC());
        ex.setCorrectAnswer(sc.correctLetter());
        ex.setCorrectAnswerDisplay(sc.correctLetter() + ": " + sc.majorAlkene());
        ex.setUnit("—");
        ex.setExplanation(sc.explanation());
    }

    private void buildGeometricIsomers(SecondBachOrganicExercise ex) {
        ex.setOrganicType(OrganicType.SPATIAL_ISOMERISM);
        ex.setExerciseMode("GEOMETRIC_ISOMERS_MCQ");
        GeometricScenario sc = GEOMETRIC_SCENARIOS.get(random.nextInt(GEOMETRIC_SCENARIOS.size()));
        ex.setStatement(String.format(
            "¿Presenta el <strong>%s</strong> (<em>%s</em>) <strong>isomería geométrica</strong> "
            + "(isómeros <em>cis/trans</em> o <em>E/Z</em>)?",
            sc.compound(), sc.formula()));
        ex.setOptionA("A) Sí, presenta isómeros geométricos (cis/trans)");
        ex.setOptionB("B) No, no presenta isomería geométrica");
        ex.setOptionC(null);
        ex.setCorrectAnswer(sc.hasGeometric() ? "A" : "B");
        ex.setCorrectAnswerDisplay((sc.hasGeometric() ? "A: Sí" : "B: No")
            + " — " + sc.compound());
        ex.setUnit("—");
        ex.setExplanation(sc.explanation());
    }

    private void buildChiralCarbons(SecondBachOrganicExercise ex) {
        ex.setOrganicType(OrganicType.SPATIAL_ISOMERISM);
        ex.setExerciseMode("CHIRAL_CARBONS_COUNT");
        ChiralScenario sc = CHIRAL_SCENARIOS.get(random.nextInt(CHIRAL_SCENARIOS.size()));
        ex.setStatement(String.format(
            "Identifica los <strong>carbonos quirales (C*)</strong> del compuesto "
            + "<strong>%s</strong> (<em>%s</em>). "
            + "¿Cuántos carbonos quirales tiene? (introduce el número entero)",
            sc.compound(), sc.formula()));
        ex.setCorrectAnswer(String.valueOf(sc.chiralCount()));
        ex.setCorrectAnswerDisplay(sc.chiralCount() + " carbono(s) quiral(es)");
        ex.setUnit("C*");
        ex.setExplanation(sc.explanation());
    }

    private void buildPolymerMonomer(SecondBachOrganicExercise ex) {
        ex.setOrganicType(OrganicType.POLYMERS_INDUSTRY);
        ex.setExerciseMode("POLYMER_MONOMER_MCQ");
        PolymerMonomerScenario sc = POLYMER_MONOMER_SCENARIOS.get(
            random.nextInt(POLYMER_MONOMER_SCENARIOS.size()));
        ex.setStatement(String.format(
            "El polímero <strong>%s</strong> se utiliza en <em>%s</em>. "
            + "¿Cuál es su monómero (o monomeros) de partida?",
            sc.polymer(), sc.use()));
        ex.setOptionA(sc.optionA()); ex.setOptionB(sc.optionB()); ex.setOptionC(sc.optionC());
        ex.setCorrectAnswer(sc.correctLetter());
        ex.setCorrectAnswerDisplay(sc.correctLetter() + ": " + sc.monomer());
        ex.setUnit("—");
        ex.setExplanation(sc.explanation());
    }

    private void buildPolymerType(SecondBachOrganicExercise ex) {
        ex.setOrganicType(OrganicType.POLYMERS_INDUSTRY);
        ex.setExerciseMode("POLYMER_TYPE_MCQ");
        PolymerTypeScenario sc = POLYMER_TYPE_SCENARIOS.get(
            random.nextInt(POLYMER_TYPE_SCENARIOS.size()));
        ex.setStatement(String.format(
            "El <strong>%s</strong> se obtiene a partir del monómero <em>%s</em>. "
            + "¿Es un polímero de <strong>adición</strong> o de <strong>condensación</strong>?",
            sc.polymer(), sc.monomer()));
        ex.setOptionA(sc.optionA()); ex.setOptionB(sc.optionB()); ex.setOptionC(sc.optionC());
        ex.setCorrectAnswer(sc.correctLetter());
        ex.setCorrectAnswerDisplay(sc.correctLetter() + ": " + sc.type());
        ex.setUnit("—");
        ex.setExplanation(sc.explanation());
    }

    // =========================================================================
    // GENERADORES DE EXPLICACIONES
    // =========================================================================

    private static String rxnTypeExpl(String type, String equation, String mechanism) {
        return "<strong>Tipo de reacción: " + type + "</strong>\n\n"
            + "\\[" + equation + "\\]\n\n"
            + "<strong>Mecanismo:</strong>\n\n"
            + mechanism + "\n\n"
            + "<strong>Criterio de clasificación:</strong>\n"
            + "<ul>"
            + "<li><strong>Adición:</strong> dos reactivos se combinan en uno solo; el nº de moléculas de producto &lt; nº de reactivos.</li>"
            + "<li><strong>Eliminación:</strong> una molécula pierde átomos (normalmente HX) y forma un enlace múltiple.</li>"
            + "<li><strong>Sustitución:</strong> un átomo o grupo es reemplazado por otro; el nº de moléculas se conserva.</li>"
            + "</ul>";
    }

    private static String markovExpl(String alkene, String reagent,
            String major, String minor,
            String hAddC, String carbocationC,
            String majorStruct, String minorStruct,
            String catStab, String catStabMinor,
            String conclusion) {
        return "<strong>Regla de Markovnikov</strong>: "
            + "en la adición de un reactivo HX a un alqueno asimétrico, "
            + "el H se une al carbono con <em>más hidrógenos</em> y X al carbono con "
            + "<em>más sustituyentes</em> (carbocatión más estable).\n\n"
            + "<strong>Alqueno:</strong> " + alkene + " | <strong>Reactivo:</strong> " + reagent + "\n\n"
            + "<strong>Análisis de carbonos del doble enlace:</strong>\n"
            + "<ul>"
            + "<li>H se añade a <strong>" + hAddC + "</strong> → carbocatión en <strong>" + carbocationC + "</strong></li>"
            + "<li>Carbocatión obtenido: <strong>" + catStab + "</strong> (producto mayoritario)</li>"
            + "<li>Alternativa: <strong>" + catStabMinor + "</strong> (producto minoritario)</li>"
            + "</ul>\n\n"
            + "<strong>Estructuras:</strong>\n"
            + "<ul>"
            + "<li>Mayoritario: <em>" + major + "</em> (" + majorStruct + ")</li>"
            + "<li>Minoritario: <em>" + minor + "</em> (" + minorStruct + ")</li>"
            + "</ul>\n\n"
            + conclusion + "\n\n"
            + "∴ Producto mayoritario: <strong>" + major + "</strong>";
    }

    private static String saytzeffExpl(String haloalkane, String major, String minor,
                                        String substitution, String conclusion) {
        return "<strong>Regla de Saytzeff (Zaitsev)</strong>: "
            + "en una reacción de β-eliminación, el producto mayoritario es el alqueno "
            + "<em>más sustituido</em> (más estable termodinámicamente).\n\n"
            + "<strong>Sustrato:</strong> " + haloalkane + "\n\n"
            + "<strong>Estabilidad de alquenos (↑ sustituyentes = ↑ estabilidad):</strong>\n"
            + "\\[\\text{tetra} > \\text{tri} > \\text{di} > \\text{mono} > \\text{eteno}\\]\n\n"
            + "<strong>Análisis:</strong>\n"
            + "<ul>"
            + "<li>Producto mayoritario (Saytzeff): <strong>" + major + "</strong></li>"
            + "<li>Producto minoritario (Hofmann): <strong>" + minor + "</strong></li>"
            + "</ul>\n\n"
            + conclusion + "\n\n"
            + "∴ Producto mayoritario: <strong>" + major + "</strong>";
    }

    private static String geomExpl(String compound, boolean hasGeom, String analysis) {
        String rule = "<strong>Condición para isomería geométrica (cis/trans)</strong>: "
            + "cada uno de los dos C del doble enlace debe tener <em>dos sustituyentes distintos</em>.\n\n";
        String verdict = hasGeom
            ? "<strong>✓ Sí presenta isomería geométrica.</strong>"
            : "<strong>✗ No presenta isomería geométrica.</strong>";
        return rule
            + "<strong>Análisis de " + compound + ":</strong>\n\n"
            + analysis + "\n\n"
            + verdict;
    }

    private static String chiralExpl(String compound, int count,
                                      String[] chiralCs, String[] analyses) {
        var sb = new StringBuilder();
        sb.append("<strong>Carbono quiral (C*)</strong>: carbono unido a "
            + "<em>cuatro sustituyentes distintos</em>. "
            + "Un compuesto con ≥ 1 C* es ópticamente activo (rota el plano de la luz polarizada) "
            + "salvo que sea una forma <em>meso</em>.\n\n");
        sb.append("<strong>Análisis de ").append(compound).append(":</strong>\n<ul>");
        for (String a : analyses) {
            sb.append("<li>").append(a).append("</li>");
        }
        sb.append("</ul>\n\n");
        if (count == 0) {
            sb.append("∴ <strong>No hay carbonos quirales. El compuesto es aquiral "
                + "(no posee actividad óptica).</strong>");
        } else {
            sb.append("∴ <strong>").append(count).append(" carbono(s) quiral(es): ")
              .append(String.join(", ", chiralCs)).append(". ")
              .append("El compuesto es quiral y posee ")
              .append(count == 1 ? "2 enantiómeros" : (int) Math.pow(2, count) + " esteroisómeros posibles")
              .append(" (máximo 2ⁿ, con n = nº de C*).")
              .append("</strong>");
        }
        return sb.toString();
    }

    private static String polymerMonomerExpl(String polymer, String monomer,
                                              String type, String details) {
        return "<strong>Monómero del " + polymer + ":</strong> " + monomer + "\n\n"
            + "<strong>Tipo de polimerización:</strong> " + type + "\n\n"
            + details + "\n\n"
            + "<strong>Reacción general:</strong>\n\n"
            + "\\[n\\,\\text{(monómero)} \\xrightarrow{\\text{init. / catalizador}} "
            + "\\text{(-monómero-)}_n\\]";
    }

    private static String polymerTypeExpl(String polymer, String monomer,
                                           String type, String details) {
        boolean condensation = "Condensación".equals(type);
        String schema = condensation
            ? "\\[n\\,\\text{A} + n\\,\\text{B} \\rightarrow "
              + "\\text{(-A-B-)}_n + n\\,\\text{H}_2\\text{O}\\]"
            : "\\[n\\,\\text{CH}_2{=}\\text{CXY} \\rightarrow "
              + "\\text{(-CH}_2\\text{-CXY-)}_n\\]";
        return "<strong>Tipo: Polimerización de " + type + "</strong>\n\n"
            + "<strong>Polímero:</strong> " + polymer + " | "
            + "<strong>Monómero:</strong> " + monomer + "\n\n"
            + details + "\n\n"
            + "<strong>Ecuación esquemática:</strong>\n\n"
            + schema + "\n\n"
            + "<strong>Diferencia clave:</strong>\n"
            + "<ul>"
            + "<li><strong>Adición:</strong> no hay subproducto; todos los átomos del monómero pasan al polímero. "
            + "Requiere doble enlace C=C o anillo tensionado.</li>"
            + "<li><strong>Condensación:</strong> se libera una molécula pequeña (H₂O, HCl, NH₃...) por cada enlace. "
            + "Los monómeros tienen grupos funcionales complementarios (–OH + –COOH, –NH₂ + –COOH...).</li>"
            + "</ul>";
    }
}
