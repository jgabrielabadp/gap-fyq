package com.gap.fyq.service;

import com.gap.fyq.model.secondbach.electromagnetism.ElectromagnetismType;
import com.gap.fyq.model.secondbach.electromagnetism.SecondBachElectromagnetismExercise;
import com.gap.fyq.repository.SecondBachElectromagnetismRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecondBachElectromagnetismService {

    private final SecondBachElectromagnetismRepository repository;
    private final Random random = new Random();

    private static final String COURSE  = "2BACH";
    private static final String BLOCK   = "BL2";
    private static final double TWO_PI  = 2.0 * Math.PI;

    // ── Constantes físicas universales ────────────────────────────────────────
    private static final double K     = 9.0e9;          // N·m²/C² (Coulomb)
    private static final double MU0   = 4.0e-7 * Math.PI; // T·m/A (permeabilidad vacío)
    private static final double Q_E   = 1.6e-19;        // C  (módulo carga electrón/protón)
    private static final double M_E   = 9.11e-31;       // kg (masa electrón)
    private static final double M_P   = 1.67e-27;       // kg (masa protón)

    // =========================================================================
    // DATA RECORDS
    // =========================================================================

    /**
     * Dos cargas puntuales Q1 (en x=0) y Q2 (en x=d). Punto P en x=xP.
     * V = k·Q1/r1 + k·Q2/r2  [suma escalar, incluye signos de carga]
     * e1x, e2x: componentes x del campo de cada carga en P (signed)
     * E = |e1x + e2x|
     */
    private record ElectrostaticData(
        double q1, double q2, double d, double xP,
        double r1, double r2,
        double V, double E,
        double e1x, double e2x,
        boolean pBetween
    ) {}

    /**
     * Partícula cargada en campo magnético ⊥.
     * unknownVar: "radio_r" | "velocidad_v" | "frecuencia_f"
     * r = mv/(|q|B)  ·  v = |q|Br/m  ·  f = |q|B/(2πm)
     */
    private record LorentzData(
        String particleName,
        double mass, double charge,
        String unknownVar,
        double B, double v, double r, double f
    ) {}

    /**
     * Inducción de Faraday.
     * isRotating=true  → espira giratoria: Φ_max=NBA, ε_max=NBAω
     * isRotating=false → campo variable:   ε=N·A·|ΔB/Δt|
     * unknownVar: "flujo_max" | "fem_max"
     */
    private record FaradayData(
        boolean isRotating,
        int N, double B, double A, double omega, double dBdt,
        double phiMax, double emfMax,
        String unknownVar
    ) {}

    // =========================================================================
    // FÁBRICAS ESTÁTICAS
    // =========================================================================

    /**
     * Configura un escenario de superposición electrostática.
     * Todas las cargas en μC; P siempre con xP > 0.
     */
    private static ElectrostaticData electro(double q1, double q2, double d, double xP) {
        double r1  = Math.abs(xP);
        double r2  = Math.abs(xP - d);
        double V   = K * q1 / r1 + K * q2 / r2;
        // Campo eléctrico en P: E = kQ/r² · r̂(P←Q), r̂ = sign(xP - xQ)
        double e1x = K * q1 / (r1 * r1) * (xP > 0   ? 1 : -1);
        double e2x = K * q2 / (r2 * r2) * (xP > d   ? 1 : (xP < d ? -1 : 0));
        boolean between = xP > 0 && xP < d;
        return new ElectrostaticData(q1, q2, d, xP, r1, r2, V, Math.abs(e1x + e2x),
                                     e1x, e2x, between);
    }

    /** Pregunta por el radio de la trayectoria. */
    private static LorentzData lorentzR(String name, double m, double B, double v) {
        double r = m * v / (Q_E * B);
        double f = Q_E * B / (TWO_PI * m);
        return new LorentzData(name, m, Q_E, "radio_r", B, v, r, f);
    }

    /** Pregunta por la velocidad de la partícula dado el radio. */
    private static LorentzData lorentzV(String name, double m, double B, double r) {
        double v = Q_E * B * r / m;
        double f = Q_E * B / (TWO_PI * m);
        return new LorentzData(name, m, Q_E, "velocidad_v", B, v, r, f);
    }

    /** Pregunta por la frecuencia de ciclotrón (independiente de v). */
    private static LorentzData lorentzF(String name, double m, double B) {
        double f = Q_E * B / (TWO_PI * m);
        return new LorentzData(name, m, Q_E, "frecuencia_f", B, 0, 0, f);
    }

    /** Espira giratoria: Φ_max = NBA, ε_max = NBAω. */
    private static FaradayData faradayRot(int N, double B, double A,
                                          double omega, String unknownVar) {
        return new FaradayData(true, N, B, A, omega, 0,
                               N * B * A, N * B * A * omega, unknownVar);
    }

    /** Campo B que varía uniformemente: |ε| = N·A·|ΔB/Δt|. */
    private static FaradayData faradayDB(int N, double A, double dBdt) {
        return new FaradayData(false, N, 0, A, 0, dBdt,
                               0, N * A * Math.abs(dBdt), "fem_max");
    }

    // =========================================================================
    // ELECTROSTATIC_SUPERPOSITION — 6 escenarios
    // K = 9×10⁹ N·m²/C²
    // =========================================================================

    private static final List<ElectrostaticData> ELECTRO = List.of(
        // ES1: Q1=+4μC, Q2=+4μC, d=0.4m, P=0.1m (entre cargas, ambas +)
        //  V=4.80e5 V, |E|=3.20e6 V/m
        electro(+4e-6, +4e-6, 0.4, 0.1),

        // ES2: Q1=+2μC, Q2=−2μC, d=0.3m, P=0.5m (fuera, dipolo)
        //  V=−5.40e4 V, |E|=3.78e5 V/m
        electro(+2e-6, -2e-6, 0.3, 0.5),

        // ES3: Q1=+5μC, Q2=+5μC, d=0.4m, P=0.6m (fuera, ambas +)
        //  V=3.00e5 V, |E|=1.25e6 V/m
        electro(+5e-6, +5e-6, 0.4, 0.6),

        // ES4: Q1=+5μC, Q2=−3μC, d=0.4m, P=0.2m (entre, mixto)
        //  V=9.00e4 V, |E|=1.80e6 V/m
        electro(+5e-6, -3e-6, 0.4, 0.2),

        // ES5: Q1=+8μC, Q2=+2μC, d=0.5m, P=1.0m (fuera, ambas +)
        //  V=1.08e5 V, |E|=1.44e5 V/m
        electro(+8e-6, +2e-6, 0.5, 1.0),

        // ES6: Q1=+6μC, Q2=−2μC, d=0.5m, P=1.0m (fuera, mixto)
        //  V=1.80e4 V, |E|=1.80e4 V/m
        electro(+6e-6, -2e-6, 0.5, 1.0)
    );

    // =========================================================================
    // LORENTZ_MOTION — 6 escenarios (2 radio, 2 velocidad, 2 frecuencia)
    // r = mv/(|q|B)  ·  v = |q|Br/m  ·  f = |q|B/(2πm)
    // =========================================================================

    private static final List<LorentzData> LORENTZ = List.of(
        // LM1 (radio): electrón, v=1.0e7 m/s, B=0.5 T → r≈1.14e−4 m
        lorentzR("electrón", M_E, 0.5,  1.0e7),
        // LM2 (radio): protón,   v=2.0e6 m/s, B=0.2 T → r≈1.04e−1 m
        lorentzR("protón",   M_P, 0.2,  2.0e6),
        // LM3 (velocidad): electrón, r=5.0e−4 m, B=0.1 T → v≈8.78e6 m/s
        lorentzV("electrón", M_E, 0.1,  5.0e-4),
        // LM4 (velocidad): protón,   r=0.05 m,   B=0.5 T → v≈2.40e6 m/s
        lorentzV("protón",   M_P, 0.5,  0.05),
        // LM5 (frecuencia): electrón, B=0.05 T → f≈1.40e9 Hz
        lorentzF("electrón", M_E, 0.05),
        // LM6 (frecuencia): protón,   B=1.0  T → f≈1.52e7 Hz
        lorentzF("protón",   M_P, 1.0)
    );

    // =========================================================================
    // FARADAY_INDUCTION — 6 escenarios (4 espira giratoria, 2 campo variable)
    // =========================================================================

    private static final List<FaradayData> FARADAY = List.of(
        // FA1: N=100, B=0.5T, A=0.02m², ω=50π rad/s → ε_max=50π≈157 V
        faradayRot(100, 0.5,  0.02, 50  * Math.PI, "fem_max"),
        // FA2: N=200, B=0.3T, A=0.01m²            → Φ_max=0.6 Wb
        faradayRot(200, 0.3,  0.01, 100 * Math.PI, "flujo_max"),
        // FA3: N=500, B=0.2T, A=0.05m², ω=20π rad/s → ε_max=100π≈314 V
        faradayRot(500, 0.2,  0.05, 20  * Math.PI, "fem_max"),
        // FA4: N=100, B=0.8T, A=0.04m²            → Φ_max=3.2 Wb
        faradayRot(100, 0.8,  0.04, 10  * Math.PI, "flujo_max"),
        // FA5: N=50,  A=0.02m², ΔB/Δt=0.5 T/s    → ε=0.5 V
        faradayDB(50,  0.02, 0.5),
        // FA6: N=200, A=0.10m², ΔB/Δt=0.3 T/s    → ε=6.0 V
        faradayDB(200, 0.10, 0.3)
    );

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public SecondBachElectromagnetismExercise generateAndSave() {
        SecondBachElectromagnetismExercise ex = new SecondBachElectromagnetismExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);
        ex.setExerciseMode("NUMERICAL");

        int roll = random.nextInt(3);
        if      (roll == 0) buildElectrostatic(ex);
        else if (roll == 1) buildLorentz(ex);
        else                buildFaraday(ex);

        log.debug("2BACH BL2 generado: type={} var={}",
            ex.getElectromagnetismType(), ex.getUnknownVariable());
        return repository.save(ex);
    }

    public SecondBachElectromagnetismExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ejercicio 2BACH BL2 no encontrado: " + id));
    }

    // =========================================================================
    // CONSTRUCTOR — ELECTROSTATIC_SUPERPOSITION
    // =========================================================================

    private void buildElectrostatic(SecondBachElectromagnetismExercise ex) {
        ex.setElectromagnetismType(ElectromagnetismType.ELECTROSTATIC_SUPERPOSITION);
        ex.setTolerancePercent(2.0);

        ElectrostaticData sc = ELECTRO.get(random.nextInt(ELECTRO.size()));
        boolean askPotential = random.nextBoolean();

        if (askPotential) {
            ex.setUnknownVariable("potencial_V");
            ex.setCorrectAnswerValue(sc.V());
            ex.setAnswerUnit("V");
            ex.setCorrectAnswerDisplay(fmtSci2(sc.V()) + " V");
        } else {
            ex.setUnknownVariable("campo_E");
            ex.setCorrectAnswerValue(sc.E());
            ex.setAnswerUnit("V/m");
            ex.setCorrectAnswerDisplay(fmtSci2(sc.E()) + " V/m");
        }

        String posDesc = sc.pBetween()
            ? "entre Q₁ y Q₂, a " + fmtSci2(sc.r1()) + " m de Q₁"
            : "a " + fmtSci2(sc.xP() - sc.d()) + " m a la derecha de Q₂";

        String askText = askPotential
            ? "Calcula el potencial eléctrico V en P (en V)."
            : "Calcula el módulo del campo eléctrico |E⃗| en P (en V/m).";

        ex.setStatement(String.format(
            "Dos cargas puntuales Q₁ = %s C y Q₂ = %s C están separadas " +
            "una distancia d = %.1f m. El punto P se encuentra %s. " +
            "(K = 9×10⁹ N·m²/C².) %s",
            fmtCharge(sc.q1()), fmtCharge(sc.q2()), sc.d(), posDesc, askText));

        ex.setExplanation(buildElectrostaticExplanation(sc, askPotential));
    }

    // =========================================================================
    // CONSTRUCTOR — LORENTZ_MOTION
    // =========================================================================

    private void buildLorentz(SecondBachElectromagnetismExercise ex) {
        ex.setElectromagnetismType(ElectromagnetismType.LORENTZ_MOTION);
        ex.setTolerancePercent(2.0);

        LorentzData sc = LORENTZ.get(random.nextInt(LORENTZ.size()));

        switch (sc.unknownVar()) {
            case "radio_r" -> {
                ex.setUnknownVariable("radio_r");
                ex.setCorrectAnswerValue(sc.r());
                ex.setAnswerUnit("m");
                ex.setCorrectAnswerDisplay(fmtSci2(sc.r()) + " m");
                ex.setStatement(String.format(
                    "Un %s entra perpendicularmente a un campo magnético uniforme " +
                    "B = %.1f T con velocidad v = %s m/s. " +
                    "(|q| = 1,6×10⁻¹⁹ C; m = %s kg.) " +
                    "Calcula el radio r de la trayectoria circular (en m).",
                    sc.particleName(), sc.B(), fmtSci2(sc.v()), fmtSci2(sc.mass())));
            }
            case "velocidad_v" -> {
                ex.setUnknownVariable("velocidad_v");
                ex.setCorrectAnswerValue(sc.v());
                ex.setAnswerUnit("m/s");
                ex.setCorrectAnswerDisplay(fmtSci2(sc.v()) + " m/s");
                ex.setStatement(String.format(
                    "Un %s describe una trayectoria circular de radio r = %s m " +
                    "al entrar perpendicularmente en un campo B = %.1f T. " +
                    "(|q| = 1,6×10⁻¹⁹ C; m = %s kg.) " +
                    "Calcula la velocidad v de la partícula (en m/s).",
                    sc.particleName(), fmtSci2(sc.r()), sc.B(), fmtSci2(sc.mass())));
            }
            default -> {  // frecuencia_f
                ex.setUnknownVariable("frecuencia_f");
                ex.setCorrectAnswerValue(sc.f());
                ex.setAnswerUnit("Hz");
                ex.setCorrectAnswerDisplay(fmtSci2(sc.f()) + " Hz");
                ex.setStatement(String.format(
                    "Un %s se mueve en un campo magnético uniforme B = %.2f T. " +
                    "(|q| = 1,6×10⁻¹⁹ C; m = %s kg.) " +
                    "Calcula la frecuencia de ciclotrón f (en Hz). " +
                    "Nota: f es independiente de la velocidad.",
                    sc.particleName(), sc.B(), fmtSci2(sc.mass())));
            }
        }

        ex.setExplanation(buildLorentzExplanation(sc));
    }

    // =========================================================================
    // CONSTRUCTOR — FARADAY_INDUCTION
    // =========================================================================

    private void buildFaraday(SecondBachElectromagnetismExercise ex) {
        ex.setElectromagnetismType(ElectromagnetismType.FARADAY_INDUCTION);
        ex.setTolerancePercent(2.0);

        FaradayData sc = FARADAY.get(random.nextInt(FARADAY.size()));
        String unknown = sc.unknownVar();

        if ("flujo_max".equals(unknown)) {
            ex.setUnknownVariable("flujo_max");
            ex.setCorrectAnswerValue(sc.phiMax());
            ex.setAnswerUnit("Wb");
            ex.setCorrectAnswerDisplay(fmtSci2(sc.phiMax()) + " Wb");
        } else {
            ex.setUnknownVariable("fem_max");
            ex.setCorrectAnswerValue(sc.emfMax());
            ex.setAnswerUnit("V");
            ex.setCorrectAnswerDisplay(fmtSci2(sc.emfMax()) + " V");
        }

        if (sc.isRotating()) {
            String askText = "flujo_max".equals(unknown)
                ? "Calcula el flujo magnético máximo Φ_max (en Wb)."
                : "Calcula la fuerza electromotriz máxima inducida |ε_max| (en V).";
            ex.setStatement(String.format(
                "Una espira rectangular de %d vueltas y sección A = %.2f m² " +
                "gira con velocidad angular ω = %.0f π rad/s en un campo " +
                "magnético uniforme B = %.1f T. %s",
                sc.N(), sc.A(), sc.omega() / Math.PI, sc.B(), askText));
        } else {
            ex.setStatement(String.format(
                "Una bobina de %d vueltas y sección A = %.2f m² está inmersa " +
                "en un campo magnético que varía uniformemente a razón de " +
                "ΔB/Δt = %.1f T/s. " +
                "Calcula la fuerza electromotriz inducida |ε| (en V).",
                sc.N(), sc.A(), sc.dBdt()));
        }

        ex.setExplanation(buildFaradayExplanation(sc));
    }

    // =========================================================================
    // EXPLICACIÓN — ELECTROSTATIC_SUPERPOSITION
    // =========================================================================

    private String buildElectrostaticExplanation(ElectrostaticData sc, boolean askPotential) {
        var sb = new StringBuilder();

        sb.append("<strong>Posiciones:</strong> Q₁ en x = 0; Q₂ en x = d = ")
          .append(sc.d()).append(" m; P en x = ").append(sc.xP())
          .append(" m → r₁ = ").append(sc.r1()).append(" m, r₂ = ")
          .append(sc.r2()).append(" m\n\n");

        // ── Potencial (suma escalar) ──────────────────────────────────────────
        sb.append("<strong>Potencial eléctrico en P (superposición escalar):</strong>\n\n")
          .append("El potencial es una magnitud escalar. Se suma directamente, ")
          .append("incluyendo el <em>signo de cada carga</em>:\n\n")
          .append("\\[V_P = \\frac{kQ_1}{r_1} + \\frac{kQ_2}{r_2}\\]\n\n")
          .append("\\[V_P = \\frac{9\\times10^9 \\times (").append(fmtK(sc.q1()))
          .append(")}{").append(sc.r1()).append("} + ")
          .append("\\frac{9\\times10^9 \\times (").append(fmtK(sc.q2()))
          .append(")}{").append(sc.r2()).append("}\\]\n\n");

        double v1 = K * sc.q1() / sc.r1();
        double v2 = K * sc.q2() / sc.r2();
        sb.append("\\[V_P = \\left(").append(fmtK(v1)).append("\\right) + \\left(")
          .append(fmtK(v2)).append("\\right) = ").append(fmtK(sc.V()))
          .append("\\,\\text{V}\\]\n\n");

        // ── Campo eléctrico (suma vectorial) ─────────────────────────────────
        sb.append("<strong>Campo eléctrico en P (superposición vectorial):</strong>\n\n")
          .append("El campo eléctrico apunta <em>desde</em> las cargas positivas ")
          .append("y <em>hacia</em> las negativas. Módulo de cada contribución: ")
          .append("\\(|\\vec{E}_i| = k|Q_i|/r_i^2\\)\n\n");

        double e1mod = Math.abs(sc.e1x());
        double e2mod = Math.abs(sc.e2x());
        String dir1 = sc.e1x() > 0 ? "→ (+x)" : "← (−x)";
        String dir2 = sc.e2x() > 0 ? "→ (+x)" : "← (−x)";

        sb.append("\\[|\\vec{E}_1| = \\frac{9\\times10^9 \\times |").append(fmtK(sc.q1()))
          .append("|}{(").append(sc.r1()).append(")^2} = ").append(fmtK(e1mod))
          .append("\\,\\text{V/m} \\quad \\text{dirección: ").append(dir1).append("}\\]\n\n")
          .append("\\[|\\vec{E}_2| = \\frac{9\\times10^9 \\times |").append(fmtK(sc.q2()))
          .append("|}{(").append(sc.r2()).append(")^2} = ").append(fmtK(e2mod))
          .append("\\,\\text{V/m} \\quad \\text{dirección: ").append(dir2).append("}\\]\n\n");

        if (sc.pBetween()) {
            sb.append("P está <strong>entre</strong> las cargas → los vectores apuntan ");
            if (Math.signum(sc.e1x()) != Math.signum(sc.e2x())) {
                sb.append("en sentidos opuestos. El campo neto es la diferencia:\n\n")
                  .append("\\[|\\vec{E}_P| = \\left|\\vec{E}_1 + \\vec{E}_2\\right| = ")
                  .append("\\left|").append(fmtK(sc.e1x())).append(" + (")
                  .append(fmtK(sc.e2x())).append(")\\right| = ")
                  .append(fmtK(sc.E())).append("\\,\\text{V/m}\\]\n\n");
            } else {
                sb.append("en el mismo sentido. La suma es escalar:\n\n")
                  .append("\\[|\\vec{E}_P| = |\\vec{E}_1| + |\\vec{E}_2| = ")
                  .append(fmtK(e1mod)).append(" + ").append(fmtK(e2mod))
                  .append(" = ").append(fmtK(sc.E())).append("\\,\\text{V/m}\\]\n\n");
            }
        } else {
            if (Math.signum(sc.e1x()) == Math.signum(sc.e2x())) {
                sb.append("P está <strong>fuera</strong> del segmento → ambos campos apuntan ")
                  .append("en la misma dirección. Suma escalar:\n\n")
                  .append("\\[|\\vec{E}_P| = |\\vec{E}_1| + |\\vec{E}_2| = ")
                  .append(fmtK(e1mod)).append(" + ").append(fmtK(e2mod))
                  .append(" = ").append(fmtK(sc.E())).append("\\,\\text{V/m}\\]\n\n");
            } else {
                sb.append("P está <strong>fuera</strong> del segmento pero los campos se oponen ")
                  .append("(carga de signo contrario). Campo neto:\n\n")
                  .append("\\[|\\vec{E}_P| = \\left|").append(fmtK(sc.e1x()))
                  .append(" + (").append(fmtK(sc.e2x())).append(")\\right| = ")
                  .append(fmtK(sc.E())).append("\\,\\text{V/m}\\]\n\n");
            }
        }

        String boxed = askPotential
            ? "V_P = \\boxed{" + fmtK(sc.V()) + "\\,\\text{V}}"
            : "|\\vec{E}_P| = \\boxed{" + fmtK(sc.E()) + "\\,\\text{V/m}}";
        sb.append("∴ \\(").append(boxed).append("\\)");
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — LORENTZ_MOTION
    // =========================================================================

    private String buildLorentzExplanation(LorentzData sc) {
        var sb = new StringBuilder();

        sb.append("<strong>Marco teórico — fuerza de Lorentz como centrípeta:</strong>\n\n")
          .append("Cuando una partícula cargada entra ⊥ a \\(\\vec{B}\\), la fuerza magnética ")
          .append("actúa como fuerza centrípeta:\n\n")
          .append("\\[|q|vB = \\frac{mv^2}{r} \\implies r = \\frac{mv}{|q|B}\\]\n\n")
          .append("Despejando v y el período \\(T = 2\\pi r/v\\):\n\n")
          .append("\\[v = \\frac{|q|Br}{m}, \\qquad T = \\frac{2\\pi m}{|q|B}, ")
          .append("\\qquad f = \\frac{|q|B}{2\\pi m}\\]\n\n")
          .append("<em>La frecuencia de ciclotrón f es independiente de la velocidad.</em>\n\n");

        sb.append("<strong>Datos:</strong> ").append(sc.particleName())
          .append(", \\(m = ").append(fmtK(sc.mass())).append("\\,\\text{kg}\\)")
          .append(", \\(|q| = 1{,}6\\times10^{-19}\\,\\text{C}\\)")
          .append(", \\(B = ").append(sc.B()).append("\\,\\text{T}\\)\n\n");

        switch (sc.unknownVar()) {
            case "radio_r" -> {
                sb.append("<strong>Cálculo del radio:</strong>\n\n")
                  .append("\\[r = \\frac{mv}{|q|B} = \\frac{").append(fmtK(sc.mass()))
                  .append("\\times ").append(fmtK(sc.v())).append("}{1{,}6\\times10^{-19}\\times ")
                  .append(sc.B()).append("} = ").append(fmtK(sc.r())).append("\\,\\text{m}\\]\n\n")
                  .append("∴ \\(r = \\boxed{").append(fmtK(sc.r())).append("\\,\\text{m}}\\)");
            }
            case "velocidad_v" -> {
                sb.append("<strong>Despejando la velocidad:</strong>\n\n")
                  .append("\\[v = \\frac{|q|Br}{m} = ")
                  .append("\\frac{1{,}6\\times10^{-19}\\times ").append(sc.B())
                  .append("\\times ").append(fmtK(sc.r())).append("}{")
                  .append(fmtK(sc.mass())).append("} = ")
                  .append(fmtK(sc.v())).append("\\,\\text{m/s}\\]\n\n")
                  .append("∴ \\(v = \\boxed{").append(fmtK(sc.v())).append("\\,\\text{m/s}}\\)");
            }
            default -> {  // frecuencia
                sb.append("<strong>Frecuencia de ciclotrón</strong> (independiente de v):\n\n")
                  .append("\\[f = \\frac{|q|B}{2\\pi m} = ")
                  .append("\\frac{1{,}6\\times10^{-19}\\times ").append(sc.B())
                  .append("}{2\\pi \\times ").append(fmtK(sc.mass()))
                  .append("} = ").append(fmtK(sc.f())).append("\\,\\text{Hz}\\]\n\n")
                  .append("∴ \\(f = \\boxed{").append(fmtK(sc.f())).append("\\,\\text{Hz}}\\)");
            }
        }
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — FARADAY_INDUCTION
    // =========================================================================

    private String buildFaradayExplanation(FaradayData sc) {
        var sb = new StringBuilder();

        if (sc.isRotating()) {
            sb.append("<strong>Marco teórico — espira giratoria en campo uniforme:</strong>\n\n")
              .append("El flujo magnético varía sinusoidalmente:\n\n")
              .append("\\[\\Phi(t) = NBA\\cos(\\omega t) \\implies \\Phi_{\\max} = NBA\\]\n\n")
              .append("Por la ley de Faraday-Lenz, la FEM inducida es:\n\n")
              .append("\\[\\varepsilon(t) = -\\frac{d\\Phi}{dt} = NBA\\omega\\sin(\\omega t) ")
              .append("\\implies |\\varepsilon_{\\max}| = NBA\\omega\\]\n\n")
              .append("<em>Regla de Lenz: el signo negativo indica que la FEM se opone ")
              .append("a la variación del flujo (principio de conservación de la energía).</em>\n\n");

            sb.append("<strong>Datos:</strong> N = ").append(sc.N())
              .append(", B = ").append(sc.B()).append(" T, A = ").append(sc.A())
              .append(" m², ω = ").append(fmtK(sc.omega())).append(" rad/s\n\n");

            sb.append("<strong>Paso 1 — Flujo máximo:</strong>\n\n")
              .append("\\[\\Phi_{\\max} = NBA = ").append(sc.N()).append("\\times ")
              .append(sc.B()).append("\\times ").append(sc.A()).append(" = ")
              .append(fmtK(sc.phiMax())).append("\\,\\text{Wb}\\]\n\n");

            sb.append("<strong>Paso 2 — FEM máxima:</strong>\n\n")
              .append("\\[|\\varepsilon_{\\max}| = NBA\\omega = ").append(fmtK(sc.phiMax()))
              .append("\\times ").append(fmtK(sc.omega())).append(" = ")
              .append(fmtK(sc.emfMax())).append("\\,\\text{V}\\]\n\n");

            String boxed = "flujo_max".equals(sc.unknownVar())
                ? "\\Phi_{\\max} = \\boxed{" + fmtK(sc.phiMax()) + "\\,\\text{Wb}}"
                : "|\\varepsilon_{\\max}| = \\boxed{" + fmtK(sc.emfMax()) + "\\,\\text{V}}";
            sb.append("∴ \\(").append(boxed).append("\\)");

        } else {
            sb.append("<strong>Marco teórico — campo magnético variable en el tiempo:</strong>\n\n")
              .append("Si \\(B\\) varía uniformemente con el tiempo, el flujo varía ")
              .append("linealmente y la FEM inducida es constante:\n\n")
              .append("\\[\\varepsilon = -N\\frac{d\\Phi}{dt} = -NA\\frac{\\Delta B}{\\Delta t}\\]\n\n")
              .append("El módulo es \\(|\\varepsilon| = NA\\left|\\dfrac{\\Delta B}{\\Delta t}\\right|\\). ")
              .append("<em>El signo negativo (Lenz) indica que la corriente inducida genera ")
              .append("un campo que se opone al cambio en B.</em>\n\n");

            sb.append("<strong>Cálculo:</strong>\n\n")
              .append("\\[|\\varepsilon| = NA\\left|\\frac{\\Delta B}{\\Delta t}\\right| = ")
              .append(sc.N()).append("\\times ").append(sc.A()).append("\\times ")
              .append(Math.abs(sc.dBdt())).append(" = ")
              .append(fmtK(sc.emfMax())).append("\\,\\text{V}\\]\n\n")
              .append("∴ \\(|\\varepsilon| = \\boxed{")
              .append(fmtK(sc.emfMax())).append("\\,\\text{V}}\\)");
        }
        return sb.toString();
    }

    // =========================================================================
    // FORMATEADORES
    // =========================================================================

    /**
     * Notación científica simplificada (punto inglés, 2 decimales).
     * Tolerancia adaptativa: el error relativo ±2% es escala-independiente,
     * por lo que la validación funciona correctamente para cualquier orden
     * de magnitud sin ajuste adicional.
     */
    String fmtSci2(double v) {
        if (v == 0) return "0";
        double abs = Math.abs(v);
        int exp = (int) Math.floor(Math.log10(abs));
        double mantissa = v / Math.pow(10, exp);
        String s = String.format(Locale.US, "%.2f", mantissa);
        if (s.startsWith("10.") || s.equals("-10.00")) {
            exp += 1;
            mantissa = v / Math.pow(10, exp);
            s = String.format(Locale.US, "%.2f", mantissa);
        }
        return s + "e" + exp;
    }

    /** KaTeX: "1{,}47 \times 10^{-3}" */
    private String fmtK(double v) {
        if (v == 0) return "0";
        double abs = Math.abs(v);
        if (abs < 1e-300) return "0";
        int exp = (int) Math.floor(Math.log10(abs));
        double mantissa = v / Math.pow(10, exp);
        String s = String.format(Locale.US, "%.2f", mantissa);
        if (s.startsWith("10.") || s.equals("-10.00")) {
            exp += 1;
            mantissa = v / Math.pow(10, exp);
            s = String.format(Locale.US, "%.2f", mantissa);
        }
        String mStr = s.replace(".", "{,}");
        if (exp == 0) return mStr;
        return mStr + " \\times 10^{" + exp + "}";
    }

    /** Formatea una carga en μC con signo: "+4.00e-6" → "+4 μC" */
    private String fmtCharge(double q) {
        double uC = q * 1e6;
        String sign = uC >= 0 ? "+" : "";
        if (uC == Math.floor(uC))
            return sign + (int) uC + " μC";
        return String.format(Locale.US, "%s%.1f μC", sign, uC);
    }
}
