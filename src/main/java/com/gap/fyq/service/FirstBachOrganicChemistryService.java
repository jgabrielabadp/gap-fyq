package com.gap.fyq.service;

import com.gap.fyq.model.firstbach.organicchemistry.FirstBachOrganicChemistryExercise;
import com.gap.fyq.model.firstbach.organicchemistry.OrganicChemistryType;
import com.gap.fyq.repository.FirstBachOrganicChemistryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirstBachOrganicChemistryService {

    private final FirstBachOrganicChemistryRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "1BACH";
    private static final String BLOCK  = "BL5";

    // =========================================================================
    // DATA RECORDS
    // =========================================================================

    /** Pregunta genérica de opción múltiple para los tres tipos de ejercicio. */
    private record MCQuestion(
        String statement,
        String opt0, String opt1, String opt2, String opt3,
        int correct,
        String explanation
    ) {}

    // =========================================================================
    // ORGANIC_NOMENCLATURE — 12 preguntas
    // 6 tipo "fórmula → nombre IUPAC" y 6 tipo "nombre IUPAC → fórmula"
    // =========================================================================

    private static final List<MCQuestion> NOMENCLATURE = List.of(

        // --- Fórmula → Nombre ---

        // N1: CH₃-CH₂-CH₂-CH₃ = butano
        new MCQuestion(
            "¿Cuál es el nombre IUPAC del compuesto de fórmula CH₃-CH₂-CH₂-CH₃?",
            "propano", "butano", "pentano", "etano", 1,
            "<strong>Análisis:</strong>\n\n" +
            "<ul><li><strong>Cadena principal:</strong> 4 carbonos → prefijo <strong>but-</strong></li>" +
            "<li><strong>Insaturaciones:</strong> ninguna (solo enlaces simples) → sufijo <strong>-ano</strong></li>" +
            "<li>No hay grupos funcionales oxigenados ni nitrogenados.</li></ul>\n\n" +
            "∴ CH₃-CH₂-CH₂-CH₃ = <strong>butano</strong>\n\n" +
            "Prefijos de longitud de cadena: met-(1) · et-(2) · prop-(3) · <strong>but-(4)</strong> · pent-(5) · hex-(6)"),

        // N2: CH₃-CH₂-CH₂-OH = propan-1-ol
        new MCQuestion(
            "¿Cuál es el nombre IUPAC del compuesto de fórmula CH₃-CH₂-CH₂-OH?",
            "etanol", "butan-1-ol", "propan-1-ol", "propan-2-ol", 2,
            "<strong>Análisis:</strong>\n\n" +
            "<ul><li><strong>Cadena principal:</strong> 3 carbonos → prefijo <strong>prop-</strong></li>" +
            "<li><strong>Grupo funcional:</strong> -OH (hidroxilo) → sufijo <strong>-ol</strong></li>" +
            "<li><strong>Localización:</strong> el -OH está en el C1 (extremo de la cadena) → localiz. <strong>1</strong></li></ul>\n\n" +
            "Numeramos la cadena desde el extremo más próximo al -OH:\n\n" +
            "C1(OH)-C2-C3 → localizador 1\n\n" +
            "∴ CH₃-CH₂-CH₂-OH = <strong>propan-1-ol</strong>"),

        // N3: CH₃-CO-CH₃ = propan-2-ona
        new MCQuestion(
            "¿Cuál es el nombre IUPAC del compuesto de fórmula CH₃-CO-CH₃?",
            "propanal", "propan-1-ol", "propan-2-ona", "ácido propanoico", 2,
            "<strong>Análisis:</strong>\n\n" +
            "<ul><li><strong>Cadena principal:</strong> 3 carbonos → prefijo <strong>prop-</strong></li>" +
            "<li><strong>Grupo funcional:</strong> C=O interno (carbonilo flanqueado por 2 C) → cetona, sufijo <strong>-ona</strong></li>" +
            "<li><strong>Localización:</strong> el C=O está en el C2 → localiz. <strong>2</strong></li></ul>\n\n" +
            "Distinción clave:\n\n" +
            "<ul><li>Si el C=O está en el extremo → <strong>aldehído</strong> (-al)</li>" +
            "<li>Si el C=O está en el interior → <strong>cetona</strong> (-ona)</li></ul>\n\n" +
            "∴ CH₃-CO-CH₃ = <strong>propan-2-ona</strong> (acetona)"),

        // N4: CH₃-CH₂-CHO = propanal
        new MCQuestion(
            "¿Cuál es el nombre IUPAC del compuesto de fórmula CH₃-CH₂-CHO?",
            "propan-1-ol", "propanona", "propanal", "butanal", 2,
            "<strong>Análisis:</strong>\n\n" +
            "<ul><li><strong>Cadena principal:</strong> 3 carbonos → prefijo <strong>prop-</strong></li>" +
            "<li><strong>Grupo funcional:</strong> -CHO (formilo en el extremo) → aldehído, sufijo <strong>-al</strong></li>" +
            "<li>El grupo aldehído siempre está en el C1; no se indica localizador.</li></ul>\n\n" +
            "∴ CH₃-CH₂-CHO = <strong>propanal</strong>\n\n" +
            "<em>Nota: el propanal y la propan-2-ona (CH₃-CO-CH₃) son isómeros de función " +
            "con la misma fórmula molecular C₃H₆O.</em>"),

        // N5: CH₃-COOH = ácido etanoico
        new MCQuestion(
            "¿Cuál es el nombre IUPAC del compuesto de fórmula CH₃-COOH?",
            "ácido metanoico", "ácido propanoico", "ácido etanoico", "aldehído etanoico", 2,
            "<strong>Análisis:</strong>\n\n" +
            "<ul><li><strong>Cadena principal:</strong> 2 carbonos (incluido el -COOH) → prefijo <strong>et-</strong></li>" +
            "<li><strong>Grupo funcional:</strong> -COOH (carboxilo) → ácido carboxílico → " +
            "nomenclatura: <strong>ácido [prefijo]anoico</strong></li>" +
            "<li>El C del -COOH siempre es el C1; no se indica localizador.</li></ul>\n\n" +
            "∴ CH₃-COOH = <strong>ácido etanoico</strong> (ácido acético)\n\n" +
            "Serie: HCOOH (met-) → CH₃COOH (et-) → CH₃CH₂COOH (prop-) → …"),

        // N6: CH₂=CH₂ = eteno
        new MCQuestion(
            "¿Cuál es el nombre IUPAC del compuesto de fórmula CH₂=CH₂?",
            "etano", "etino", "propeno", "eteno", 3,
            "<strong>Análisis:</strong>\n\n" +
            "<ul><li><strong>Cadena principal:</strong> 2 carbonos → prefijo <strong>et-</strong></li>" +
            "<li><strong>Insaturación:</strong> 1 doble enlace C=C → sufijo <strong>-eno</strong></li>" +
            "<li>Con solo 2 carbonos, el doble enlace siempre está en el C1; no se indica localiz.</li></ul>\n\n" +
            "∴ CH₂=CH₂ = <strong>eteno</strong>\n\n" +
            "Sufijos de insaturación: -ano (ninguna) · -eno (1 doble) · -ino (1 triple)"),

        // --- Nombre → Fórmula ---

        // N7: propan-2-ol
        new MCQuestion(
            "¿Qué fórmula semidesarrollada corresponde al propan-2-ol?",
            "CH₃-CH₂-CH₂-OH", "CH₃-CH(OH)-CH₃",
            "CH₃-CH₂-CH₂-CH₂-OH", "CH₃-OH", 1,
            "<strong>Decodificación del nombre propan-2-ol:</strong>\n\n" +
            "<ul><li><strong>prop-</strong>: cadena de 3 carbonos</li>" +
            "<li><strong>-an-</strong>: cadena saturada (sin insaturaciones)</li>" +
            "<li><strong>2-ol</strong>: grupo -OH en el carbono nº 2</li></ul>\n\n" +
            "Construimos: C1-C2(OH)-C3\n\n" +
            "∴ CH₃-<strong>CH(OH)</strong>-CH₃ = <strong>propan-2-ol</strong>\n\n" +
            "<em>Compara con propan-1-ol: CH₃-CH₂-CH₂-OH, donde el -OH está en el C1.</em>"),

        // N8: etanal (acetaldehído)
        new MCQuestion(
            "¿Qué fórmula semidesarrollada corresponde al etanal?",
            "CH₃-CH₂-OH", "CH₃-CO-CH₃", "CH₃-CHO", "CH₃-CH₂-CHO", 2,
            "<strong>Decodificación del nombre etanal:</strong>\n\n" +
            "<ul><li><strong>et-</strong>: cadena de 2 carbonos</li>" +
            "<li><strong>-an-</strong>: cadena saturada</li>" +
            "<li><strong>-al</strong>: grupo aldehído -CHO en el C1</li></ul>\n\n" +
            "Cadena: -CHO | C2 = CH₃\n\n" +
            "∴ CH₃-CHO = <strong>etanal</strong> (acetaldehído)\n\n" +
            "No confundir con etanol (CH₃-CH₂-OH): la diferencia es -CHO frente a -CH₂OH."),

        // N9: ácido propanoico
        new MCQuestion(
            "¿Qué fórmula corresponde al ácido propanoico?",
            "CH₃-COOH", "CH₃-CH₂-COOH", "HCOOH", "CH₃-CH₂-CH₂-COOH", 1,
            "<strong>Decodificación del nombre ácido propanoico:</strong>\n\n" +
            "<ul><li><strong>prop-</strong>: cadena de 3 carbonos (incluido el -COOH)</li>" +
            "<li><strong>-anoico</strong>: ácido carboxílico saturado</li></ul>\n\n" +
            "C1(=COOH) - C2 - C3 → CH₃-CH₂-COOH\n\n" +
            "∴ <strong>CH₃-CH₂-COOH</strong> = ácido propanoico\n\n" +
            "CH₃-COOH tiene solo 2 C → ácido etanoico; HCOOH tiene 1 C → ácido metanoico."),

        // N10: but-1-eno
        new MCQuestion(
            "¿Qué fórmula semidesarrollada corresponde al but-1-eno?",
            "CH₃-CH₂-CH₂-CH₃", "CH₂=CH-CH₂-CH₃",
            "CH₃-CH=CH-CH₃", "CH₂=CH₂", 1,
            "<strong>Decodificación del nombre but-1-eno:</strong>\n\n" +
            "<ul><li><strong>but-</strong>: cadena de 4 carbonos</li>" +
            "<li><strong>1-eno</strong>: doble enlace C=C comenzando en el C1</li></ul>\n\n" +
            "C1=C2-C3-C4 → CH₂=CH-CH₂-CH₃\n\n" +
            "∴ <strong>CH₂=CH-CH₂-CH₃</strong> = but-1-eno\n\n" +
            "<em>Compara con but-2-eno (CH₃-CH=CH-CH₃), isómero de posición del anterior.</em>"),

        // N11: butan-2-ona
        new MCQuestion(
            "¿Qué fórmula corresponde a la butan-2-ona?",
            "CH₃-CH₂-CH₂-CHO", "CH₃-CO-CH₂-CH₃",
            "CH₃-CH₂-CO-CH₂-CH₃", "CH₃-CH₂-CH₂-CO-CH₃", 1,
            "<strong>Decodificación del nombre butan-2-ona:</strong>\n\n" +
            "<ul><li><strong>butan-</strong>: cadena de 4 carbonos</li>" +
            "<li><strong>2-ona</strong>: grupo cetona (C=O) en el carbono nº 2</li></ul>\n\n" +
            "C1-C2(=O)-C3-C4 → CH₃-CO-CH₂-CH₃\n\n" +
            "∴ <strong>CH₃-CO-CH₂-CH₃</strong> = butan-2-ona\n\n" +
            "La opción C (pentan-3-ona) tiene 5 carbonos; la D tiene el C=O en C4, que " +
            "equivale a butan-2-ona al numerar desde el otro extremo — sin embargo el nombre " +
            "correcto exige el localizador más bajo posible: 2."),

        // N12: metanal
        new MCQuestion(
            "¿Qué fórmula corresponde al metanal (formaldehído)?",
            "CH₃-CHO", "CH₃-CO-CH₃", "HCHO", "CH₃-CH₂-CHO", 2,
            "<strong>Decodificación del nombre metanal:</strong>\n\n" +
            "<ul><li><strong>met-</strong>: cadena de 1 carbono</li>" +
            "<li><strong>-anal</strong>: grupo aldehído (-CHO); con 1 C, la molécula entera es H-CHO</li></ul>\n\n" +
            "∴ <strong>HCHO</strong> = metanal (formaldehído)\n\n" +
            "Es el aldehído más simple: un único átomo de carbono que forma el grupo -CHO.")
    );

    // =========================================================================
    // STRUCTURAL_ISOMERISM — 10 pares de isómeros
    // Opciones fijas (en mismo orden) para todos los ejercicios:
    //   0 = Isomería de cadena
    //   1 = Isomería de posición
    //   2 = Isomería de función
    //   3 = Estereoisomería geométrica (cis-trans)
    // =========================================================================

    /** Par de isómeros con su tipo (0=cadena, 1=posición, 2=función). */
    private record IsomerPair(
        String compA, String formulaA,
        String compB, String formulaB,
        String molFormula,   // fórmula molecular común
        int type,            // 0,1,2 → mapea a correctIndex
        String explanation
    ) {}

    private static final List<IsomerPair> ISOMERS = List.of(

        // I1: cadena — butano vs 2-metilpropano (C₄H₁₀)
        new IsomerPair(
            "butano", "CH₃-CH₂-CH₂-CH₃",
            "2-metilpropano", "CH₃-CH(CH₃)-CH₃",
            "C₄H₁₀", 0,
            "<strong>Comprobación de fórmulas moleculares:</strong>\n\n" +
            "<ul><li>Butano: 4C + 10H → C₄H₁₀ ✓</li>" +
            "<li>2-metilpropano: 4C + 10H → C₄H₁₀ ✓</li></ul>\n\n" +
            "<strong>Análisis del tipo de isomería:</strong>\n\n" +
            "Ambos son alcanos (sin grupos funcionales). La diferencia está en el <em>esqueleto carbonado</em>:\n\n" +
            "<ul><li>Butano: cadena lineal C-C-C-C</li>" +
            "<li>2-metilpropano: cadena ramificada con un CH₃ lateral en el C2</li></ul>\n\n" +
            "Mismo grupo funcional (ninguno), distinto esqueleto → <strong>isomería de cadena</strong>."),

        // I2: cadena — pentano vs 2-metilbutano (C₅H₁₂)
        new IsomerPair(
            "pentano", "CH₃-CH₂-CH₂-CH₂-CH₃",
            "2-metilbutano", "CH₃-CH(CH₃)-CH₂-CH₃",
            "C₅H₁₂", 0,
            "<strong>Comprobación de fórmulas moleculares:</strong>\n\n" +
            "<ul><li>Pentano: 5C + 12H → C₅H₁₂ ✓</li>" +
            "<li>2-metilbutano: 5C + 12H → C₅H₁₂ ✓</li></ul>\n\n" +
            "<strong>Análisis:</strong>\n\n" +
            "Pentano tiene cadena lineal de 5 carbonos; 2-metilbutano tiene una cadena principal de 4 carbonos " +
            "con un grupo metilo (-CH₃) en el C2. Mismo tipo de función (alcano), " +
            "diferente esqueleto → <strong>isomería de cadena</strong>."),

        // I3: cadena — butan-1-ol vs 2-metilpropan-1-ol (C₄H₁₀O)
        new IsomerPair(
            "butan-1-ol", "CH₃-CH₂-CH₂-CH₂-OH",
            "2-metilpropan-1-ol", "(CH₃)₂CH-CH₂-OH",
            "C₄H₁₀O", 0,
            "<strong>Comprobación de fórmulas moleculares:</strong>\n\n" +
            "<ul><li>Butan-1-ol: 4C + 10H + O → C₄H₁₀O ✓</li>" +
            "<li>2-metilpropan-1-ol: 4C + 10H + O → C₄H₁₀O ✓</li></ul>\n\n" +
            "<strong>Análisis:</strong>\n\n" +
            "Ambos son alcoholes primarios (el -OH está en el C terminal). La diferencia:\n\n" +
            "<ul><li>Butan-1-ol: cadena lineal de 4C con -OH en el C1</li>" +
            "<li>2-metilpropan-1-ol: cadena ramificada de 3C + 1 metilo lateral con -OH en el C1</li></ul>\n\n" +
            "Misma función y posición del -OH, distinto esqueleto → <strong>isomería de cadena</strong>."),

        // I4: posición — propan-1-ol vs propan-2-ol (C₃H₈O)
        new IsomerPair(
            "propan-1-ol", "CH₃-CH₂-CH₂-OH",
            "propan-2-ol", "CH₃-CH(OH)-CH₃",
            "C₃H₈O", 1,
            "<strong>Comprobación de fórmulas moleculares:</strong>\n\n" +
            "<ul><li>Propan-1-ol: 3C + 8H + O → C₃H₈O ✓</li>" +
            "<li>Propan-2-ol: 3C + 8H + O → C₃H₈O ✓</li></ul>\n\n" +
            "<strong>Análisis:</strong>\n\n" +
            "Misma cadena principal (propano, 3C) y mismo grupo funcional (-OH, alcohol). " +
            "La única diferencia es la posición del grupo hidroxilo:\n\n" +
            "<ul><li>propan-<strong>1</strong>-ol: -OH en C1 (extremo)</li>" +
            "<li>propan-<strong>2</strong>-ol: -OH en C2 (centro)</li></ul>\n\n" +
            "Misma cadena + misma función + diferente posición del sustituyente → <strong>isomería de posición</strong>."),

        // I5: posición — butan-1-ol vs butan-2-ol (C₄H₁₀O)
        new IsomerPair(
            "butan-1-ol", "CH₃-CH₂-CH₂-CH₂-OH",
            "butan-2-ol", "CH₃-CH(OH)-CH₂-CH₃",
            "C₄H₁₀O", 1,
            "<strong>Comprobación de fórmulas moleculares:</strong>\n\n" +
            "<ul><li>Butan-1-ol: 4C + 10H + O → C₄H₁₀O ✓</li>" +
            "<li>Butan-2-ol: 4C + 10H + O → C₄H₁₀O ✓</li></ul>\n\n" +
            "<strong>Análisis:</strong>\n\n" +
            "Ambos tienen la cadena butano (4C) y el grupo -OH (alcohol). " +
            "El grupo hidroxilo ocupa posiciones distintas:\n\n" +
            "<ul><li>butan-<strong>1</strong>-ol: alcohol primario, -OH en C1</li>" +
            "<li>butan-<strong>2</strong>-ol: alcohol secundario, -OH en C2</li></ul>\n\n" +
            "→ <strong>isomería de posición</strong>."),

        // I6: posición — but-1-eno vs but-2-eno (C₄H₈)
        new IsomerPair(
            "but-1-eno", "CH₂=CH-CH₂-CH₃",
            "but-2-eno", "CH₃-CH=CH-CH₃",
            "C₄H₈", 1,
            "<strong>Comprobación de fórmulas moleculares:</strong>\n\n" +
            "<ul><li>But-1-eno: 4C + 8H → C₄H₈ ✓</li>" +
            "<li>But-2-eno: 4C + 8H → C₄H₈ ✓</li></ul>\n\n" +
            "<strong>Análisis:</strong>\n\n" +
            "Ambos son alquenos (un doble enlace C=C) con cadena de 4 carbonos. " +
            "La diferencia es la posición del doble enlace:\n\n" +
            "<ul><li>but-<strong>1</strong>-eno: C1=C2</li>" +
            "<li>but-<strong>2</strong>-eno: C2=C3</li></ul>\n\n" +
            "Misma cadena + mismo tipo de insaturación + diferente localización → <strong>isomería de posición</strong>."),

        // I7: función — propanal vs propan-2-ona (C₃H₆O)
        new IsomerPair(
            "propanal", "CH₃-CH₂-CHO",
            "propan-2-ona (acetona)", "CH₃-CO-CH₃",
            "C₃H₆O", 2,
            "<strong>Comprobación de fórmulas moleculares:</strong>\n\n" +
            "<ul><li>Propanal: 3C + 6H + O → C₃H₆O ✓</li>" +
            "<li>Propan-2-ona: 3C + 6H + O → C₃H₆O ✓</li></ul>\n\n" +
            "<strong>Análisis:</strong>\n\n" +
            "Ambos tienen el mismo número y tipo de átomos, pero presentan <em>grupos funcionales diferentes</em>:\n\n" +
            "<ul><li>Propanal: grupo <strong>aldehído</strong> (-CHO) en el extremo de la cadena</li>" +
            "<li>Propan-2-ona: grupo <strong>cetona</strong> (-CO-) en el interior de la cadena</li></ul>\n\n" +
            "Diferente grupo funcional → <strong>isomería de función</strong>."),

        // I8: función — etanol vs metoximetano (C₂H₆O)
        new IsomerPair(
            "etanol", "CH₃-CH₂-OH",
            "metoximetano (dimetiléter)", "CH₃-O-CH₃",
            "C₂H₆O", 2,
            "<strong>Comprobación de fórmulas moleculares:</strong>\n\n" +
            "<ul><li>Etanol: 2C + 6H + O → C₂H₆O ✓</li>" +
            "<li>Metoximetano: 2C + 6H + O → C₂H₆O ✓</li></ul>\n\n" +
            "<strong>Análisis:</strong>\n\n" +
            "Misma fórmula molecular, pero grupos funcionales completamente distintos:\n\n" +
            "<ul><li>Etanol: <strong>alcohol</strong> (-OH), grupo funcional -C-OH</li>" +
            "<li>Metoximetano: <strong>éter</strong>, grupo funcional -C-O-C-</li></ul>\n\n" +
            "El átomo de oxígeno está enlazado a un solo carbono en el alcohol y a dos en el éter. " +
            "→ <strong>isomería de función</strong>."),

        // I9: función — butanal vs butan-2-ona (C₄H₈O)
        new IsomerPair(
            "butanal", "CH₃-CH₂-CH₂-CHO",
            "butan-2-ona", "CH₃-CO-CH₂-CH₃",
            "C₄H₈O", 2,
            "<strong>Comprobación de fórmulas moleculares:</strong>\n\n" +
            "<ul><li>Butanal: 4C + 8H + O → C₄H₈O ✓</li>" +
            "<li>Butan-2-ona: 4C + 8H + O → C₄H₈O ✓</li></ul>\n\n" +
            "<strong>Análisis:</strong>\n\n" +
            "Ambos tienen el grupo carbonilo (C=O), pero en posición diferente:\n\n" +
            "<ul><li>Butanal: C=O <strong>terminal</strong> → <strong>aldehído</strong></li>" +
            "<li>Butan-2-ona: C=O <strong>interno</strong> → <strong>cetona</strong></li></ul>\n\n" +
            "Diferente grupo funcional (aldehído ≠ cetona) → <strong>isomería de función</strong>."),

        // I10: función — ácido etanoico vs metanoato de metilo (C₂H₄O₂)
        new IsomerPair(
            "ácido etanoico", "CH₃-COOH",
            "metanoato de metilo", "HCOO-CH₃",
            "C₂H₄O₂", 2,
            "<strong>Comprobación de fórmulas moleculares:</strong>\n\n" +
            "<ul><li>Ácido etanoico: 2C + 4H + 2O → C₂H₄O₂ ✓</li>" +
            "<li>Metanoato de metilo: 2C + 4H + 2O → C₂H₄O₂ ✓</li></ul>\n\n" +
            "<strong>Análisis:</strong>\n\n" +
            "<ul><li>Ácido etanoico: grupo funcional <strong>ácido carboxílico</strong> (-COOH)</li>" +
            "<li>Metanoato de metilo: grupo funcional <strong>éster</strong> (-COO-)</li></ul>\n\n" +
            "Los ésteres se forman por condensación de un ácido y un alcohol. " +
            "Aquí, el éster HCOO-CH₃ (formiato de metilo) es el isómero de función " +
            "del ácido acético CH₃-COOH. → <strong>isomería de función</strong>.")
    );

    // Opciones fijas para todos los ejercicios de STRUCTURAL_ISOMERISM
    private static final String ISO_OPT0 = "Isomería de cadena (distinto esqueleto carbonado)";
    private static final String ISO_OPT1 = "Isomería de posición (misma cadena y función, distinta localización)";
    private static final String ISO_OPT2 = "Isomería de función (distinto grupo funcional)";
    private static final String ISO_OPT3 = "Estereoisomería geométrica (isomería cis-trans)";

    // =========================================================================
    // CARBON_ALLOTROPES — 10 preguntas conceptuales
    // =========================================================================

    private static final List<MCQuestion> ALLOTROPES = List.of(

        // A1: grafito — conductor, sp², laminar
        new MCQuestion(
            "¿Qué alótropo del carbono tiene estructura laminar con hibridación sp² y conduce la electricidad en el plano de las capas?",
            "Diamante", "Fullereno C₆₀", "Grafito", "Carbono amorfo", 2,
            "El <strong>grafito</strong> está formado por láminas paralelas de átomos de carbono con hibridación " +
            "<strong>sp²</strong>. Cada átomo se une covalentemente a otros 3 en una red hexagonal plana.\n\n" +
            "El cuarto electrón de cada C queda en el orbital \\(p_z\\) perpendicular al plano, " +
            "formando un sistema \\(\\pi\\) <strong>deslocalizado</strong> sobre toda la capa:\n\n" +
            "<ul><li>→ excelente conductor eléctrico <em>en el plano</em></li>" +
            "<li>→ aislante entre capas (fuerzas de Van der Waals débiles)</li>" +
            "<li>→ útil como electrodo y lubricante sólido</li></ul>"),

        // A2: diamante — más duro, sp³, 3D
        new MCQuestion(
            "¿Cuál es el alótropo natural del carbono más duro conocido?",
            "Grafeno", "Grafito", "Nanotubos de carbono", "Diamante", 3,
            "El <strong>diamante</strong> es el material natural más duro (10 en la escala de Mohs). " +
            "Esto se debe a su estructura:\n\n" +
            "<ul><li>Cada C está hibridado <strong>sp³</strong> y enlazado a otros 4 C mediante " +
            "enlaces \\(\\sigma\\) covalentes fuertes.</li>" +
            "<li>Forma una <strong>red covalente tridimensional</strong> continua sin planos débiles.</li>" +
            "<li>No hay electrones \\(\\pi\\) deslocalizados → <strong>no conduce la electricidad</strong>.</li></ul>\n\n" +
            "Aplicaciones: herramientas de corte, abrasivos, joyería."),

        // A3: grafeno — monocapa
        new MCQuestion(
            "¿Cómo se denomina el alótropo del carbono formado por una única lámina atómica de estructura hexagonal?",
            "Grafito B laminar", "Nanotubos de carbono", "Fullereno C₈₀", "Grafeno", 3,
            "El <strong>grafeno</strong> es esencialmente una sola capa de grafito: " +
            "una lámina de un único átomo de grosor con red hexagonal de carbonos sp².\n\n" +
            "Propiedades destacadas:\n\n" +
            "<ul><li>Mayor movilidad de electrones conocida (\\(\\approx 200\\,000\\,\\text{cm}^2/\\text{V·s}\\))</li>" +
            "<li>Resistencia mecánica altísima (≈200 veces más resistente que el acero)</li>" +
            "<li>Casi transparente óptica</li></ul>\n\n" +
            "Potenciales aplicaciones: transistores ultrafinos, pantallas flexibles, baterías avanzadas."),

        // A4: fullereno C60
        new MCQuestion(
            "¿Cuál es la fórmula del buckminsterfulereno, el fullereno más común?",
            "C₃₀", "C₄₈", "C₆₀", "C₈₀", 2,
            "El <strong>buckminsterfulereno C₆₀</strong> fue descubierto en 1985 " +
            "(Premio Nobel de Química 1996: Curl, Kroto y Smalley).\n\n" +
            "Estructura:\n\n" +
            "<ul><li>60 átomos de C que forman una esfera de fútbol truncada</li>" +
            "<li>Compuesta de 20 hexágonos y 12 pentágonos</li>" +
            "<li>Hibridación aproximada sp² (curvatura induce un carácter sp³ parcial)</li></ul>\n\n" +
            "Aplicaciones: nanomedicina (transporte de fármacos), superconductores al doparse, " +
            "lubricantes moleculares."),

        // A5: diamante sp³
        new MCQuestion(
            "En el diamante, la hibridación de cada átomo de carbono y la geometría resultante son:",
            "sp, lineal (2 enlaces)", "sp², trigonal plano (3 enlaces)",
            "sp³, tetraédrico (4 enlaces)", "sp³d, bipirámide trigonal (5 enlaces)", 2,
            "En el <strong>diamante</strong>:\n\n" +
            "<ul><li>Cada C tiene 4 electrones de valencia que forman 4 enlaces covalentes \\(\\sigma\\)</li>" +
            "<li>Para maximizar la separación angular, adopta hibridación <strong>sp³</strong></li>" +
            "<li>Geometría: <strong>tetraédrica</strong>, con ángulos de enlace de \\(109{,}5°\\)</li></ul>\n\n" +
            "\\[\\text{C} \\xrightarrow{\\text{hibrid. sp}^3} 4 \\text{ orbitales sp}^3 " +
            "\\rightarrow \\text{tetraedro}\\]\n\n" +
            "Esta geometría 3D sin planos de deslizamiento explica la máxima dureza natural."),

        // A6: nanotubos de carbono
        new MCQuestion(
            "Los nanotubos de carbono se pueden describir geométricamente como:",
            "Fragmentos esféricos de diamante", "Láminas de grafeno enrolladas en cilindros",
            "Fullerenos C₆₀ polimerizados en cadena", "Carbono amorfo compactado a alta presión", 1,
            "Los <strong>nanotubos de carbono</strong> (CNT, del inglés Carbon Nanotubes) " +
            "son estructuras cilíndricas nanométricas obtenidas conceptualmente al <strong>enrollar " +
            "una lámina de grafeno</strong> sobre sí misma.\n\n" +
            "<ul><li>Diámetro típico: 1-50 nm; longitud: hasta varios mm</li>" +
            "<li>Los CNT pueden ser metálicos o semiconductores según el ángulo de enrollamiento (quiralidad)</li>" +
            "<li>Propiedades mecánicas extraordinarias (resistencia a tracción mayor que el acero)</li></ul>\n\n" +
            "Aplicaciones: electrónica molecular, sensores, materiales compuestos ultraligeros."),

        // A7: grafito lubricante
        new MCQuestion(
            "¿Por qué el grafito se utiliza como lubricante sólido?",
            "Por su extrema dureza, que impide el desgaste",
            "Sus láminas se deslizan entre sí fácilmente gracias a las débiles fuerzas de Van der Waals interplanares",
            "Porque absorbe agua y forma una película líquida",
            "Porque funde a baja temperatura y actúa como fluido", 1,
            "El grafito tiene una estructura <strong>laminar</strong>:\n\n" +
            "<ul><li>Dentro de cada capa: enlaces covalentes \\(\\sigma\\) y \\(\\pi\\) <em>fuertes</em> (sp²)</li>" +
            "<li>Entre capas: solo <strong>fuerzas de Van der Waals</strong> débiles " +
            "(separación ≈ 335 pm)</li></ul>\n\n" +
            "Las capas se desplazan unas sobre otras con muy poca resistencia → " +
            "<strong>efecto lubricante</strong>.\n\n" +
            "Aplicaciones: lubricante sólido en mecánica, grafito en lápices (deja una " +
            "marca al desprenderse capas)."),

        // A8: red covalente 3D → diamante
        new MCQuestion(
            "¿En cuál de los siguientes alótropos existe una red covalente tridimensional que explica su extrema dureza y su carácter de aislante eléctrico?",
            "Grafito", "Fullereno C₆₀", "Grafeno", "Diamante", 3,
            "<strong>Diamante</strong>: red covalente tridimensional en la que cada C está " +
            "unido a 4 C vecinos por enlaces \\(\\sigma\\) sp³.\n\n" +
            "<ul><li>No hay electrones \\(\\pi\\) libres → <strong>aislante eléctrico</strong></li>" +
            "<li>Todos los electrones de valencia están localizados en enlaces → no hay portadores</li>" +
            "<li>Red sin planos de deslizamiento → <strong>dureza máxima</strong></li></ul>\n\n" +
            "Contrasta con el grafito (conductor por sus electrones \\(\\pi\\) deslocalizados) " +
            "y con el grafeno (semimetal)."),

        // A9: grafeno en transistores
        new MCQuestion(
            "¿Cuál de estos alótropos del carbono se investiga intensamente para transistores ultrafinos y electrónica flexible gracias a su altísima movilidad electrónica?",
            "Grafito en bloque", "Fullereno C₆₀", "Diamante", "Grafeno", 3,
            "El <strong>grafeno</strong> presenta características únicas para electrónica:\n\n" +
            "<ul><li>Movilidad de portadores \\(\\mu \\approx 2 \\times 10^5\\,\\text{cm}^2/\\text{V·s}\\) " +
            "(≈100× mayor que el silicio)</li>" +
            "<li>Relación de dispersión lineal: los electrones se comportan como fermiones de Dirac " +
            "sin masa efectiva</li>" +
            "<li>Transparencia óptica del 97,7 %</li></ul>\n\n" +
            "Aplicaciones potenciales: transistores de efecto de campo ultrafinos, " +
            "pantallas táctiles flexibles, conexiones en chips de próxima generación."),

        // A10: conducción — grafito vs diamante
        new MCQuestion(
            "¿Por qué el diamante no conduce la electricidad mientras que el grafito sí lo hace (en el plano)?",
            "En el diamante no existen electrones de valencia",
            "En el diamante todos los electrones están localizados en enlaces σ; en el grafito hay electrones π deslocalizados que actúan como portadores de carga",
            "El grafito contiene impurezas metálicas que le confieren conductividad",
            "El diamante tiene mayor densidad y eso impide el movimiento de cargas", 1,
            "<strong>Comparativa electrónica de los dos alótropos:</strong>\n\n" +
            "<table class=\"data-table\"><thead><tr>" +
            "<th>Propiedad</th><th>Diamante (sp³)</th><th>Grafito (sp²)</th>" +
            "</tr></thead><tbody>" +
            "<tr><td>Electrones de Valencia</td><td>Todos en enlaces σ localizados</td>" +
            "<td>3 en σ + 1 en π deslocalizado</td></tr>" +
            "<tr><td>Conductividad eléctrica</td><td>Aislante</td><td>Conductor en el plano</td></tr>" +
            "<tr><td>Banda de conducción</td><td>Gap ≈ 5,5 eV (muy ancho)</td>" +
            "<td>Semimetal (banda π)</td></tr>" +
            "</tbody></table>\n\n" +
            "La deslocalización de los electrones \\(\\pi\\) en el grafito crea la banda de conducción " +
            "que permite el movimiento de carga bajo campo eléctrico.")
    );

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public FirstBachOrganicChemistryExercise generateAndSave() {
        FirstBachOrganicChemistryExercise ex = new FirstBachOrganicChemistryExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);
        ex.setExerciseMode("MULTIPLE_CHOICE");

        int roll = random.nextInt(3);
        if      (roll == 0) buildNomenclature(ex);
        else if (roll == 1) buildIsomerism(ex);
        else                buildAllotropes(ex);

        log.debug("1BACH BL5 generado: type={}", ex.getOrganicChemistryType());
        return repository.save(ex);
    }

    public FirstBachOrganicChemistryExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ejercicio 1BACH BL5 no encontrado: " + id));
    }

    // =========================================================================
    // CONSTRUCTORES
    // =========================================================================

    private void buildNomenclature(FirstBachOrganicChemistryExercise ex) {
        ex.setOrganicChemistryType(OrganicChemistryType.ORGANIC_NOMENCLATURE);
        MCQuestion q = NOMENCLATURE.get(random.nextInt(NOMENCLATURE.size()));
        fillMC(ex, q.statement(), q.opt0(), q.opt1(), q.opt2(), q.opt3(),
               q.correct(), q.explanation());
    }

    private void buildIsomerism(FirstBachOrganicChemistryExercise ex) {
        ex.setOrganicChemistryType(OrganicChemistryType.STRUCTURAL_ISOMERISM);
        IsomerPair p = ISOMERS.get(random.nextInt(ISOMERS.size()));

        String statement = String.format(
            "Los compuestos %s (%s) y %s (%s) tienen la misma fórmula molecular %s. " +
            "¿Qué tipo de isomería de constitución presentan?",
            p.compA(), p.formulaA(), p.compB(), p.formulaB(), p.molFormula());

        fillMC(ex, statement, ISO_OPT0, ISO_OPT1, ISO_OPT2, ISO_OPT3,
               p.type(), p.explanation());
    }

    private void buildAllotropes(FirstBachOrganicChemistryExercise ex) {
        ex.setOrganicChemistryType(OrganicChemistryType.CARBON_ALLOTROPES);
        MCQuestion q = ALLOTROPES.get(random.nextInt(ALLOTROPES.size()));
        fillMC(ex, q.statement(), q.opt0(), q.opt1(), q.opt2(), q.opt3(),
               q.correct(), q.explanation());
    }

    /** Rellena los campos de opción múltiple comunes a los tres tipos. */
    private void fillMC(FirstBachOrganicChemistryExercise ex,
                        String statement,
                        String opt0, String opt1, String opt2, String opt3,
                        int correct, String explanation) {
        ex.setStatement(statement);
        ex.setOption0(opt0);
        ex.setOption1(opt1);
        ex.setOption2(opt2);
        ex.setOption3(opt3);
        ex.setCorrectIndex(correct);
        ex.setExplanation(explanation);
    }
}
