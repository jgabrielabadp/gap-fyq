package com.gap.fyq.service;

import com.gap.fyq.model.secondbach.structurelink.SecondBachStructureLinkExercise;
import com.gap.fyq.model.secondbach.structurelink.StructureLinkType;
import com.gap.fyq.repository.SecondBachStructureLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecondBachStructureLinkService {

    private final SecondBachStructureLinkRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "2BACH_Q";
    private static final String BLOCK  = "BL1";

    // =========================================================================
    // DATA RECORDS
    // =========================================================================

    /**
     * Datos de cada elemento: número cuánticos del ÚLTIMO electrón según
     * el orden de llenado Aufbau. ms2 = 2·mₛ (±1 para evitar fracciones).
     * atomicRadius en pm, en = electronegatividad Pauling, ie1 en kJ/mol.
     */
    private record ElementData(
        int z, String symbol, String nameEs, int period, int group,
        String config,
        int n, int l, int ml, int ms2,
        double atomicRadius, double en, double ie1
    ) {}

    /**
     * Molécula representativa para TRPEV.
     * lonePairs = pares no enlazantes en el ÁTOMO CENTRAL.
     * geometry  = geometría MOLECULAR (no electrónica).
     * polarity  = "polar" | "apolar"
     */
    private record MoleculeData(
        String formula, String formulaHtml, String nameEs, String centralAtom,
        String hybridization, int lonePairs, int bondingPairs,
        String electronDomainGeometry, String geometry, String polarity,
        double bondAngle, String angleNote
    ) {}

    /** Escenario pre-construido para comparaciones de propiedades periódicas. */
    private record PeriodicScenario(
        String statement,
        String optionA, String optionB, String optionC,
        String correctLetter, String correctDisplay,
        String explanation
    ) {}

    // =========================================================================
    // CATÁLOGO DE 36 ELEMENTOS (H → Kr)
    // =========================================================================
    // Columnas: z, symbol, nameEs, period, group, config,
    //           n_last, l_last, ml_last, ms2_last,
    //           r(pm), EN, IE1(kJ/mol)

    private static final List<ElementData> ELEMENTS = List.of(
        new ElementData( 1,"H",  "Hidrógeno", 1, 1,
            "1s1",                                              1,0, 0,+1, 53, 2.20,1312),
        new ElementData( 2,"He", "Helio",     1,18,
            "1s2",                                              1,0, 0,-1, 31, 0.00,2372),
        new ElementData( 3,"Li", "Litio",     2, 1,
            "1s2 2s1",                                          2,0, 0,+1,167, 0.98, 520),
        new ElementData( 4,"Be", "Berilio",   2, 2,
            "1s2 2s2",                                          2,0, 0,-1,112, 1.57, 900),
        new ElementData( 5,"B",  "Boro",      2,13,
            "1s2 2s2 2p1",                                      2,1,-1,+1, 87, 2.04, 800),
        new ElementData( 6,"C",  "Carbono",   2,14,
            "1s2 2s2 2p2",                                      2,1, 0,+1, 77, 2.55,1086),
        new ElementData( 7,"N",  "Nitrógeno", 2,15,
            "1s2 2s2 2p3",                                      2,1,+1,+1, 75, 3.04,1402),
        new ElementData( 8,"O",  "Oxígeno",   2,16,
            "1s2 2s2 2p4",                                      2,1,-1,-1, 73, 3.44,1314),
        new ElementData( 9,"F",  "Flúor",     2,17,
            "1s2 2s2 2p5",                                      2,1, 0,-1, 71, 3.98,1681),
        new ElementData(10,"Ne", "Neón",      2,18,
            "1s2 2s2 2p6",                                      2,1,+1,-1, 69, 0.00,2081),
        new ElementData(11,"Na", "Sodio",     3, 1,
            "1s2 2s2 2p6 3s1",                                  3,0, 0,+1,186, 0.93, 496),
        new ElementData(12,"Mg", "Magnesio",  3, 2,
            "1s2 2s2 2p6 3s2",                                  3,0, 0,-1,160, 1.31, 738),
        new ElementData(13,"Al", "Aluminio",  3,13,
            "1s2 2s2 2p6 3s2 3p1",                              3,1,-1,+1,143, 1.61, 577),
        new ElementData(14,"Si", "Silicio",   3,14,
            "1s2 2s2 2p6 3s2 3p2",                              3,1, 0,+1,118, 1.90, 786),
        new ElementData(15,"P",  "Fósforo",   3,15,
            "1s2 2s2 2p6 3s2 3p3",                              3,1,+1,+1,110, 2.19,1012),
        new ElementData(16,"S",  "Azufre",    3,16,
            "1s2 2s2 2p6 3s2 3p4",                              3,1,-1,-1,104, 2.58, 999),
        new ElementData(17,"Cl", "Cloro",     3,17,
            "1s2 2s2 2p6 3s2 3p5",                              3,1, 0,-1, 99, 3.16,1251),
        new ElementData(18,"Ar", "Argón",     3,18,
            "1s2 2s2 2p6 3s2 3p6",                              3,1,+1,-1, 97, 0.00,1521),
        new ElementData(19,"K",  "Potasio",   4, 1,
            "1s2 2s2 2p6 3s2 3p6 4s1",                          4,0, 0,+1,227, 0.82, 419),
        new ElementData(20,"Ca", "Calcio",    4, 2,
            "1s2 2s2 2p6 3s2 3p6 4s2",                          4,0, 0,-1,197, 1.00, 590),
        new ElementData(21,"Sc", "Escandio",  4, 3,
            "1s2 2s2 2p6 3s2 3p6 3d1 4s2",                      3,2,-2,+1,162, 1.36, 633),
        new ElementData(22,"Ti", "Titanio",   4, 4,
            "1s2 2s2 2p6 3s2 3p6 3d2 4s2",                      3,2,-1,+1,147, 1.54, 659),
        new ElementData(23,"V",  "Vanadio",   4, 5,
            "1s2 2s2 2p6 3s2 3p6 3d3 4s2",                      3,2, 0,+1,134, 1.63, 651),
        new ElementData(24,"Cr", "Cromo",     4, 6,
            "1s2 2s2 2p6 3s2 3p6 3d5 4s1",                      3,2,+2,+1,128, 1.66, 653),
        new ElementData(25,"Mn", "Manganeso", 4, 7,
            "1s2 2s2 2p6 3s2 3p6 3d5 4s2",                      3,2,+1,+1,127, 1.55, 717),
        new ElementData(26,"Fe", "Hierro",    4, 8,
            "1s2 2s2 2p6 3s2 3p6 3d6 4s2",                      3,2,-2,-1,126, 1.83, 762),
        new ElementData(27,"Co", "Cobalto",   4, 9,
            "1s2 2s2 2p6 3s2 3p6 3d7 4s2",                      3,2,-1,-1,125, 1.88, 760),
        new ElementData(28,"Ni", "Níquel",    4,10,
            "1s2 2s2 2p6 3s2 3p6 3d8 4s2",                      3,2, 0,-1,124, 1.91, 737),
        new ElementData(29,"Cu", "Cobre",     4,11,
            "1s2 2s2 2p6 3s2 3p6 3d10 4s1",                     4,0, 0,+1,128, 1.90, 745),
        new ElementData(30,"Zn", "Zinc",      4,12,
            "1s2 2s2 2p6 3s2 3p6 3d10 4s2",                     3,2,+2,-1,122, 1.65, 906),
        new ElementData(31,"Ga", "Galio",     4,13,
            "1s2 2s2 2p6 3s2 3p6 3d10 4s2 4p1",                 4,1,-1,+1,122, 1.81, 579),
        new ElementData(32,"Ge", "Germanio",  4,14,
            "1s2 2s2 2p6 3s2 3p6 3d10 4s2 4p2",                 4,1, 0,+1,120, 2.01, 762),
        new ElementData(33,"As", "Arsénico",  4,15,
            "1s2 2s2 2p6 3s2 3p6 3d10 4s2 4p3",                 4,1,+1,+1,119, 2.18, 947),
        new ElementData(34,"Se", "Selenio",   4,16,
            "1s2 2s2 2p6 3s2 3p6 3d10 4s2 4p4",                 4,1,-1,-1,120, 2.55, 941),
        new ElementData(35,"Br", "Bromo",     4,17,
            "1s2 2s2 2p6 3s2 3p6 3d10 4s2 4p5",                 4,1, 0,-1,114, 2.96,1140),
        new ElementData(36,"Kr", "Kriptón",   4,18,
            "1s2 2s2 2p6 3s2 3p6 3d10 4s2 4p6",                 4,1,+1,-1,110, 3.00,1351)
    );

    // Subconjunto para CONFIG_TEXT (los 26 más trabajados en 2BACH)
    private static final List<ElementData> ELEMENTS_CONFIG = ELEMENTS.stream()
        .filter(e -> e.z() >= 3 && (e.z() <= 20 || e.z() == 24 || e.z() == 26
                                 || e.z() == 29 || e.z() == 30
                                 || e.z() == 35 || e.z() == 36))
        .toList();

    // Subconjunto para QUANTUM_MCQ (excluye Cr y Cu por anomalía)
    private static final List<ElementData> ELEMENTS_MCQ = ELEMENTS.stream()
        .filter(e -> e.z() >= 3 && e.z() != 24 && e.z() != 29
                  && (e.z() <= 30 || (e.z() >= 31 && e.z() <= 36)))
        .toList();

    // =========================================================================
    // CATÁLOGO DE MOLÉCULAS PARA TRPEV
    // =========================================================================

    private static final List<MoleculeData> MOLECULES = List.of(
        new MoleculeData("CH4","CH&#8324;","metano","C",
            "sp3",0,4,"tetraédrica","tetraédrica","apolar",109.5,"(tetrahedral perfecto)"),
        new MoleculeData("NH3","NH&#8323;","amoníaco","N",
            "sp3",1,3,"tetraédrica","piramidal trigonal","polar",107.0,"< 109,5° por repulsión del par libre"),
        new MoleculeData("H2O","H&#8322;O","agua","O",
            "sp3",2,2,"tetraédrica","angular","polar",104.5,"<< 109,5° por 2 pares libres"),
        new MoleculeData("BF3","BF&#8323;","trifluoruro de boro","B",
            "sp2",0,3,"trigonal plana","trigonal plana","apolar",120.0,""),
        new MoleculeData("CO2","CO&#8322;","dióxido de carbono","C",
            "sp",0,4,"lineal","lineal","apolar",180.0,""),
        new MoleculeData("SO2","SO&#8322;","dióxido de azufre","S",
            "sp2",1,2,"trigonal plana","angular","polar",119.0,"< 120° por par libre"),
        new MoleculeData("CCl4","CCl&#8324;","tetracloruro de carbono","C",
            "sp3",0,4,"tetraédrica","tetraédrica","apolar",109.5,""),
        new MoleculeData("PH3","PH&#8323;","fosfina","P",
            "sp3",1,3,"tetraédrica","piramidal trigonal","polar",93.0,"< 107° (P más voluminoso que N)"),
        new MoleculeData("H2S","H&#8322;S","sulfuro de hidrógeno","S",
            "sp3",2,2,"tetraédrica","angular","polar",92.0,"< 104,5° (S más grande que O)"),
        new MoleculeData("PCl5","PCl&#8325;","pentacloruro de fósforo","P",
            "sp3d",0,5,"bipiramidal trigonal","bipiramidal trigonal","apolar",90.0,"axial 90°, ecuatorial 120°"),
        new MoleculeData("SF6","SF&#8326;","hexafluoruro de azufre","S",
            "sp3d2",0,6,"octaédrica","octaédrica","apolar",90.0,""),
        new MoleculeData("SO3","SO&#8323;","trióxido de azufre","S",
            "sp2",0,3,"trigonal plana","trigonal plana","apolar",120.0,"")
    );

    // =========================================================================
    // ESCENARIOS DE PROPIEDADES PERIÓDICAS (10 escenarios)
    // =========================================================================

    private static final List<PeriodicScenario> PERIODIC_SCENARIOS = List.of(

        new PeriodicScenario(
            "¿Cuál de los siguientes elementos del Grupo 1 (metales alcalinos) tiene mayor radio atómico?",
            "A) Litio (Li, Z=3)", "B) Sodio (Na, Z=11)", "C) Potasio (K, Z=19)",
            "C", "Potasio (K)",
            buildPeriodicExpl("radio atómico",
                "En un grupo, al bajar, el número cuántico principal <em>n</em> del último electrón "
                + "aumenta (2s → 3s → 4s). Aunque la carga nuclear crece, el efecto pantalla de los "
                + "electrones internos compensa: Z<sub>ef</sub> ≈ Z − σ permanece similar. "
                + "La distancia media del electrón externo al núcleo sube con <em>n</em>, "
                + "por lo que <strong>el radio atómico aumenta al bajar en un grupo</strong>.",
                "Li ≈ 167 pm", "Na ≈ 186 pm", "K ≈ 227 pm",
                "r(K) > r(Na) > r(Li) → opción C")),

        new PeriodicScenario(
            "¿Cuál de los siguientes elementos del Periodo 3 tiene mayor radio atómico?",
            "A) Sodio (Na, Z=11)", "B) Magnesio (Mg, Z=12)", "C) Aluminio (Al, Z=13)",
            "A", "Sodio (Na)",
            buildPeriodicExpl("radio atómico",
                "En un periodo, al avanzar de izquierda a derecha, Z crece mientras los electrones "
                + "se añaden al mismo nivel n. El apantallamiento entre electrones del mismo nivel "
                + "es débil (σ ≈ 0,35 por electrón del mismo nivel). Así, Z<sub>ef</sub> sube y "
                + "los electrones externos son más atraídos, <strong>reduciendo el radio de izquierda a derecha</strong>.",
                "Na ≈ 186 pm", "Mg ≈ 160 pm", "Al ≈ 143 pm",
                "r(Na) > r(Mg) > r(Al) → opción A")),

        new PeriodicScenario(
            "¿Cuál de los siguientes elementos del Grupo 1 tiene mayor primera energía de ionización (EI₁)?",
            "A) Litio (Li, Z=3)", "B) Sodio (Na, Z=11)", "C) Potasio (K, Z=19)",
            "A", "Litio (Li)",
            buildPeriodicExpl("primera energía de ionización",
                "La EI₁ es la energía necesaria para arrancar el electrón más externo. "
                + "Al bajar en un grupo, el radio atómico crece y el electrón externo está "
                + "más lejos del núcleo y más apantallado: la atracción es menor y cuesta "
                + "<strong>menos</strong> energía ionizarlo. "
                + "Por tanto, <strong>EI₁ disminuye al bajar en un grupo</strong>.",
                "Li EI₁ ≈ 520 kJ/mol", "Na EI₁ ≈ 496 kJ/mol", "K EI₁ ≈ 419 kJ/mol",
                "EI₁(Li) > EI₁(Na) > EI₁(K) → opción A")),

        new PeriodicScenario(
            "¿Cuál de los siguientes elementos del Periodo 3 tiene mayor primera energía de ionización?",
            "A) Sodio (Na, Z=11)", "B) Fósforo (P, Z=15)", "C) Cloro (Cl, Z=17)",
            "C", "Cloro (Cl)",
            buildPeriodicExpl("primera energía de ionización",
                "En un periodo, Z<sub>ef</sub> aumenta de izquierda a derecha (mismo n, mayor Z). "
                + "El radio disminuye y el electrón externo queda más ligado al núcleo. "
                + "<strong>EI₁ aumenta de izquierda a derecha en un periodo</strong> (con pequeñas "
                + "irregularidades en B/N por semillenado y llenado de subniveles).",
                "Na EI₁ ≈ 496 kJ/mol", "P EI₁ ≈ 1012 kJ/mol", "Cl EI₁ ≈ 1251 kJ/mol",
                "EI₁(Cl) > EI₁(P) > EI₁(Na) → opción C")),

        new PeriodicScenario(
            "¿Cuál de los siguientes elementos del Grupo 17 (halógenos) tiene mayor electronegatividad (escala de Pauling)?",
            "A) Flúor (F, Z=9)", "B) Cloro (Cl, Z=17)", "C) Bromo (Br, Z=35)",
            "A", "Flúor (F, χ = 3,98)",
            buildPeriodicExpl("electronegatividad",
                "La electronegatividad mide la tendencia de un átomo a atraer hacia sí los "
                + "electrones del enlace. Al subir en un grupo, el radio disminuye y Z<sub>ef</sub> "
                + "es relativamente alto respecto al tamaño atómico: el núcleo atrae con más "
                + "fuerza los electrones enlazantes. "
                + "<strong>El flúor es el elemento más electronegativo de la tabla periódica (χ = 3,98)</strong>.",
                "F χ = 3,98", "Cl χ = 3,16", "Br χ = 2,96",
                "χ(F) > χ(Cl) > χ(Br) → opción A")),

        new PeriodicScenario(
            "¿Cuál de los siguientes elementos del Periodo 2 tiene mayor electronegatividad?",
            "A) Carbono (C, Z=6)", "B) Nitrógeno (N, Z=7)", "C) Oxígeno (O, Z=8)",
            "C", "Oxígeno (O, χ = 3,44)",
            buildPeriodicExpl("electronegatividad",
                "En un periodo, la electronegatividad aumenta de izquierda a derecha: "
                + "mayor Z con mismo nivel de valencia → mayor Z<sub>ef</sub> → "
                + "mayor atracción sobre los electrones enlazantes. "
                + "En periodo 2: C (2,55) < N (3,04) < O (3,44) < F (3,98).",
                "C χ = 2,55", "N χ = 3,04", "O χ = 3,44",
                "χ(O) > χ(N) > χ(C) → opción C")),

        new PeriodicScenario(
            "Los iones Na⁺, Mg²⁺ y Al³⁺ son isoelectrónicos (tienen 10 electrones, config. del Ne). "
            + "¿Cuál posee menor radio iónico?",
            "A) Na⁺ (Z=11)", "B) Mg²⁺ (Z=12)", "C) Al³⁺ (Z=13)",
            "C", "Al³⁺ (Z=13, radio menor)",
            "<strong>Serie isoelectrónica:</strong> los tres iones tienen el mismo número de "
            + "electrones (10) y la misma configuración (1s² 2s² 2p⁶). La diferencia está en "
            + "la carga nuclear:\n\n"
            + "\\[Z_{\\text{ef}} \\approx Z - \\sigma \\quad (\\sigma\\text{ igual en los tres})\\]\n\n"
            + "Valores de Z: Na⁺ → 11, Mg²⁺ → 12, Al³⁺ → 13. Mayor Z ⟹ mayor atracción "
            + "sobre los mismos 10 electrones ⟹ <strong>radio más pequeño</strong>.\n\n"
            + "\\[r(\\text{Al}^{3+}) < r(\\text{Mg}^{2+}) < r(\\text{Na}^+)\\]\n\n"
            + "Valores orientativos: Al³⁺ ≈ 54 pm, Mg²⁺ ≈ 72 pm, Na⁺ ≈ 102 pm. → opción C"),

        new PeriodicScenario(
            "¿Cuál de los siguientes elementos del Grupo 16 tiene mayor radio atómico?",
            "A) Oxígeno (O, Z=8)", "B) Azufre (S, Z=16)", "C) Selenio (Se, Z=34)",
            "C", "Selenio (Se)",
            buildPeriodicExpl("radio atómico",
                "Grupo 16: cada elemento ocupa un periodo superior (n=2, 3, 4). "
                + "Al bajar en el grupo, se añaden capas electrónicas completas que "
                + "apantallan al electrón externo del núcleo: el radio crece.",
                "O ≈ 73 pm", "S ≈ 104 pm", "Se ≈ 120 pm",
                "r(Se) > r(S) > r(O) → opción C")),

        new PeriodicScenario(
            "¿Cuál de los siguientes elementos del Grupo 2 (alcalinotérreos) tiene mayor primera energía de ionización?",
            "A) Berilio (Be, Z=4)", "B) Magnesio (Mg, Z=12)", "C) Calcio (Ca, Z=20)",
            "A", "Berilio (Be)",
            buildPeriodicExpl("primera energía de ionización",
                "Grupo 2: al bajar, el radio atómico aumenta y el apantallamiento de los "
                + "electrones internos crece. El electrón de valencia 2s/3s/4s queda más alejado "
                + "y apantallado, por lo que se necesita menos energía para ionizarlo. "
                + "La EI₁ disminuye al descender en un grupo.",
                "Be EI₁ ≈ 900 kJ/mol", "Mg EI₁ ≈ 738 kJ/mol", "Ca EI₁ ≈ 590 kJ/mol",
                "EI₁(Be) > EI₁(Mg) > EI₁(Ca) → opción A")),

        new PeriodicScenario(
            "¿Cuál de los siguientes elementos del Periodo 3 tiene mayor electronegatividad?",
            "A) Sodio (Na, Z=11)", "B) Fósforo (P, Z=15)", "C) Cloro (Cl, Z=17)",
            "C", "Cloro (Cl, χ = 3,16)",
            buildPeriodicExpl("electronegatividad",
                "En periodo 3, de Na a Cl, Z crece y el radio decrece manteniendo n=3. "
                + "Z<sub>ef</sub> aumenta apreciablemente: los electrones enlazantes son atraídos "
                + "con mayor fuerza hacia el átomo más a la derecha. "
                + "El Cl, con 7 electrones de valencia y gran Z<sub>ef</sub>, "
                + "es el más electronegativo del Periodo 3.",
                "Na χ = 0,93", "P χ = 2,19", "Cl χ = 3,16",
                "χ(Cl) > χ(P) > χ(Na) → opción C"))
    );

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public SecondBachStructureLinkExercise generateAndSave() {
        var ex = new SecondBachStructureLinkExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);

        int roll = random.nextInt(4);
        switch (roll) {
            case 0 -> buildConfigText(ex);
            case 1 -> buildQuantumMCQ(ex);
            case 2 -> buildPeriodicProperties(ex);
            default -> buildMolecularGeometry(ex);
        }

        log.debug("2BACH_Q BL1 generado: type={} mode={}",
            ex.getStructureLinkType(), ex.getExerciseMode());
        return repository.save(ex);
    }

    public SecondBachStructureLinkExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ejercicio 2BACH_Q BL1 no encontrado: " + id));
    }

    // =========================================================================
    // BUILDER — CONFIG_TEXT
    // =========================================================================

    private void buildConfigText(SecondBachStructureLinkExercise ex) {
        ex.setStructureLinkType(StructureLinkType.QUANTUM_NUMBERS_CONFIG);
        ex.setExerciseMode("CONFIG_TEXT");

        ElementData el = ELEMENTS_CONFIG.get(random.nextInt(ELEMENTS_CONFIG.size()));

        String note = (el.symbol().equals("Cr") || el.symbol().equals("Cu"))
            ? " Ten en cuenta su configuración anómala (estabilidad del subnivel semilleno o lleno)."
            : "";
        ex.setStatement(String.format(
            "Escribe la configuración electrónica completa del %s (%s, Z = %d).%s",
            el.nameEs(), el.symbol(), el.z(), note));

        ex.setCorrectAnswer(el.config());
        ex.setCorrectAnswerDisplay(el.config());
        ex.setExplanation(buildConfigExplanation(el));
    }

    // =========================================================================
    // BUILDER — QUANTUM_MCQ
    // =========================================================================

    private void buildQuantumMCQ(SecondBachStructureLinkExercise ex) {
        ex.setStructureLinkType(StructureLinkType.QUANTUM_NUMBERS_CONFIG);
        ex.setExerciseMode("QUANTUM_MCQ");

        ElementData el = ELEMENTS_MCQ.get(random.nextInt(ELEMENTS_MCQ.size()));

        String correct = formatQN(el.n(), el.l(), el.ml(), el.ms2());
        String d1 = generateDistractor(el, 1);
        String d2 = generateDistractor(el, 2);
        // guarantee d1 ≠ d2 ≠ correct
        if (d1.equals(correct) || d1.equals(d2)) d1 = formatQN(el.n(), el.l(), el.ml(), -el.ms2());
        if (d2.equals(correct) || d2.equals(d1)) d2 = formatQN(el.n(), el.l(),
            el.ml() != 0 ? 0 : (el.l() > 0 ? el.l() : 1), -el.ms2());

        int correctPos = random.nextInt(3);
        String[] opts = new String[3];
        opts[correctPos]           = correct;
        opts[(correctPos + 1) % 3] = d1;
        opts[(correctPos + 2) % 3] = d2;

        ex.setOptionA(opts[0]);
        ex.setOptionB(opts[1]);
        ex.setOptionC(opts[2]);
        ex.setCorrectAnswer(new String[]{"A","B","C"}[correctPos]);
        ex.setCorrectAnswerDisplay(correct);

        ex.setStatement(String.format(
            "¿Cuál de los siguientes conjuntos de números cuánticos (n, l, mₗ, mₛ) "
            + "corresponde correctamente al ÚLTIMO electrón del %s (%s, Z = %d) "
            + "según el principio de Aufbau y las reglas de Hund?",
            el.nameEs(), el.symbol(), el.z()));

        ex.setExplanation(buildQuantumMCQExplanation(el));
    }

    private String generateDistractor(ElementData el, int type) {
        int n = el.n(), l = el.l(), ml = el.ml(), ms2 = el.ms2();
        if (type == 1) {
            // Cambiar l: usar l+1 si cabe, si no l-1 o 0
            int newL = (l + 1 < n) ? l + 1 : Math.max(0, l - 1);
            int newMl = 0;
            return formatQN(n, newL, newMl, ms2);
        } else {
            // Cambiar n: usar n-1 si >1, si no n+1
            int newN = (n > 1) ? n - 1 : n + 1;
            int newL = Math.min(l, newN - 1);
            int newMl = (Math.abs(ml) <= newL) ? ml : newL;
            return formatQN(newN, newL, newMl, ms2);
        }
    }

    private String formatQN(int n, int l, int ml, int ms2) {
        String msStr = (ms2 > 0) ? "+1/2" : "-1/2";
        return String.format("n=%d, l=%d, mₗ=%d, mₛ=%s", n, l, ml, msStr);
    }

    // =========================================================================
    // BUILDER — PERIODIC_PROPERTIES
    // =========================================================================

    private void buildPeriodicProperties(SecondBachStructureLinkExercise ex) {
        ex.setStructureLinkType(StructureLinkType.PERIODIC_PROPERTIES);
        ex.setExerciseMode("PERIODIC_MCQ");

        PeriodicScenario sc = PERIODIC_SCENARIOS.get(random.nextInt(PERIODIC_SCENARIOS.size()));
        ex.setStatement(sc.statement());
        ex.setOptionA(sc.optionA());
        ex.setOptionB(sc.optionB());
        ex.setOptionC(sc.optionC());
        ex.setCorrectAnswer(sc.correctLetter());
        ex.setCorrectAnswerDisplay(sc.correctDisplay());
        ex.setExplanation(sc.explanation());
    }

    // =========================================================================
    // BUILDER — MOLECULAR_GEOMETRY_TRPEV
    // =========================================================================

    private void buildMolecularGeometry(SecondBachStructureLinkExercise ex) {
        ex.setStructureLinkType(StructureLinkType.MOLECULAR_GEOMETRY_TRPEV);
        ex.setExerciseMode("GEOMETRY_MULTI");

        MoleculeData mol = MOLECULES.get(random.nextInt(MOLECULES.size()));

        ex.setStatement(String.format(
            "Aplica la Teoría de Repulsión de Pares Electrónicos de Valencia (RPECV/TRPEV) "
            + "a la molécula de %s (%s). El átomo central es el %s. "
            + "Indica: (1) hibridación del átomo central, "
            + "(2) número de pares no enlazantes en el átomo central, "
            + "(3) geometría molecular resultante, "
            + "(4) carácter polar o apolar de la molécula.",
            mol.nameEs(), mol.formulaHtml(), mol.centralAtom()));

        // correctAnswer codificado: hibridación|paresLibres|geometría|polaridad
        ex.setCorrectAnswer(mol.hybridization() + "|" + mol.lonePairs()
                + "|" + mol.geometry() + "|" + mol.polarity());
        ex.setCorrectAnswerDisplay(
            "Hibridación: " + mol.hybridization() +
            " | Pares libres: " + mol.lonePairs() +
            " | Geometría: " + mol.geometry() +
            " | Carácter: " + mol.polarity());
        ex.setExplanation(buildGeometryExplanation(mol));
    }

    // =========================================================================
    // EXPLICACIONES — CONFIG_TEXT
    // =========================================================================

    private String buildConfigExplanation(ElementData el) {
        var sb = new StringBuilder();

        sb.append("<strong>Principio de construcción progresiva (Aufbau) — ")
          .append(el.nameEs()).append(" (").append(el.symbol())
          .append(", Z = ").append(el.z()).append("):</strong>\n\n");

        sb.append("Los electrones se colocan en los orbitales de menor a mayor energía siguiendo ")
          .append("el <em>diagrama de Moeller</em> (regla n+l mínimo):\n\n")
          .append("\\[1s\\to 2s\\to 2p\\to 3s\\to 3p\\to 4s\\to 3d\\to 4p\\to\\cdots\\]\n\n");

        sb.append("<strong>Reglas aplicadas:</strong>\n")
          .append("<ul>")
          .append("<li><strong>Pauli:</strong> cada orbital admite máximo 2 electrones con espines opuestos (↑↓).</li>")
          .append("<li><strong>Hund:</strong> dentro de un subnivel degenerado, los electrones ocupan primero orbitales diferentes con espín paralelo (↑) antes de emparejarse.</li>")
          .append("<li><strong>Aufbau:</strong> se llenan los orbitales en orden creciente de energía.</li>")
          .append("</ul>\n\n");

        sb.append("<strong>Distribución para Z = ").append(el.z()).append(" electrones:</strong>\n\n");
        sb.append("<code>").append(el.config()).append("</code>\n\n");

        // Detallar por subnivel
        sb.append(buildSubshellDetail(el));

        if (el.symbol().equals("Cr")) {
            sb.append("\n\n<strong>Configuración anómala del Cr ([Ar]3d⁵ 4s¹):</strong>\n\n")
              .append("La configuración esperada según Aufbau sería [Ar]3d⁴ 4s². Sin embargo, ")
              .append("el subnivel <strong>3d semilleno</strong> (5 electrones, uno por orbital) ")
              .append("tiene una estabilidad extra por la energía de intercambio cuántica. ")
              .append("Esta ganancia energética compensa la promoción de un electrón de 4s a 3d:\n\n")
              .append("\\[\\text{[Ar]}3d^4 4s^2 \\xrightarrow{\\Delta E_{\\text{intercambio}}} ")
              .append("\\text{[Ar]}3d^5 4s^1\\quad (\\text{más estable})\\]");
        }
        if (el.symbol().equals("Cu")) {
            sb.append("\n\n<strong>Configuración anómala del Cu ([Ar]3d¹⁰ 4s¹):</strong>\n\n")
              .append("La configuración esperada sería [Ar]3d⁹ 4s². El subnivel ")
              .append("<strong>3d completamente lleno</strong> (10 electrones) es especialmente ")
              .append("estable. La ganancia energética de completar el 3d supera el coste de ")
              .append("promover un electrón del 4s:\n\n")
              .append("\\[\\text{[Ar]}3d^9 4s^2 \\xrightarrow{\\Delta E} ")
              .append("\\text{[Ar]}3d^{10} 4s^1\\quad (\\text{más estable})\\]");
        }

        sb.append("\n\n∴ Configuración electrónica completa: <strong><code>")
          .append(el.config()).append("</code></strong>");
        return sb.toString();
    }

    private String buildSubshellDetail(ElementData el) {
        // Reconstruye el llenado subshell a subshell para la explicación
        int[] aufbauN = {1,2,2,3,3,4,3,4,5,4,5,6,4,5,6,7};
        int[] aufbauL = {0,0,1,0,1,0,2,1,0,2,1,0,3,2,1,0};
        int maxElec   = el.z();
        var sb = new StringBuilder("<ul>");
        int placed = 0;
        for (int i = 0; i < aufbauN.length && placed < maxElec; i++) {
            int n = aufbauN[i], l = aufbauL[i];
            int cap = 2 * (2 * l + 1);
            int here = Math.min(cap, maxElec - placed);
            String label = n + subLabel(l);
            sb.append("<li>").append(label).append(": ").append(here)
              .append(" e⁻ ").append(hund(l, here)).append("</li>");
            placed += here;
        }
        sb.append("</ul>");
        return sb.toString();
    }

    private String subLabel(int l) {
        return switch (l) { case 0 -> "s"; case 1 -> "p"; case 2 -> "d"; default -> "f"; };
    }

    private String hund(int l, int electrons) {
        int orbitals = 2 * l + 1;
        if (electrons <= orbitals) return "(↑ solo, Hund)";
        int paired = electrons - orbitals;
        return "(" + paired + " pareados + " + (electrons - paired) + " sin pareja)";
    }

    // =========================================================================
    // EXPLICACIONES — QUANTUM_MCQ
    // =========================================================================

    private String buildQuantumMCQExplanation(ElementData el) {
        var sb = new StringBuilder();

        sb.append("<strong>Los cuatro números cuánticos (modelo mecano-cuántico):</strong>\n")
          .append("<ul>")
          .append("<li>\\(n\\) (principal): nivel de energía / capa. Entero ≥ 1.</li>")
          .append("<li>\\(l\\) (azimutal): forma del orbital. \\(0 \\le l \\le n-1\\). ")
          .append("0 = s, 1 = p, 2 = d, 3 = f.</li>")
          .append("<li>\\(m_l\\) (magnético): orientación. \\(-l \\le m_l \\le +l\\).</li>")
          .append("<li>\\(m_s\\) (espín): +½ (↑) o −½ (↓).</li>")
          .append("</ul>\n\n");

        sb.append("<strong>Configuración del ").append(el.nameEs())
          .append(" (Z=").append(el.z()).append("):</strong>\n\n")
          .append("<code>").append(el.config()).append("</code>\n\n");

        sb.append("<strong>Último subnivel:</strong> ")
          .append(el.n()).append(subLabel(el.l())).append(" → ")
          .append("n = ").append(el.n())
          .append(", l = ").append(el.l())
          .append(" (subnivel ").append(subLabel(el.l())).append(").\n\n");

        // Diagrama de caja textual para el último subnivel
        sb.append(buildBoxDiagram(el)).append("\n\n");

        sb.append("<strong>Aplicando la regla de Hund</strong> al subnivel ")
          .append(el.n()).append(subLabel(el.l())).append(":\n")
          .append("el último electrón ocupa el orbital con ")
          .append("mₗ = ").append(el.ml())
          .append(", espín mₛ = ").append(el.ms2() > 0 ? "+1/2 (↑)" : "-1/2 (↓)").append(".\n\n");

        sb.append("∴ Conjunto correcto: <strong>")
          .append(formatQN(el.n(), el.l(), el.ml(), el.ms2())).append("</strong>");
        return sb.toString();
    }

    private String buildBoxDiagram(ElementData el) {
        int l = el.l();
        int z = el.z();
        // Cuenta cuántos electrones hay en el último subnivel
        int[] aufbauN = {1,2,2,3,3,4,3,4};
        int[] aufbauL = {0,0,1,0,1,0,2,1};
        int placed = 0, inSubshell = 0;
        for (int i = 0; i < aufbauN.length; i++) {
            int cap = 2 * (2 * aufbauL[i] + 1);
            if (aufbauN[i] == el.n() && aufbauL[i] == el.l()) {
                inSubshell = Math.min(cap, z - placed);
                break;
            }
            placed += Math.min(cap, Math.max(0, z - placed));
        }

        int orbitals = 2 * l + 1;
        StringBuilder row = new StringBuilder("<strong>Diagrama de cajas " + el.n() + subLabel(l) + ":</strong> ");
        int[] spins = new int[orbitals]; // 0=vacío, 1=↑, 2=↑↓
        // Hund: primer barrido ↑
        for (int k = 0; k < Math.min(inSubshell, orbitals); k++) spins[k] = 1;
        // segundo barrido: ↑↓
        for (int k = 0; k < inSubshell - orbitals && k < orbitals; k++) spins[k] = 2;

        row.append("[");
        for (int k = 0; k < orbitals; k++) {
            row.append(" ");
            row.append(switch (spins[k]) { case 1 -> "↑_"; case 2 -> "↑↓"; default -> "__"; });
            row.append(" |");
        }
        row.append("] (").append(inSubshell).append(" e⁻)");
        return row.toString();
    }

    // =========================================================================
    // EXPLICACIONES — MOLECULAR_GEOMETRY_TRPEV
    // =========================================================================

    private String buildGeometryExplanation(MoleculeData mol) {
        var sb = new StringBuilder();

        sb.append("<strong>Resolución TRPEV/RPECV para ").append(mol.formulaHtml())
          .append(" (").append(mol.nameEs()).append("):</strong>\n\n");

        // Paso 1: Electrones de valencia del átomo central
        sb.append("<strong>Paso 1 — Electrones de valencia del átomo central (")
          .append(mol.centralAtom()).append("):</strong>\n")
          .append(buildValenceElectronNote(mol.centralAtom())).append("\n\n");

        // Paso 2: Dominios electrónicos
        int totalDomains = mol.bondingPairs() + mol.lonePairs();
        sb.append("<strong>Paso 2 — Dominios electrónicos:</strong>\n\n")
          .append("\\[D_{\\text{total}} = D_{\\text{enlazantes}} + D_{\\text{libres}} = ")
          .append(mol.bondingPairs()).append(" + ").append(mol.lonePairs())
          .append(" = ").append(totalDomains).append("\\]\n\n");

        // Paso 3: Hibridación
        sb.append("<strong>Paso 3 — Hibridación</strong> (según nº de dominios electrónicos):\n\n")
          .append(buildHybridizationNote(totalDomains, mol.hybridization())).append("\n\n");

        // Paso 4: Geometría electrónica vs molecular
        sb.append("<strong>Paso 4 — Geometría:</strong>\n")
          .append("<ul>")
          .append("<li><em>Geometría electrónica</em> (incluye pares libres): ")
          .append("<strong>").append(mol.electronDomainGeometry()).append("</strong></li>")
          .append("<li><em>Geometría molecular</em> (solo posición de átomos): ")
          .append("<strong>").append(mol.geometry()).append("</strong>");
        if (mol.lonePairs() > 0) {
            sb.append(" — Los pares libres <strong>no son átomos visibles</strong> pero ocupan ")
              .append("más espacio angular que los pares enlazantes, comprimiendo los ángulos.");
        }
        sb.append("</li></ul>\n\n");

        // Paso 5: Ángulo
        sb.append("<strong>Paso 5 — Ángulo de enlace aproximado:</strong> ≈ ")
          .append(mol.bondAngle() == 180 ? "180°" : String.format("%.1f°", mol.bondAngle()));
        if (!mol.angleNote().isEmpty()) sb.append(" ").append(mol.angleNote());
        sb.append("\n\n");

        // Paso 6: Polaridad
        sb.append("<strong>Paso 6 — Polaridad:</strong>\n")
          .append(buildPolarityNote(mol)).append("\n\n");

        sb.append("∴ Hibridación: <strong>").append(mol.hybridization())
          .append("</strong> | Pares libres: <strong>").append(mol.lonePairs())
          .append("</strong> | Geometría: <strong>").append(mol.geometry())
          .append("</strong> | Carácter: <strong>").append(mol.polarity()).append("</strong>");
        return sb.toString();
    }

    private String buildValenceElectronNote(String atom) {
        return switch (atom) {
            case "C"  -> "El carbono (Grupo 14) tiene <strong>4 electrones de valencia</strong> (2s² 2p²). Necesita 4 enlaces para completar el octeto.";
            case "N"  -> "El nitrógeno (Grupo 15) tiene <strong>5 electrones de valencia</strong> (2s² 2p³). Forma 3 enlaces + 1 par libre.";
            case "O"  -> "El oxígeno (Grupo 16) tiene <strong>6 electrones de valencia</strong> (2s² 2p⁴). Forma 2 enlaces + 2 pares libres.";
            case "B"  -> "El boro (Grupo 13) tiene <strong>3 electrones de valencia</strong> (2s² 2p¹). Forma 3 enlaces y queda con sexto incompleto (deficiente de electrones).";
            case "S"  -> "El azufre (Grupo 16, período 3) tiene <strong>6 electrones de valencia</strong>. Al pertenecer al período 3 puede expandir su octeto usando orbitales 3d.";
            case "P"  -> "El fósforo (Grupo 15, período 3) tiene <strong>5 electrones de valencia</strong>. Puede superar el octeto usando orbitales 3d vacíos.";
            default   -> "El átomo central aporta sus electrones de valencia al enlace.";
        };
    }

    private String buildHybridizationNote(int domains, String hybrid) {
        return "\\[" + domains + "\\text{ dominios} \\implies \\text{hibridación } " + hybrid + "\\]\n"
             + switch (domains) {
               case 2 -> "2 dominios → orbitales s + p → <strong>sp</strong> → geometría electrónica lineal (180°).";
               case 3 -> "3 dominios → orbitales s + 2p → <strong>sp²</strong> → geometría electrónica trigonal plana (120°).";
               case 4 -> "4 dominios → orbitales s + 3p → <strong>sp³</strong> → geometría electrónica tetraédrica (109,5°).";
               case 5 -> "5 dominios → orbitales s + 3p + d → <strong>sp³d</strong> → geometría electrónica bipiramidal trigonal.";
               case 6 -> "6 dominios → orbitales s + 3p + 2d → <strong>sp³d²</strong> → geometría electrónica octaédrica (90°).";
               default -> "";
             };
    }

    private String buildPolarityNote(MoleculeData mol) {
        if (mol.polarity().equals("apolar")) {
            return "La molécula es <strong>apolar</strong> porque "
                + (mol.lonePairs() == 0
                    ? "todos los vectores dipolo de los enlaces son iguales y se cancelan por simetría (Σ\\(\\vec{\\mu}\\) = 0)."
                    : "la distribución geométrica hace que los momentos dipolares se anulen.");
        } else {
            return "La molécula es <strong>polar</strong> porque "
                + (mol.lonePairs() > 0
                    ? "la presencia de " + mol.lonePairs() + " par(es) libre(s) rompe la simetría: "
                      + "los vectores dipolo de los enlaces <strong>no se cancelan</strong> "
                      + "(\\(\\Sigma\\vec{\\mu} \\neq 0\\))."
                    : "los electrones de enlace están desigualmente distribuidos por diferencia de electronegatividad "
                      + "y la geometría no permite que los dipolos se anulen.");
        }
    }

    // =========================================================================
    // HELPER — buildPeriodicExpl  (factoría estática de explicación)
    // =========================================================================

    private static String buildPeriodicExpl(String property,
                                             String principleText,
                                             String valA, String valB, String valC,
                                             String conclusion) {
        return "<strong>Tendencia periódica: " + property + "</strong>\n\n"
             + principleText + "\n\n"
             + "<strong>Valores de referencia:</strong>\n"
             + "<ul>"
             + "<li>" + valA + "</li>"
             + "<li>" + valB + "</li>"
             + "<li>" + valC + "</li>"
             + "</ul>\n\n"
             + "<strong>Conclusión:</strong> " + conclusion;
    }
}
