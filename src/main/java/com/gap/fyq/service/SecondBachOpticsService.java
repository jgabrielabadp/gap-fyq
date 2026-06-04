package com.gap.fyq.service;

import com.gap.fyq.model.secondbach.optics.OpticsType;
import com.gap.fyq.model.secondbach.optics.SecondBachOpticsExercise;
import com.gap.fyq.repository.SecondBachOpticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecondBachOpticsService {

    private final SecondBachOpticsRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "2BACH";
    private static final String BLOCK  = "BL4";

    // ── Constantes ────────────────────────────────────────────────────────────
    private static final double C = 3.0e8;      // m/s  velocidad de la luz en el vacío
    private static final double D_NEAR = 0.25;  // m    distancia de visión normal (25 cm)

    // =========================================================================
    // DATA RECORDS
    // =========================================================================

    /**
     * Ley de Snell y ángulo límite.
     * n₁·sin(θ₁) = n₂·sin(θ₂)  (θ en RADIANES internamente, en grados al mostrar)
     * Ángulo límite: sin(θ_c) = n₂/n₁  (solo si n₁ > n₂)
     * Velocidad pedida: c / vAskN  (siempre del medio más denso)
     */
    private record SnellData(
        String medium1, double n1,
        String medium2, double n2,
        double thetaIDeg,   // ángulo de incidencia (grados)
        double thetaRDeg,   // ángulo de refracción (grados)
        boolean hasLimit,
        double thetaCDeg,   // ángulo límite (grados), 0 si !hasLimit
        String vMediumName, // nombre del medio cuya velocidad se pregunta
        double vMediumN,    // índice de refracción de ese medio
        double v            // velocidad = C / vMediumN (m/s)
    ) {}

    /**
     * Lentes delgadas — convenio DIN/cartesiano:
     *   • s  < 0 : objeto a la izquierda del vértice (real)
     *   • s' > 0 : imagen a la derecha (real); s' < 0 (virtual)
     *   • f' > 0 : lente convergente;  f' < 0 : divergente
     * Ecuación de conjugación: 1/f' = 1/s' − 1/s
     * Aumento lateral: m = s'/s
     */
    private record LensData(
        String lensType,   // "convergente" o "divergente"
        double f,          // focal f' (cm)
        double s,          // posición objeto (cm, negativo)
        double sPrime,     // posición imagen (cm)
        double m,          // aumento lateral (sin dimensión, con signo)
        double y,          // tamaño objeto (cm, positivo)
        double yPrime      // tamaño imagen (cm, con signo)
    ) {}

    /**
     * Defectos visuales y corrección en dioptrías (D = 1/f' en metros).
     * Miopía:       lente divergente, P = −1/PR  (PR = punto remoto)
     * Hipermetropía: lente convergente, P = 4 − 1/PP  (PP = punto próximo, obj. a 25 cm)
     */
    private record EyeData(
        String condition,   // "miope" o "hipermétrope"
        double distanceCm,  // PR para miopía, PP para hipermetropía (cm)
        double potency      // dioptrías (negativo si miopía)
    ) {}

    // =========================================================================
    // FÁBRICAS ESTÁTICAS
    // =========================================================================

    /**
     * Calcula ángulo de refracción y ángulo límite a partir de los índices.
     * vAskN: índice del medio cuya velocidad se va a preguntar (siempre el más denso).
     * Toda trigonometría en RADIANES; resultados convertidos a grados para almacenar.
     */
    private static SnellData snell(
            String m1, double n1, String m2, double n2, double thetaIDeg,
            String vAskMedium, double vAskN) {

        double sinR = n1 * Math.sin(Math.toRadians(thetaIDeg)) / n2;
        // Clamp para evitar NaN por error de punto flotante cerca de 90°
        double thetaRDeg = Math.toDegrees(Math.asin(Math.min(sinR, 1.0)));
        boolean hasLimit = n1 > n2;
        double thetaCDeg = hasLimit ? Math.toDegrees(Math.asin(n2 / n1)) : 0.0;
        double v = C / vAskN;
        return new SnellData(m1, n1, m2, n2, thetaIDeg, thetaRDeg,
                             hasLimit, thetaCDeg, vAskMedium, vAskN, v);
    }

    /** 1/s' = 1/f + 1/s  (DIN: s < 0 real object). */
    private static LensData lens(String type, double f, double s, double y) {
        double sPrime = 1.0 / (1.0 / f + 1.0 / s);
        double m      = sPrime / s;
        double yPrime = m * y;
        return new LensData(type, f, s, sPrime, m, y, yPrime);
    }

    private static EyeData myopia(double prCm) {
        double pr = prCm / 100.0;
        return new EyeData("miope", prCm, -1.0 / pr);
    }

    private static EyeData hyperopia(double ppCm) {
        double pp = ppCm / 100.0;
        return new EyeData("hipermétrope", ppCm, 1.0 / D_NEAR - 1.0 / pp);
    }

    // =========================================================================
    // SNELL_REFRACTION_LIMIT — 6 escenarios
    // =========================================================================

    private static final List<SnellData> SNELL = List.of(
        // SN1: aire→vidrio(1,5), θ_i=30° → θ_r≈19,47°, v_vidrio=2,00e8 m/s
        snell("aire", 1.0,  "vidrio",       1.50, 30, "vidrio",       1.50),
        // SN2: aire→agua(1,33), θ_i=45° → θ_r≈32,12°, v_agua≈2,26e8 m/s
        snell("aire", 1.0,  "agua",         1.33, 45, "agua",         1.33),
        // SN3: vidrio→aire,     θ_i=30° → θ_r≈48,59°, θ_c≈41,81°, v_vidrio=2,00e8
        snell("vidrio", 1.5, "aire",        1.0,  30, "vidrio",       1.50),
        // SN4: agua→aire,       θ_i=20° → θ_r≈27,07°, θ_c≈48,75°, v_agua≈2,26e8
        snell("agua",  1.33, "aire",        1.0,  20, "agua",         1.33),
        // SN5: aire→vidrio óptico(1,65), θ_i=45° → θ_r≈25,38°, v≈1,82e8
        snell("aire", 1.0,  "vidrio óptico",1.65, 45, "vidrio óptico",1.65),
        // SN6: vidrio→agua,     θ_i=20° → θ_r≈22,70°, θ_c≈62,46°, v_agua≈2,26e8
        snell("vidrio", 1.5, "agua",        1.33, 20, "agua",         1.33)
    );

    // =========================================================================
    // GEOMETRIC_LENSES — 6 escenarios (4 convergentes, 2 divergentes)
    // =========================================================================

    private static final List<LensData> LENSES = List.of(
        // GL1: conv f'=+20, s=−30, y=2 → s'=60, m=−2,00, y'=−4  (real, invertida, mayor)
        lens("convergente", +20, -30, 2),
        // GL2: conv f'=+15, s=−45, y=4 → s'=22,5, m=−0,50, y'=−2  (real, invertida, menor)
        lens("convergente", +15, -45, 4),
        // GL3: div  f'=−20, s=−30, y=3 → s'=−12, m=+0,40, y'=1,2  (virtual, derecha, menor)
        lens("divergente",  -20, -30, 3),
        // GL4: conv f'=+10, s=−40, y=6 → s'≈13,33, m≈−0,33, y'=−2  (real, invertida, menor)
        lens("convergente", +10, -40, 6),
        // GL5: conv f'=+25, s=−100, y=9 → s'≈33,33, m≈−0,33, y'=−3  (real, invertida, menor)
        lens("convergente", +25,-100, 9),
        // GL6: div  f'=−30, s=−60, y=6 → s'=−20, m≈+0,33, y'=2  (virtual, derecha, menor)
        lens("divergente",  -30, -60, 6)
    );

    // =========================================================================
    // EYE_DEFECTS_DIOPTERS — 3 miopía + 3 hipermetropía
    // =========================================================================

    private static final List<EyeData> EYE = List.of(
        // MY1: miope  PR=200 cm → P=−0,50 D
        myopia(200),
        // MY2: miope  PR=100 cm → P=−1,00 D
        myopia(100),
        // MY3: miope  PR=50 cm  → P=−2,00 D
        myopia(50),
        // HY1: hipermétrope PP=50 cm  → P=4−2=+2,00 D
        hyperopia(50),
        // HY2: hipermétrope PP=100 cm → P=4−1=+3,00 D
        hyperopia(100),
        // HY3: hipermétrope PP=40 cm  → P=4−2,5=+1,50 D
        hyperopia(40)
    );

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public SecondBachOpticsExercise generateAndSave() {
        SecondBachOpticsExercise ex = new SecondBachOpticsExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);
        ex.setExerciseMode("NUMERICAL");

        int roll = random.nextInt(3);
        if      (roll == 0) buildSnell(ex);
        else if (roll == 1) buildLens(ex);
        else                buildEye(ex);

        log.debug("2BACH BL4 generado: type={} var={}",
            ex.getOpticsType(), ex.getUnknownVariable());
        return repository.save(ex);
    }

    public SecondBachOpticsExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ejercicio 2BACH BL4 no encontrado: " + id));
    }

    // =========================================================================
    // CONSTRUCTOR — SNELL_REFRACTION_LIMIT
    // =========================================================================

    private void buildSnell(SecondBachOpticsExercise ex) {
        ex.setOpticsType(OpticsType.SNELL_REFRACTION_LIMIT);
        ex.setTolerancePercent(2.0);

        SnellData sc = SNELL.get(random.nextInt(SNELL.size()));

        // Incógnitas disponibles: θ_r y v siempre; θ_c solo si hasLimit
        int maxIdx = sc.hasLimit() ? 3 : 2;
        int idx = random.nextInt(maxIdx);

        switch (idx) {
            case 0 -> {  // angulo_refraccion
                ex.setUnknownVariable("angulo_refraccion");
                ex.setCorrectAnswerValue(sc.thetaRDeg());
                ex.setAnswerUnit("°");
                ex.setCorrectAnswerDisplay(fmt2(sc.thetaRDeg()) + "°");
                ex.setStatement(String.format(
                    "Un rayo de luz pasa del %s (n₁ = %.2f) al %s (n₂ = %.2f) " +
                    "con un ángulo de incidencia θ₁ = %.0f°. " +
                    "Calcula el ángulo de refracción θ₂ (en grados).",
                    sc.medium1(), sc.n1(), sc.medium2(), sc.n2(), sc.thetaIDeg()));
            }
            case 1 -> {  // velocidad_medio
                ex.setUnknownVariable("velocidad_medio");
                ex.setCorrectAnswerValue(sc.v());
                ex.setAnswerUnit("m/s");
                ex.setCorrectAnswerDisplay(fmtSci2(sc.v()) + " m/s");
                ex.setStatement(String.format(
                    "El índice de refracción del %s es n = %.2f. " +
                    "(c = 3×10⁸ m/s.) Calcula la velocidad de la luz " +
                    "en ese medio (en m/s).",
                    sc.vMediumName(), sc.vMediumN()));
            }
            default -> {  // angulo_limite
                ex.setUnknownVariable("angulo_limite");
                ex.setCorrectAnswerValue(sc.thetaCDeg());
                ex.setAnswerUnit("°");
                ex.setCorrectAnswerDisplay(fmt2(sc.thetaCDeg()) + "°");
                ex.setStatement(String.format(
                    "Un rayo viaja del %s (n₁ = %.2f) al %s (n₂ = %.2f). " +
                    "Calcula el ángulo límite θ_c de reflexión total interna (en grados).",
                    sc.medium1(), sc.n1(), sc.medium2(), sc.n2()));
            }
        }
        ex.setExplanation(buildSnellExplanation(sc, idx));
    }

    // =========================================================================
    // CONSTRUCTOR — GEOMETRIC_LENSES
    // =========================================================================

    private void buildLens(SecondBachOpticsExercise ex) {
        ex.setOpticsType(OpticsType.GEOMETRIC_LENSES);
        ex.setTolerancePercent(2.0);

        LensData sc = LENSES.get(random.nextInt(LENSES.size()));

        String[] vars  = {"posicion_imagen", "aumento_lateral", "tamanyo_imagen"};
        double[] vals  = {sc.sPrime(),        sc.m(),             sc.yPrime()};
        String[] units = {"cm",               "",                 "cm"};
        int idx = random.nextInt(3);

        ex.setUnknownVariable(vars[idx]);
        ex.setCorrectAnswerValue(vals[idx]);
        ex.setAnswerUnit(units[idx]);
        ex.setCorrectAnswerDisplay(fmt2(vals[idx]) + (units[idx].isEmpty() ? "" : " " + units[idx]));

        String fSign = sc.f() > 0 ? "+" : "";
        String askText = switch (vars[idx]) {
            case "posicion_imagen"  ->
                "Calcula la posición de la imagen s′ (en cm, con su signo).";
            case "aumento_lateral" ->
                "Calcula el aumento lateral m (adimensional, con su signo).";
            default ->
                String.format("Calcula el tamaño de la imagen y′ (en cm, con su signo). " +
                              "El objeto tiene una altura y = %.0f cm.", sc.y());
        };

        ex.setStatement(String.format(
            "Un objeto se coloca en s = %.0f cm de una lente %s de distancia focal " +
            "f′ = %s%.0f cm. (Convenio DIN: s < 0 a la izquierda del vértice.) %s",
            sc.s(), sc.lensType(), fSign, sc.f(), askText));

        ex.setExplanation(buildLensExplanation(sc, idx));
    }

    // =========================================================================
    // CONSTRUCTOR — EYE_DEFECTS_DIOPTERS
    // =========================================================================

    private void buildEye(SecondBachOpticsExercise ex) {
        ex.setOpticsType(OpticsType.EYE_DEFECTS_DIOPTERS);
        ex.setTolerancePercent(2.0);

        EyeData sc = EYE.get(random.nextInt(EYE.size()));

        ex.setUnknownVariable("potencia_lente");
        ex.setCorrectAnswerValue(sc.potency());
        ex.setAnswerUnit("D");
        ex.setCorrectAnswerDisplay(fmt2(sc.potency()) + " D");

        if ("miope".equals(sc.condition())) {
            ex.setStatement(String.format(
                "Un paciente miope puede ver con nitidez hasta un máximo de %.0f cm " +
                "(punto remoto PR). Calcula la potencia P de la lente correctora " +
                "necesaria (en dioptrías, D). Una potencia negativa indica lente divergente.",
                sc.distanceCm()));
        } else {
            ex.setStatement(String.format(
                "Un paciente hipermétrope tiene su punto próximo en PP = %.0f cm; " +
                "no puede enfocar objetos a la distancia normal de visión (25 cm). " +
                "Calcula la potencia P de la lente convergente correctora (en D).",
                sc.distanceCm()));
        }

        ex.setExplanation(buildEyeExplanation(sc));
    }

    // =========================================================================
    // EXPLICACIÓN — SNELL_REFRACTION_LIMIT
    // =========================================================================

    private String buildSnellExplanation(SnellData sc, int askedIdx) {
        var sb = new StringBuilder();

        sb.append("<strong>Ley de Snell-Descartes:</strong>\n\n")
          .append("\\[n_1 \\cdot \\sin\\theta_1 = n_2 \\cdot \\sin\\theta_2 ")
          .append("\\quad\\Rightarrow\\quad ")
          .append("\\sin\\theta_2 = \\frac{n_1}{n_2}\\sin\\theta_1\\]\n\n")
          .append("<em>⚠ Los ángulos se miden desde la normal a la superficie y los ")
          .append("cálculos deben realizarse en RADIANES en la calculadora.</em>\n\n");

        sb.append("<strong>Datos:</strong> n₁ = ").append(sc.n1())
          .append(" (").append(sc.medium1()).append("), ")
          .append("n₂ = ").append(sc.n2())
          .append(" (").append(sc.medium2()).append("), ")
          .append("θ₁ = ").append((int) sc.thetaIDeg()).append("°\n\n");

        // Ángulo de refracción
        double sinR = sc.n1() * Math.sin(Math.toRadians(sc.thetaIDeg())) / sc.n2();
        sb.append("<strong>Paso 1 — Ángulo de refracción:</strong>\n\n")
          .append("\\[\\sin\\theta_2 = \\frac{").append(sc.n1())
          .append("}{").append(sc.n2()).append("}\\cdot\\sin(")
          .append((int) sc.thetaIDeg()).append("°) = \\frac{")
          .append(sc.n1()).append("\\times ").append(fmt2(Math.sin(Math.toRadians(sc.thetaIDeg()))))
          .append("}{").append(sc.n2()).append("} = ").append(fmt2(sinR)).append("\\]\n\n")
          .append("\\[\\theta_2 = \\arcsin(").append(fmt2(sinR)).append(") = ")
          .append(fmtK(sc.thetaRDeg())).append("°\\]\n\n");

        // Velocidad en el medio
        sb.append("<strong>Paso 2 — Velocidad en el ").append(sc.vMediumName()).append(":</strong>\n\n")
          .append("\\[v = \\frac{c}{n} = \\frac{3\\times10^8}{").append(sc.vMediumN())
          .append("} = ").append(fmtK(sc.v())).append("\\,\\text{m/s}\\]\n\n");

        // Ángulo límite
        if (sc.hasLimit()) {
            double sinC = sc.n2() / sc.n1();
            sb.append("<strong>Paso 3 — Ángulo límite de reflexión total interna</strong>")
              .append(" (condición θ₂ = 90°, sin θ₂ = 1):\n\n")
              .append("\\[n_1\\sin\\theta_c = n_2 \\cdot 1 \\implies ")
              .append("\\sin\\theta_c = \\frac{n_2}{n_1} = \\frac{")
              .append(sc.n2()).append("}{").append(sc.n1()).append("} = ")
              .append(fmt2(sinC)).append("\\]\n\n")
              .append("\\[\\theta_c = \\arcsin(").append(fmt2(sinC)).append(") = ")
              .append(fmtK(sc.thetaCDeg())).append("°\\]\n\n")
              .append("<em>Para θ₁ > θ_c no existe rayo refractado: toda la luz se refleja.")
              .append("</em>\n\n");
        } else {
            sb.append("<em>Como n₁ < n₂, no existe ángulo límite (la luz siempre se refracta).</em>\n\n");
        }

        String boxed = switch (askedIdx) {
            case 0 -> "\\theta_2 = \\boxed{" + fmtK(sc.thetaRDeg()) + "°}";
            case 1 -> "v = \\boxed{" + fmtK(sc.v()) + "\\,\\text{m/s}}";
            default -> "\\theta_c = \\boxed{" + fmtK(sc.thetaCDeg()) + "°}";
        };
        sb.append("∴ \\(").append(boxed).append("\\)");
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — GEOMETRIC_LENSES
    // =========================================================================

    private String buildLensExplanation(LensData sc, int askedIdx) {
        var sb = new StringBuilder();

        sb.append("<strong>Ecuación de conjugación de lentes delgadas (convenio DIN):</strong>\n\n")
          .append("\\[\\frac{1}{f'} = \\frac{1}{s'} - \\frac{1}{s} ")
          .append("\\quad\\Rightarrow\\quad \\frac{1}{s'} = \\frac{1}{f'} + \\frac{1}{s}\\]\n\n")
          .append("<em>Convenio de signos (DIN/cartesiano):</em>\n\n")
          .append("<ul>")
          .append("<li>\\(s < 0\\): objeto a la <strong>izquierda</strong> (real).</li>")
          .append("<li>\\(s' > 0\\): imagen a la <strong>derecha</strong> (real); "
                + "\\(s' < 0\\) imagen virtual.</li>")
          .append("<li>\\(f' > 0\\): lente convergente; \\(f' < 0\\): divergente.</li>")
          .append("</ul>\n\n");

        sb.append("<strong>Datos:</strong> ")
          .append("s = ").append(fmt2(sc.s())).append(" cm, ")
          .append("f′ = ").append(fmt2(sc.f())).append(" cm\n\n");

        sb.append("<strong>Paso 1 — Posición de la imagen:</strong>\n\n")
          .append("\\[\\frac{1}{s'} = \\frac{1}{").append(fmt2(sc.f())).append("} + ")
          .append("\\frac{1}{(").append(fmt2(sc.s())).append(")} = ")
          .append(fmtFrac(sc.f())).append(" + ").append(fmtFrac(sc.s()))
          .append(" = ").append(fmtFrac(sc.sPrime()))
          .append("\\quad\\Rightarrow\\quad s' = ").append(fmtK(sc.sPrime()))
          .append("\\,\\text{cm}\\]\n\n");

        sb.append("<strong>Paso 2 — Aumento lateral:</strong>\n\n")
          .append("\\[m = \\frac{s'}{s} = \\frac{").append(fmtK(sc.sPrime()))
          .append("}{").append(fmtK(sc.s())).append("} = ")
          .append(fmtK(sc.m())).append("\\]\n\n");

        sb.append("<strong>Paso 3 — Tamaño de la imagen:</strong>\n\n")
          .append("\\[y' = m\\cdot y = ").append(fmtK(sc.m()))
          .append("\\times ").append(fmt2(sc.y()))
          .append(" = ").append(fmtK(sc.yPrime())).append("\\,\\text{cm}\\]\n\n");

        // Naturaleza de la imagen
        String tipo    = sc.sPrime() > 0 ? "real" : "virtual";
        String oriente = sc.m() < 0 ? "invertida" : "derecha";
        String tamano  = Math.abs(sc.m()) > 1 ? "mayor" : "menor";
        sb.append("<strong>Naturaleza de la imagen:</strong> ")
          .append("<em>").append(tipo).append(", ").append(oriente)
          .append(", ").append(tamano).append(" que el objeto.</em>\n\n");
        if (sc.sPrime() > 0) {
            sb.append("<em>s' > 0 → imagen al otro lado de la lente: los rayos reales convergen → imagen <strong>real</strong>.</em>\n\n");
        } else {
            sb.append("<em>s' < 0 → imagen al mismo lado que el objeto: los rayos no convergen realmente; la imagen es la prolongación de los rayos → imagen <strong>virtual</strong>.</em>\n\n");
        }

        String boxed = switch (askedIdx) {
            case 0 -> "s' = \\boxed{" + fmtK(sc.sPrime()) + "\\,\\text{cm}}";
            case 1 -> "m = \\boxed{" + fmtK(sc.m()) + "}";
            default -> "y' = \\boxed{" + fmtK(sc.yPrime()) + "\\,\\text{cm}}";
        };
        sb.append("∴ \\(").append(boxed).append("\\)");
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — EYE_DEFECTS_DIOPTERS
    // =========================================================================

    private String buildEyeExplanation(EyeData sc) {
        var sb = new StringBuilder();

        if ("miope".equals(sc.condition())) {
            double pr = sc.distanceCm() / 100.0;
            sb.append("<strong>Miopía — el ojo miope no puede ver lejos:</strong>\n\n")
              .append("El punto remoto PR está más cerca que el infinito. ")
              .append("La lente correctora debe formar la imagen de un objeto en el ")
              .append("<em>infinito</em> (s → −∞) en el punto remoto PR (imagen virtual):\n\n")
              .append("\\[s \\to -\\infty, \\quad s' = -\\text{PR}\\quad")
              .append("\\text{(imagen virtual a la izquierda = negativa)}\\]\n\n")
              .append("Aplicando la ecuación de conjugación:\n\n")
              .append("\\[\\frac{1}{f'} = \\frac{1}{s'} - \\frac{1}{s} = ")
              .append("\\frac{1}{-\\text{PR}} - 0 = -\\frac{1}{\\text{PR}}\\]\n\n")
              .append("\\[P = \\frac{1}{f'\\,(\\text{m})} = -\\frac{1}{\\text{PR}} = ")
              .append("-\\frac{1}{").append(fmt2(pr)).append("\\,\\text{m}} = ")
              .append(fmtK(sc.potency())).append("\\,\\text{D}\\]\n\n")
              .append("<em>P < 0 → lente divergente, que diverge los rayos paralelos hacia el PR.</em>\n\n");
        } else {
            double pp = sc.distanceCm() / 100.0;
            sb.append("<strong>Hipermetropía — el ojo hipermétrope no puede ver de cerca:</strong>\n\n")
              .append("El punto próximo PP está más lejos de 25 cm. ")
              .append("La lente debe permitir leer a 25 cm (s = −0,25 m) formando ")
              .append("una imagen virtual en PP (s' = −PP):\n\n")
              .append("\\[\\frac{1}{f'} = \\frac{1}{-\\text{PP}} - \\frac{1}{-0{,}25} = ")
              .append("-\\frac{1}{\\text{PP}} + 4\\]\n\n")
              .append("\\[P = 4 - \\frac{1}{\\text{PP}} = 4 - \\frac{1}{")
              .append(fmt2(pp)).append("\\,\\text{m}} = 4 - ")
              .append(fmtK(1.0 / pp)).append(" = ")
              .append(fmtK(sc.potency())).append("\\,\\text{D}\\]\n\n")
              .append("<em>P > 0 → lente convergente, que acerca virtualmente el objeto al PP.</em>\n\n");
        }
        sb.append("∴ \\(P = \\boxed{").append(fmtK(sc.potency())).append("\\,\\text{D}}\\)");
        return sb.toString();
    }

    // =========================================================================
    // FORMATEADORES
    // =========================================================================

    /** 2 decimales fijos (para ángulos, distancias, dioptrías). */
    String fmt2(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    /** Notación científica simplificada: "2.00e8". */
    private String fmtSci2(double v) {
        if (v == 0) return "0";
        double abs = Math.abs(v);
        int exp = (int) Math.floor(Math.log10(abs));
        double mant = v / Math.pow(10, exp);
        String s = String.format(Locale.US, "%.2f", mant);
        if (s.startsWith("10.") || s.equals("-10.00")) {
            exp++; mant = v / Math.pow(10, exp);
            s = String.format(Locale.US, "%.2f", mant);
        }
        return s + "e" + exp;
    }

    /** KaTeX: "1{,}47 \times 10^{8}" o "48{,}59" (sin potencia si exp=0). */
    private String fmtK(double v) {
        if (v == 0) return "0";
        double abs = Math.abs(v);
        if (abs < 1e-300) return "0";
        if (abs >= 0.01 && abs < 100000) {
            return String.format(Locale.US, "%.2f", v).replace(".", "{,}");
        }
        int exp = (int) Math.floor(Math.log10(abs));
        double mant = v / Math.pow(10, exp);
        String s = String.format(Locale.US, "%.2f", mant);
        if (s.startsWith("10.") || s.equals("-10.00")) {
            exp++; mant = v / Math.pow(10, exp);
            s = String.format(Locale.US, "%.2f", mant);
        }
        return s.replace(".", "{,}") + " \\times 10^{" + exp + "}";
    }

    /**
     * Muestra un valor como fracción KaTeX 1/x para la ecuación de conjugación.
     * Ej: 60 → "\frac{1}{60{,}00}", -12 → "\frac{1}{-12{,}00}"
     */
    private String fmtFrac(double v) {
        return "\\dfrac{1}{" + fmtK(v) + "}";
    }
}
