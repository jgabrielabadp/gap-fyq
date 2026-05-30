package com.gap.fyq.service;

import com.gap.fyq.model.fourtheso.energy.FourthEsoEnergyExercise;
import com.gap.fyq.model.fourtheso.energy.FourthEsoEnergyType;
import com.gap.fyq.repository.FourthEsoEnergyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class FourthEsoEnergyService {

    private final FourthEsoEnergyRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "4ESO";
    private static final String BLOCK  = "BL5";
    private static final double G = 9.8;

    private record Scenario(
        String statement,
        double correctAnswer,
        String answerUnit,
        String correctAnswerDisplay,
        String unknownVariable,
        String explanation
    ) {}

    // =========================================================================
    // MECHANICAL_ENERGY_LOSS — 10 escenarios
    // E_mec_final = E_mec_inicial − W_Fr
    // Todos los resultados son raíces de cuadrados perfectos → valores limpios.
    // =========================================================================

    private static final List<Scenario> ENERGY_LOSS_SCENARIOS = List.of(

        // ── Tipo A: KE → KE en plano horizontal (find v₂) ────────────────────
        // v₂ = √(v₁² − 2·W_Fr/m)

        // m=2, v₁=10, W_Fr=64 → v₂=√(100−64)=√36=6,00
        new Scenario(
            "Un bloque de 2 kg se mueve a 10 m/s sobre una superficie horizontal. " +
            "El rozamiento realiza un trabajo de 64 J sobre el bloque. " +
            "Calcula la velocidad final.",
            6.00, "m/s", "6,00 m/s", "velocidad_final",
            "Aplicamos el <strong>balance de energía mecánica con rozamiento</strong>:\n\n" +
            "\\[E_{mec,f} = E_{mec,i} - W_{Fr}\\]\n\n" +
            "El objeto se desplaza en horizontal (\\(E_p\\) constante):\n\n" +
            "\\[\\frac{1}{2}mv_2^2 = \\frac{1}{2}mv_1^2 - W_{Fr}\\]\n\n" +
            "\\[v_2 = \\sqrt{v_1^2 - \\frac{2\\,W_{Fr}}{m}} = " +
            "\\sqrt{10^2 - \\frac{2 \\times 64}{2}} = \\sqrt{100 - 64} = \\sqrt{36} = 6{,}00\\,\\text{m/s}\\]\n\n" +
            "∴  v₂ = <strong>6,00 m/s</strong>."
        ),

        // m=4, v₁=6, W_Fr=40 → v₂=√(36−20)=√16=4,00
        new Scenario(
            "Un objeto de 4 kg se desplaza a 6 m/s. El rozamiento realiza un trabajo " +
            "de 40 J. Calcula la velocidad final del objeto.",
            4.00, "m/s", "4,00 m/s", "velocidad_final",
            "\\[v_2 = \\sqrt{v_1^2 - \\frac{2\\,W_{Fr}}{m}} = " +
            "\\sqrt{6^2 - \\frac{2 \\times 40}{4}} = \\sqrt{36 - 20} = \\sqrt{16} = 4{,}00\\,\\text{m/s}\\]\n\n" +
            "∴  v₂ = <strong>4,00 m/s</strong>."
        ),

        // m=5, v₁=8, W_Fr=70 → v₂=√(64−28)=√36=6,00
        new Scenario(
            "Una caja de 5 kg se desliza a 8 m/s sobre una superficie con rozamiento. " +
            "El trabajo realizado por la fricción es de 70 J. Calcula la velocidad final.",
            6.00, "m/s", "6,00 m/s", "velocidad_final",
            "\\[v_2 = \\sqrt{v_1^2 - \\frac{2\\,W_{Fr}}{m}} = " +
            "\\sqrt{8^2 - \\frac{2 \\times 70}{5}} = \\sqrt{64 - 28} = \\sqrt{36} = 6{,}00\\,\\text{m/s}\\]\n\n" +
            "∴  v₂ = <strong>6,00 m/s</strong>."
        ),

        // m=10, v₁=6, W_Fr=100 → v₂=√(36−20)=√16=4,00
        new Scenario(
            "Un bloque de 10 kg se mueve a 6 m/s sobre una pista rugosa. " +
            "La fricción realiza un trabajo de 100 J. Calcula la velocidad final.",
            4.00, "m/s", "4,00 m/s", "velocidad_final",
            "\\[v_2 = \\sqrt{v_1^2 - \\frac{2\\,W_{Fr}}{m}} = " +
            "\\sqrt{6^2 - \\frac{2 \\times 100}{10}} = \\sqrt{36 - 20} = \\sqrt{16} = 4{,}00\\,\\text{m/s}\\]\n\n" +
            "∴  v₂ = <strong>4,00 m/s</strong>."
        ),

        // m=2, v₁=6, W_Fr=20 → v₂=√(36−20)=√16=4,00
        new Scenario(
            "Un objeto de 2 kg se desplaza a 6 m/s. El rozamiento hace un trabajo de 20 J. " +
            "Calcula la velocidad final.",
            4.00, "m/s", "4,00 m/s", "velocidad_final",
            "\\[v_2 = \\sqrt{6^2 - \\frac{2 \\times 20}{2}} = \\sqrt{36 - 20} = \\sqrt{16} = 4{,}00\\,\\text{m/s}\\]\n\n" +
            "∴  v₂ = <strong>4,00 m/s</strong>."
        ),

        // ── Tipo B: KE → PE (find h_max) ──────────────────────────────────────
        // h = (½mv₁² − W_Fr) / (mg)

        // m=10, v₁=10, W_Fr=10 → h=(500−10)/98=490/98=5,00
        new Scenario(
            "Un objeto de 10 kg se lanza hacia arriba a 10 m/s por un plano inclinado. " +
            "El rozamiento realiza un trabajo de 10 J. Calcula la altura máxima alcanzada. " +
            "(g = 9,8 m/s²)",
            5.00, "m", "5,00 m", "altura_max",
            "El objeto parte con Ec y sube hasta detenerse a altura h (Ep). " +
            "Balance con rozamiento:\n\n" +
            "\\[mgh = \\frac{1}{2}mv_1^2 - W_{Fr}\\]\n\n" +
            "\\[h = \\frac{\\frac{1}{2}mv_1^2 - W_{Fr}}{mg} = " +
            "\\frac{\\frac{1}{2} \\times 10 \\times 10^2 - 10}{10 \\times 9{,}8} = " +
            "\\frac{500 - 10}{98} = \\frac{490}{98} = 5{,}00\\,\\text{m}\\]\n\n" +
            "∴  h_max = <strong>5,00 m</strong>."
        ),

        // m=10, v₁=10, W_Fr=206 → h=(500−206)/98=294/98=3,00
        new Scenario(
            "Un objeto de 10 kg se lanza hacia arriba a 10 m/s. " +
            "El rozamiento realiza un trabajo de 206 J durante el ascenso. " +
            "Calcula la altura máxima. (g = 9,8 m/s²)",
            3.00, "m", "3,00 m", "altura_max",
            "\\[h = \\frac{\\frac{1}{2}mv_1^2 - W_{Fr}}{mg} = " +
            "\\frac{500 - 206}{10 \\times 9{,}8} = \\frac{294}{98} = 3{,}00\\,\\text{m}\\]\n\n" +
            "∴  h_max = <strong>3,00 m</strong>."
        ),

        // ── Tipo C: PE → KE (slide from height, find v₂) ─────────────────────
        // v₂ = √(2(mgh − W_Fr)/m)

        // m=2, h=5, W_Fr=34 → mgh=98, 98−34=64, v₂=√(2×64/2)=√64=8,00
        new Scenario(
            "Un objeto de 2 kg se desliza desde una altura de 5 m. " +
            "El rozamiento realiza un trabajo de 34 J durante el descenso. " +
            "Calcula la velocidad al llegar al suelo. (g = 9,8 m/s²)",
            8.00, "m/s", "8,00 m/s", "velocidad_final",
            "El objeto parte en reposo a h = 5 m (Ep) y llega al suelo con velocidad v₂ (Ec):\n\n" +
            "\\[\\frac{1}{2}mv_2^2 = mgh - W_{Fr}\\]\n\n" +
            "\\[v_2 = \\sqrt{\\frac{2(mgh - W_{Fr})}{m}} = " +
            "\\sqrt{\\frac{2(2 \\times 9{,}8 \\times 5 - 34)}{2}} = " +
            "\\sqrt{\\frac{2 \\times (98 - 34)}{2}} = \\sqrt{64} = 8{,}00\\,\\text{m/s}\\]\n\n" +
            "∴  v₂ = <strong>8,00 m/s</strong>."
        ),

        // m=2, h=5, W_Fr=82 → 98−82=16, v₂=√(2×16/2)=√16=4,00
        new Scenario(
            "Un objeto de 2 kg se desliza desde una altura de 5 m. " +
            "El rozamiento realiza un trabajo de 82 J durante el descenso. " +
            "Calcula la velocidad al llegar al suelo. (g = 9,8 m/s²)",
            4.00, "m/s", "4,00 m/s", "velocidad_final",
            "\\[v_2 = \\sqrt{\\frac{2(mgh - W_{Fr})}{m}} = " +
            "\\sqrt{\\frac{2(98 - 82)}{2}} = \\sqrt{\\frac{32}{2}} = \\sqrt{16} = 4{,}00\\,\\text{m/s}\\]\n\n" +
            "∴  v₂ = <strong>4,00 m/s</strong>."
        ),

        // m=5, h=4, W_Fr=106 → mgh=196, 196−106=90, v₂=√(2×90/5)=√36=6,00
        new Scenario(
            "Un bloque de 5 kg se desliza desde una altura de 4 m. " +
            "El rozamiento realiza un trabajo de 106 J. " +
            "Calcula la velocidad al llegar al suelo. (g = 9,8 m/s²)",
            6.00, "m/s", "6,00 m/s", "velocidad_final",
            "\\[v_2 = \\sqrt{\\frac{2(mgh - W_{Fr})}{m}} = " +
            "\\sqrt{\\frac{2(5 \\times 9{,}8 \\times 4 - 106)}{5}} = " +
            "\\sqrt{\\frac{2 \\times (196 - 106)}{5}} = \\sqrt{\\frac{180}{5}} = \\sqrt{36} = 6{,}00\\,\\text{m/s}\\]\n\n" +
            "∴  v₂ = <strong>6,00 m/s</strong>."
        )
    );

    // =========================================================================
    // THERMOCHEMISTRY_CALC — 12 escenarios
    // Q = n × |ΔH|   con   n = m / Mm
    // Combustiones exotérmicas estándar de 4º ESO (ΔH en kJ/mol de reactivo).
    // =========================================================================

    private static final List<Scenario> THERMO_SCENARIOS = List.of(

        // ── Metano CH₄ (Mm=16 g/mol, |ΔH_comb|=890 kJ/mol) ──────────────────

        new Scenario(
            "Al quemar 16 g de metano (CH₄, Mm = 16 g/mol) según la reacción " +
            "CH₄ + 2O₂ → CO₂ + 2H₂O con ΔH = −890 kJ/mol, ¿cuántos kJ se liberan?",
            890.00, "kJ", "890,00 kJ", "calor_kJ",
            "<strong>Paso 1</strong> — Moles de CH₄:\n\n" +
            "\\[n = \\frac{m}{M_m} = \\frac{16\\,\\text{g}}{16\\,\\text{g/mol}} = 1{,}00\\,\\text{mol}\\]\n\n" +
            "<strong>Paso 2</strong> — Calor liberado (reacción exotérmica: ΔH < 0):\n\n" +
            "\\[Q = n \\times |\\Delta H| = 1{,}00\\,\\text{mol} \\times 890\\,\\text{kJ/mol} = 890{,}00\\,\\text{kJ}\\]\n\n" +
            "∴  Se liberan <strong>890,00 kJ</strong>."
        ),

        new Scenario(
            "Al quemar 32 g de metano (CH₄, Mm = 16 g/mol) según " +
            "CH₄ + 2O₂ → CO₂ + 2H₂O con ΔH = −890 kJ/mol, ¿cuántos kJ se liberan?",
            1780.00, "kJ", "1780,00 kJ", "calor_kJ",
            "<strong>Paso 1</strong> — \\(n = 32/16 = 2{,}00\\,\\text{mol}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(Q = 2{,}00 \\times 890 = 1780{,}00\\,\\text{kJ}\\)\n\n" +
            "∴  Se liberan <strong>1780,00 kJ</strong>."
        ),

        new Scenario(
            "Al quemar 8 g de metano (CH₄, Mm = 16 g/mol) según " +
            "CH₄ + 2O₂ → CO₂ + 2H₂O con ΔH = −890 kJ/mol, ¿cuántos kJ se liberan?",
            445.00, "kJ", "445,00 kJ", "calor_kJ",
            "<strong>Paso 1</strong> — \\(n = 8/16 = 0{,}50\\,\\text{mol}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(Q = 0{,}50 \\times 890 = 445{,}00\\,\\text{kJ}\\)\n\n" +
            "∴  Se liberan <strong>445,00 kJ</strong>."
        ),

        // ── Hidrógeno H₂ (Mm=2 g/mol, |ΔH_comb|=286 kJ/mol) ─────────────────

        new Scenario(
            "Al quemar 2 g de hidrógeno (H₂, Mm = 2 g/mol) según " +
            "H₂ + ½O₂ → H₂O con ΔH = −286 kJ/mol, ¿cuántos kJ se liberan?",
            286.00, "kJ", "286,00 kJ", "calor_kJ",
            "<strong>Paso 1</strong> — \\(n = 2/2 = 1{,}00\\,\\text{mol}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(Q = 1{,}00 \\times 286 = 286{,}00\\,\\text{kJ}\\)\n\n" +
            "∴  Se liberan <strong>286,00 kJ</strong>."
        ),

        new Scenario(
            "Al quemar 4 g de hidrógeno (H₂, Mm = 2 g/mol) según " +
            "H₂ + ½O₂ → H₂O con ΔH = −286 kJ/mol, ¿cuántos kJ se liberan?",
            572.00, "kJ", "572,00 kJ", "calor_kJ",
            "<strong>Paso 1</strong> — \\(n = 4/2 = 2{,}00\\,\\text{mol}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(Q = 2{,}00 \\times 286 = 572{,}00\\,\\text{kJ}\\)\n\n" +
            "∴  Se liberan <strong>572,00 kJ</strong>."
        ),

        // ── Propano C₃H₈ (Mm=44 g/mol, |ΔH_comb|=2220 kJ/mol) ───────────────

        new Scenario(
            "Al quemar 44 g de propano (C₃H₈, Mm = 44 g/mol) según " +
            "C₃H₈ + 5O₂ → 3CO₂ + 4H₂O con ΔH = −2220 kJ/mol, ¿cuántos kJ se liberan?",
            2220.00, "kJ", "2220,00 kJ", "calor_kJ",
            "<strong>Paso 1</strong> — \\(n = 44/44 = 1{,}00\\,\\text{mol}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(Q = 1{,}00 \\times 2220 = 2220{,}00\\,\\text{kJ}\\)\n\n" +
            "∴  Se liberan <strong>2220,00 kJ</strong>."
        ),

        new Scenario(
            "Al quemar 22 g de propano (C₃H₈, Mm = 44 g/mol) según " +
            "C₃H₈ + 5O₂ → 3CO₂ + 4H₂O con ΔH = −2220 kJ/mol, ¿cuántos kJ se liberan?",
            1110.00, "kJ", "1110,00 kJ", "calor_kJ",
            "<strong>Paso 1</strong> — \\(n = 22/44 = 0{,}50\\,\\text{mol}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(Q = 0{,}50 \\times 2220 = 1110{,}00\\,\\text{kJ}\\)\n\n" +
            "∴  Se liberan <strong>1110,00 kJ</strong>."
        ),

        // ── Etanol C₂H₅OH (Mm=46 g/mol, |ΔH_comb|=1370 kJ/mol) ──────────────

        new Scenario(
            "Al quemar 46 g de etanol (C₂H₅OH, Mm = 46 g/mol) según " +
            "C₂H₅OH + 3O₂ → 2CO₂ + 3H₂O con ΔH = −1370 kJ/mol, ¿cuántos kJ se liberan?",
            1370.00, "kJ", "1370,00 kJ", "calor_kJ",
            "<strong>Paso 1</strong> — \\(M_m(\\text{C}_2\\text{H}_5\\text{OH}) = 2 \\times 12 + 6 \\times 1 + 16 = 46\\,\\text{g/mol}\\)\n\n" +
            "\\[n = \\frac{46}{46} = 1{,}00\\,\\text{mol}\\]\n\n" +
            "<strong>Paso 2</strong> — \\(Q = 1{,}00 \\times 1370 = 1370{,}00\\,\\text{kJ}\\)\n\n" +
            "∴  Se liberan <strong>1370,00 kJ</strong>."
        ),

        new Scenario(
            "Al quemar 23 g de etanol (C₂H₅OH, Mm = 46 g/mol) según " +
            "C₂H₅OH + 3O₂ → 2CO₂ + 3H₂O con ΔH = −1370 kJ/mol, ¿cuántos kJ se liberan?",
            685.00, "kJ", "685,00 kJ", "calor_kJ",
            "<strong>Paso 1</strong> — \\(n = 23/46 = 0{,}50\\,\\text{mol}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(Q = 0{,}50 \\times 1370 = 685{,}00\\,\\text{kJ}\\)\n\n" +
            "∴  Se liberan <strong>685,00 kJ</strong>."
        ),

        // ── Carbono grafito C (Mm=12 g/mol, |ΔH_comb|=393,5 kJ/mol) ──────────

        new Scenario(
            "Al quemar 12 g de carbono grafito (C, Mm = 12 g/mol) según " +
            "C + O₂ → CO₂ con ΔH = −393,5 kJ/mol, ¿cuántos kJ se liberan?",
            393.50, "kJ", "393,50 kJ", "calor_kJ",
            "<strong>Paso 1</strong> — \\(n = 12/12 = 1{,}00\\,\\text{mol}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(Q = 1{,}00 \\times 393{,}5 = 393{,}50\\,\\text{kJ}\\)\n\n" +
            "∴  Se liberan <strong>393,50 kJ</strong>."
        ),

        new Scenario(
            "Al quemar 24 g de carbono grafito (C, Mm = 12 g/mol) según " +
            "C + O₂ → CO₂ con ΔH = −393,5 kJ/mol, ¿cuántos kJ se liberan?",
            787.00, "kJ", "787,00 kJ", "calor_kJ",
            "<strong>Paso 1</strong> — \\(n = 24/12 = 2{,}00\\,\\text{mol}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(Q = 2{,}00 \\times 393{,}5 = 787{,}00\\,\\text{kJ}\\)\n\n" +
            "∴  Se liberan <strong>787,00 kJ</strong>."
        ),

        // ── CO (Mm=28 g/mol, |ΔH_comb|=283 kJ/mol) ───────────────────────────

        new Scenario(
            "Al quemar 28 g de monóxido de carbono (CO, Mm = 28 g/mol) según " +
            "2CO + O₂ → 2CO₂ con ΔH = −283 kJ/mol de CO, ¿cuántos kJ se liberan?",
            283.00, "kJ", "283,00 kJ", "calor_kJ",
            "<strong>Paso 1</strong> — \\(n = 28/28 = 1{,}00\\,\\text{mol}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(Q = 1{,}00 \\times 283 = 283{,}00\\,\\text{kJ}\\)\n\n" +
            "∴  Se liberan <strong>283,00 kJ</strong>."
        )
    );

    // =========================================================================
    // WAVE_PROPERTIES — 12 escenarios
    // Ecuación de onda: v = λ · f
    // Sonido: v_sonido = 340 m/s
    // Luz:    c = 3 × 10⁸ m/s   (λ en nm, convertir a m = nm × 10⁻⁹)
    // =========================================================================

    private static final List<Scenario> WAVE_SCENARIOS = List.of(

        // ── Sonido: hallar λ (v=340 m/s) ─────────────────────────────────────

        new Scenario(
            "El sonido viaja a 340 m/s en el aire. Una onda sonora tiene una frecuencia " +
            "de 100 Hz. Calcula su longitud de onda en metros.",
            3.40, "m", "3,40 m", "longitud_onda_m",
            "Despejamos λ de la ecuación de onda \\(v = \\lambda \\cdot f\\):\n\n" +
            "\\[\\lambda = \\frac{v}{f} = \\frac{340\\,\\text{m/s}}{100\\,\\text{Hz}} = 3{,}40\\,\\text{m}\\]\n\n" +
            "∴  λ = <strong>3,40 m</strong>."
        ),

        new Scenario(
            "El sonido viaja a 340 m/s en el aire. Una onda sonora tiene una frecuencia " +
            "de 340 Hz. Calcula su longitud de onda en metros.",
            1.00, "m", "1,00 m", "longitud_onda_m",
            "\\[\\lambda = \\frac{v}{f} = \\frac{340}{340} = 1{,}00\\,\\text{m}\\]\n\n" +
            "∴  λ = <strong>1,00 m</strong>."
        ),

        new Scenario(
            "El sonido viaja a 340 m/s en el aire. Una onda sonora tiene una frecuencia " +
            "de 680 Hz. Calcula su longitud de onda en metros.",
            0.50, "m", "0,50 m", "longitud_onda_m",
            "\\[\\lambda = \\frac{340}{680} = 0{,}50\\,\\text{m}\\]\n\n" +
            "∴  λ = <strong>0,50 m</strong>."
        ),

        new Scenario(
            "El sonido viaja a 340 m/s en el aire. Una onda sonora tiene una frecuencia " +
            "de 1700 Hz. Calcula su longitud de onda en metros.",
            0.20, "m", "0,20 m", "longitud_onda_m",
            "\\[\\lambda = \\frac{340}{1700} = 0{,}20\\,\\text{m}\\]\n\n" +
            "∴  λ = <strong>0,20 m</strong>."
        ),

        // ── Sonido: hallar f ──────────────────────────────────────────────────

        new Scenario(
            "El sonido viaja a 340 m/s en el aire. Una onda sonora tiene una longitud " +
            "de onda de 2 m. Calcula su frecuencia en Hz.",
            170.00, "Hz", "170,00 Hz", "frecuencia_sonido",
            "Despejamos f de \\(v = \\lambda \\cdot f\\):\n\n" +
            "\\[f = \\frac{v}{\\lambda} = \\frac{340\\,\\text{m/s}}{2\\,\\text{m}} = 170{,}00\\,\\text{Hz}\\]\n\n" +
            "∴  f = <strong>170,00 Hz</strong>."
        ),

        new Scenario(
            "El sonido viaja a 340 m/s en el aire. Una onda sonora tiene una longitud " +
            "de onda de 0,4 m. Calcula su frecuencia en Hz.",
            850.00, "Hz", "850,00 Hz", "frecuencia_sonido",
            "\\[f = \\frac{v}{\\lambda} = \\frac{340}{0{,}4} = 850{,}00\\,\\text{Hz}\\]\n\n" +
            "∴  f = <strong>850,00 Hz</strong>."
        ),

        // ── Luz: hallar f (c=3×10⁸ m/s) — respuesta en notación científica ────

        // λ=500 nm → f = 3×10⁸ / 5×10⁻⁷ = 6×10¹⁴ Hz
        new Scenario(
            "Una onda de luz visible tiene λ = 500 nm. " +
            "Calcula su frecuencia en Hz. (c = 3 × 10⁸ m/s)",
            6e14, "Hz", "6,00 × 10¹⁴ Hz", "frecuencia_luz",
            "Convertimos la longitud de onda a metros:\n\n" +
            "\\[\\lambda = 500\\,\\text{nm} = 500 \\times 10^{-9}\\,\\text{m} = 5{,}00 \\times 10^{-7}\\,\\text{m}\\]\n\n" +
            "Despejamos la frecuencia:\n\n" +
            "\\[f = \\frac{c}{\\lambda} = \\frac{3 \\times 10^8\\,\\text{m/s}}{5{,}00 \\times 10^{-7}\\,\\text{m}} " +
            "= 6{,}00 \\times 10^{14}\\,\\text{Hz}\\]\n\n" +
            "∴  f = <strong>6,00 × 10¹⁴ Hz</strong>."
        ),

        // λ=600 nm → f = 3×10⁸ / 6×10⁻⁷ = 5×10¹⁴ Hz
        new Scenario(
            "Una onda de luz visible tiene λ = 600 nm. " +
            "Calcula su frecuencia en Hz. (c = 3 × 10⁸ m/s)",
            5e14, "Hz", "5,00 × 10¹⁴ Hz", "frecuencia_luz",
            "\\[\\lambda = 600\\,\\text{nm} = 6{,}00 \\times 10^{-7}\\,\\text{m}\\]\n\n" +
            "\\[f = \\frac{3 \\times 10^8}{6{,}00 \\times 10^{-7}} = 5{,}00 \\times 10^{14}\\,\\text{Hz}\\]\n\n" +
            "∴  f = <strong>5,00 × 10¹⁴ Hz</strong>."
        ),

        // λ=750 nm → f = 3×10⁸ / 7.5×10⁻⁷ = 4×10¹⁴ Hz
        new Scenario(
            "Una onda de luz infrarroja tiene λ = 750 nm. " +
            "Calcula su frecuencia en Hz. (c = 3 × 10⁸ m/s)",
            4e14, "Hz", "4,00 × 10¹⁴ Hz", "frecuencia_luz",
            "\\[\\lambda = 750\\,\\text{nm} = 7{,}50 \\times 10^{-7}\\,\\text{m}\\]\n\n" +
            "\\[f = \\frac{3 \\times 10^8}{7{,}50 \\times 10^{-7}} = 4{,}00 \\times 10^{14}\\,\\text{Hz}\\]\n\n" +
            "∴  f = <strong>4,00 × 10¹⁴ Hz</strong>."
        ),

        // ── Luz: hallar λ en nm ───────────────────────────────────────────────

        // f=6×10¹⁴ Hz → λ = 3×10⁸ / 6×10¹⁴ = 5×10⁻⁷ m = 500 nm
        new Scenario(
            "Una onda de luz tiene una frecuencia de 6 × 10¹⁴ Hz. " +
            "Calcula su longitud de onda en nanómetros (nm). (c = 3 × 10⁸ m/s)",
            500.00, "nm", "500,00 nm", "longitud_onda_nm",
            "\\[\\lambda = \\frac{c}{f} = \\frac{3 \\times 10^8\\,\\text{m/s}}{6 \\times 10^{14}\\,\\text{Hz}} " +
            "= 5{,}00 \\times 10^{-7}\\,\\text{m}\\]\n\n" +
            "Convertimos a nanómetros:\n\n" +
            "\\[\\lambda = 5{,}00 \\times 10^{-7}\\,\\text{m} \\times \\frac{10^9\\,\\text{nm}}{1\\,\\text{m}} " +
            "= 500{,}00\\,\\text{nm}\\]\n\n" +
            "∴  λ = <strong>500,00 nm</strong> (luz visible, color verde-amarillo)."
        ),

        // f=5×10¹⁴ Hz → λ = 600 nm
        new Scenario(
            "Una onda de luz tiene una frecuencia de 5 × 10¹⁴ Hz. " +
            "Calcula su longitud de onda en nanómetros. (c = 3 × 10⁸ m/s)",
            600.00, "nm", "600,00 nm", "longitud_onda_nm",
            "\\[\\lambda = \\frac{3 \\times 10^8}{5 \\times 10^{14}} = 6{,}00 \\times 10^{-7}\\,\\text{m} " +
            "= 600{,}00\\,\\text{nm}\\]\n\n" +
            "∴  λ = <strong>600,00 nm</strong> (luz visible, color naranja-rojo)."
        ),

        // f=4×10¹⁴ Hz → λ = 750 nm
        new Scenario(
            "Una onda de luz tiene una frecuencia de 4 × 10¹⁴ Hz. " +
            "Calcula su longitud de onda en nanómetros. (c = 3 × 10⁸ m/s)",
            750.00, "nm", "750,00 nm", "longitud_onda_nm",
            "\\[\\lambda = \\frac{3 \\times 10^8}{4 \\times 10^{14}} = 7{,}50 \\times 10^{-7}\\,\\text{m} " +
            "= 750{,}00\\,\\text{nm}\\]\n\n" +
            "∴  λ = <strong>750,00 nm</strong> (infrarrojo cercano / límite del rojo visible)."
        )
    );

    // =========================================================================
    // API pública
    // =========================================================================

    public FourthEsoEnergyExercise generateAndSave() {
        FourthEsoEnergyExercise ex = new FourthEsoEnergyExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);

        int roll = random.nextInt(9);
        if (roll < 3) {
            applyScenario(ex, FourthEsoEnergyType.MECHANICAL_ENERGY_LOSS,
                ENERGY_LOSS_SCENARIOS.get(random.nextInt(ENERGY_LOSS_SCENARIOS.size())));
        } else if (roll < 6) {
            applyScenario(ex, FourthEsoEnergyType.THERMOCHEMISTRY_CALC,
                THERMO_SCENARIOS.get(random.nextInt(THERMO_SCENARIOS.size())));
        } else {
            applyScenario(ex, FourthEsoEnergyType.WAVE_PROPERTIES,
                WAVE_SCENARIOS.get(random.nextInt(WAVE_SCENARIOS.size())));
        }

        log.debug("4ESO BL5 generado: type={} unknown={}", ex.getEnergyType(), ex.getUnknownVariable());
        return repository.save(ex);
    }

    public FourthEsoEnergyExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ejercicio 4ESO BL5 no encontrado: " + id));
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private void applyScenario(FourthEsoEnergyExercise ex,
                                FourthEsoEnergyType type, Scenario sc) {
        ex.setEnergyType(type);
        ex.setExerciseMode("NUMERICAL");
        ex.setStatement(sc.statement());
        ex.setCorrectAnswerValue(sc.correctAnswer());
        ex.setCorrectAnswerDisplay(sc.correctAnswerDisplay());
        ex.setAnswerUnit(sc.answerUnit());
        ex.setUnknownVariable(sc.unknownVariable());
        ex.setExplanation(sc.explanation());
        // Tolerancia estándar 2 %; para luz en Hz (grandes magnitudes), idem
        ex.setTolerancePercent(2.0);
    }
}
