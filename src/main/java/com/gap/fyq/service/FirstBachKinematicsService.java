package com.gap.fyq.service;

import com.gap.fyq.model.firstbach.kinematics.FirstBachKinematicsExercise;
import com.gap.fyq.model.firstbach.kinematics.KinematicsType;
import com.gap.fyq.repository.FirstBachKinematicsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirstBachKinematicsService {

    private final FirstBachKinematicsRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "1BACH";
    private static final String BLOCK  = "BL6";
    private static final double G      = 10.0; // m/s² (valor estándar en 1bach)

    // =========================================================================
    // DATA RECORDS
    // =========================================================================

    /**
     * Escenario de aceleración intrínseca.
     * aₜ = |dv/dt|, aₙ = v²/R, |a| = √(aₜ²+aₙ²).
     */
    private record IntrinsicData(
        String vDisplay,        // "v(t) = 4t + 2 m/s" — texto plano para el enunciado
        String vKatex,          // "v(t) = 4t + 2\\,\\text{m/s}"
        String derivKatex,      // muestra el cálculo de dv/dt
        double t0, double R,
        double vAt, double at, double an, double aTotal
    ) {}

    /** Lanzamiento horizontal o parabólico. */
    private record ProjectileData(
        boolean isHorizontal,
        double h,           // altura inicial (horizontal) o 0 (parabólico)
        double v0,
        double thetaDeg,    // 0 para horizontal
        double t,           // tiempo de vuelo
        double alcance,     // alcance horizontal
        double vFinal,      // módulo de v al llegar al suelo (horizontal) o en apogeo
        double hMax         // altura máxima (parabólico) o h (horizontal, dato)
    ) {}

    /** Movimiento circular uniformemente acelerado (MCUA). */
    private record RotationalData(
        String context,
        double omega0,      // rad/s (puede requerir conversión previa desde rpm)
        double omega0rpm,   // rpm (0 si no aplica)
        double alpha,       // rad/s²
        double tTotal,      // s usados en la ec. cinemática (0 si se usa θ)
        double theta,       // rad (0 si el problema es temporal)
        double omegaF,
        double nRev,
        String unknownVar,  // "omega_final" | "n_vueltas" | "tiempo"
        double answer,
        String answerUnit
    ) {}

    // =========================================================================
    // INTRINSIC_ACCELERATION — 8 escenarios
    // Verificados: aₙ = v(t₀)²/R, aₜ = |dv/dt|_{t₀}, |a|=√(aₜ²+aₙ²)
    // =========================================================================

    private static final List<IntrinsicData> INTRINSIC = List.of(

        // IA1: v=4t+2, t₀=2, R=50 → v=10, aₜ=4, aₙ=2.00, |a|=4.47
        new IntrinsicData(
            "v(t) = 4t + 2 m/s",
            "v(t) = 4t + 2\\,\\text{m/s}",
            "a_t = \\frac{dv}{dt} = 4\\,\\text{m/s}^2 \\quad(\\text{derivada constante})",
            2.0, 50.0,
            10.0, 4.0, 2.0, Math.sqrt(16+4)),       // √20=4.4721

        // IA2: v=3t², t₀=1, R=30 → v=3, aₜ=6, aₙ=0.30, |a|=6.01
        new IntrinsicData(
            "v(t) = 3t² m/s",
            "v(t) = 3t^2\\,\\text{m/s}",
            "a_t = \\frac{dv}{dt} = 6t\\Big|_{t=1} = 6\\,\\text{m/s}^2",
            1.0, 30.0,
            3.0, 6.0, 9.0/30.0, Math.sqrt(36 + (9.0/30.0)*(9.0/30.0))),  // √36.09=6.0075

        // IA3: v=2t+6, t₀=3, R=24 → v=12, aₜ=2, aₙ=6.00, |a|=6.32
        new IntrinsicData(
            "v(t) = 2t + 6 m/s",
            "v(t) = 2t + 6\\,\\text{m/s}",
            "a_t = \\frac{dv}{dt} = 2\\,\\text{m/s}^2 \\quad(\\text{derivada constante})",
            3.0, 24.0,
            12.0, 2.0, 144.0/24.0, Math.sqrt(4+36)),   // √40=6.3246

        // IA4: v=t²+4t, t₀=2, R=40 → v=12, aₜ=8, aₙ=3.60, |a|=8.77
        new IntrinsicData(
            "v(t) = t² + 4t m/s",
            "v(t) = t^2 + 4t\\,\\text{m/s}",
            "a_t = \\frac{dv}{dt} = (2t+4)\\Big|_{t=2} = 8\\,\\text{m/s}^2",
            2.0, 40.0,
            12.0, 8.0, 144.0/40.0, Math.sqrt(64 + (144.0/40.0)*(144.0/40.0))),

        // IA5: v=5t, t₀=4, R=100 → v=20, aₜ=5, aₙ=4.00, |a|=6.40
        new IntrinsicData(
            "v(t) = 5t m/s",
            "v(t) = 5t\\,\\text{m/s}",
            "a_t = \\frac{dv}{dt} = 5\\,\\text{m/s}^2 \\quad(\\text{derivada constante})",
            4.0, 100.0,
            20.0, 5.0, 400.0/100.0, Math.sqrt(25+16)),  // √41=6.4031

        // IA6: v=6-2t, t₀=1, R=20 → v=4, aₜ=2, aₙ=0.80, |a|=2.15
        new IntrinsicData(
            "v(t) = 6 − 2t m/s  (movimiento retardado)",
            "v(t) = 6 - 2t\\,\\text{m/s}",
            "a_t = \\left|\\frac{dv}{dt}\\right| = |-2| = 2\\,\\text{m/s}^2",
            1.0, 20.0,
            4.0, 2.0, 16.0/20.0, Math.sqrt(4 + (16.0/20.0)*(16.0/20.0))),

        // IA7: v=t²+2t+1=(t+1)², t₀=3, R=50 → v=16, aₜ=8, aₙ=5.12, |a|=9.50
        new IntrinsicData(
            "v(t) = t² + 2t + 1 m/s",
            "v(t) = t^2 + 2t + 1\\,\\text{m/s}",
            "a_t = \\frac{dv}{dt} = (2t+2)\\Big|_{t=3} = 8\\,\\text{m/s}^2",
            3.0, 50.0,
            16.0, 8.0, 256.0/50.0, Math.sqrt(64 + (256.0/50.0)*(256.0/50.0))),

        // IA8: v=3t+1, t₀=2, R=25 → v=7, aₜ=3, aₙ=1.96, |a|=3.58
        new IntrinsicData(
            "v(t) = 3t + 1 m/s",
            "v(t) = 3t + 1\\,\\text{m/s}",
            "a_t = \\frac{dv}{dt} = 3\\,\\text{m/s}^2 \\quad(\\text{derivada constante})",
            2.0, 25.0,
            7.0, 3.0, 49.0/25.0, Math.sqrt(9 + (49.0/25.0)*(49.0/25.0)))
    );

    // =========================================================================
    // PROJECTILE_MOTION — 6 lanzamientos horizontales + 2 parabólicos
    // g = 10 m/s² para obtener resultados exactos con 2 decimales.
    // Horizontal: t=√(2h/g), x=v₀t, |v|=√(v₀²+(gt)²)
    // Parabólico: T=2v₀sinθ/g, x=v₀cosθ·T, h_max=v₀²sin²θ/(2g)
    // =========================================================================

    private static final List<ProjectileData> PROJECTILES = List.of(

        // PM1: h=20, v₀=15 → t=2, x=30, |v|=√(225+400)=√625=25 m/s
        new ProjectileData(true, 20.0, 15.0, 0,
            2.0, 30.0, 25.0, 20.0),

        // PM2: h=5, v₀=10 → t=1, x=10, |v|=√(100+100)=√200=14.14 m/s
        new ProjectileData(true, 5.0, 10.0, 0,
            1.0, 10.0, Math.sqrt(200), 5.0),

        // PM3: h=80, v₀=25 → t=4, x=100, |v|=√(625+1600)=√2225=47.17 m/s
        new ProjectileData(true, 80.0, 25.0, 0,
            4.0, 100.0, Math.sqrt(2225), 80.0),

        // PM4: h=45, v₀=20 → t=3, x=60, |v|=√(400+900)=√1300=36.06 m/s
        new ProjectileData(true, 45.0, 20.0, 0,
            3.0, 60.0, Math.sqrt(1300), 45.0),

        // PM5: h=125, v₀=30 → t=5, x=150, |v|=√(900+2500)=√3400=58.31 m/s
        new ProjectileData(true, 125.0, 30.0, 0,
            5.0, 150.0, Math.sqrt(3400), 125.0),

        // PM6: h=20, v₀=6 → t=2, x=12, |v|=√(36+400)=√436=20.88 m/s
        new ProjectileData(true, 20.0, 6.0, 0,
            2.0, 12.0, Math.sqrt(436), 20.0),

        // PM7 parabólico: v₀=20, θ=45° → T=2×(20×sin45°)/10=2√2≈2.83s,
        //   x=v₀²sin(2×45°)/g=400/10=40m, h_max=v₀²sin²45°/(2g)=200/20=10m
        new ProjectileData(false, 0, 20.0, 45.0,
            2.0*Math.sqrt(2), 40.0, 20.0, 10.0),

        // PM8 parabólico: v₀=30, θ=30° → T=2×15/10=3s,
        //   x=30²×sin60°/10=900×(√3/2)/10=77.94m, h_max=225/20=11.25m
        new ProjectileData(false, 0, 30.0, 30.0,
            3.0, 900.0*Math.sin(Math.toRadians(60))/10.0, 30.0, 11.25)
    );

    // =========================================================================
    // ROTATIONAL_MCUA — 8 escenarios pre-calculados
    // Ecuaciones: ω=ω₀+αt  |  θ=ω₀t+½αt²  |  ω²=ω₀²+2αθ  |  N=θ/(2π)
    // Conversión rpm: ω=2πn/60
    // =========================================================================

    private static final List<RotationalData> ROTATIONAL = List.of(

        // R1: ω₀=5, α=2, t=3 → ωf=11 rad/s
        new RotationalData(
            "Una rueda parte de ω₀ = 5 rad/s con α = 2 rad/s².",
            5.0, 0, 2.0, 3.0, 0, 11.0, 0,
            "omega_final", 11.0, "rad/s"),

        // R2: ω₀=0, α=0,5, t=10 → ωf=5 rad/s
        new RotationalData(
            "Un motor arranca desde el reposo con α = 0,5 rad/s².",
            0.0, 0, 0.5, 10.0, 0, 5.0, 0,
            "omega_final", 5.0, "rad/s"),

        // R3: ω₀=0, α=3, t=4 → N=24/(2π)=3.82 rev
        new RotationalData(
            "Una turbina parte del reposo con α = 3 rad/s².",
            0.0, 0, 3.0, 4.0, 24.0, 0, 24.0/(2*Math.PI),
            "n_vueltas", 24.0/(2*Math.PI), "rev"),

        // R4: ω₀=100rpm→10π/3 rad/s, α=-2 → t_stop=(10π/3)/2=5π/3≈5.24s
        new RotationalData(
            "Un ventilador gira a 100 rpm y frena con α = −2 rad/s².",
            100.0*2*Math.PI/60.0, 100.0, -2.0, 0, 0, 0, 0,
            "tiempo", 100.0*2*Math.PI/60.0/2.0, "s"),

        // R5: ω₀=0, α=4, θ=5×2π=10π → ωf=√(2×4×10π)=√(80π)≈15.85 rad/s
        new RotationalData(
            "Una polea parte del reposo con α = 4 rad/s² y completa 5 vueltas.",
            0.0, 0, 4.0, 0, 5.0*2*Math.PI, Math.sqrt(2*4*5.0*2*Math.PI), 5.0,
            "omega_final", Math.sqrt(2*4*5.0*2*Math.PI), "rad/s"),

        // R6: ω₀=2π, α=π, t=4 → N=16π/(2π)=8 rev
        new RotationalData(
            "Un disco gira con ω₀ = 2π rad/s y α = π rad/s².",
            2*Math.PI, 0, Math.PI, 4.0, 16*Math.PI, 6*Math.PI, 8.0,
            "n_vueltas", 8.0, "rev"),

        // R7: ω₀=600rpm→20π≈62.83, α=-5, hasta 300rpm→10π≈31.42 → t≈6.28s
        new RotationalData(
            "Una centrifugadora desacelera de 600 rpm a 300 rpm con α = −5 rad/s².",
            600.0*2*Math.PI/60.0, 600.0, -5.0, 0, 0, 300.0*2*Math.PI/60.0, 0,
            "tiempo", (300.0-600.0)*2*Math.PI/60.0/(-5.0), "s"),

        // R8: ω₀=3, α=1.5, t=6 → N=45/(2π)≈7.16 rev
        new RotationalData(
            "Un torno gira con ω₀ = 3 rad/s y α = 1,5 rad/s².",
            3.0, 0, 1.5, 6.0, 45.0, 0, 45.0/(2*Math.PI),
            "n_vueltas", 45.0/(2*Math.PI), "rev")
    );

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public FirstBachKinematicsExercise generateAndSave() {
        FirstBachKinematicsExercise ex = new FirstBachKinematicsExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);
        ex.setExerciseMode("NUMERICAL");

        int roll = random.nextInt(3);
        if      (roll == 0) buildIntrinsic(ex);
        else if (roll == 1) buildProjectile(ex);
        else                buildRotational(ex);

        log.debug("1BACH BL6 generado: type={} var={}",
            ex.getKinematicsType(), ex.getUnknownVariable());
        return repository.save(ex);
    }

    public FirstBachKinematicsExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ejercicio 1BACH BL6 no encontrado: " + id));
    }

    // =========================================================================
    // CONSTRUCTOR — INTRINSIC_ACCELERATION
    // =========================================================================

    private void buildIntrinsic(FirstBachKinematicsExercise ex) {
        ex.setKinematicsType(KinematicsType.INTRINSIC_ACCELERATION);
        ex.setTolerancePercent(2.0);

        IntrinsicData sc = INTRINSIC.get(random.nextInt(INTRINSIC.size()));

        // Seleccionar incógnita aleatoriamente
        String[] vars  = {"at", "an", "a_total"};
        double[] vals  = {sc.at(), sc.an(), sc.aTotal()};
        String[] units = {"m/s²", "m/s²", "m/s²"};
        String[] asks  = {
            "Calcula la aceleración tangencial (aₜ).",
            "Calcula la aceleración normal o centrípeta (aₙ).",
            "Calcula el módulo de la aceleración total (|ā|)."
        };
        int idx = random.nextInt(3);

        ex.setUnknownVariable(vars[idx]);
        ex.setCorrectAnswerValue(vals[idx]);
        ex.setAnswerUnit(units[idx]);
        ex.setCorrectAnswerDisplay(fmt2(vals[idx]) + " m/s²");

        ex.setStatement(String.format(
            "Una partícula se mueve con %s a lo largo de una trayectoria curva de radio " +
            "R = %s m. En el instante t₀ = %s s, %s",
            sc.vDisplay(),
            fmt1(sc.R()),
            fmt1(sc.t0()),
            asks[idx]));

        ex.setExplanation(buildIntrinsicExplanation(sc, idx));
    }

    // =========================================================================
    // CONSTRUCTOR — PROJECTILE_MOTION
    // =========================================================================

    private void buildProjectile(FirstBachKinematicsExercise ex) {
        ex.setKinematicsType(KinematicsType.PROJECTILE_MOTION);
        ex.setTolerancePercent(2.0);

        ProjectileData sc = PROJECTILES.get(random.nextInt(PROJECTILES.size()));

        if (sc.isHorizontal()) {
            String[] vars  = {"tiempo_vuelo", "alcance", "velocidad_final"};
            double[] vals  = {sc.t(), sc.alcance(), sc.vFinal()};
            String[] units = {"s", "m", "m/s"};
            String[] asks  = {
                "¿Cuánto tarda en llegar al suelo?",
                "¿Cuál es el alcance horizontal?",
                "¿Cuál es el módulo de la velocidad al llegar al suelo?"
            };
            int idx = random.nextInt(3);

            ex.setUnknownVariable(vars[idx]);
            ex.setCorrectAnswerValue(vals[idx]);
            ex.setAnswerUnit(units[idx]);
            ex.setCorrectAnswerDisplay(fmt2(vals[idx]) + " " + units[idx]);

            ex.setStatement(String.format(
                "Desde una altura h = %s m se lanza horizontalmente un proyectil con " +
                "v₀ = %s m/s (g = 10 m/s²). %s",
                fmt1(sc.h()), fmt1(sc.v0()), asks[idx]));

            ex.setExplanation(buildProjHorizExplanation(sc, idx));

        } else {
            // Parabólico
            String[] vars  = {"tiempo_vuelo", "alcance", "altura_max"};
            double[] vals  = {sc.t(), sc.alcance(), sc.hMax()};
            String[] units = {"s", "m", "m"};
            String[] asks  = {
                "¿Cuál es el tiempo total de vuelo?",
                "¿Cuál es el alcance horizontal?",
                "¿Cuál es la altura máxima alcanzada?"
            };
            int idx = random.nextInt(3);

            ex.setUnknownVariable(vars[idx]);
            ex.setCorrectAnswerValue(vals[idx]);
            ex.setAnswerUnit(units[idx]);
            ex.setCorrectAnswerDisplay(fmt2(vals[idx]) + " " + units[idx]);

            ex.setStatement(String.format(
                "Un proyectil se lanza desde el suelo con v₀ = %s m/s y un ángulo " +
                "θ = %s° respecto a la horizontal (g = 10 m/s²). %s",
                fmt1(sc.v0()), fmt1(sc.thetaDeg()), asks[idx]));

            ex.setExplanation(buildProjParabExplanation(sc, idx));
        }
    }

    // =========================================================================
    // CONSTRUCTOR — ROTATIONAL_MCUA
    // =========================================================================

    private void buildRotational(FirstBachKinematicsExercise ex) {
        ex.setKinematicsType(KinematicsType.ROTATIONAL_MCUA);
        ex.setTolerancePercent(2.0);

        RotationalData sc = ROTATIONAL.get(random.nextInt(ROTATIONAL.size()));

        ex.setUnknownVariable(sc.unknownVar());
        ex.setCorrectAnswerValue(sc.answer());
        ex.setAnswerUnit(sc.answerUnit());
        ex.setCorrectAnswerDisplay(fmt2(sc.answer()) + " " + sc.answerUnit());

        String askVerb = switch (sc.unknownVar()) {
            case "omega_final" -> "Calcula la velocidad angular final ωf (en rad/s).";
            case "n_vueltas"   -> "Calcula el número de vueltas N.";
            case "tiempo"      -> "Calcula el tiempo transcurrido (en s).";
            default            -> "Calcula el ángulo girado θ (en rad).";
        };

        String omega0Str = sc.omega0rpm() > 0
            ? sc.omega0rpm() + " rpm (" + fmt2(sc.omega0()) + " rad/s)"
            : fmt2(sc.omega0()) + " rad/s";

        String thetaStr  = sc.theta() > 0
            ? " completando " + fmt2(sc.theta() / (2*Math.PI)) + " vueltas"
            : " durante t = " + fmt2(sc.tTotal()) + " s";

        // Para R4 y R7 que tienen omega en rpm como dato de llegada
        boolean isDeceleToRPM = "tiempo".equals(sc.unknownVar())
                && sc.omega0rpm() > 0 && sc.omegaF() > 0;

        String stmtExtra = isDeceleToRPM
            ? " hasta detenerse"
            : (sc.theta() > 0
                ? thetaStr
                : " durante t = " + fmt2(sc.tTotal()) + " s");

        if (isDeceleToRPM && sc.omegaF() > 1.0) {
            // R7: decelerates from 600 to 300 rpm
            double omegaFrpm = Math.round(sc.omegaF() * 60 / (2*Math.PI));
            stmtExtra = " hasta alcanzar " + (int)omegaFrpm + " rpm";
        }

        ex.setStatement(String.format(
            "%s Parte de ω₀ = %s con aceleración angular α = %s rad/s²%s. %s",
            sc.context(), omega0Str, fmt2(sc.alpha()), stmtExtra, askVerb));

        ex.setExplanation(buildRotationalExplanation(sc));
    }

    // =========================================================================
    // EXPLICACIÓN — INTRINSIC_ACCELERATION
    // =========================================================================

    private String buildIntrinsicExplanation(IntrinsicData sc, int askedIdx) {
        String boxed = switch (askedIdx) {
            case 0 -> "a_t = \\boxed{" + fmtK2(sc.at()) + "\\,\\text{m/s}^2}";
            case 1 -> "a_n = \\boxed{" + fmtK2(sc.an()) + "\\,\\text{m/s}^2}";
            default -> "|\\vec{a}| = \\boxed{" + fmtK2(sc.aTotal()) + "\\,\\text{m/s}^2}";
        };
        var sb = new StringBuilder();

        sb.append("<strong>Datos:</strong> \\(").append(sc.vKatex())
          .append("\\) · R = ").append(fmt1(sc.R()))
          .append(" m · t₀ = ").append(fmt1(sc.t0())).append(" s\n\n");

        sb.append("<strong>Componentes intrínsecas de la aceleración:</strong>\n\n")
          .append("<ul>")
          .append("<li>\\(a_t = \\dfrac{dv}{dt}\\) (tangencial, cambia el módulo de la velocidad)</li>")
          .append("<li>\\(a_n = \\dfrac{v^2}{R}\\) (normal o centrípeta, cambia la dirección)</li>")
          .append("</ul>\n\n");

        // Paso 1 — velocidad en t₀
        sb.append("<strong>Paso 1 — Velocidad en t₀ = ").append(fmt1(sc.t0())).append(" s:</strong>\n\n")
          .append("\\[").append(sc.vKatex()).append("\\implies v(")
          .append(fmt1(sc.t0())).append(") = ")
          .append(fmtK2(sc.vAt())).append("\\,\\text{m/s}\\]\n\n");

        // Paso 2 — aₜ
        sb.append("<strong>Paso 2 — Aceleración tangencial (derivada de v):</strong>\n\n")
          .append("\\[").append(sc.derivKatex()).append("\\]\n\n");

        // Paso 3 — aₙ
        sb.append("<strong>Paso 3 — Aceleración normal (centrípeta):</strong>\n\n")
          .append("\\[a_n = \\frac{v^2(t_0)}{R} = \\frac{(")
          .append(fmtK2(sc.vAt())).append(")^2}{").append(fmtK2(sc.R()))
          .append("} = \\frac{").append(fmtK2(sc.vAt()*sc.vAt()))
          .append("}{").append(fmtK2(sc.R())).append("} = ")
          .append(fmtK2(sc.an())).append("\\,\\text{m/s}^2\\]\n\n");

        // Paso 4 — |a|
        sb.append("<strong>Paso 4 — Módulo de la aceleración total:</strong>\n\n")
          .append("\\[|\\vec{a}| = \\sqrt{a_t^2 + a_n^2} = \\sqrt{(")
          .append(fmtK2(sc.at())).append(")^2 + (")
          .append(fmtK2(sc.an())).append(")^2} = \\sqrt{")
          .append(fmtK2(sc.at()*sc.at())).append(" + ")
          .append(fmtK2(sc.an()*sc.an())).append("} = ")
          .append(fmtK2(sc.aTotal())).append("\\,\\text{m/s}^2\\]\n\n");

        sb.append("∴ \\(").append(boxed).append("\\)");
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — PROJECTILE_MOTION (horizontal)
    // =========================================================================

    private String buildProjHorizExplanation(ProjectileData sc, int askedIdx) {
        String[] boxed = {
            "t = \\boxed{" + fmtK2(sc.t()) + "\\,\\text{s}}",
            "x = \\boxed{" + fmtK2(sc.alcance()) + "\\,\\text{m}}",
            "|\\vec{v}| = \\boxed{" + fmtK2(sc.vFinal()) + "\\,\\text{m/s}}"
        };
        double vy = G * sc.t();
        var sb = new StringBuilder();

        sb.append("<strong>Descomposición del movimiento por ejes:</strong>\n\n")
          .append("<ul>")
          .append("<li><strong>Eje X (MRU):</strong> \\(x(t) = v_0\\cdot t = ")
          .append(fmtK1(sc.v0())).append("t\\,\\text{m}\\)</li>")
          .append("<li><strong>Eje Y (caída libre):</strong> \\(y(t) = h - \\frac{1}{2}gt^2 = ")
          .append(fmtK1(sc.h())).append(" - 5t^2\\,\\text{m}\\)</li>")
          .append("</ul>\n\n");

        // Paso 1 — tiempo
        sb.append("<strong>Paso 1 — Tiempo de vuelo</strong> (condición y = 0):\n\n")
          .append("\\[0 = ").append(fmtK1(sc.h()))
          .append(" - \\frac{1}{2}\\cdot 10 \\cdot t^2 \\implies t = \\sqrt{\\frac{2h}{g}} = ")
          .append("\\sqrt{\\frac{2\\times").append(fmtK1(sc.h())).append("}{10}} = ")
          .append(fmtK2(sc.t())).append("\\,\\text{s}\\]\n\n");

        // Paso 2 — alcance
        sb.append("<strong>Paso 2 — Alcance horizontal:</strong>\n\n")
          .append("\\[x = v_0 \\cdot t = ").append(fmtK1(sc.v0()))
          .append(" \\times ").append(fmtK2(sc.t()))
          .append(" = ").append(fmtK2(sc.alcance())).append("\\,\\text{m}\\]\n\n");

        // Paso 3 — velocidad final
        sb.append("<strong>Paso 3 — Velocidad final (al llegar al suelo):</strong>\n\n")
          .append("\\[v_x = v_0 = ").append(fmtK1(sc.v0())).append("\\,\\text{m/s}, \\quad ")
          .append("v_y = g\\cdot t = 10 \\times ").append(fmtK2(sc.t()))
          .append(" = ").append(fmtK2(vy)).append("\\,\\text{m/s}\\]\n\n")
          .append("\\[|\\vec{v}| = \\sqrt{v_x^2 + v_y^2} = \\sqrt{(")
          .append(fmtK1(sc.v0())).append(")^2 + (").append(fmtK2(vy))
          .append(")^2} = ").append(fmtK2(sc.vFinal())).append("\\,\\text{m/s}\\]\n\n");

        sb.append("∴ \\(").append(boxed[askedIdx]).append("\\)");
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — PROJECTILE_MOTION (parabólico)
    // =========================================================================

    private String buildProjParabExplanation(ProjectileData sc, int askedIdx) {
        String[] boxed = {
            "T = \\boxed{" + fmtK2(sc.t()) + "\\,\\text{s}}",
            "x_{\\max} = \\boxed{" + fmtK2(sc.alcance()) + "\\,\\text{m}}",
            "h_{\\max} = \\boxed{" + fmtK2(sc.hMax()) + "\\,\\text{m}}"
        };
        double thetaRad = Math.toRadians(sc.thetaDeg());
        double v0x = sc.v0() * Math.cos(thetaRad);
        double v0y = sc.v0() * Math.sin(thetaRad);
        var sb = new StringBuilder();

        sb.append("<strong>Descomposición de la velocidad inicial:</strong>\n\n")
          .append("\\[v_{0x} = v_0\\cos\\theta = ").append(fmtK1(sc.v0()))
          .append("\\cos(").append((int)sc.thetaDeg()).append("°) = ")
          .append(fmtK2(v0x)).append("\\,\\text{m/s}\\]\n\n")
          .append("\\[v_{0y} = v_0\\sin\\theta = ").append(fmtK1(sc.v0()))
          .append("\\sin(").append((int)sc.thetaDeg()).append("°) = ")
          .append(fmtK2(v0y)).append("\\,\\text{m/s}\\]\n\n");

        sb.append("<strong>Paso 1 — Tiempo de vuelo total</strong> (el proyectil sube y baja simétricamente):\n\n")
          .append("\\[T = \\frac{2v_{0y}}{g} = \\frac{2\\times")
          .append(fmtK2(v0y)).append("}{10} = ").append(fmtK2(sc.t()))
          .append("\\,\\text{s}\\]\n\n");

        sb.append("<strong>Paso 2 — Alcance horizontal:</strong>\n\n")
          .append("\\[x_{\\max} = v_{0x}\\cdot T = ").append(fmtK2(v0x))
          .append("\\times ").append(fmtK2(sc.t())).append(" = ")
          .append(fmtK2(sc.alcance())).append("\\,\\text{m}\\]\n\n");

        sb.append("<strong>Paso 3 — Altura máxima</strong> (en el apogeo, vᵧ = 0):\n\n")
          .append("\\[h_{\\max} = \\frac{v_{0y}^2}{2g} = \\frac{(")
          .append(fmtK2(v0y)).append(")^2}{2\\times 10} = \\frac{")
          .append(fmtK2(v0y*v0y)).append("}{20} = ")
          .append(fmtK2(sc.hMax())).append("\\,\\text{m}\\]\n\n");

        sb.append("∴ \\(").append(boxed[askedIdx]).append("\\)");
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — ROTATIONAL_MCUA
    // =========================================================================

    private String buildRotationalExplanation(RotationalData sc) {
        var sb = new StringBuilder();

        sb.append("<strong>Ecuaciones del MCUA:</strong>\n\n")
          .append("\\[\\omega(t) = \\omega_0 + \\alpha\\,t \\qquad ")
          .append("\\theta(t) = \\omega_0 t + \\tfrac{1}{2}\\alpha t^2 \\qquad ")
          .append("\\omega^2 = \\omega_0^2 + 2\\alpha\\theta\\]\n\n");

        // Conversión rpm si aplica
        if (sc.omega0rpm() > 0) {
            sb.append("<strong>Conversión de unidades:</strong>\n\n")
              .append("\\[\\omega_0 = ").append(fmt1(sc.omega0rpm()))
              .append("\\,\\text{rpm} \\times \\frac{2\\pi\\,\\text{rad}}{60\\,\\text{s}} = ")
              .append(fmtK2(sc.omega0())).append("\\,\\text{rad/s}\\]\n\n");
        }
        if (sc.omegaF() > 0 && "tiempo".equals(sc.unknownVar()) && sc.omega0rpm() > 0) {
            double omegaFrpm = sc.omegaF() * 60 / (2*Math.PI);
            sb.append("\\[\\omega_f = ").append(fmt1(omegaFrpm))
              .append("\\,\\text{rpm} \\times \\frac{2\\pi}{60} = ")
              .append(fmtK2(sc.omegaF())).append("\\,\\text{rad/s}\\]\n\n");
        }

        switch (sc.unknownVar()) {

            case "omega_final" -> {
                if (sc.theta() > 0) {
                    // Used ω² = ω₀² + 2αθ
                    sb.append("<strong>Aplicando \\(\\omega^2 = \\omega_0^2 + 2\\alpha\\theta\\):</strong>\n\n")
                      .append("\\[\\omega_f = \\sqrt{\\omega_0^2 + 2\\alpha\\theta} = ")
                      .append("\\sqrt{").append(fmtK2(sc.omega0())).append("^2 + 2\\times")
                      .append(fmtK2(sc.alpha())).append("\\times")
                      .append(fmtK2(sc.theta())).append("}\\]\n\n")
                      .append("\\[\\omega_f = \\sqrt{").append(fmtK2(sc.omega0()*sc.omega0()))
                      .append(" + ").append(fmtK2(2*sc.alpha()*sc.theta()))
                      .append("} = ").append(fmtK2(sc.omegaF())).append("\\,\\text{rad/s}\\]\n\n");
                } else {
                    // Used ω = ω₀ + αt
                    sb.append("<strong>Aplicando \\(\\omega(t) = \\omega_0 + \\alpha\\,t\\):</strong>\n\n")
                      .append("\\[\\omega_f = ").append(fmtK2(sc.omega0()))
                      .append(" + ").append(fmtK2(sc.alpha()))
                      .append("\\times ").append(fmtK2(sc.tTotal()))
                      .append(" = ").append(fmtK2(sc.omegaF())).append("\\,\\text{rad/s}\\]\n\n");
                }
                sb.append("∴ \\(\\omega_f = \\boxed{").append(fmtK2(sc.omegaF()))
                  .append("\\,\\text{rad/s}}\\)");
            }

            case "n_vueltas" -> {
                double theta = sc.theta() > 0 ? sc.theta()
                    : sc.omega0()*sc.tTotal() + 0.5*sc.alpha()*sc.tTotal()*sc.tTotal();
                sb.append("<strong>Ángulo girado con \\(\\theta = \\omega_0 t + \\frac{1}{2}\\alpha t^2\\):</strong>\n\n")
                  .append("\\[\\theta = ").append(fmtK2(sc.omega0()))
                  .append("\\times ").append(fmtK2(sc.tTotal()))
                  .append(" + \\frac{1}{2}\\times ").append(fmtK2(sc.alpha()))
                  .append("\\times (").append(fmtK2(sc.tTotal())).append(")^2 = ")
                  .append(fmtK2(theta)).append("\\,\\text{rad}\\]\n\n")
                  .append("<strong>Número de vueltas:</strong>\n\n")
                  .append("\\[N = \\frac{\\theta}{2\\pi} = \\frac{").append(fmtK2(theta))
                  .append("}{2\\pi} = ").append(fmtK2(sc.nRev())).append("\\,\\text{rev}\\]\n\n");
                sb.append("∴ \\(N = \\boxed{").append(fmtK2(sc.nRev()))
                  .append("\\,\\text{rev}}\\)");
            }

            case "tiempo" -> {
                sb.append("<strong>Despejando t de \\(\\omega_f = \\omega_0 + \\alpha\\,t\\):</strong>\n\n")
                  .append("\\[t = \\frac{\\omega_f - \\omega_0}{\\alpha} = ")
                  .append("\\frac{").append(fmtK2(sc.omegaF()))
                  .append(" - ").append(fmtK2(sc.omega0())).append("}{")
                  .append(fmtK2(sc.alpha())).append("} = ")
                  .append(fmtK2(sc.answer())).append("\\,\\text{s}\\]\n\n");
                sb.append("∴ \\(t = \\boxed{").append(fmtK2(sc.answer()))
                  .append("\\,\\text{s}}\\)");
            }
        }
        return sb.toString();
    }

    // =========================================================================
    // FORMATEADORES
    // =========================================================================

    private String fmt1(double v) {
        if (v == Math.floor(v) && !Double.isInfinite(v))
            return String.valueOf((long) v);
        return String.format("%.1f", v).replace(".", ",");
    }

    private String fmt2(double v) {
        return String.format("%.2f", v).replace(".", ",");
    }

    private String fmtK1(double v) { return fmt1(v).replace(",", "{,}"); }
    private String fmtK2(double v) { return fmt2(v).replace(",", "{,}"); }
}
