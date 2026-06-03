package com.gap.fyq.service;

import com.gap.fyq.model.secondbach.gravitational.GravitationalType;
import com.gap.fyq.model.secondbach.gravitational.SecondBachGravitationalExercise;
import com.gap.fyq.repository.SecondBachGravitationalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecondBachGravitationalService {

    private final SecondBachGravitationalRepository repository;
    private final Random random = new Random();

    private static final String COURSE  = "2BACH";
    private static final String BLOCK   = "BL1";
    private static final double G       = 6.67e-11;
    private static final double TWO_PI  = 2.0 * Math.PI;

    // ── Masas de cuerpos celestes (kg) ────────────────────────────────────────
    private static final double M_EARTH   = 5.972e24;
    private static final double M_MOON    = 7.342e22;
    private static final double M_MARS    = 6.390e23;
    private static final double M_JUPITER = 1.898e27;

    // ── Radios de cuerpos celestes (m) ────────────────────────────────────────
    private static final double R_EARTH   = 6.371e6;
    private static final double R_MOON    = 1.737e6;
    private static final double R_MARS    = 3.390e6;
    private static final double R_JUPITER = 7.149e7;

    // =========================================================================
    // DATA RECORDS
    // =========================================================================

    /**
     * Satélite en órbita circular.
     * r = bodyRadius + h; v = √(GM/r); T = 2πr/v; Eₘ = −GMm/(2r) [negativo]
     */
    private record OrbitalData(
        String bodyName, double bodyMass, double bodyRadius,
        double h, double satMass,
        double r, double vOrb, double tOrb, double eMec
    ) {}

    /**
     * Velocidad de escape (isEscapeVelocity=true) o trabajo para cambiar de
     * órbita / escapar (isEscapeVelocity=false).
     * W = Eₘ₂ − Eₘ₁; si h2=∞ → Eₘ₂=0 → W=−Eₘ₁ > 0.
     */
    private record EscapeData(
        boolean isEscapeVelocity,
        String bodyName, double bodyMass, double bodyRadius,
        double h1, double h2, double satMass,
        double vEsc, double eMec1, double eMec2, double work
    ) {}

    /**
     * Dos masas puntuales: M1 en x=0, M2 en x=d.
     * V = −GM1/r1 − GM2/r2  [siempre negativo]
     * gMod = |suma vectorial de los dos campos en P|
     * pIsOutside = P está fuera del segmento [0, d]
     */
    private record FieldData(
        double m1, double m2, double d, double xP,
        double r1, double r2,
        double V, double gMod, double gNet,
        boolean pIsOutside
    ) {}

    // =========================================================================
    // FÁBRICAS ESTÁTICAS
    // =========================================================================

    private static OrbitalData orbit(String name, double M, double R,
                                     double h, double m) {
        double r  = R + h;
        double gm = G * M;
        double v  = Math.sqrt(gm / r);
        double T  = TWO_PI * r / v;
        double Em = -gm * m / (2.0 * r);
        return new OrbitalData(name, M, R, h, m, r, v, T, Em);
    }

    private static EscapeData escVel(String name, double M, double R) {
        double gm = G * M;
        double v  = Math.sqrt(2.0 * gm / R);
        return new EscapeData(true, name, M, R, 0, 0, 0, v, 0, 0, 0);
    }

    private static EscapeData workChange(String name, double M, double R,
                                         double h1, double h2, double m) {
        double gm  = G * M;
        double r1  = R + h1;
        double r2  = (h2 == Double.POSITIVE_INFINITY) ? Double.POSITIVE_INFINITY : R + h2;
        double Em1 = -gm * m / (2.0 * r1);
        double Em2 = (Double.isInfinite(r2)) ? 0.0 : -gm * m / (2.0 * r2);
        double W   = Em2 - Em1;
        return new EscapeData(false, name, M, R, h1, h2, m, 0, Em1, Em2, W);
    }

    private static FieldData field(double m1, double m2, double d, double xP) {
        double r1  = Math.abs(xP);
        double r2  = Math.abs(xP - d);
        double V   = -G * m1 / r1 - G * m2 / r2;
        // g1 apunta hacia M1 (en x=0)
        double g1x = (xP > 0) ? -G * m1 / (r1 * r1) :  G * m1 / (r1 * r1);
        // g2 apunta hacia M2 (en x=d)
        double g2x = (xP > d) ? -G * m2 / (r2 * r2) :  G * m2 / (r2 * r2);
        double gNet = g1x + g2x;
        return new FieldData(m1, m2, d, xP, r1, r2, V, Math.abs(gNet), gNet,
                             xP > d || xP < 0);
    }

    // =========================================================================
    // ORBITAL_MECHANICS — 6 escenarios con distintos cuerpos y alturas
    // v = √(GM/r)  ·  T = 2πr/v  ·  Eₘ = −GMm/(2r) < 0
    // =========================================================================

    private static final List<OrbitalData> ORBITAL = List.of(
        // OM1: LEO Tierra, h=400 km (tipo ISS), m=500 kg
        orbit("la Tierra",  M_EARTH,   R_EARTH,   400e3,  500),
        // OM2: Órbita baja lunar, h=100 km, m=200 kg
        orbit("la Luna",    M_MOON,    R_MOON,    100e3,  200),
        // OM3: Órbita marciana baja, h=300 km, m=1000 kg
        orbit("Marte",      M_MARS,    R_MARS,    300e3, 1000),
        // OM4: Órbita joviana, h=2000 km, m=2000 kg
        orbit("Júpiter",    M_JUPITER, R_JUPITER, 2000e3, 2000),
        // OM5: Tierra, h=2000 km, m=800 kg
        orbit("la Tierra",  M_EARTH,   R_EARTH,   2000e3,  800),
        // OM6: Luna, h=200 km, m=1500 kg
        orbit("la Luna",    M_MOON,    R_MOON,    200e3,  1500)
    );

    // =========================================================================
    // ESCAPE_VELOCITY_WORK — 3 velocidades de escape + 3 trabajos de cambio
    // v_e = √(2GM/R)  ·  W = Eₘ₂ − Eₘ₁
    // =========================================================================

    private static final List<EscapeData> ESCAPE = List.of(
        // EV1: velocidad de escape desde la Tierra
        escVel("la Tierra", M_EARTH,   R_EARTH),
        // EV2: velocidad de escape desde la Luna
        escVel("la Luna",   M_MOON,    R_MOON),
        // EV3: velocidad de escape desde Marte
        escVel("Marte",     M_MARS,    R_MARS),
        // EV4: Tierra, h1=400 km → h2=800 km, m=500 kg
        workChange("la Tierra", M_EARTH, R_EARTH, 400e3,  800e3,  500),
        // EV5: Luna, h1=100 km → h2=400 km, m=200 kg
        workChange("la Luna",   M_MOON,  R_MOON,  100e3,  400e3,  200),
        // EV6: Marte, h=300 km → escape al infinito, m=1000 kg
        workChange("Marte",     M_MARS,  R_MARS,  300e3,  Double.POSITIVE_INFINITY, 1000)
    );

    // =========================================================================
    // FIELD_POTENTIAL_POINTS — 6 superposiciones de campo/potencial
    // M1 en x=0, M2 en x=d. P puede estar entre ellas o fuera.
    // V = −GM1/r1 − GM2/r2  ·  g⃗ = suma vectorial
    // =========================================================================

    private static final List<FieldData> FIELD = List.of(
        // FP1: P fuera a la derecha. M1=6e24, M2=2e24, d=6e8, P=9e8
        field(6.0e24, 2.0e24, 6.0e8, 9.0e8),
        // FP2: P entre las masas. M1=8e24, M2=2e24, d=5e8, P=2e8
        field(8.0e24, 2.0e24, 5.0e8, 2.0e8),
        // FP3: Escala solar, P entre masas. M1=5e30, M2=2e30, d=1.5e11, P=1e11
        field(5.0e30, 2.0e30, 1.5e11, 1.0e11),
        // FP4: P fuera a la derecha. M1=3e24, M2=6e24, d=8e8, P=1.2e9
        field(3.0e24, 6.0e24, 8.0e8, 1.2e9),
        // FP5: P fuera a la derecha. M1=4e22, M2=2e23, d=4e8, P=6e8
        field(4.0e22, 2.0e23, 4.0e8, 6.0e8),
        // FP6: P entre las masas. M1=9e23, M2=4e23, d=3e9, P=1e9
        field(9.0e23, 4.0e23, 3.0e9, 1.0e9)
    );

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public SecondBachGravitationalExercise generateAndSave() {
        SecondBachGravitationalExercise ex = new SecondBachGravitationalExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);
        ex.setExerciseMode("NUMERICAL");

        int roll = random.nextInt(3);
        if      (roll == 0) buildOrbital(ex);
        else if (roll == 1) buildEscape(ex);
        else                buildField(ex);

        log.debug("2BACH BL1 generado: type={} var={}",
            ex.getGravitationalType(), ex.getUnknownVariable());
        return repository.save(ex);
    }

    public SecondBachGravitationalExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ejercicio 2BACH BL1 no encontrado: " + id));
    }

    // =========================================================================
    // CONSTRUCTOR — ORBITAL_MECHANICS
    // =========================================================================

    private void buildOrbital(SecondBachGravitationalExercise ex) {
        ex.setGravitationalType(GravitationalType.ORBITAL_MECHANICS);
        ex.setTolerancePercent(2.0);

        OrbitalData sc = ORBITAL.get(random.nextInt(ORBITAL.size()));

        String[] vars  = {"v_orbital",   "T_orbital",  "E_mecanica"};
        double[] vals  = {sc.vOrb(),      sc.tOrb(),    sc.eMec()};
        String[] units = {"m/s",          "s",          "J"};
        String[] asks  = {
            "Calcula la velocidad orbital v (en m/s).",
            "Calcula el período orbital T (en s).",
            "Calcula la energía mecánica total Eₘ (en J). Recuerda: el resultado debe ser negativo."
        };
        int idx = random.nextInt(3);

        ex.setUnknownVariable(vars[idx]);
        ex.setCorrectAnswerValue(vals[idx]);
        ex.setAnswerUnit(units[idx]);
        ex.setCorrectAnswerDisplay(fmtSci2(vals[idx]) + " " + units[idx]);

        ex.setStatement(String.format(
            "Un satélite de %.0f kg orbita %s en una órbita circular a una altura " +
            "h = %.0f km sobre la superficie. La masa de %s es M = %s kg y su radio " +
            "R = %s m. (G = 6,67×10⁻¹¹ N·m²/kg².) %s",
            sc.satMass(), sc.bodyName(), sc.h() / 1000.0,
            sc.bodyName(), fmtSci2(sc.bodyMass()), fmtSci2(sc.bodyRadius()),
            asks[idx]));

        ex.setExplanation(buildOrbitalExplanation(sc, idx));
    }

    // =========================================================================
    // CONSTRUCTOR — ESCAPE_VELOCITY_WORK
    // =========================================================================

    private void buildEscape(SecondBachGravitationalExercise ex) {
        ex.setGravitationalType(GravitationalType.ESCAPE_VELOCITY_WORK);
        ex.setTolerancePercent(2.0);

        EscapeData sc = ESCAPE.get(random.nextInt(ESCAPE.size()));

        if (sc.isEscapeVelocity()) {
            ex.setUnknownVariable("v_escape");
            ex.setCorrectAnswerValue(sc.vEsc());
            ex.setAnswerUnit("m/s");
            ex.setCorrectAnswerDisplay(fmtSci2(sc.vEsc()) + " m/s");

            ex.setStatement(String.format(
                "La masa de %s es M = %s kg y su radio R = %s m. " +
                "(G = 6,67×10⁻¹¹ N·m²/kg².) Calcula la velocidad de escape " +
                "desde la superficie de %s (en m/s).",
                sc.bodyName(), fmtSci2(sc.bodyMass()), fmtSci2(sc.bodyRadius()),
                sc.bodyName()));
        } else {
            ex.setUnknownVariable("trabajo_orbital");
            ex.setCorrectAnswerValue(sc.work());
            ex.setAnswerUnit("J");
            ex.setCorrectAnswerDisplay(fmtSci2(sc.work()) + " J");

            if (Double.isInfinite(sc.h2())) {
                ex.setStatement(String.format(
                    "Un satélite de %.0f kg orbita %s a h₁ = %.0f km. " +
                    "La masa de %s es M = %s kg y su radio R = %s m. " +
                    "(G = 6,67×10⁻¹¹ N·m²/kg².) Calcula el trabajo mínimo W " +
                    "para sacarlo del campo gravitatorio de %s (en J).",
                    sc.satMass(), sc.bodyName(), sc.h1() / 1000.0,
                    sc.bodyName(), fmtSci2(sc.bodyMass()), fmtSci2(sc.bodyRadius()),
                    sc.bodyName()));
            } else {
                ex.setStatement(String.format(
                    "Un satélite de %.0f kg orbita %s a h₁ = %.0f km. " +
                    "Se desea trasladarlo a una órbita de h₂ = %.0f km. " +
                    "La masa de %s es M = %s kg y su radio R = %s m. " +
                    "(G = 6,67×10⁻¹¹ N·m²/kg².) Calcula el trabajo W = Eₘ₂ − Eₘ₁ (en J).",
                    sc.satMass(), sc.bodyName(), sc.h1() / 1000.0, sc.h2() / 1000.0,
                    sc.bodyName(), fmtSci2(sc.bodyMass()), fmtSci2(sc.bodyRadius())));
            }
        }

        ex.setExplanation(buildEscapeExplanation(sc));
    }

    // =========================================================================
    // CONSTRUCTOR — FIELD_POTENTIAL_POINTS
    // =========================================================================

    private void buildField(SecondBachGravitationalExercise ex) {
        ex.setGravitationalType(GravitationalType.FIELD_POTENTIAL_POINTS);
        ex.setTolerancePercent(2.0);

        FieldData sc = FIELD.get(random.nextInt(FIELD.size()));
        boolean askPotential = random.nextBoolean();

        if (askPotential) {
            ex.setUnknownVariable("potencial_V");
            ex.setCorrectAnswerValue(sc.V());
            ex.setAnswerUnit("J/kg");
            ex.setCorrectAnswerDisplay(fmtSci2(sc.V()) + " J/kg");
        } else {
            ex.setUnknownVariable("campo_g");
            ex.setCorrectAnswerValue(sc.gMod());
            ex.setAnswerUnit("N/kg");
            ex.setCorrectAnswerDisplay(fmtSci2(sc.gMod()) + " N/kg");
        }

        String posDesc = sc.pIsOutside()
            ? "a " + fmtSci2(sc.xP() - sc.d()) + " m a la derecha de M₂"
            : "entre M₁ y M₂, a " + fmtSci2(sc.r1()) + " m de M₁";

        String askText = askPotential
            ? "Calcula el potencial gravitatorio V en P (en J/kg). Recuerda: V siempre es negativo."
            : "Calcula el módulo de la intensidad del campo gravitatorio |g⃗| en P (en N/kg).";

        ex.setStatement(String.format(
            "Dos masas puntuales M₁ = %s kg y M₂ = %s kg se sitúan separadas " +
            "una distancia d = %s m. El punto P se encuentra %s. " +
            "(G = 6,67×10⁻¹¹ N·m²/kg².) %s",
            fmtSci2(sc.m1()), fmtSci2(sc.m2()), fmtSci2(sc.d()),
            posDesc, askText));

        ex.setExplanation(buildFieldExplanation(sc, askPotential));
    }

    // =========================================================================
    // EXPLICACIÓN — ORBITAL_MECHANICS
    // =========================================================================

    private String buildOrbitalExplanation(OrbitalData sc, int askedIdx) {
        double gm = G * sc.bodyMass();
        var sb = new StringBuilder();

        sb.append("<strong>Marco teórico — condición de órbita circular:</strong>\n\n")
          .append("La fuerza gravitatoria proporciona la fuerza centrípeta:\n\n")
          .append("\\[\\frac{GMm}{r^2} = \\frac{mv^2}{r} \\implies v = \\sqrt{\\frac{GM}{r}}\\]\n\n");

        sb.append("<strong>Datos:</strong>\n\n")
          .append("<ul>")
          .append("<li>Masa del cuerpo: \\(M = ").append(fmtK(sc.bodyMass())).append("\\,\\text{kg}\\)</li>")
          .append("<li>Radio del cuerpo: \\(R = ").append(fmtK(sc.bodyRadius())).append("\\,\\text{m}\\)</li>")
          .append("<li>Altura de la órbita: \\(h = ").append(fmtK(sc.h())).append("\\,\\text{m}\\)</li>")
          .append("<li>Radio orbital: \\(r = R + h = ").append(fmtK(sc.bodyRadius()))
          .append(" + ").append(fmtK(sc.h())).append(" = ").append(fmtK(sc.r())).append("\\,\\text{m}\\)</li>")
          .append("</ul>\n\n");

        sb.append("<strong>Paso 1 — Velocidad orbital:</strong>\n\n")
          .append("\\[v = \\sqrt{\\frac{GM}{r}} = \\sqrt{\\frac{").append(fmtK(gm))
          .append("}{").append(fmtK(sc.r())).append("}} = ")
          .append(fmtK(sc.vOrb())).append("\\,\\text{m/s}\\]\n\n");

        sb.append("<strong>Paso 2 — Período orbital:</strong>\n\n")
          .append("\\[T = \\frac{2\\pi r}{v} = \\frac{2\\pi \\times ").append(fmtK(sc.r()))
          .append("}{").append(fmtK(sc.vOrb())).append("} = ")
          .append(fmtK(sc.tOrb())).append("\\,\\text{s}\\]\n\n");

        sb.append("<strong>Paso 3 — Energía mecánica total</strong> ")
          .append("<em>(siempre negativa en órbita ligada)</em>:\n\n")
          .append("\\(E_c = \\frac{1}{2}mv^2 = \\frac{GMm}{2r}\\); ")
          .append("\\(E_p = -\\frac{GMm}{r}\\) ")
          .append("<em>(signo negativo: campo atractivo, cero de energía en el infinito)</em>.\n\n")
          .append("\\[E_m = E_c + E_p = \\frac{GMm}{2r} - \\frac{GMm}{r} = -\\frac{GMm}{2r}\\]\n\n")
          .append("\\[E_m = -\\frac{").append(fmtK(gm)).append("\\times ")
          .append(fmt0(sc.satMass())).append("}{2\\times ").append(fmtK(sc.r()))
          .append("} = ").append(fmtK(sc.eMec())).append("\\,\\text{J}\\]\n\n")
          .append("<em>Resultado negativo ⟹ órbita ligada. Se necesita trabajo positivo para escapar.</em>\n\n");

        String boxed = switch (askedIdx) {
            case 0 -> "v = \\boxed{" + fmtK(sc.vOrb()) + "\\,\\text{m/s}}";
            case 1 -> "T = \\boxed{" + fmtK(sc.tOrb()) + "\\,\\text{s}}";
            default -> "E_m = \\boxed{" + fmtK(sc.eMec()) + "\\,\\text{J}}";
        };
        sb.append("∴ \\(").append(boxed).append("\\)");
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — ESCAPE_VELOCITY_WORK
    // =========================================================================

    private String buildEscapeExplanation(EscapeData sc) {
        double gm = G * sc.bodyMass();
        var sb = new StringBuilder();

        if (sc.isEscapeVelocity()) {
            sb.append("<strong>Marco teórico — velocidad de escape:</strong>\n\n")
              .append("Condición de escape: \\(E_{m,\\infty} = 0\\). Desde la superficie:\n\n")
              .append("\\[E_m = \\frac{1}{2}mv_e^2 - \\frac{GMm}{R} = 0 ")
              .append("\\implies v_e = \\sqrt{\\frac{2GM}{R}}\\]\n\n")
              .append("<em>La masa del proyectil no influye: la velocidad de escape es ")
              .append("independiente de la masa lanzada.</em>\n\n");

            double radicand = 2.0 * gm / sc.bodyRadius();
            sb.append("<strong>Cálculo:</strong>\n\n")
              .append("\\[v_e = \\sqrt{\\frac{2\\times ").append(fmtK(gm))
              .append("}{").append(fmtK(sc.bodyRadius())).append("}} = \\sqrt{")
              .append(fmtK(radicand)).append("} = ")
              .append(fmtK(sc.vEsc())).append("\\,\\text{m/s}\\]\n\n")
              .append("∴ \\(v_e = \\boxed{").append(fmtK(sc.vEsc())).append("\\,\\text{m/s}}\\)");

        } else {
            double r1 = sc.bodyRadius() + sc.h1();
            sb.append("<strong>Marco teórico — trabajo para cambiar de órbita:</strong>\n\n")
              .append("El trabajo externo realizado sobre el satélite es:\n\n")
              .append("\\[W = E_{m_2} - E_{m_1} = \\left(-\\frac{GMm}{2r_2}\\right) - ")
              .append("\\left(-\\frac{GMm}{2r_1}\\right) = \\frac{GMm}{2}\\left(")
              .append("\\frac{1}{r_1}-\\frac{1}{r_2}\\right)\\]\n\n");

            sb.append("<strong>Energía mecánica en la órbita inicial:</strong>\n\n")
              .append("\\[r_1 = R + h_1 = ").append(fmtK(sc.bodyRadius())).append(" + ")
              .append(fmtK(sc.h1())).append(" = ").append(fmtK(r1)).append("\\,\\text{m}\\]\n\n")
              .append("\\[E_{m_1} = -\\frac{").append(fmtK(gm)).append("\\times ")
              .append(fmt0(sc.satMass())).append("}{2\\times ").append(fmtK(r1))
              .append("} = ").append(fmtK(sc.eMec1())).append("\\,\\text{J}\\]\n\n");

            if (Double.isInfinite(sc.h2())) {
                sb.append("<strong>Órbita final — infinito:</strong>\n\n")
                  .append("\\[E_{m_2} = 0\\,\\text{J}\\]")
                  .append(" <em>(en el infinito la energía mecánica del sistema es nula)</em>\n\n")
                  .append("<strong>Trabajo mínimo para escapar desde la órbita:</strong>\n\n")
                  .append("\\[W = E_{m_2} - E_{m_1} = 0 - \\left(").append(fmtK(sc.eMec1()))
                  .append("\\right) = ").append(fmtK(sc.work())).append("\\,\\text{J}\\]\n\n")
                  .append("<em>W &gt; 0: hay que aportar energía para vencer la ligadura gravitatoria. ")
                  .append("Nótese que W = |Eₘ₁|: el trabajo de escape iguala la energía de ligadura.</em>\n\n");
            } else {
                double r2 = sc.bodyRadius() + sc.h2();
                sb.append("<strong>Energía mecánica en la órbita final:</strong>\n\n")
                  .append("\\[r_2 = R + h_2 = ").append(fmtK(sc.bodyRadius())).append(" + ")
                  .append(fmtK(sc.h2())).append(" = ").append(fmtK(r2)).append("\\,\\text{m}\\]\n\n")
                  .append("\\[E_{m_2} = -\\frac{").append(fmtK(gm)).append("\\times ")
                  .append(fmt0(sc.satMass())).append("}{2\\times ").append(fmtK(r2))
                  .append("} = ").append(fmtK(sc.eMec2())).append("\\,\\text{J}\\]\n\n")
                  .append("<strong>Trabajo para el cambio de órbita:</strong>\n\n")
                  .append("\\[W = E_{m_2} - E_{m_1} = \\left(").append(fmtK(sc.eMec2()))
                  .append("\\right) - \\left(").append(fmtK(sc.eMec1()))
                  .append("\\right) = ").append(fmtK(sc.work())).append("\\,\\text{J}\\]\n\n")
                  .append("<em>W &gt; 0: la órbita más alta tiene mayor energía mecánica ")
                  .append("(menos negativa). Hay que suministrar energía al sistema.</em>\n\n");
            }
            sb.append("∴ \\(W = \\boxed{").append(fmtK(sc.work())).append("\\,\\text{J}}\\)");
        }
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — FIELD_POTENTIAL_POINTS
    // =========================================================================

    private String buildFieldExplanation(FieldData sc, boolean askPotential) {
        double g1 = G * sc.m1() / (sc.r1() * sc.r1());
        double g2 = G * sc.m2() / (sc.r2() * sc.r2());
        double v1 = -G * sc.m1() / sc.r1();
        double v2 = -G * sc.m2() / sc.r2();
        var sb = new StringBuilder();

        sb.append("<strong>Posiciones relativas:</strong> M₁ en x = 0; M₂ en x = d = ")
          .append(fmtK(sc.d())).append(" m; P en x = ").append(fmtK(sc.xP()))
          .append(" m → r₁ = ").append(fmtK(sc.r1())).append(" m, r₂ = ")
          .append(fmtK(sc.r2())).append(" m\n\n");

        // ── Potencial gravitatorio ─────────────────────────────────────────────
        sb.append("<strong>Potencial gravitatorio en P (superposición escalar):</strong>\n\n")
          .append("El potencial es una magnitud escalar; ambas contribuciones son siempre negativas ")
          .append("<em>(cero de energía en el infinito)</em>:\n\n")
          .append("\\[V_P = V_1 + V_2 = -\\frac{GM_1}{r_1} - \\frac{GM_2}{r_2}\\]\n\n")
          .append("\\[V_P = -\\frac{6{,}67\\times10^{-11}\\times").append(fmtK(sc.m1()))
          .append("}{").append(fmtK(sc.r1())).append("} - ")
          .append("\\frac{6{,}67\\times10^{-11}\\times").append(fmtK(sc.m2()))
          .append("}{").append(fmtK(sc.r2())).append("}\\]\n\n")
          .append("\\[V_P = \\left(").append(fmtK(v1)).append("\\right) + \\left(")
          .append(fmtK(v2)).append("\\right) = ").append(fmtK(sc.V()))
          .append("\\,\\text{J/kg}\\]\n\n");

        // ── Intensidad del campo ───────────────────────────────────────────────
        sb.append("<strong>Intensidad del campo gravitatorio en P (superposición vectorial):</strong>\n\n")
          .append("El campo de cada masa apunta hacia ella y tiene módulo \\(|\\vec{g}| = GM/r^2\\):\n\n")
          .append("\\[|\\vec{g}_1| = \\frac{GM_1}{r_1^2} = \\frac{6{,}67\\times10^{-11}\\times")
          .append(fmtK(sc.m1())).append("}{\\left(").append(fmtK(sc.r1()))
          .append("\\right)^2} = ").append(fmtK(g1)).append("\\,\\text{N/kg}\\]\n\n")
          .append("\\[|\\vec{g}_2| = \\frac{GM_2}{r_2^2} = \\frac{6{,}67\\times10^{-11}\\times")
          .append(fmtK(sc.m2())).append("}{\\left(").append(fmtK(sc.r2()))
          .append("\\right)^2} = ").append(fmtK(g2)).append("\\,\\text{N/kg}\\]\n\n");

        if (sc.pIsOutside()) {
            sb.append("P está fuera del segmento ⟹ ambos vectores de campo apuntan en la <strong>misma ")
              .append("dirección</strong>. La suma vectorial se reduce a suma de módulos:\n\n")
              .append("\\[|\\vec{g}_P| = |\\vec{g}_1| + |\\vec{g}_2| = ")
              .append(fmtK(g1)).append(" + ").append(fmtK(g2))
              .append(" = ").append(fmtK(sc.gMod())).append("\\,\\text{N/kg}\\]\n\n");
        } else {
            sb.append("P está entre M₁ y M₂ ⟹ los vectores apuntan en <strong>sentidos opuestos</strong>. ")
              .append("El módulo resultante es la diferencia de módulos:\n\n")
              .append("\\[|\\vec{g}_P| = \\bigl||\\vec{g}_1| - |\\vec{g}_2|\\bigr| = ")
              .append("\\left|").append(fmtK(g1)).append(" - ").append(fmtK(g2))
              .append("\\right| = ").append(fmtK(sc.gMod())).append("\\,\\text{N/kg}\\]\n\n")
              .append("<em>La dirección del campo resultante apunta hacia la masa de mayor atracción.</em>\n\n");
        }

        String boxed = askPotential
            ? "V_P = \\boxed{" + fmtK(sc.V()) + "\\,\\text{J/kg}}"
            : "|\\vec{g}_P| = \\boxed{" + fmtK(sc.gMod()) + "\\,\\text{N/kg}}";
        sb.append("∴ \\(").append(boxed).append("\\)");
        return sb.toString();
    }

    // =========================================================================
    // FORMATEADORES
    // =========================================================================

    /**
     * Notación científica simplificada con 2 decimales (punto inglés):
     * e.g. -1.47e10, 3.45e3, 1.98e-3
     */
    String fmtSci2(double v) {
        if (v == 0) return "0";
        double abs = Math.abs(v);
        int exp = (int) Math.floor(Math.log10(abs));
        double mantissa = v / Math.pow(10, exp);
        String s = String.format(Locale.US, "%.2f", mantissa);
        // Corrige el caso límite 9.99... → 10.00
        if (s.startsWith("10.") || s.equals("-10.00")) {
            exp += 1;
            mantissa = v / Math.pow(10, exp);
            s = String.format(Locale.US, "%.2f", mantissa);
        }
        return s + "e" + exp;
    }

    /**
     * Formato KaTeX: "1{,}47 \times 10^{10}" o "1{,}47 \times 10^{-3}"
     */
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

    /** Para masas de satélites (números enteros pequeños) */
    private String fmt0(double v) {
        return String.format(Locale.US, "%.0f", v);
    }
}
