package com.gap.fyq.service;

import com.gap.fyq.model.fourtheso.matter.FourthEsoMatterExercise;
import com.gap.fyq.model.fourtheso.matter.FourthEsoMatterType;
import com.gap.fyq.repository.FourthEsoMatterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class FourthEsoMatterService {

    private final FourthEsoMatterRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "4ESO";
    private static final String BLOCK  = "BL2";

    // =========================================================================
    // Tabla de los 36 primeros elementos (Z = 1 … 36)
    // configNormalized: notación estándar ASCII para validación
    // configDisplay:    notación con superíndices Unicode para mostrar al alumno
    // isException:      Cr (Z=24) y Cu (Z=29) por semillenado / llenado del 3d
    // =========================================================================

    private record ElementData(
        int z, String symbol, String nameEs,
        String configNormalized, String configDisplay,
        boolean isException
    ) {}

    private static final List<ElementData> ELEMENTS = List.of(
        new ElementData( 1, "H",  "Hidrógeno",   "1s1",                                               "1s¹",                                               false),
        new ElementData( 2, "He", "Helio",        "1s2",                                               "1s²",                                               false),
        new ElementData( 3, "Li", "Litio",        "1s2 2s1",                                           "1s² 2s¹",                                           false),
        new ElementData( 4, "Be", "Berilio",      "1s2 2s2",                                           "1s² 2s²",                                           false),
        new ElementData( 5, "B",  "Boro",         "1s2 2s2 2p1",                                       "1s² 2s² 2p¹",                                       false),
        new ElementData( 6, "C",  "Carbono",      "1s2 2s2 2p2",                                       "1s² 2s² 2p²",                                       false),
        new ElementData( 7, "N",  "Nitrógeno",    "1s2 2s2 2p3",                                       "1s² 2s² 2p³",                                       false),
        new ElementData( 8, "O",  "Oxígeno",      "1s2 2s2 2p4",                                       "1s² 2s² 2p⁴",                                       false),
        new ElementData( 9, "F",  "Flúor",        "1s2 2s2 2p5",                                       "1s² 2s² 2p⁵",                                       false),
        new ElementData(10, "Ne", "Neón",         "1s2 2s2 2p6",                                       "1s² 2s² 2p⁶",                                       false),
        new ElementData(11, "Na", "Sodio",        "1s2 2s2 2p6 3s1",                                   "1s² 2s² 2p⁶ 3s¹",                                   false),
        new ElementData(12, "Mg", "Magnesio",     "1s2 2s2 2p6 3s2",                                   "1s² 2s² 2p⁶ 3s²",                                   false),
        new ElementData(13, "Al", "Aluminio",     "1s2 2s2 2p6 3s2 3p1",                               "1s² 2s² 2p⁶ 3s² 3p¹",                               false),
        new ElementData(14, "Si", "Silicio",      "1s2 2s2 2p6 3s2 3p2",                               "1s² 2s² 2p⁶ 3s² 3p²",                               false),
        new ElementData(15, "P",  "Fósforo",      "1s2 2s2 2p6 3s2 3p3",                               "1s² 2s² 2p⁶ 3s² 3p³",                               false),
        new ElementData(16, "S",  "Azufre",       "1s2 2s2 2p6 3s2 3p4",                               "1s² 2s² 2p⁶ 3s² 3p⁴",                               false),
        new ElementData(17, "Cl", "Cloro",        "1s2 2s2 2p6 3s2 3p5",                               "1s² 2s² 2p⁶ 3s² 3p⁵",                               false),
        new ElementData(18, "Ar", "Argón",        "1s2 2s2 2p6 3s2 3p6",                               "1s² 2s² 2p⁶ 3s² 3p⁶",                               false),
        new ElementData(19, "K",  "Potasio",      "1s2 2s2 2p6 3s2 3p6 4s1",                           "1s² 2s² 2p⁶ 3s² 3p⁶ 4s¹",                           false),
        new ElementData(20, "Ca", "Calcio",       "1s2 2s2 2p6 3s2 3p6 4s2",                           "1s² 2s² 2p⁶ 3s² 3p⁶ 4s²",                           false),
        new ElementData(21, "Sc", "Escandio",     "1s2 2s2 2p6 3s2 3p6 3d1 4s2",                       "1s² 2s² 2p⁶ 3s² 3p⁶ 3d¹ 4s²",                       false),
        new ElementData(22, "Ti", "Titanio",      "1s2 2s2 2p6 3s2 3p6 3d2 4s2",                       "1s² 2s² 2p⁶ 3s² 3p⁶ 3d² 4s²",                       false),
        new ElementData(23, "V",  "Vanadio",      "1s2 2s2 2p6 3s2 3p6 3d3 4s2",                       "1s² 2s² 2p⁶ 3s² 3p⁶ 3d³ 4s²",                       false),
        new ElementData(24, "Cr", "Cromo",        "1s2 2s2 2p6 3s2 3p6 3d5 4s1",                       "1s² 2s² 2p⁶ 3s² 3p⁶ 3d⁵ 4s¹",                       true),
        new ElementData(25, "Mn", "Manganeso",    "1s2 2s2 2p6 3s2 3p6 3d5 4s2",                       "1s² 2s² 2p⁶ 3s² 3p⁶ 3d⁵ 4s²",                       false),
        new ElementData(26, "Fe", "Hierro",       "1s2 2s2 2p6 3s2 3p6 3d6 4s2",                       "1s² 2s² 2p⁶ 3s² 3p⁶ 3d⁶ 4s²",                       false),
        new ElementData(27, "Co", "Cobalto",      "1s2 2s2 2p6 3s2 3p6 3d7 4s2",                       "1s² 2s² 2p⁶ 3s² 3p⁶ 3d⁷ 4s²",                       false),
        new ElementData(28, "Ni", "Níquel",       "1s2 2s2 2p6 3s2 3p6 3d8 4s2",                       "1s² 2s² 2p⁶ 3s² 3p⁶ 3d⁸ 4s²",                       false),
        new ElementData(29, "Cu", "Cobre",        "1s2 2s2 2p6 3s2 3p6 3d10 4s1",                      "1s² 2s² 2p⁶ 3s² 3p⁶ 3d¹⁰ 4s¹",                      true),
        new ElementData(30, "Zn", "Zinc",         "1s2 2s2 2p6 3s2 3p6 3d10 4s2",                      "1s² 2s² 2p⁶ 3s² 3p⁶ 3d¹⁰ 4s²",                      false),
        new ElementData(31, "Ga", "Galio",        "1s2 2s2 2p6 3s2 3p6 3d10 4s2 4p1",                  "1s² 2s² 2p⁶ 3s² 3p⁶ 3d¹⁰ 4s² 4p¹",                  false),
        new ElementData(32, "Ge", "Germanio",     "1s2 2s2 2p6 3s2 3p6 3d10 4s2 4p2",                  "1s² 2s² 2p⁶ 3s² 3p⁶ 3d¹⁰ 4s² 4p²",                  false),
        new ElementData(33, "As", "Arsénico",     "1s2 2s2 2p6 3s2 3p6 3d10 4s2 4p3",                  "1s² 2s² 2p⁶ 3s² 3p⁶ 3d¹⁰ 4s² 4p³",                  false),
        new ElementData(34, "Se", "Selenio",      "1s2 2s2 2p6 3s2 3p6 3d10 4s2 4p4",                  "1s² 2s² 2p⁶ 3s² 3p⁶ 3d¹⁰ 4s² 4p⁴",                  false),
        new ElementData(35, "Br", "Bromo",        "1s2 2s2 2p6 3s2 3p6 3d10 4s2 4p5",                  "1s² 2s² 2p⁶ 3s² 3p⁶ 3d¹⁰ 4s² 4p⁵",                  false),
        new ElementData(36, "Kr", "Kriptón",      "1s2 2s2 2p6 3s2 3p6 3d10 4s2 4p6",                  "1s² 2s² 2p⁶ 3s² 3p⁶ 3d¹⁰ 4s² 4p⁶",                  false)
    );

    // =========================================================================
    // Escenarios de ISOTOPE_MASS_CALCULATION
    // Todos los porcentajes suman 100 % y el resultado está redondeado a 2 decimales.
    // Fórmula: M_at = Σ (m_i × A_i) / 100
    // =========================================================================

    private record Isotope(int massNumber, double mass, double abundance) {}

    private record IsotopeScenario(
        String elementName, String elementSymbol, int atomicNumber,
        List<Isotope> isotopes,
        double avgMass  // pre-calculado y verificado
    ) {}

    private static final List<IsotopeScenario> ISOTOPE_SCENARIOS = List.of(

        // Litio: 6,02×7,50 + 7,02×92,50 = 45,15 + 649,35 = 694,50 → 6,95 u
        new IsotopeScenario("Litio", "Li", 3,
            List.of(new Isotope(6, 6.02, 7.50), new Isotope(7, 7.02, 92.50)),
            6.95),

        // Boro: 10,01×20,00 + 11,01×80,00 = 200,20 + 880,80 = 1081,00 → 10,81 u
        new IsotopeScenario("Boro", "B", 5,
            List.of(new Isotope(10, 10.01, 20.00), new Isotope(11, 11.01, 80.00)),
            10.81),

        // Carbono: 12,00×98,90 + 13,00×1,10 = 1186,80 + 14,30 = 1201,10 → 12,01 u
        new IsotopeScenario("Carbono", "C", 6,
            List.of(new Isotope(12, 12.00, 98.90), new Isotope(13, 13.00, 1.10)),
            12.01),

        // Cloro: 35,00×76,00 + 37,00×24,00 = 2660,00 + 888,00 = 3548,00 → 35,48 u
        new IsotopeScenario("Cloro", "Cl", 17,
            List.of(new Isotope(35, 35.00, 76.00), new Isotope(37, 37.00, 24.00)),
            35.48),

        // Bromo: 79,00×50,00 + 81,00×50,00 = 3950,00 + 4050,00 = 8000,00 → 80,00 u
        new IsotopeScenario("Bromo", "Br", 35,
            List.of(new Isotope(79, 79.00, 50.00), new Isotope(81, 81.00, 50.00)),
            80.00),

        // Potasio: 39,00×94,00 + 41,00×6,00 = 3666,00 + 246,00 = 3912,00 → 39,12 u
        new IsotopeScenario("Potasio", "K", 19,
            List.of(new Isotope(39, 39.00, 94.00), new Isotope(41, 41.00, 6.00)),
            39.12),

        // Cobre: 63,00×70,00 + 65,00×30,00 = 4410,00 + 1950,00 = 6360,00 → 63,60 u
        new IsotopeScenario("Cobre", "Cu", 29,
            List.of(new Isotope(63, 63.00, 70.00), new Isotope(65, 65.00, 30.00)),
            63.60),

        // Magnesio (3 isótopos): 24,00×79,00 + 25,00×10,00 + 26,00×11,00
        //   = 1896,00 + 250,00 + 286,00 = 2432,00 → 24,32 u
        new IsotopeScenario("Magnesio", "Mg", 12,
            List.of(new Isotope(24, 24.00, 79.00), new Isotope(25, 25.00, 10.00),
                    new Isotope(26, 26.00, 11.00)),
            24.32),

        // Silicio (3 isótopos): 28,00×90,00 + 29,00×5,00 + 30,00×5,00
        //   = 2520,00 + 145,00 + 150,00 = 2815,00 → 28,15 u
        new IsotopeScenario("Silicio", "Si", 14,
            List.of(new Isotope(28, 28.00, 90.00), new Isotope(29, 29.00, 5.00),
                    new Isotope(30, 30.00, 5.00)),
            28.15),

        // Zinc (3 isótopos): 64,00×50,00 + 66,00×28,00 + 68,00×22,00
        //   = 3200,00 + 1848,00 + 1496,00 = 6544,00 → 65,44 u
        new IsotopeScenario("Zinc", "Zn", 30,
            List.of(new Isotope(64, 64.00, 50.00), new Isotope(66, 66.00, 28.00),
                    new Isotope(68, 68.00, 22.00)),
            65.44)
    );

    // =========================================================================
    // Escenarios de CHEMICAL_BOND_PROPERTIES (tipo test)
    // =========================================================================

    private record McQuestion(
        String statement,
        String opt0, String opt1, String opt2, String opt3,
        int correct, String explanation
    ) {}

    private static final List<McQuestion> BOND_MC = List.of(

        new McQuestion(
            "Una sustancia tiene las siguientes propiedades: punto de fusión 801 °C, " +
            "es sólida a temperatura ambiente, se disuelve en agua y la solución conduce " +
            "la electricidad. ¿Qué tipo de enlace presenta? (Se trata del NaCl.)",
            "Covalente molecular",
            "Iónico",
            "Red covalente",
            "Metálico",
            1,
            "El <strong>NaCl</strong> es un compuesto <strong>iónico</strong>. Sus propiedades " +
            "características son:\n\n<ul>" +
            "<li><strong>Alto punto de fusión</strong> (801 °C): la red cristalina iónica " +
            "requiere mucha energía para romperse.</li>" +
            "<li><strong>Conducción eléctrica en disolución o fundido</strong>: los iones " +
            "Na⁺ y Cl⁻ quedan libres y actúan como portadores de carga.</li>" +
            "<li><strong>Solubilidad en agua</strong>: el agua polar estabiliza los iones " +
            "mediante interacciones ion-dipolo.</li></ul>"
        ),

        new McQuestion(
            "El dióxido de carbono (CO₂) es un gas a temperatura ambiente, no conduce la " +
            "electricidad y tiene un punto de ebullición de −78 °C. ¿Qué tipo de enlace " +
            "explica estas propiedades?",
            "Metálico",
            "Red covalente",
            "Iónico",
            "Covalente molecular",
            3,
            "El CO₂ es un compuesto <strong>covalente molecular</strong>. Sus características:\n\n<ul>" +
            "<li><strong>Punto de ebullición muy bajo</strong> (−78 °C): las fuerzas " +
            "intermoleculares (van der Waals) que mantienen unidas las moléculas son débiles.</li>" +
            "<li><strong>No conduce la electricidad</strong>: no existen iones ni electrones " +
            "libres; los electrones están localizados en los enlaces covalentes C=O.</li>" +
            "<li>Gas a temperatura ambiente: las moléculas tienen suficiente energía térmica " +
            "para superar las débiles fuerzas intermoleculares.</li></ul>"
        ),

        new McQuestion(
            "El diamante tiene un punto de fusión de aproximadamente 3550 °C, es " +
            "extremadamente duro, no conduce la electricidad y no se disuelve en ningún " +
            "disolvente habitual. ¿Qué tipo de enlace lo caracteriza?",
            "Iónico",
            "Covalente molecular",
            "Red covalente",
            "Metálico",
            2,
            "El diamante es una <strong>red covalente</strong> (o sólido covalente de red). " +
            "Cada átomo de carbono forma <strong>4 enlaces covalentes σ</strong> con sus " +
            "vecinos en una estructura tetraédrica tridimensional continua. Sus propiedades:\n\n<ul>" +
            "<li><strong>Altísimo punto de fusión</strong>: romper la red exige romper " +
            "millones de enlaces covalentes C–C simultáneamente.</li>" +
            "<li><strong>Gran dureza</strong>: la red tridimensional rígida resiste la deformación.</li>" +
            "<li><strong>No conduce la electricidad</strong>: todos los electrones están " +
            "en enlaces localizados (no hay electrones libres ni iones).</li></ul>"
        ),

        new McQuestion(
            "El cobre (Cu) conduce muy bien la electricidad y el calor, es dúctil y " +
            "maleable, tiene brillo metálico y punto de fusión de 1085 °C. " +
            "¿Qué tipo de enlace explica sus propiedades?",
            "Covalente molecular",
            "Red covalente",
            "Iónico",
            "Metálico",
            3,
            "El cobre tiene <strong>enlace metálico</strong>. El modelo del «mar de " +
            "electrones» explica sus propiedades:\n\n<ul>" +
            "<li><strong>Excelente conductor eléctrico y térmico</strong>: los electrones " +
            "de valencia quedan deslocalizados y se mueven libremente por toda la red.</li>" +
            "<li><strong>Ductilidad y maleabilidad</strong>: los planos de iones " +
            "pueden deslizarse entre sí sin romper el enlace metálico.</li>" +
            "<li><strong>Brillo metálico</strong>: los electrones libres absorben y " +
            "reemiten fotones de luz visible.</li></ul>"
        ),

        new McQuestion(
            "¿Qué tipo de compuesto tiene generalmente los puntos de fusión MÁS BAJOS, " +
            "presentando sustancias que son gases o líquidos a temperatura ambiente?",
            "Compuestos iónicos",
            "Metales",
            "Compuestos covalentes moleculares",
            "Redes covalentes",
            2,
            "Los <strong>compuestos covalentes moleculares</strong> tienen los puntos de " +
            "fusión más bajos porque las únicas interacciones entre moléculas son fuerzas " +
            "de <strong>van der Waals</strong> (dispersión de London, dipolo-dipolo), " +
            "que son mucho más débiles que los enlaces iónicos, covalentes o metálicos.\n\n" +
            "Ejemplos: H₂ (−259 °C), O₂ (−219 °C), CO₂ (−78 °C), H₂O (0 °C), " +
            "benceno (5,5 °C). Son los únicos que existen como gases o líquidos a 25 °C " +
            "de forma generalizada."
        ),

        new McQuestion(
            "¿Qué tipo de material conduce la electricidad en estado SÓLIDO, sin necesidad " +
            "de disolverse ni fundirse previamente?",
            "Compuestos iónicos",
            "Compuestos covalentes moleculares",
            "Redes covalentes",
            "Metales",
            3,
            "Solo los <strong>metales</strong> conducen la electricidad en estado sólido. " +
            "Sus electrones de valencia están deslocalizados formando un «mar de electrones» " +
            "que se mueve libremente bajo la aplicación de un campo eléctrico.\n\n" +
            "Los compuestos <em>iónicos</em> solo conducen cuando están fundidos o disueltos " +
            "(los iones quedan libres); en sólido, los iones están fijos en la red cristalina " +
            "y no pueden moverse."
        ),

        new McQuestion(
            "El bromuro de potasio (KBr) tiene un punto de fusión de 734 °C, se disuelve " +
            "en agua generando iones K⁺ y Br⁻ que conducen la electricidad, y en estado " +
            "sólido no conduce. ¿Qué tipo de enlace tiene?",
            "Iónico",
            "Covalente molecular",
            "Metálico",
            "Red covalente",
            0,
            "El KBr tiene <strong>enlace iónico</strong>: se forma por la transferencia de " +
            "un electrón del K (metal alcalino, baja energía de ionización) al Br " +
            "(halógeno, alta afinidad electrónica), originando K⁺ y Br⁻ que se organizan " +
            "en una red cristalina cúbica.\n\n" +
            "La clave diagnóstica: <em>conduce solo al disolver o fundir</em> (los iones " +
            "quedan libres), pero <em>no en sólido</em> (los iones están fijos en la red)."
        ),

        new McQuestion(
            "El cuarzo (SiO₂) tiene un punto de fusión de 1713 °C, es muy duro, no " +
            "conduce la electricidad y no se disuelve en agua. ¿Qué tipo de enlace presenta?",
            "Iónico",
            "Metálico",
            "Covalente molecular",
            "Red covalente",
            3,
            "El SiO₂ es una <strong>red covalente</strong>: cada Si forma 4 enlaces " +
            "covalentes con átomos de O y cada O forma 2 enlaces con Si, generando una " +
            "estructura tridimensional continua análoga al diamante.\n\n<ul>" +
            "<li><strong>Alto punto de fusión</strong> (1713 °C): hay que romper la red " +
            "completa de enlaces Si–O.</li>" +
            "<li><strong>No conduce</strong>: no hay electrones deslocalizados ni iones.</li>" +
            "<li><strong>No se disuelve en agua</strong>: los enlaces Si–O son muy fuertes " +
            "y la red resiste la hidrólisis en condiciones normales.</li></ul>"
        ),

        new McQuestion(
            "¿Qué tipo de compuesto, al disolverse en agua, genera iones que hacen que la " +
            "solución conduzca la electricidad (electrolito)?",
            "Covalente molecular apolar",
            "Metálico",
            "Iónico",
            "Red covalente",
            2,
            "Los compuestos <strong>iónicos</strong> son electrolitos: al disolverse en " +
            "agua, la red cristalina se disgrega y los iones (catión y anión) quedan " +
            "rodeados de moléculas de agua (hidratación iónica), libres para moverse y " +
            "transportar carga eléctrica.\n\nEjemplos: NaCl → Na⁺(aq) + Cl⁻(aq); " +
            "KBr → K⁺(aq) + Br⁻(aq); MgCl₂ → Mg²⁺(aq) + 2 Cl⁻(aq).\n\n" +
            "Los compuestos covalentes apolares (hexano, benceno) no se disuelven bien " +
            "en agua y, aunque algunos covalentes polares sí se disuelven, no generan iones."
        ),

        new McQuestion(
            "El hierro (Fe) es opaco, tiene brillo metálico, conduce bien el calor y la " +
            "electricidad, es maleable, y tiene punto de fusión 1538 °C. " +
            "¿Qué tipo de enlace explica todas estas propiedades conjuntamente?",
            "Iónico",
            "Red covalente",
            "Covalente molecular",
            "Metálico",
            3,
            "El hierro tiene <strong>enlace metálico</strong>. El conjunto de sus " +
            "propiedades solo se explica con el modelo del mar de electrones:\n\n<ul>" +
            "<li><strong>Conductividad eléctrica y térmica</strong>: electrones libres " +
            "deslocalizados actúan como portadores de carga y calor.</li>" +
            "<li><strong>Maleabilidad</strong>: los iones positivos (Fe²⁺ o Fe³⁺) pueden " +
            "deslizarse manteniendo el enlace metálico.</li>" +
            "<li><strong>Opacidad y brillo</strong>: los electrones libres interaccionan " +
            "con la luz visible, absorbiéndola y reemitiéndola.</li>" +
            "<li><strong>Punto de fusión elevado</strong> (1538 °C): el enlace metálico " +
            "en el Fe es fuerte por el alto número de electrones de valencia disponibles.</li></ul>"
        )
    );

    // =========================================================================
    // API pública
    // =========================================================================

    public FourthEsoMatterExercise generateAndSave() {
        FourthEsoMatterExercise ex = new FourthEsoMatterExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);

        // Distribución: 33 % ELECTRONIC_CONFIGURATION, 34 % ISOTOPE_MASS, 33 % BOND_PROPERTIES
        int roll = random.nextInt(9);
        if (roll < 3) {
            buildElectronicConfiguration(ex);
        } else if (roll < 6) {
            buildIsotopeMass(ex);
        } else {
            buildChemicalBond(ex);
        }

        log.debug("4ESO BL2 generado: type={} mode={}", ex.getMatterType(), ex.getExerciseMode());
        return repository.save(ex);
    }

    public FourthEsoMatterExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ejercicio 4ESO BL2 no encontrado: " + id));
    }

    // =========================================================================
    // Constructor — ELECTRONIC_CONFIGURATION
    // =========================================================================

    private void buildElectronicConfiguration(FourthEsoMatterExercise ex) {
        ex.setMatterType(FourthEsoMatterType.ELECTRONIC_CONFIGURATION);
        ex.setExerciseMode("TEXT");
        ex.setUnknownVariable("config_electronica");

        ElementData el = ELEMENTS.get(random.nextInt(ELEMENTS.size()));

        StringBuilder stmt = new StringBuilder();
        stmt.append("Escribe la configuración electrónica completa del ")
            .append(el.nameEs()).append(" (").append(el.symbol())
            .append(", Z = ").append(el.z()).append(").");
        if (el.isException()) {
            stmt.append(" Pista: es un caso excepcional por estabilidad del subnivel 3d.");
        }
        stmt.append(" Usa la notación estándar (p.ej. 1s2 2s2 2p6).");

        ex.setStatement(stmt.toString());
        ex.setCorrectConfigNormalized(el.configNormalized());
        ex.setCorrectConfigDisplay(el.configDisplay());
        ex.setExplanation(buildElectronicConfigExplanation(el));
    }

    private String buildElectronicConfigExplanation(ElementData el) {
        StringBuilder sb = new StringBuilder();
        sb.append("Seguimos el <strong>principio de Aufbau</strong> y el ")
          .append("<strong>diagrama de Möller</strong> para el <strong>")
          .append(el.nameEs()).append("</strong> (símbolo ").append(el.symbol())
          .append(", Z = ").append(el.z()).append(").\n\n");

        sb.append("Orden de llenado de subshells: ")
          .append("1s → 2s → 2p → 3s → 3p → <strong>4s → 3d</strong> → 4p\n\n");

        if (el.isException()) {
            if (el.z() == 24) {
                sb.append("<em>Excepción (Cr):</em> la configuración esperada por Aufbau ")
                  .append("sería […] 3d⁴ 4s², pero el semillenado del subnivel 3d ")
                  .append("(3d⁵) es más estable (Regla de Hund extendida). ")
                  .append("Un electrón migra del 4s al 3d → configuración real: ")
                  .append("<strong>3d⁵ 4s¹</strong>.\n\n");
            } else {
                sb.append("<em>Excepción (Cu):</em> la configuración esperada sería ")
                  .append("[…] 3d⁹ 4s², pero el llenado completo del subnivel 3d ")
                  .append("(3d¹⁰) es energéticamente más estable. ")
                  .append("Un electrón migra del 4s al 3d → configuración real: ")
                  .append("<strong>3d¹⁰ 4s¹</strong>.\n\n");
            }
        }

        sb.append("<strong>Distribución de los ").append(el.z())
          .append(" electrones:</strong>\n\n");

        String[] tokens = el.configNormalized().split(" ");
        int placed = 0;
        for (String token : tokens) {
            int sep = lastSubshellLetterPos(token);
            int count = Integer.parseInt(token.substring(sep + 1));
            placed += count;
            int remaining = el.z() - placed;
            sb.append("• <strong>").append(token).append("</strong> → ").append(count)
              .append(count == 1 ? " e⁻" : " e⁻");
            sb.append(" (").append(placed).append(" colocados");
            if (remaining > 0) sb.append(", ").append(remaining).append(" restantes");
            sb.append(")\n");
        }

        sb.append("\nConfiguración electrónica completa:\n\n")
          .append("\\[").append(configToKatex(el.configNormalized())).append("\\]");

        return sb.toString();
    }

    // =========================================================================
    // Constructor — ISOTOPE_MASS_CALCULATION
    // =========================================================================

    private void buildIsotopeMass(FourthEsoMatterExercise ex) {
        ex.setMatterType(FourthEsoMatterType.ISOTOPE_MASS_CALCULATION);
        ex.setExerciseMode("NUMERICAL");
        ex.setUnknownVariable("masa_atomica_media");
        ex.setAnswerUnit("u");
        ex.setTolerancePercent(2.0);

        IsotopeScenario sc = ISOTOPE_SCENARIOS.get(random.nextInt(ISOTOPE_SCENARIOS.size()));

        // Construir enunciado (plain text, sin HTML)
        StringBuilder stmt = new StringBuilder();
        stmt.append("El ").append(sc.elementName()).append(" (")
            .append(sc.elementSymbol()).append(", Z = ").append(sc.atomicNumber())
            .append(") tiene ")
            .append(sc.isotopes().size() == 2 ? "dos" : "tres")
            .append(" isótopos naturales: ");
        for (int i = 0; i < sc.isotopes().size(); i++) {
            if (i > 0) stmt.append(i == sc.isotopes().size() - 1 ? " y " : ", ");
            Isotope iso = sc.isotopes().get(i);
            stmt.append(iso.massNumber()).append(sc.elementSymbol())
                .append(" (").append(fmtPct(iso.abundance())).append(" %, ")
                .append(fmtMass(iso.mass())).append(" u)");
        }
        stmt.append(". Calcula la masa atómica media ponderada.");

        ex.setStatement(stmt.toString());
        ex.setCorrectAnswerValue(sc.avgMass());
        ex.setCorrectAnswerDisplay(fmtMass(sc.avgMass()) + " u");
        ex.setExplanation(buildIsotopeExplanation(sc));
    }

    private String buildIsotopeExplanation(IsotopeScenario sc) {
        int n = sc.isotopes().size();
        StringBuilder sb = new StringBuilder();

        sb.append("La <strong>masa atómica media ponderada</strong> se calcula sumando la ")
          .append("contribución de cada isótopo ponderada por su abundancia:\n\n");

        // Fórmula general en KaTeX
        sb.append("\\[M_{at} = \\sum_i \\frac{m_i \\times A_i\\,(\\%)}{100}\\]\n\n");

        sb.append("Sustituyendo los datos del <strong>").append(sc.elementName())
          .append("</strong>:\n\n");

        // Línea con los términos
        sb.append("\\[M_{at} = \\frac{");
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(" + ");
            Isotope iso = sc.isotopes().get(i);
            sb.append(fmtKatex(iso.mass())).append(" \\times ").append(fmtKatex(iso.abundance()));
        }
        sb.append("}{100}\\]\n\n");

        // Línea con los productos calculados y la suma
        sb.append("\\[= \\frac{");
        double numerator = 0.0;
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(" + ");
            Isotope iso = sc.isotopes().get(i);
            double product = iso.mass() * iso.abundance();
            numerator += product;
            sb.append(fmtKatex(product));
        }
        sb.append("}{100}\\]\n\n");

        // División final
        sb.append("\\[= \\frac{").append(fmtKatex(numerator))
          .append("}{100} = ").append(fmtKatex(sc.avgMass())).append("\\,\\text{u}\\]\n\n");

        sb.append("∴  Masa atómica media del ").append(sc.elementName())
          .append(": <strong>").append(fmtMass(sc.avgMass())).append(" u</strong>");

        return sb.toString();
    }

    // =========================================================================
    // Constructor — CHEMICAL_BOND_PROPERTIES
    // =========================================================================

    private void buildChemicalBond(FourthEsoMatterExercise ex) {
        ex.setMatterType(FourthEsoMatterType.CHEMICAL_BOND_PROPERTIES);
        ex.setExerciseMode("MULTIPLE_CHOICE");

        McQuestion q = BOND_MC.get(random.nextInt(BOND_MC.size()));
        ex.setStatement(q.statement());
        ex.setOption0(q.opt0());
        ex.setOption1(q.opt1());
        ex.setOption2(q.opt2());
        ex.setOption3(q.opt3());
        ex.setCorrectIndex(q.correct());
        ex.setExplanation(q.explanation());
    }

    // =========================================================================
    // Utilidades de formato y conversión de configuraciones
    // =========================================================================

    /** Posición del último carácter de subshell (s/p/d/f) en un token como "3d10". */
    private int lastSubshellLetterPos(String token) {
        for (int i = token.length() - 1; i >= 0; i--) {
            char c = token.charAt(i);
            if (c == 's' || c == 'p' || c == 'd' || c == 'f') return i;
        }
        return -1;
    }

    /** "1s2 2s2 2p6 3d10" → "1s^2\\,2s^2\\,2p^6\\,3d^{10}" para KaTeX. */
    private String configToKatex(String normalized) {
        StringBuilder sb = new StringBuilder();
        String[] tokens = normalized.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) sb.append("\\,");
            String t = tokens[i];
            int sep = lastSubshellLetterPos(t);
            String orb = t.substring(0, sep + 1);
            String cnt = t.substring(sep + 1);
            sb.append(orb).append(cnt.length() > 1 ? "^{" + cnt + "}" : "^" + cnt);
        }
        return sb.toString();
    }

    /** Formatea un double con 2 decimales usando coma española para mostrar al alumno. */
    private String fmtMass(double value) {
        return String.format("%.2f", value).replace(".", ",");
    }

    /** Formatea un porcentaje: sin decimales si es entero, con 2 si no lo es. */
    private String fmtPct(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.format("%.2f", value).replace(".", ",");
    }

    /** Formatea un double con 2 decimales usando {,} para KaTeX. */
    private String fmtKatex(double value) {
        return String.format("%.2f", value).replace(".", "{,}");
    }
}
