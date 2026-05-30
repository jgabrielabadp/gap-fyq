package com.gap.fyq.service;

import com.gap.fyq.model.fourtheso.chemicalchanges.FourthEsoChemicalChangesExercise;
import com.gap.fyq.model.fourtheso.chemicalchanges.FourthEsoChemicalChangesType;
import com.gap.fyq.repository.FourthEsoChemicalChangesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class FourthEsoChemicalChangesService {

    private final FourthEsoChemicalChangesRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "4ESO";
    private static final String BLOCK  = "BL3";

    // =========================================================================
    // Registro genérico de escenario (todos los tipos comparten esta estructura)
    // =========================================================================

    private record Scenario(
        String statement,
        double correctAnswer,
        String answerUnit,
        String correctAnswerDisplay,
        String unknownVariable,
        String explanation
    ) {}

    // =========================================================================
    // MOL_AVOGADRO_CONVERSION — 12 escenarios
    // Na = 6,022 × 10²³ mol⁻¹   |   fórmulas: n = m/Mm   y   N = n × Na
    // Resultados verificados a 2 d.p.
    // =========================================================================

    private static final List<Scenario> AVOGADRO_SCENARIOS = List.of(

        // ── Gramos → Moles ────────────────────────────────────────────────────

        new Scenario(
            "Calcula el número de moles en 36 g de agua (H₂O, Mm = 18 g/mol).",
            2.00, "mol", "2,00 mol", "n_moles",
            "Aplicamos la relación entre masa, moles y masa molar:\n\n" +
            "\\[n = \\frac{m}{M_m}\\]\n\n" +
            "Sustituimos \\(m = 36\\,\\text{g}\\) y \\(M_m(\\text{H}_2\\text{O}) = 18\\,\\text{g/mol}\\):\n\n" +
            "\\[n = \\frac{36\\,\\text{g}}{18\\,\\text{g/mol}} = 2{,}00\\,\\text{mol}\\]\n\n" +
            "∴  En 36 g de H₂O hay <strong>2,00 mol</strong>."
        ),

        new Scenario(
            "Calcula el número de moles en 88 g de dióxido de carbono (CO₂, Mm = 44 g/mol).",
            2.00, "mol", "2,00 mol", "n_moles",
            "La masa molar del CO₂ es \\(M_m = 12 + 2 \\times 16 = 44\\,\\text{g/mol}\\).\n\n" +
            "\\[n = \\frac{m}{M_m} = \\frac{88\\,\\text{g}}{44\\,\\text{g/mol}} = 2{,}00\\,\\text{mol}\\]\n\n" +
            "∴  En 88 g de CO₂ hay <strong>2,00 mol</strong>."
        ),

        new Scenario(
            "Calcula el número de moles en 200 g de carbonato de calcio (CaCO₃, Mm = 100 g/mol).",
            2.00, "mol", "2,00 mol", "n_moles",
            "\\[M_m(\\text{CaCO}_3) = 40 + 12 + 3 \\times 16 = 100\\,\\text{g/mol}\\]\n\n" +
            "\\[n = \\frac{200\\,\\text{g}}{100\\,\\text{g/mol}} = 2{,}00\\,\\text{mol}\\]\n\n" +
            "∴  En 200 g de CaCO₃ hay <strong>2,00 mol</strong>."
        ),

        new Scenario(
            "Calcula el número de moles en 160 g de hidróxido de sodio (NaOH, Mm = 40 g/mol).",
            4.00, "mol", "4,00 mol", "n_moles",
            "\\[M_m(\\text{NaOH}) = 23 + 16 + 1 = 40\\,\\text{g/mol}\\]\n\n" +
            "\\[n = \\frac{160\\,\\text{g}}{40\\,\\text{g/mol}} = 4{,}00\\,\\text{mol}\\]\n\n" +
            "∴  En 160 g de NaOH hay <strong>4,00 mol</strong>."
        ),

        // ── Moles → Gramos ────────────────────────────────────────────────────

        new Scenario(
            "¿Qué masa tienen 3 mol de agua (H₂O, Mm = 18 g/mol)?",
            54.00, "g", "54,00 g", "masa_g",
            "Despejamos la masa de la relación \\(n = m / M_m\\):\n\n" +
            "\\[m = n \\times M_m = 3{,}00\\,\\text{mol} \\times 18\\,\\text{g/mol} = 54{,}00\\,\\text{g}\\]\n\n" +
            "∴  3 mol de H₂O tienen una masa de <strong>54,00 g</strong>."
        ),

        new Scenario(
            "¿Qué masa tienen 2 mol de CO₂ (Mm = 44 g/mol)?",
            88.00, "g", "88,00 g", "masa_g",
            "\\[m = n \\times M_m = 2{,}00\\,\\text{mol} \\times 44\\,\\text{g/mol} = 88{,}00\\,\\text{g}\\]\n\n" +
            "∴  2 mol de CO₂ tienen una masa de <strong>88,00 g</strong>."
        ),

        new Scenario(
            "¿Qué masa tienen 5 mol de hierro (Fe, Mm = 56 g/mol)?",
            280.00, "g", "280,00 g", "masa_g",
            "\\[m = n \\times M_m = 5{,}00\\,\\text{mol} \\times 56\\,\\text{g/mol} = 280{,}00\\,\\text{g}\\]\n\n" +
            "∴  5 mol de Fe tienen una masa de <strong>280,00 g</strong>."
        ),

        new Scenario(
            "¿Qué masa tienen 0,5 mol de cloruro de sodio (NaCl, Mm = 58,5 g/mol)?",
            29.25, "g", "29,25 g", "masa_g",
            "\\[m = n \\times M_m = 0{,}50\\,\\text{mol} \\times 58{,}5\\,\\text{g/mol} = 29{,}25\\,\\text{g}\\]\n\n" +
            "∴  0,5 mol de NaCl tienen una masa de <strong>29,25 g</strong>."
        ),

        // ── Moles → Partículas ────────────────────────────────────────────────

        new Scenario(
            "¿Cuántas moléculas hay en 2 mol de H₂O? (N_A = 6,022 × 10²³ mol⁻¹.)",
            2 * 6.022e23, "moléculas", "1,204 × 10²⁴ moléculas", "num_particulas",
            "Usamos el <strong>número de Avogadro</strong> \\(N_A = 6{,}022 \\times 10^{23}\\,\\text{mol}^{-1}\\):\n\n" +
            "\\[N = n \\times N_A = 2{,}00\\,\\text{mol} \\times 6{,}022 \\times 10^{23}\\,\\text{mol}^{-1}\\]\n\n" +
            "\\[N = 1{,}204 \\times 10^{24}\\,\\text{moléculas}\\]\n\n" +
            "∴  2 mol de H₂O contienen <strong>1,204 × 10²⁴ moléculas</strong>."
        ),

        new Scenario(
            "¿Cuántas moléculas hay en 0,5 mol de O₂? (N_A = 6,022 × 10²³ mol⁻¹.)",
            0.5 * 6.022e23, "moléculas", "3,011 × 10²³ moléculas", "num_particulas",
            "\\[N = n \\times N_A = 0{,}50\\,\\text{mol} \\times 6{,}022 \\times 10^{23}\\,\\text{mol}^{-1}\\]\n\n" +
            "\\[N = 3{,}011 \\times 10^{23}\\,\\text{moléculas}\\]\n\n" +
            "∴  0,5 mol de O₂ contienen <strong>3,011 × 10²³ moléculas</strong>."
        ),

        // ── Partículas → Moles ────────────────────────────────────────────────

        new Scenario(
            "¿Cuántos moles corresponden a 1,204 × 10²⁴ moléculas de H₂O? (N_A = 6,022 × 10²³ mol⁻¹.)",
            2.00, "mol", "2,00 mol", "n_moles",
            "Despejamos n de la relación \\(N = n \\times N_A\\):\n\n" +
            "\\[n = \\frac{N}{N_A} = \\frac{1{,}204 \\times 10^{24}}{6{,}022 \\times 10^{23}\\,\\text{mol}^{-1}} = 2{,}00\\,\\text{mol}\\]\n\n" +
            "∴  1,204 × 10²⁴ moléculas de H₂O son <strong>2,00 mol</strong>."
        ),

        new Scenario(
            "¿Cuántos moles corresponden a 3,011 × 10²³ átomos de Fe? (N_A = 6,022 × 10²³ mol⁻¹.)",
            0.50, "mol", "0,50 mol", "n_moles",
            "\\[n = \\frac{N}{N_A} = \\frac{3{,}011 \\times 10^{23}}{6{,}022 \\times 10^{23}\\,\\text{mol}^{-1}} = 0{,}50\\,\\text{mol}\\]\n\n" +
            "∴  3,011 × 10²³ átomos de Fe son <strong>0,50 mol</strong>."
        )
    );

    // =========================================================================
    // ADVANCED_STOICHIOMETRY — 12 escenarios (6 gas ideal + 6 molaridad)
    // R = 0,082 L·atm/(mol·K)   |   M = n / V(L)
    // =========================================================================

    private static final List<Scenario> STOICH_SCENARIOS = List.of(

        // ── Gas ideal: encontrar V ────────────────────────────────────────────

        new Scenario(
            "¿Qué volumen ocupa 2 mol de O₂(g) a 1 atm y 273 K? " +
            "Usa R = 0,082 L·atm/(mol·K).",
            44.77, "L", "44,77 L", "volumen_L",
            "Partimos de la <strong>ecuación del gas ideal</strong> \\(P \\cdot V = n \\cdot R \\cdot T\\) " +
            "y despejamos V:\n\n" +
            "\\[V = \\frac{n \\cdot R \\cdot T}{P}\\]\n\n" +
            "Sustituimos \\(n=2\\,\\text{mol}\\), \\(R=0{,}082\\,\\frac{\\text{L·atm}}{\\text{mol·K}}\\), " +
            "\\(T=273\\,\\text{K}\\), \\(P=1\\,\\text{atm}\\):\n\n" +
            "\\[V = \\frac{2 \\times 0{,}082 \\times 273}{1} = \\frac{44{,}77\\,\\text{L·atm}}{1\\,\\text{atm}} = 44{,}77\\,\\text{L}\\]\n\n" +
            "∴  V = <strong>44,77 L</strong>."
        ),

        new Scenario(
            "¿Qué volumen ocupa 1 mol de CO₂(g) a 1 atm y 273 K? " +
            "Usa R = 0,082 L·atm/(mol·K). (Volumen molar a condiciones normales.)",
            22.39, "L", "22,39 L", "volumen_L",
            "\\[V = \\frac{n \\cdot R \\cdot T}{P} = \\frac{1 \\times 0{,}082 \\times 273}{1} = 22{,}39\\,\\text{L}\\]\n\n" +
            "Este resultado (~22,4 L) es el <strong>volumen molar a condiciones normales</strong> " +
            "(CN: 0 °C y 1 atm): 1 mol de cualquier gas ideal ocupa ≈ 22,4 L.\n\n" +
            "∴  V = <strong>22,39 L</strong>."
        ),

        new Scenario(
            "¿Qué volumen ocupa 3 mol de CH₄(g) a 2 atm y 300 K? " +
            "Usa R = 0,082 L·atm/(mol·K).",
            36.90, "L", "36,90 L", "volumen_L",
            "\\[V = \\frac{n \\cdot R \\cdot T}{P} = \\frac{3 \\times 0{,}082 \\times 300}{2} = " +
            "\\frac{73{,}80}{2} = 36{,}90\\,\\text{L}\\]\n\n" +
            "∴  V = <strong>36,90 L</strong>."
        ),

        // ── Gas ideal: encontrar n ────────────────────────────────────────────

        new Scenario(
            "Un recipiente contiene 44,77 L de N₂(g) a 1 atm y 273 K. " +
            "¿Cuántos moles de N₂ hay? Usa R = 0,082 L·atm/(mol·K).",
            2.00, "mol", "2,00 mol", "n_moles",
            "Despejamos n de \\(P \\cdot V = n \\cdot R \\cdot T\\):\n\n" +
            "\\[n = \\frac{P \\cdot V}{R \\cdot T} = \\frac{1 \\times 44{,}77}{0{,}082 \\times 273} = " +
            "\\frac{44{,}77}{22{,}39} \\approx 2{,}00\\,\\text{mol}\\]\n\n" +
            "∴  n = <strong>2,00 mol</strong> de N₂."
        ),

        new Scenario(
            "Un globo contiene 22,39 L de He(g) a 1 atm y 273 K. " +
            "¿Cuántos moles hay? Usa R = 0,082 L·atm/(mol·K).",
            1.00, "mol", "1,00 mol", "n_moles",
            "\\[n = \\frac{P \\cdot V}{R \\cdot T} = \\frac{1 \\times 22{,}39}{0{,}082 \\times 273} = " +
            "\\frac{22{,}39}{22{,}39} = 1{,}00\\,\\text{mol}\\]\n\n" +
            "∴  n = <strong>1,00 mol</strong> de He."
        ),

        new Scenario(
            "Un depósito de 36,90 L contiene O₂(g) a 2 atm y 300 K. " +
            "¿Cuántos moles de O₂ contiene? Usa R = 0,082 L·atm/(mol·K).",
            3.00, "mol", "3,00 mol", "n_moles",
            "\\[n = \\frac{P \\cdot V}{R \\cdot T} = \\frac{2 \\times 36{,}90}{0{,}082 \\times 300} = " +
            "\\frac{73{,}80}{24{,}60} = 3{,}00\\,\\text{mol}\\]\n\n" +
            "∴  n = <strong>3,00 mol</strong> de O₂."
        ),

        // ── Molaridad: encontrar M ────────────────────────────────────────────

        new Scenario(
            "Se disuelven 58,5 g de NaCl (Mm = 58,5 g/mol) en agua hasta " +
            "obtener 1 L de disolución. ¿Cuál es la molaridad?",
            1.00, "mol/L", "1,00 mol/L", "molaridad",
            "La <strong>molaridad</strong> es \\(M = n / V\\,\\text{(L)}\\). " +
            "Primero calculamos los moles:\n\n" +
            "\\[n = \\frac{m}{M_m} = \\frac{58{,}5\\,\\text{g}}{58{,}5\\,\\text{g/mol}} = 1{,}00\\,\\text{mol}\\]\n\n" +
            "\\[M = \\frac{1{,}00\\,\\text{mol}}{1\\,\\text{L}} = 1{,}00\\,\\text{mol/L}\\]\n\n" +
            "∴  M = <strong>1,00 mol/L</strong>."
        ),

        new Scenario(
            "Se disuelven 40 g de NaOH (Mm = 40 g/mol) en agua hasta " +
            "obtener 500 mL de disolución. ¿Cuál es la molaridad?",
            2.00, "mol/L", "2,00 mol/L", "molaridad",
            "\\[n = \\frac{40\\,\\text{g}}{40\\,\\text{g/mol}} = 1{,}00\\,\\text{mol}\\]\n\n" +
            "Convertimos el volumen: \\(V = 500\\,\\text{mL} = 0{,}500\\,\\text{L}\\)\n\n" +
            "\\[M = \\frac{1{,}00\\,\\text{mol}}{0{,}500\\,\\text{L}} = 2{,}00\\,\\text{mol/L}\\]\n\n" +
            "∴  M = <strong>2,00 mol/L</strong>."
        ),

        new Scenario(
            "Se disuelven 9 g de glucosa C₆H₁₂O₆ (Mm = 180 g/mol) en agua " +
            "hasta obtener 500 mL de disolución. ¿Cuál es la molaridad?",
            0.10, "mol/L", "0,10 mol/L", "molaridad",
            "\\[n = \\frac{9\\,\\text{g}}{180\\,\\text{g/mol}} = 0{,}05\\,\\text{mol}\\]\n\n" +
            "\\[M = \\frac{0{,}05\\,\\text{mol}}{0{,}500\\,\\text{L}} = 0{,}10\\,\\text{mol/L}\\]\n\n" +
            "∴  M = <strong>0,10 mol/L</strong>."
        ),

        // ── Molaridad: encontrar masa ─────────────────────────────────────────

        new Scenario(
            "¿Qué masa de NaCl (Mm = 58,5 g/mol) se necesita para preparar " +
            "250 mL de disolución 2 M?",
            29.25, "g", "29,25 g", "masa_g",
            "Calculamos los moles usando \\(n = M \\times V\\):\n\n" +
            "\\[n = 2{,}00\\,\\frac{\\text{mol}}{\\text{L}} \\times 0{,}250\\,\\text{L} = 0{,}500\\,\\text{mol}\\]\n\n" +
            "\\[m = n \\times M_m = 0{,}500\\,\\text{mol} \\times 58{,}5\\,\\text{g/mol} = 29{,}25\\,\\text{g}\\]\n\n" +
            "∴  Se necesitan <strong>29,25 g</strong> de NaCl."
        ),

        new Scenario(
            "¿Qué masa de HCl (Mm = 36,5 g/mol) contienen 200 mL de disolución 0,5 M?",
            3.65, "g", "3,65 g", "masa_g",
            "\\[n = M \\times V = 0{,}50\\,\\frac{\\text{mol}}{\\text{L}} \\times 0{,}200\\,\\text{L} = 0{,}100\\,\\text{mol}\\]\n\n" +
            "\\[m = 0{,}100\\,\\text{mol} \\times 36{,}5\\,\\text{g/mol} = 3{,}65\\,\\text{g}\\]\n\n" +
            "∴  Hay <strong>3,65 g</strong> de HCl."
        ),

        new Scenario(
            "¿Qué masa de H₂SO₄ (Mm = 98 g/mol) hay en 100 mL de disolución 1 M?",
            9.80, "g", "9,80 g", "masa_g",
            "\\[n = 1{,}00\\,\\frac{\\text{mol}}{\\text{L}} \\times 0{,}100\\,\\text{L} = 0{,}100\\,\\text{mol}\\]\n\n" +
            "\\[m = 0{,}100\\,\\text{mol} \\times 98\\,\\text{g/mol} = 9{,}80\\,\\text{g}\\]\n\n" +
            "∴  Hay <strong>9,80 g</strong> de H₂SO₄."
        )
    );

    // =========================================================================
    // LIMITING_REACTANT — 6 escenarios con reacciones clásicas
    // Método: dividir moles/coeficiente; el menor cociente es el reactivo limitante.
    // Resultados verificados manualmente a 2 d.p.
    // =========================================================================

    private static final List<Scenario> LR_SCENARIOS = List.of(

        // LR-1: N₂ + 3H₂ → 2NH₃
        // 56 g N₂ = 2 mol; 9 g H₂ = 4,5 mol
        // Cocientes: N₂ 2/1=2,00 ; H₂ 4,5/3=1,50 → H₂ limitante
        // NH₃ = (4,5/3)×2 = 3 mol × 17 g/mol = 51,00 g
        new Scenario(
            "Se mezclan 56 g de N₂ (Mm=28 g/mol) con 9 g de H₂ (Mm=2 g/mol) " +
            "según la reacción: N₂ + 3H₂ → 2NH₃ (Mm NH₃=17 g/mol). " +
            "Identifica el reactivo limitante y calcula la masa de NH₃ formada.",
            51.00, "g", "51,00 g", "masa_producto_g",
            "<strong>Paso 1 — Moles de cada reactivo:</strong>\n\n" +
            "\\[n(\\text{N}_2) = \\frac{56\\,\\text{g}}{28\\,\\text{g/mol}} = 2{,}00\\,\\text{mol}\\]\n" +
            "\\[n(\\text{H}_2) = \\frac{9\\,\\text{g}}{2\\,\\text{g/mol}} = 4{,}50\\,\\text{mol}\\]\n\n" +
            "<strong>Paso 2 — Cociente mol/coeficiente estequiométrico:</strong>\n\n" +
            "\\[\\frac{n(\\text{N}_2)}{1} = 2{,}00 \\qquad " +
            "\\frac{n(\\text{H}_2)}{3} = 1{,}50\\]\n\n" +
            "El menor cociente es el del <strong>H₂ → reactivo limitante</strong>.\n\n" +
            "<strong>Paso 3 — Moles de NH₃ producido (a partir del limitante):</strong>\n\n" +
            "\\[n(\\text{NH}_3) = \\frac{4{,}50\\,\\text{mol H}_2}{3} \\times 2 = 3{,}00\\,\\text{mol}\\]\n\n" +
            "<strong>Paso 4 — Masa de NH₃:</strong>\n\n" +
            "\\[m(\\text{NH}_3) = 3{,}00\\,\\text{mol} \\times 17\\,\\text{g/mol} = 51{,}00\\,\\text{g}\\]\n\n" +
            "∴  Se forman <strong>51,00 g de NH₃</strong>. El N₂ queda en exceso."
        ),

        // LR-2: CH₄ + 2O₂ → CO₂ + 2H₂O
        // 16 g CH₄ = 1 mol; 48 g O₂ = 1,5 mol
        // Cocientes: CH₄ 1/1=1,00 ; O₂ 1,5/2=0,75 → O₂ limitante
        // CO₂ = (1,5/2)×1 = 0,75 mol × 44 = 33,00 g
        new Scenario(
            "Se mezclan 16 g de CH₄ (Mm=16 g/mol) con 48 g de O₂ (Mm=32 g/mol) " +
            "según: CH₄ + 2O₂ → CO₂ + 2H₂O (Mm CO₂=44 g/mol). " +
            "Identifica el reactivo limitante y calcula la masa de CO₂ formada.",
            33.00, "g", "33,00 g", "masa_producto_g",
            "<strong>Paso 1 — Moles:</strong>\n\n" +
            "\\[n(\\text{CH}_4) = \\frac{16}{16} = 1{,}00\\,\\text{mol}\\quad;" +
            "\\quad n(\\text{O}_2) = \\frac{48}{32} = 1{,}50\\,\\text{mol}\\]\n\n" +
            "<strong>Paso 2 — Cocientes mol/coeficiente:</strong>\n\n" +
            "\\[\\frac{n(\\text{CH}_4)}{1} = 1{,}00 \\qquad " +
            "\\frac{n(\\text{O}_2)}{2} = 0{,}75\\]\n\n" +
            "<strong>O₂ → reactivo limitante</strong> (menor cociente).\n\n" +
            "<strong>Paso 3 — Moles de CO₂:</strong>\n\n" +
            "\\[n(\\text{CO}_2) = \\frac{1{,}50\\,\\text{mol O}_2}{2} \\times 1 = 0{,}75\\,\\text{mol}\\]\n\n" +
            "<strong>Paso 4 — Masa de CO₂:</strong>\n\n" +
            "\\[m(\\text{CO}_2) = 0{,}75\\,\\text{mol} \\times 44\\,\\text{g/mol} = 33{,}00\\,\\text{g}\\]\n\n" +
            "∴  Se forman <strong>33,00 g de CO₂</strong>. El CH₄ queda en exceso."
        ),

        // LR-3: 2H₂ + O₂ → 2H₂O
        // 4 g H₂ = 2 mol; 48 g O₂ = 1,5 mol
        // Cocientes: H₂ 2/2=1,00 ; O₂ 1,5/1=1,50 → H₂ limitante
        // H₂O = (2/2)×2 = 2 mol × 18 = 36,00 g
        new Scenario(
            "Se mezclan 4 g de H₂ (Mm=2 g/mol) con 48 g de O₂ (Mm=32 g/mol) " +
            "según: 2H₂ + O₂ → 2H₂O (Mm H₂O=18 g/mol). " +
            "Identifica el reactivo limitante y calcula la masa de H₂O formada.",
            36.00, "g", "36,00 g", "masa_producto_g",
            "<strong>Paso 1 — Moles:</strong>\n\n" +
            "\\[n(\\text{H}_2) = \\frac{4}{2} = 2{,}00\\,\\text{mol}\\quad;" +
            "\\quad n(\\text{O}_2) = \\frac{48}{32} = 1{,}50\\,\\text{mol}\\]\n\n" +
            "<strong>Paso 2 — Cocientes mol/coeficiente:</strong>\n\n" +
            "\\[\\frac{n(\\text{H}_2)}{2} = 1{,}00 \\qquad " +
            "\\frac{n(\\text{O}_2)}{1} = 1{,}50\\]\n\n" +
            "<strong>H₂ → reactivo limitante</strong>.\n\n" +
            "<strong>Paso 3 — Moles de H₂O:</strong>\n\n" +
            "\\[n(\\text{H}_2\\text{O}) = \\frac{2{,}00\\,\\text{mol H}_2}{2} \\times 2 = 2{,}00\\,\\text{mol}\\]\n\n" +
            "<strong>Paso 4 — Masa de H₂O:</strong>\n\n" +
            "\\[m(\\text{H}_2\\text{O}) = 2{,}00\\,\\text{mol} \\times 18\\,\\text{g/mol} = 36{,}00\\,\\text{g}\\]\n\n" +
            "∴  Se forman <strong>36,00 g de H₂O</strong>. El O₂ queda en exceso."
        ),

        // LR-4: Fe + 2HCl → FeCl₂ + H₂
        // 56 g Fe = 1 mol; 36,5 g HCl = 1 mol
        // Cocientes: Fe 1/1=1,00 ; HCl 1/2=0,50 → HCl limitante
        // FeCl₂ = (1/2)×1 = 0,50 mol × 127 = 63,50 g
        new Scenario(
            "Se mezclan 56 g de Fe (Mm=56 g/mol) con 36,5 g de HCl (Mm=36,5 g/mol) " +
            "según: Fe + 2HCl → FeCl₂ + H₂ (Mm FeCl₂=127 g/mol). " +
            "Identifica el reactivo limitante y calcula la masa de FeCl₂ formada.",
            63.50, "g", "63,50 g", "masa_producto_g",
            "<strong>Paso 1 — Moles:</strong>\n\n" +
            "\\[n(\\text{Fe}) = \\frac{56}{56} = 1{,}00\\,\\text{mol}\\quad;" +
            "\\quad n(\\text{HCl}) = \\frac{36{,}5}{36{,}5} = 1{,}00\\,\\text{mol}\\]\n\n" +
            "<strong>Paso 2 — Cocientes mol/coeficiente:</strong>\n\n" +
            "\\[\\frac{n(\\text{Fe})}{1} = 1{,}00 \\qquad " +
            "\\frac{n(\\text{HCl})}{2} = 0{,}50\\]\n\n" +
            "<strong>HCl → reactivo limitante</strong>.\n\n" +
            "<strong>Paso 3 — Moles de FeCl₂:</strong>\n\n" +
            "\\[n(\\text{FeCl}_2) = \\frac{1{,}00\\,\\text{mol HCl}}{2} \\times 1 = 0{,}50\\,\\text{mol}\\]\n\n" +
            "<strong>Paso 4 — Masa de FeCl₂:</strong>\n\n" +
            "\\[m(\\text{FeCl}_2) = 0{,}50\\,\\text{mol} \\times 127\\,\\text{g/mol} = 63{,}50\\,\\text{g}\\]\n\n" +
            "∴  Se forman <strong>63,50 g de FeCl₂</strong>. El Fe queda en exceso."
        ),

        // LR-5: C₃H₈ + 5O₂ → 3CO₂ + 4H₂O
        // 44 g C₃H₈ = 1 mol; 128 g O₂ = 4 mol
        // Cocientes: C₃H₈ 1/1=1,00 ; O₂ 4/5=0,80 → O₂ limitante
        // CO₂ = (4/5)×3 = 2,4 mol × 44 = 105,60 g
        new Scenario(
            "Se mezclan 44 g de propano C₃H₈ (Mm=44 g/mol) con 128 g de O₂ (Mm=32 g/mol) " +
            "según: C₃H₈ + 5O₂ → 3CO₂ + 4H₂O (Mm CO₂=44 g/mol). " +
            "Identifica el reactivo limitante y calcula la masa de CO₂ formada.",
            105.60, "g", "105,60 g", "masa_producto_g",
            "<strong>Paso 1 — Moles:</strong>\n\n" +
            "\\[n(\\text{C}_3\\text{H}_8) = \\frac{44}{44} = 1{,}00\\,\\text{mol}\\quad;" +
            "\\quad n(\\text{O}_2) = \\frac{128}{32} = 4{,}00\\,\\text{mol}\\]\n\n" +
            "<strong>Paso 2 — Cocientes mol/coeficiente:</strong>\n\n" +
            "\\[\\frac{n(\\text{C}_3\\text{H}_8)}{1} = 1{,}00 \\qquad " +
            "\\frac{n(\\text{O}_2)}{5} = 0{,}80\\]\n\n" +
            "<strong>O₂ → reactivo limitante</strong>.\n\n" +
            "<strong>Paso 3 — Moles de CO₂:</strong>\n\n" +
            "\\[n(\\text{CO}_2) = \\frac{4{,}00\\,\\text{mol O}_2}{5} \\times 3 = 2{,}40\\,\\text{mol}\\]\n\n" +
            "<strong>Paso 4 — Masa de CO₂:</strong>\n\n" +
            "\\[m(\\text{CO}_2) = 2{,}40\\,\\text{mol} \\times 44\\,\\text{g/mol} = 105{,}60\\,\\text{g}\\]\n\n" +
            "∴  Se forman <strong>105,60 g de CO₂</strong>. El C₃H₈ queda en exceso."
        ),

        // LR-6: 2Na + 2H₂O → 2NaOH + H₂
        // 46 g Na = 2 mol; 27 g H₂O = 1,5 mol
        // Cocientes: Na 2/2=1,00 ; H₂O 1,5/2=0,75 → H₂O limitante
        // NaOH = (1,5/2)×2 = 1,5 mol × 40 = 60,00 g
        new Scenario(
            "Se mezclan 46 g de Na (Mm=23 g/mol) con 27 g de H₂O (Mm=18 g/mol) " +
            "según: 2Na + 2H₂O → 2NaOH + H₂ (Mm NaOH=40 g/mol). " +
            "Identifica el reactivo limitante y calcula la masa de NaOH formada.",
            60.00, "g", "60,00 g", "masa_producto_g",
            "<strong>Paso 1 — Moles:</strong>\n\n" +
            "\\[n(\\text{Na}) = \\frac{46}{23} = 2{,}00\\,\\text{mol}\\quad;" +
            "\\quad n(\\text{H}_2\\text{O}) = \\frac{27}{18} = 1{,}50\\,\\text{mol}\\]\n\n" +
            "<strong>Paso 2 — Cocientes mol/coeficiente:</strong>\n\n" +
            "\\[\\frac{n(\\text{Na})}{2} = 1{,}00 \\qquad " +
            "\\frac{n(\\text{H}_2\\text{O})}{2} = 0{,}75\\]\n\n" +
            "<strong>H₂O → reactivo limitante</strong>.\n\n" +
            "<strong>Paso 3 — Moles de NaOH:</strong>\n\n" +
            "\\[n(\\text{NaOH}) = \\frac{1{,}50\\,\\text{mol H}_2\\text{O}}{2} \\times 2 = 1{,}50\\,\\text{mol}\\]\n\n" +
            "<strong>Paso 4 — Masa de NaOH:</strong>\n\n" +
            "\\[m(\\text{NaOH}) = 1{,}50\\,\\text{mol} \\times 40\\,\\text{g/mol} = 60{,}00\\,\\text{g}\\]\n\n" +
            "∴  Se forman <strong>60,00 g de NaOH</strong>. El Na queda en exceso."
        )
    );

    // =========================================================================
    // API pública
    // =========================================================================

    public FourthEsoChemicalChangesExercise generateAndSave() {
        FourthEsoChemicalChangesExercise ex = new FourthEsoChemicalChangesExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);

        // Distribución: 33 % MOL_AVOGADRO, 34 % ADVANCED_STOICH, 33 % LIMITING_REACTANT
        int roll = random.nextInt(9);
        if (roll < 3) {
            applyScenario(ex, FourthEsoChemicalChangesType.MOL_AVOGADRO_CONVERSION,
                AVOGADRO_SCENARIOS.get(random.nextInt(AVOGADRO_SCENARIOS.size())));
        } else if (roll < 6) {
            applyScenario(ex, FourthEsoChemicalChangesType.ADVANCED_STOICHIOMETRY,
                STOICH_SCENARIOS.get(random.nextInt(STOICH_SCENARIOS.size())));
        } else {
            applyScenario(ex, FourthEsoChemicalChangesType.LIMITING_REACTANT,
                LR_SCENARIOS.get(random.nextInt(LR_SCENARIOS.size())));
        }

        log.debug("4ESO BL3 generado: type={} unknown={}", ex.getChangesType(), ex.getUnknownVariable());
        return repository.save(ex);
    }

    public FourthEsoChemicalChangesExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ejercicio 4ESO BL3 no encontrado: " + id));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void applyScenario(FourthEsoChemicalChangesExercise ex,
                                FourthEsoChemicalChangesType type, Scenario sc) {
        ex.setChangesType(type);
        ex.setExerciseMode("NUMERICAL");
        ex.setStatement(sc.statement());
        ex.setCorrectAnswerValue(sc.correctAnswer());
        ex.setCorrectAnswerDisplay(sc.correctAnswerDisplay());
        ex.setAnswerUnit(sc.answerUnit());
        ex.setUnknownVariable(sc.unknownVariable());
        ex.setExplanation(sc.explanation());
        // Tolerancia más amplia para partículas (evita problemas de redondeo en 10²³)
        ex.setTolerancePercent("num_particulas".equals(sc.unknownVariable()) ? 1.0 : 2.0);
    }
}
