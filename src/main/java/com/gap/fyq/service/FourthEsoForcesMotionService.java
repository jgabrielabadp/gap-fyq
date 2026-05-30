package com.gap.fyq.service;

import com.gap.fyq.model.fourtheso.forcesmotion.FourthEsoDynamicsType;
import com.gap.fyq.model.fourtheso.forcesmotion.FourthEsoForcesMotionExercise;
import com.gap.fyq.repository.FourthEsoForcesMotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class FourthEsoForcesMotionService {

    private final FourthEsoForcesMotionRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "4ESO";
    private static final String BLOCK  = "BL4";

    private static final double G = 9.8;   // m/s²
    private static final double PI = Math.PI;

    // =========================================================================
    // Registro genérico (igual que BL3)
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
    // VERTICAL_MOTION — 12 escenarios
    // g = 9,8 m/s²   |   Ecuaciones MRUV:
    //   caída libre:          h = ½·g·t²  →  t = √(2h/g)  ;  v = g·t  ;  v² = 2gh
    //   lanzamiento vertical: h_max = v₀²/(2g) ; t_subida = v₀/g ; t_vuelo = 2v₀/g
    // Todos los resultados verificados algebraicamente (raíces de cuadrados perfectos).
    // =========================================================================

    private static final List<Scenario> VERTICAL_SCENARIOS = List.of(

        // ── Caída libre: hallar tiempo ─────────────────────────────────────────

        // h=19,6 m → t = √(2×19,6/9,8) = √4 = 2,00 s
        new Scenario(
            "Un objeto cae libremente (v₀ = 0) desde una altura de 19,6 m. " +
            "¿Cuánto tarda en llegar al suelo? (g = 9,8 m/s²)",
            2.00, "s", "2,00 s", "tiempo_s",
            "En la <strong>caída libre</strong> con v₀ = 0, la posición varía según:\n\n" +
            "\\[h = \\frac{1}{2} \\cdot g \\cdot t^2\\]\n\n" +
            "Despejamos t:\n\n" +
            "\\[t = \\sqrt{\\frac{2h}{g}} = \\sqrt{\\frac{2 \\times 19{,}6}{9{,}8}} " +
            "= \\sqrt{\\frac{39{,}2}{9{,}8}} = \\sqrt{4} = 2{,}00\\,\\text{s}\\]\n\n" +
            "∴  t = <strong>2,00 s</strong>."
        ),

        // h=44,1 m → t = √(2×44,1/9,8) = √9 = 3,00 s
        new Scenario(
            "Un objeto cae libremente (v₀ = 0) desde una altura de 44,1 m. " +
            "¿Cuánto tarda en llegar al suelo? (g = 9,8 m/s²)",
            3.00, "s", "3,00 s", "tiempo_s",
            "\\[t = \\sqrt{\\frac{2h}{g}} = \\sqrt{\\frac{2 \\times 44{,}1}{9{,}8}} " +
            "= \\sqrt{\\frac{88{,}2}{9{,}8}} = \\sqrt{9} = 3{,}00\\,\\text{s}\\]\n\n" +
            "∴  t = <strong>3,00 s</strong>."
        ),

        // h=78,4 m → t = √(2×78,4/9,8) = √16 = 4,00 s
        new Scenario(
            "Un objeto cae libremente (v₀ = 0) desde una altura de 78,4 m. " +
            "¿Cuánto tarda en llegar al suelo? (g = 9,8 m/s²)",
            4.00, "s", "4,00 s", "tiempo_s",
            "\\[t = \\sqrt{\\frac{2h}{g}} = \\sqrt{\\frac{2 \\times 78{,}4}{9{,}8}} " +
            "= \\sqrt{\\frac{156{,}8}{9{,}8}} = \\sqrt{16} = 4{,}00\\,\\text{s}\\]\n\n" +
            "∴  t = <strong>4,00 s</strong>."
        ),

        // ── Caída libre: hallar velocidad de impacto ───────────────────────────

        // h=19,6 m → v = √(2·g·h) = √(384,16) = 19,60 m/s
        new Scenario(
            "Un objeto cae libremente desde 19,6 m de altura. " +
            "¿Con qué velocidad llega al suelo? (g = 9,8 m/s²)",
            Math.sqrt(2 * G * 19.6), "m/s", "19,60 m/s", "velocidad_ms",
            "Usamos la relación energética del MRUV (sin medir el tiempo):\n\n" +
            "\\[v^2 = 2 \\cdot g \\cdot h\\]\n\n" +
            "\\[v = \\sqrt{2 \\times 9{,}8 \\times 19{,}6} = \\sqrt{384{,}16} = 19{,}60\\,\\text{m/s}\\]\n\n" +
            "∴  v_impacto = <strong>19,60 m/s</strong>."
        ),

        // h=44,1 m → v = √(2·9,8·44,1) = √(864,36) = 29,40 m/s
        new Scenario(
            "Un objeto cae libremente desde 44,1 m de altura. " +
            "¿Con qué velocidad llega al suelo? (g = 9,8 m/s²)",
            Math.sqrt(2 * G * 44.1), "m/s", "29,40 m/s", "velocidad_ms",
            "\\[v = \\sqrt{2 \\cdot g \\cdot h} = \\sqrt{2 \\times 9{,}8 \\times 44{,}1} " +
            "= \\sqrt{864{,}36} = 29{,}40\\,\\text{m/s}\\]\n\n" +
            "∴  v_impacto = <strong>29,40 m/s</strong>."
        ),

        // ── Lanzamiento vertical: hallar altura máxima ─────────────────────────

        // v₀=9,8 m/s → h_max = 9,8²/(2·9,8) = 4,90 m
        new Scenario(
            "Se lanza un objeto verticalmente hacia arriba con v₀ = 9,8 m/s. " +
            "Calcula la altura máxima alcanzada. (g = 9,8 m/s²)",
            (9.8 * 9.8) / (2 * G), "m", "4,90 m", "altura_m",
            "En el punto más alto la velocidad es cero. Aplicamos la ecuación cinemática:\n\n" +
            "\\[v^2 = v_0^2 - 2 \\cdot g \\cdot h \\quad \\Rightarrow \\quad 0 = v_0^2 - 2g \\cdot h_{max}\\]\n\n" +
            "\\[h_{max} = \\frac{v_0^2}{2g} = \\frac{(9{,}8)^2}{2 \\times 9{,}8} = " +
            "\\frac{96{,}04}{19{,}6} = 4{,}90\\,\\text{m}\\]\n\n" +
            "∴  h_max = <strong>4,90 m</strong>."
        ),

        // v₀=19,6 m/s → h_max = 19,6²/(2·9,8) = 384,16/19,6 = 19,60 m
        new Scenario(
            "Se lanza un objeto verticalmente hacia arriba con v₀ = 19,6 m/s. " +
            "Calcula la altura máxima alcanzada. (g = 9,8 m/s²)",
            (19.6 * 19.6) / (2 * G), "m", "19,60 m", "altura_m",
            "\\[h_{max} = \\frac{v_0^2}{2g} = \\frac{(19{,}6)^2}{2 \\times 9{,}8} = " +
            "\\frac{384{,}16}{19{,}6} = 19{,}60\\,\\text{m}\\]\n\n" +
            "∴  h_max = <strong>19,60 m</strong>."
        ),

        // v₀=29,4 m/s → h_max = 29,4²/(2·9,8) = 864,36/19,6 = 44,10 m
        new Scenario(
            "Se lanza un objeto verticalmente hacia arriba con v₀ = 29,4 m/s. " +
            "Calcula la altura máxima alcanzada. (g = 9,8 m/s²)",
            (29.4 * 29.4) / (2 * G), "m", "44,10 m", "altura_m",
            "\\[h_{max} = \\frac{v_0^2}{2g} = \\frac{(29{,}4)^2}{2 \\times 9{,}8} = " +
            "\\frac{864{,}36}{19{,}6} = 44{,}10\\,\\text{m}\\]\n\n" +
            "∴  h_max = <strong>44,10 m</strong>."
        ),

        // ── Lanzamiento vertical: hallar tiempo de subida ──────────────────────

        // v₀=19,6 m/s → t_subida = 19,6/9,8 = 2,00 s
        new Scenario(
            "Se lanza un objeto verticalmente hacia arriba con v₀ = 19,6 m/s. " +
            "¿En cuánto tiempo alcanza la altura máxima? (g = 9,8 m/s²)",
            19.6 / G, "s", "2,00 s", "tiempo_s",
            "En el punto más alto v = 0. De \\(v = v_0 - g \\cdot t\\) despejamos t:\n\n" +
            "\\[t_{subida} = \\frac{v_0}{g} = \\frac{19{,}6}{9{,}8} = 2{,}00\\,\\text{s}\\]\n\n" +
            "∴  t_subida = <strong>2,00 s</strong>."
        ),

        // v₀=29,4 m/s → t_subida = 29,4/9,8 = 3,00 s
        new Scenario(
            "Se lanza un objeto verticalmente hacia arriba con v₀ = 29,4 m/s. " +
            "¿En cuánto tiempo alcanza la altura máxima? (g = 9,8 m/s²)",
            29.4 / G, "s", "3,00 s", "tiempo_s",
            "\\[t_{subida} = \\frac{v_0}{g} = \\frac{29{,}4}{9{,}8} = 3{,}00\\,\\text{s}\\]\n\n" +
            "∴  t_subida = <strong>3,00 s</strong>."
        ),

        // ── Lanzamiento vertical: hallar tiempo total de vuelo ─────────────────

        // v₀=19,6 m/s → t_vuelo = 2×19,6/9,8 = 4,00 s
        new Scenario(
            "Se lanza un objeto verticalmente hacia arriba con v₀ = 19,6 m/s. " +
            "¿Cuál es el tiempo total de vuelo hasta que regresa al punto de lanzamiento? " +
            "(g = 9,8 m/s²)",
            2 * 19.6 / G, "s", "4,00 s", "tiempo_s",
            "Por simetría, el tiempo de bajada es igual al de subida. " +
            "El tiempo total de vuelo es:\n\n" +
            "\\[t_{vuelo} = 2 \\cdot t_{subida} = \\frac{2 \\cdot v_0}{g} = " +
            "\\frac{2 \\times 19{,}6}{9{,}8} = 4{,}00\\,\\text{s}\\]\n\n" +
            "∴  t_vuelo = <strong>4,00 s</strong>."
        ),

        // v₀=29,4 m/s → t_vuelo = 2×29,4/9,8 = 6,00 s
        new Scenario(
            "Se lanza un objeto verticalmente hacia arriba con v₀ = 29,4 m/s. " +
            "¿Cuál es el tiempo total de vuelo hasta que regresa al punto de lanzamiento? " +
            "(g = 9,8 m/s²)",
            2 * 29.4 / G, "s", "6,00 s", "tiempo_s",
            "\\[t_{vuelo} = \\frac{2 \\cdot v_0}{g} = \\frac{2 \\times 29{,}4}{9{,}8} " +
            "= 6{,}00\\,\\text{s}\\]\n\n" +
            "∴  t_vuelo = <strong>6,00 s</strong>."
        )
    );

    // =========================================================================
    // CIRCULAR_MOTION — 12 escenarios
    // ω = 2π·n/60 (rad/s)   |   v = ω·r (m/s)   |   a_c = ω²·r (m/s²)
    // Resultados a 2 d.p.; la tolerancia del 2 % absorbe diferencias de redondeo de π.
    // =========================================================================

    private static final List<Scenario> CIRCULAR_SCENARIOS = List.of(

        // ── Hallar ω ──────────────────────────────────────────────────────────

        // 60 rpm, r=1 m → ω = 2π = 6,28 rad/s
        new Scenario(
            "Un punto de una rueda describe una trayectoria circular de radio r = 1 m. " +
            "La rueda gira a 60 rpm. Calcula la velocidad angular en rad/s.",
            2 * PI, "rad/s", "6,28 rad/s", "omega_rads",
            "Convertimos las rpm a velocidad angular:\n\n" +
            "\\[\\omega = \\frac{2\\pi \\cdot n}{60} = \\frac{2\\pi \\times 60}{60} " +
            "= 2\\pi \\approx 6{,}28\\,\\text{rad/s}\\]\n\n" +
            "∴  ω = <strong>6,28 rad/s</strong>."
        ),

        // 30 rpm, r=2 m → ω = π = 3,14 rad/s
        new Scenario(
            "Una rueda gira a 30 rpm. Calcula la velocidad angular en rad/s.",
            PI, "rad/s", "3,14 rad/s", "omega_rads",
            "\\[\\omega = \\frac{2\\pi \\times 30}{60} = \\pi \\approx 3{,}14\\,\\text{rad/s}\\]\n\n" +
            "∴  ω = <strong>3,14 rad/s</strong>."
        ),

        // 120 rpm, r=0,5 m → ω = 4π = 12,57 rad/s
        new Scenario(
            "Un motor gira a 120 rpm. Calcula la velocidad angular en rad/s.",
            4 * PI, "rad/s", "12,57 rad/s", "omega_rads",
            "\\[\\omega = \\frac{2\\pi \\times 120}{60} = 4\\pi \\approx 12{,}57\\,\\text{rad/s}\\]\n\n" +
            "∴  ω = <strong>12,57 rad/s</strong>."
        ),

        // 600 rpm, r=0,1 m → ω = 20π = 62,83 rad/s
        new Scenario(
            "Un ventilador gira a 600 rpm. Calcula la velocidad angular en rad/s.",
            20 * PI, "rad/s", "62,83 rad/s", "omega_rads",
            "\\[\\omega = \\frac{2\\pi \\times 600}{60} = 20\\pi \\approx 62{,}83\\,\\text{rad/s}\\]\n\n" +
            "∴  ω = <strong>62,83 rad/s</strong>."
        ),

        // ── Hallar v ──────────────────────────────────────────────────────────

        // 60 rpm, r=0,5 m → v = 2π×0,5 = π = 3,14 m/s
        new Scenario(
            "Una rueda gira a 60 rpm. Un punto de su llanta está a r = 0,5 m del eje. " +
            "Calcula la velocidad lineal en m/s.",
            PI, "m/s", "3,14 m/s", "velocidad_lineal",
            "<strong>Paso 1</strong> — Velocidad angular:\n\n" +
            "\\[\\omega = \\frac{2\\pi \\times 60}{60} = 2\\pi\\,\\text{rad/s}\\]\n\n" +
            "<strong>Paso 2</strong> — Velocidad lineal:\n\n" +
            "\\[v = \\omega \\cdot r = 2\\pi \\times 0{,}5 = \\pi \\approx 3{,}14\\,\\text{m/s}\\]\n\n" +
            "∴  v = <strong>3,14 m/s</strong>."
        ),

        // 60 rpm, r=2 m → v = 2π×2 = 4π = 12,57 m/s
        new Scenario(
            "Una rueda gira a 60 rpm. Un punto de su llanta está a r = 2 m del eje. " +
            "Calcula la velocidad lineal en m/s.",
            4 * PI, "m/s", "12,57 m/s", "velocidad_lineal",
            "<strong>Paso 1</strong> — \\(\\omega = 2\\pi\\,\\text{rad/s}\\)\n\n" +
            "<strong>Paso 2</strong> — Velocidad lineal:\n\n" +
            "\\[v = \\omega \\cdot r = 2\\pi \\times 2 = 4\\pi \\approx 12{,}57\\,\\text{m/s}\\]\n\n" +
            "∴  v = <strong>12,57 m/s</strong>."
        ),

        // 30 rpm, r=1 m → v = π×1 = π = 3,14 m/s
        new Scenario(
            "Una noria gira a 30 rpm. Un pasajero está a r = 1 m del eje. " +
            "Calcula su velocidad lineal en m/s.",
            PI, "m/s", "3,14 m/s", "velocidad_lineal",
            "<strong>Paso 1</strong> — \\(\\omega = \\frac{2\\pi \\times 30}{60} = \\pi\\,\\text{rad/s}\\)\n\n" +
            "<strong>Paso 2</strong> — Velocidad lineal:\n\n" +
            "\\[v = \\omega \\cdot r = \\pi \\times 1 = \\pi \\approx 3{,}14\\,\\text{m/s}\\]\n\n" +
            "∴  v = <strong>3,14 m/s</strong>."
        ),

        // 120 rpm, r=0,5 m → v = 4π×0,5 = 2π = 6,28 m/s
        new Scenario(
            "Un disco gira a 120 rpm. Un punto está a r = 0,5 m del eje. " +
            "Calcula la velocidad lineal en m/s.",
            2 * PI, "m/s", "6,28 m/s", "velocidad_lineal",
            "<strong>Paso 1</strong> — \\(\\omega = 4\\pi\\,\\text{rad/s}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(v = 4\\pi \\times 0{,}5 = 2\\pi \\approx 6{,}28\\,\\text{m/s}\\)\n\n" +
            "∴  v = <strong>6,28 m/s</strong>."
        ),

        // ── Hallar a_c ────────────────────────────────────────────────────────

        // 60 rpm, r=1 m → a_c = (2π)²×1 = 4π² = 39,48 m/s²
        new Scenario(
            "Una rueda gira a 60 rpm. Un punto de su llanta está a r = 1 m del eje. " +
            "Calcula la aceleración centrípeta en m/s².",
            4 * PI * PI, "m/s²", "39,48 m/s²", "aceleracion_centripeta",
            "<strong>Paso 1</strong> — \\(\\omega = 2\\pi\\,\\text{rad/s}\\)\n\n" +
            "<strong>Paso 2</strong> — Aceleración centrípeta:\n\n" +
            "\\[a_c = \\omega^2 \\cdot r = (2\\pi)^2 \\times 1 = 4\\pi^2 \\approx 39{,}48\\,\\text{m/s}^2\\]\n\n" +
            "∴  a_c = <strong>39,48 m/s²</strong>."
        ),

        // 30 rpm, r=2 m → a_c = π²×2 = 2π² = 19,74 m/s²
        new Scenario(
            "Una rueda gira a 30 rpm. Un punto de su llanta está a r = 2 m del eje. " +
            "Calcula la aceleración centrípeta en m/s².",
            2 * PI * PI, "m/s²", "19,74 m/s²", "aceleracion_centripeta",
            "<strong>Paso 1</strong> — \\(\\omega = \\pi\\,\\text{rad/s}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(a_c = \\pi^2 \\times 2 = 2\\pi^2 \\approx 19{,}74\\,\\text{m/s}^2\\)\n\n" +
            "∴  a_c = <strong>19,74 m/s²</strong>."
        ),

        // 120 rpm, r=0,5 m → a_c = (4π)²×0,5 = 8π² = 78,96 m/s²
        new Scenario(
            "Un motor gira a 120 rpm. Un punto está a r = 0,5 m del eje. " +
            "Calcula la aceleración centrípeta en m/s².",
            8 * PI * PI, "m/s²", "78,96 m/s²", "aceleracion_centripeta",
            "<strong>Paso 1</strong> — \\(\\omega = 4\\pi\\,\\text{rad/s}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(a_c = (4\\pi)^2 \\times 0{,}5 = 16\\pi^2 \\times 0{,}5 " +
            "= 8\\pi^2 \\approx 78{,}96\\,\\text{m/s}^2\\)\n\n" +
            "∴  a_c = <strong>78,96 m/s²</strong>."
        ),

        // 60 rpm, r=0,25 m → a_c = (2π)²×0,25 = π² = 9,87 m/s²
        new Scenario(
            "Una rueda gira a 60 rpm. Un punto está a r = 0,25 m del eje. " +
            "Calcula la aceleración centrípeta en m/s².",
            PI * PI, "m/s²", "9,87 m/s²", "aceleracion_centripeta",
            "<strong>Paso 1</strong> — \\(\\omega = 2\\pi\\,\\text{rad/s}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(a_c = (2\\pi)^2 \\times 0{,}25 = 4\\pi^2 \\times 0{,}25 " +
            "= \\pi^2 \\approx 9{,}87\\,\\text{m/s}^2\\)\n\n" +
            "∴  a_c = <strong>9,87 m/s²</strong>."
        )
    );

    // =========================================================================
    // FRICTION_DYNAMICS — 11 escenarios (5 Fr + 6 aceleración)
    // Plano horizontal: N = m·g   |   F_r = μ·N = μ·m·g
    // 2ª Ley de Newton: ΣF = F_tractor − F_r = m·a
    // g = 9,8 m/s²; μ entre 0,1 y 0,5 (valores físicamente realistas).
    // Todos los resultados verificados a 2 d.p.
    // =========================================================================

    private static final List<Scenario> FRICTION_SCENARIOS = List.of(

        // ── Hallar F_r = μ·m·g ────────────────────────────────────────────────

        // m=10, μ=0,3 → Fr = 0,3×10×9,8 = 29,40 N
        new Scenario(
            "Un bloque de 10 kg reposa sobre una superficie horizontal. " +
            "El coeficiente de rozamiento cinético es μ = 0,3. " +
            "Calcula la fuerza de rozamiento que actúa sobre el bloque. (g = 9,8 m/s²)",
            29.40, "N", "29,40 N", "fuerza_rozamiento",
            "En un plano horizontal, la fuerza normal es \\(N = m \\cdot g\\). " +
            "La <strong>fuerza de rozamiento cinético</strong> es:\n\n" +
            "\\[F_r = \\mu \\cdot N = \\mu \\cdot m \\cdot g\\]\n\n" +
            "\\[F_r = 0{,}3 \\times 10\\,\\text{kg} \\times 9{,}8\\,\\text{m/s}^2 " +
            "= 29{,}40\\,\\text{N}\\]\n\n" +
            "∴  F_r = <strong>29,40 N</strong>."
        ),

        // m=20, μ=0,2 → Fr = 0,2×20×9,8 = 39,20 N
        new Scenario(
            "Un cajón de 20 kg se arrastra sobre una superficie con μ = 0,2. " +
            "Calcula la fuerza de rozamiento. (g = 9,8 m/s²)",
            39.20, "N", "39,20 N", "fuerza_rozamiento",
            "\\[F_r = \\mu \\cdot m \\cdot g = 0{,}2 \\times 20 \\times 9{,}8 " +
            "= 39{,}20\\,\\text{N}\\]\n\n" +
            "∴  F_r = <strong>39,20 N</strong>."
        ),

        // m=5, μ=0,5 → Fr = 0,5×5×9,8 = 24,50 N
        new Scenario(
            "Una caja de 5 kg se desliza sobre una superficie rugosa con μ = 0,5. " +
            "Calcula la fuerza de rozamiento. (g = 9,8 m/s²)",
            24.50, "N", "24,50 N", "fuerza_rozamiento",
            "\\[F_r = \\mu \\cdot m \\cdot g = 0{,}5 \\times 5 \\times 9{,}8 " +
            "= 24{,}50\\,\\text{N}\\]\n\n" +
            "∴  F_r = <strong>24,50 N</strong>."
        ),

        // m=50, μ=0,4 → Fr = 0,4×50×9,8 = 196,00 N
        new Scenario(
            "Un objeto de 50 kg se desplaza sobre una superficie con μ = 0,4. " +
            "Calcula la fuerza de rozamiento. (g = 9,8 m/s²)",
            196.00, "N", "196,00 N", "fuerza_rozamiento",
            "\\[F_r = \\mu \\cdot m \\cdot g = 0{,}4 \\times 50 \\times 9{,}8 " +
            "= 196{,}00\\,\\text{N}\\]\n\n" +
            "∴  F_r = <strong>196,00 N</strong>."
        ),

        // m=100, μ=0,1 → Fr = 0,1×100×9,8 = 98,00 N
        new Scenario(
            "Un bloque de 100 kg se arrastra sobre asfalto con μ = 0,1. " +
            "Calcula la fuerza de rozamiento. (g = 9,8 m/s²)",
            98.00, "N", "98,00 N", "fuerza_rozamiento",
            "\\[F_r = \\mu \\cdot m \\cdot g = 0{,}1 \\times 100 \\times 9{,}8 " +
            "= 98{,}00\\,\\text{N}\\]\n\n" +
            "∴  F_r = <strong>98,00 N</strong>."
        ),

        // ── Hallar aceleración: a = (F_tractor − F_r) / m ────────────────────

        // m=10, F=50 N, μ=0,3 → Fr=29,40 → ΣF=20,60 → a=2,06 m/s²
        new Scenario(
            "Un bloque de 10 kg sobre una superficie horizontal (μ = 0,3) es empujado " +
            "con una fuerza horizontal de 50 N. Calcula la aceleración. (g = 9,8 m/s²)",
            2.06, "m/s²", "2,06 m/s²", "aceleracion_ms2",
            "<strong>Paso 1</strong> — Fuerza de rozamiento:\n\n" +
            "\\[F_r = \\mu \\cdot m \\cdot g = 0{,}3 \\times 10 \\times 9{,}8 = 29{,}40\\,\\text{N}\\]\n\n" +
            "<strong>Paso 2</strong> — 2ª Ley de Newton (eje horizontal):\n\n" +
            "\\[\\sum F = F_{tractor} - F_r = 50 - 29{,}40 = 20{,}60\\,\\text{N}\\]\n\n" +
            "<strong>Paso 3</strong> — Aceleración:\n\n" +
            "\\[a = \\frac{\\sum F}{m} = \\frac{20{,}60}{10} = 2{,}06\\,\\text{m/s}^2\\]\n\n" +
            "∴  a = <strong>2,06 m/s²</strong>."
        ),

        // m=20, F=100 N, μ=0,2 → Fr=39,20 → ΣF=60,80 → a=3,04 m/s²
        new Scenario(
            "Un cajón de 20 kg sobre una superficie horizontal (μ = 0,2) es arrastrado " +
            "con una fuerza de 100 N. Calcula la aceleración. (g = 9,8 m/s²)",
            3.04, "m/s²", "3,04 m/s²", "aceleracion_ms2",
            "<strong>Paso 1</strong> — \\(F_r = 0{,}2 \\times 20 \\times 9{,}8 = 39{,}20\\,\\text{N}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(\\sum F = 100 - 39{,}20 = 60{,}80\\,\\text{N}\\)\n\n" +
            "<strong>Paso 3</strong> — \\(a = \\frac{60{,}80}{20} = 3{,}04\\,\\text{m/s}^2\\)\n\n" +
            "∴  a = <strong>3,04 m/s²</strong>."
        ),

        // m=5, F=40 N, μ=0,4 → Fr=19,60 → ΣF=20,40 → a=4,08 m/s²
        new Scenario(
            "Una caja de 5 kg sobre una superficie con μ = 0,4 es empujada " +
            "con 40 N. Calcula la aceleración. (g = 9,8 m/s²)",
            4.08, "m/s²", "4,08 m/s²", "aceleracion_ms2",
            "<strong>Paso 1</strong> — \\(F_r = 0{,}4 \\times 5 \\times 9{,}8 = 19{,}60\\,\\text{N}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(\\sum F = 40 - 19{,}60 = 20{,}40\\,\\text{N}\\)\n\n" +
            "<strong>Paso 3</strong> — \\(a = \\frac{20{,}40}{5} = 4{,}08\\,\\text{m/s}^2\\)\n\n" +
            "∴  a = <strong>4,08 m/s²</strong>."
        ),

        // m=50, F=300 N, μ=0,3 → Fr=147,00 → ΣF=153,00 → a=3,06 m/s²
        new Scenario(
            "Un objeto de 50 kg sobre una superficie con μ = 0,3 es tirado " +
            "con 300 N. Calcula la aceleración. (g = 9,8 m/s²)",
            3.06, "m/s²", "3,06 m/s²", "aceleracion_ms2",
            "<strong>Paso 1</strong> — \\(F_r = 0{,}3 \\times 50 \\times 9{,}8 = 147{,}00\\,\\text{N}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(\\sum F = 300 - 147{,}00 = 153{,}00\\,\\text{N}\\)\n\n" +
            "<strong>Paso 3</strong> — \\(a = \\frac{153{,}00}{50} = 3{,}06\\,\\text{m/s}^2\\)\n\n" +
            "∴  a = <strong>3,06 m/s²</strong>."
        ),

        // m=10, F=120 N, μ=0,5 → Fr=49,00 → ΣF=71,00 → a=7,10 m/s²
        new Scenario(
            "Un bloque de 10 kg sobre una superficie rugosa (μ = 0,5) es impulsado " +
            "con 120 N. Calcula la aceleración. (g = 9,8 m/s²)",
            7.10, "m/s²", "7,10 m/s²", "aceleracion_ms2",
            "<strong>Paso 1</strong> — \\(F_r = 0{,}5 \\times 10 \\times 9{,}8 = 49{,}00\\,\\text{N}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(\\sum F = 120 - 49{,}00 = 71{,}00\\,\\text{N}\\)\n\n" +
            "<strong>Paso 3</strong> — \\(a = \\frac{71{,}00}{10} = 7{,}10\\,\\text{m/s}^2\\)\n\n" +
            "∴  a = <strong>7,10 m/s²</strong>."
        ),

        // m=20, F=80 N, μ=0,1 → Fr=19,60 → ΣF=60,40 → a=3,02 m/s²
        new Scenario(
            "Una caja de 20 kg sobre una superficie casi lisa (μ = 0,1) es arrastrada " +
            "con 80 N. Calcula la aceleración. (g = 9,8 m/s²)",
            3.02, "m/s²", "3,02 m/s²", "aceleracion_ms2",
            "<strong>Paso 1</strong> — \\(F_r = 0{,}1 \\times 20 \\times 9{,}8 = 19{,}60\\,\\text{N}\\)\n\n" +
            "<strong>Paso 2</strong> — \\(\\sum F = 80 - 19{,}60 = 60{,}40\\,\\text{N}\\)\n\n" +
            "<strong>Paso 3</strong> — \\(a = \\frac{60{,}40}{20} = 3{,}02\\,\\text{m/s}^2\\)\n\n" +
            "∴  a = <strong>3,02 m/s²</strong>."
        )
    );

    // =========================================================================
    // API pública
    // =========================================================================

    public FourthEsoForcesMotionExercise generateAndSave() {
        FourthEsoForcesMotionExercise ex = new FourthEsoForcesMotionExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);

        int roll = random.nextInt(9);
        if (roll < 3) {
            applyScenario(ex, FourthEsoDynamicsType.VERTICAL_MOTION,
                VERTICAL_SCENARIOS.get(random.nextInt(VERTICAL_SCENARIOS.size())));
        } else if (roll < 6) {
            applyScenario(ex, FourthEsoDynamicsType.CIRCULAR_MOTION,
                CIRCULAR_SCENARIOS.get(random.nextInt(CIRCULAR_SCENARIOS.size())));
        } else {
            applyScenario(ex, FourthEsoDynamicsType.FRICTION_DYNAMICS,
                FRICTION_SCENARIOS.get(random.nextInt(FRICTION_SCENARIOS.size())));
        }

        log.debug("4ESO BL4 generado: type={} unknown={}", ex.getDynamicsType(), ex.getUnknownVariable());
        return repository.save(ex);
    }

    public FourthEsoForcesMotionExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ejercicio 4ESO BL4 no encontrado: " + id));
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private void applyScenario(FourthEsoForcesMotionExercise ex,
                                FourthEsoDynamicsType type, Scenario sc) {
        ex.setDynamicsType(type);
        ex.setExerciseMode("NUMERICAL");
        ex.setStatement(sc.statement());
        ex.setCorrectAnswerValue(sc.correctAnswer());
        ex.setCorrectAnswerDisplay(sc.correctAnswerDisplay());
        ex.setAnswerUnit(sc.answerUnit());
        ex.setUnknownVariable(sc.unknownVariable());
        ex.setExplanation(sc.explanation());
        ex.setTolerancePercent(2.0);
    }
}
