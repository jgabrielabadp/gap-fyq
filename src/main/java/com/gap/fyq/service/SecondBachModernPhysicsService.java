package com.gap.fyq.service;

import com.gap.fyq.model.secondbach.modernphysics.ModernPhysicsType;
import com.gap.fyq.model.secondbach.modernphysics.SecondBachModernPhysicsExercise;
import com.gap.fyq.repository.SecondBachModernPhysicsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecondBachModernPhysicsService {

    private final SecondBachModernPhysicsRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "2BACH";
    private static final String BLOCK  = "BL5";

    // ── Constantes físicas universales ────────────────────────────────────────
    private static final double H    = 6.63e-34;   // J·s  constante de Planck
    private static final double C    = 3.0e8;      // m/s  velocidad de la luz en el vacío
    private static final double E    = 1.6e-19;    // C    carga elemental (1 eV = E Julios)
    private static final double M_E  = 9.11e-31;   // kg   masa del electrón
    private static final double LN2  = Math.log(2); // ln 2 ≈ 0.6931

    // =========================================================================
    // DATA RECORDS
    // =========================================================================

    /**
     * Efecto fotoeléctrico (Einstein): E_k = hf − W₀
     * Frecuencia umbral: f₀ = W₀/h
     * Potencial de frenado: V_stop = E_k/e
     */
    private record PhotoData(
        String metalName,
        double workEV,   // función de trabajo (eV)
        double workJ,    // W₀ en julios = workEV × E
        double freq,     // frecuencia incidente (Hz)
        double f0,       // frecuencia umbral (Hz)
        double ekMax,    // energía cinética máxima (J)
        double vStop     // potencial de frenado (V)
    ) {}

    /**
     * Longitud de onda de De Broglie: λ = h/(m·v)
     */
    private record DeBroglieData(
        double mass,     // kg
        double velocity, // m/s
        double lambda    // m
    ) {}

    /**
     * Cinemática relativista: γ = 1/√(1−β²), β = v/c
     * Dilatación temporal: t = γ·t₀
     * Contracción de longitudes: L = L₀/γ
     */
    private record LorentzData(
        double beta,         // v/c
        double gamma,
        double t0,           // tiempo propio (s)
        double tDilated,     // tiempo dilatado (s)
        double L0,           // longitud propia (m)
        double LContracted   // longitud contraída (m)
    ) {}

    /**
     * Desintegración radiactiva: N(t) = N₀·e^(−λt)
     * unknownType: "constante_lambda" | "actividad_inicial" | "masa_remanente"
     */
    private record RadioactiveData(
        String unknownType,
        double halfLifeS,    // T½ (s)
        double n0,           // nucleos iniciales
        double m0Grams,      // masa inicial (g)
        double timeS,        // tiempo transcurrido (s)
        double lambda,       // constante radiactiva (1/s)
        double a0,           // actividad inicial (Bq)
        double mRemaining    // masa remanente (g)
    ) {}

    // =========================================================================
    // FÁBRICAS ESTÁTICAS
    // =========================================================================

    private static PhotoData photo(String name, double workEV, double freqHz) {
        double workJ = workEV * E;
        double f0    = workJ / H;
        double ekMax = H * freqHz - workJ;
        double vStop = ekMax / E;
        return new PhotoData(name, workEV, workJ, freqHz, f0, ekMax, vStop);
    }

    private static DeBroglieData deBroglie(double mass, double vel) {
        return new DeBroglieData(mass, vel, H / (mass * vel));
    }

    private static LorentzData lorentz(double beta, double t0, double L0) {
        double gamma = 1.0 / Math.sqrt(1.0 - beta * beta);
        return new LorentzData(beta, gamma, t0, gamma * t0, L0, L0 / gamma);
    }

    private static RadioactiveData radio(String type,
                                         double t12s, double n0,
                                         double m0g,  double timeS) {
        double lambda = LN2 / t12s;
        double a0     = lambda * n0;
        double mRem   = m0g * Math.exp(-lambda * timeS);
        return new RadioactiveData(type, t12s, n0, m0g, timeS, lambda, a0, mRem);
    }

    // =========================================================================
    // PHOTOELECTRIC_EFFECT — 6 metales
    // E_k = hf − W₀  ·  f₀ = W₀/h  ·  V_stop = E_k/e
    // =========================================================================

    private static final List<PhotoData> PHOTO = List.of(
        // PE1: zinc,    W=4,30 eV, f=1,5·10¹⁵ Hz → f₀≈1,04·10¹⁵, Ek≈3,07·10⁻¹⁹ J
        photo("zinc",     4.30, 1.5e15),
        // PE2: sodio,   W=2,30 eV, f=8,0·10¹⁴ Hz → f₀≈5,55·10¹⁴, Ek≈1,62·10⁻¹⁹ J
        photo("sodio",    2.30, 8.0e14),
        // PE3: cesio,   W=2,00 eV, f=7,0·10¹⁴ Hz → f₀≈4,83·10¹⁴, Ek≈1,44·10⁻¹⁹ J
        photo("cesio",    2.00, 7.0e14),
        // PE4: platino, W=5,65 eV, f=2,0·10¹⁵ Hz → f₀≈1,36·10¹⁵, Ek≈4,22·10⁻¹⁹ J
        photo("platino",  5.65, 2.0e15),
        // PE5: cobre,   W=4,65 eV, f=1,8·10¹⁵ Hz → f₀≈1,12·10¹⁵, Ek≈4,49·10⁻¹⁹ J
        photo("cobre",    4.65, 1.8e15),
        // PE6: potasio, W=2,25 eV, f=6,5·10¹⁴ Hz → f₀≈5,43·10¹⁴, Ek≈7,10·10⁻²⁰ J
        photo("potasio",  2.25, 6.5e14)
    );

    // =========================================================================
    // DE_BROGLIE — 3 electrones
    // λ = h/(m·v)
    // =========================================================================

    private static final List<DeBroglieData> DEBROGLIE = List.of(
        // DB1: v=1,0·10⁶ m/s → λ≈7,28·10⁻¹⁰ m
        deBroglie(M_E, 1.0e6),
        // DB2: v=5,0·10⁶ m/s → λ≈1,46·10⁻¹⁰ m
        deBroglie(M_E, 5.0e6),
        // DB3: v=2,0·10⁶ m/s → λ≈3,64·10⁻¹⁰ m
        deBroglie(M_E, 2.0e6)
    );

    // =========================================================================
    // LORENTZ — 3 velocidades relativistas (β = v/c)
    // γ = 1/√(1−β²)  ·  t = γt₀  ·  L = L₀/γ
    // =========================================================================

    private static final List<LorentzData> LORENTZ = List.of(
        // REL1: β=0,6 → γ=1,25, t₀=10s→t=12,5s, L₀=1000m→L=800m
        lorentz(0.6, 10.0,  1000.0),
        // REL2: β=0,8 → γ≈1,667, t₀=5s→t≈8,33s, L₀=500m→L=300m
        lorentz(0.8,  5.0,   500.0),
        // REL3: β=0,5 → γ≈1,155, t₀=8s→t≈9,24s, L₀=200m→L≈173m
        lorentz(0.5,  8.0,   200.0)
    );

    // =========================================================================
    // RADIOACTIVE_DECAY — 2 λ + 2 actividad + 2 masa remanente
    // λ = ln2/T½  ·  A = λN  ·  m(t) = m₀·e^(−λt)
    // =========================================================================

    private static final List<RadioactiveData> RADIOACTIVE = List.of(
        // RAD1 (λ): T½=600 s → λ=1,155·10⁻³ /s
        radio("constante_lambda", 600,   0,      0, 0),
        // RAD2 (λ): T½=1,0·10⁴ s → λ=6,93·10⁻⁵ /s
        radio("constante_lambda", 1.0e4, 0,      0, 0),
        // RAD3 (A): T½=300 s, N₀=5,0·10¹⁵ → A₀≈1,155·10¹³ Bq
        radio("actividad_inicial", 300,  5.0e15, 0, 0),
        // RAD4 (A): T½=2000 s, N₀=8,0·10¹⁸ → A₀≈2,773·10¹⁵ Bq
        radio("actividad_inicial", 2000, 8.0e18, 0, 0),
        // RAD5 (m): T½=600 s, m₀=8,0 g, t=1800 s=3·T½ → m=1,00 g
        radio("masa_remanente", 600,  0, 8.0, 1800),
        // RAD6 (m): T½=900 s, m₀=6,0 g, t=2700 s=3·T½ → m=0,75 g
        radio("masa_remanente", 900,  0, 6.0, 2700)
    );

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public SecondBachModernPhysicsExercise generateAndSave() {
        SecondBachModernPhysicsExercise ex = new SecondBachModernPhysicsExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);
        ex.setExerciseMode("NUMERICAL");

        int roll = random.nextInt(3);
        if      (roll == 0) buildPhotoelectric(ex);
        else if (roll == 1) buildDeBroglieRelativity(ex);
        else                buildRadioactive(ex);

        log.debug("2BACH BL5 generado: type={} var={}",
            ex.getModernPhysicsType(), ex.getUnknownVariable());
        return repository.save(ex);
    }

    public SecondBachModernPhysicsExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ejercicio 2BACH BL5 no encontrado: " + id));
    }

    // =========================================================================
    // CONSTRUCTOR — PHOTOELECTRIC_EFFECT
    // =========================================================================

    private void buildPhotoelectric(SecondBachModernPhysicsExercise ex) {
        ex.setModernPhysicsType(ModernPhysicsType.PHOTOELECTRIC_EFFECT);
        ex.setTolerancePercent(2.0);

        PhotoData sc = PHOTO.get(random.nextInt(PHOTO.size()));
        int idx = random.nextInt(3);  // 0=f₀, 1=Ek, 2=Vstop

        switch (idx) {
            case 0 -> {
                ex.setUnknownVariable("frecuencia_umbral");
                ex.setCorrectAnswerValue(sc.f0());
                ex.setAnswerUnit("Hz");
                ex.setCorrectAnswerDisplay(fmtDisp(sc.f0()) + " Hz");
                ex.setStatement(String.format(
                    "La función de trabajo del %s es W₀ = %.2f eV. " +
                    "(h = 6,63×10⁻³⁴ J·s; e = 1,6×10⁻¹⁹ C.) " +
                    "Calcula la frecuencia umbral f₀ (en Hz).",
                    sc.metalName(), sc.workEV()));
            }
            case 1 -> {
                ex.setUnknownVariable("energia_cinetica");
                ex.setCorrectAnswerValue(sc.ekMax());
                ex.setAnswerUnit("J");
                ex.setCorrectAnswerDisplay(fmtDisp(sc.ekMax()) + " J");
                ex.setStatement(String.format(
                    "Luz de frecuencia f = %s Hz incide sobre una superficie de %s " +
                    "(función de trabajo W₀ = %.2f eV). " +
                    "(h = 6,63×10⁻³⁴ J·s; e = 1,6×10⁻¹⁹ C.) " +
                    "Calcula la energía cinética máxima E_k de los fotoelectrones (en J).",
                    fmtDisp(sc.freq()), sc.metalName(), sc.workEV()));
            }
            default -> {
                ex.setUnknownVariable("potencial_frenado");
                ex.setCorrectAnswerValue(sc.vStop());
                ex.setAnswerUnit("V");
                ex.setCorrectAnswerDisplay(fmtDisp(sc.vStop()) + " V");
                ex.setStatement(String.format(
                    "Luz de frecuencia f = %s Hz incide sobre %s (W₀ = %.2f eV). " +
                    "(h = 6,63×10⁻³⁴ J·s; e = 1,6×10⁻¹⁹ C.) " +
                    "Calcula el potencial de frenado V_stop (en V).",
                    fmtDisp(sc.freq()), sc.metalName(), sc.workEV()));
            }
        }
        ex.setExplanation(buildPhotoExplanation(sc, idx));
    }

    // =========================================================================
    // CONSTRUCTOR — DE_BROGLIE_RELATIVITY
    // =========================================================================

    private void buildDeBroglieRelativity(SecondBachModernPhysicsExercise ex) {
        ex.setModernPhysicsType(ModernPhysicsType.DE_BROGLIE_RELATIVITY);
        ex.setTolerancePercent(2.0);

        if (random.nextBoolean()) {
            // ── De Broglie ────────────────────────────────────────────────────
            DeBroglieData sc = DEBROGLIE.get(random.nextInt(DEBROGLIE.size()));
            ex.setUnknownVariable("longitud_deBroglie");
            ex.setCorrectAnswerValue(sc.lambda());
            ex.setAnswerUnit("m");
            ex.setCorrectAnswerDisplay(fmtDisp(sc.lambda()) + " m");
            ex.setStatement(String.format(
                "Un electrón se mueve con velocidad v = %s m/s. " +
                "(h = 6,63×10⁻³⁴ J·s; mₑ = 9,11×10⁻³¹ kg.) " +
                "Calcula su longitud de onda de De Broglie λ (en m).",
                fmtDisp(sc.velocity())));
            ex.setExplanation(buildDeBroglieExplanation(sc));

        } else {
            // ── Lorentz ───────────────────────────────────────────────────────
            LorentzData sc = LORENTZ.get(random.nextInt(LORENTZ.size()));
            int idx = random.nextInt(3);  // 0=γ, 1=t_dilatado, 2=L_contraida

            switch (idx) {
                case 0 -> {
                    ex.setUnknownVariable("factor_lorentz");
                    ex.setCorrectAnswerValue(sc.gamma());
                    ex.setAnswerUnit("");
                    ex.setCorrectAnswerDisplay(fmtDisp(sc.gamma()));
                    ex.setStatement(String.format(
                        "Una nave espacial se desplaza a v = %.1fc. " +
                        "(c = 3×10⁸ m/s.) Calcula el factor de Lorentz γ (adimensional).",
                        sc.beta()));
                }
                case 1 -> {
                    ex.setUnknownVariable("tiempo_dilatado");
                    ex.setCorrectAnswerValue(sc.tDilated());
                    ex.setAnswerUnit("s");
                    ex.setCorrectAnswerDisplay(fmtDisp(sc.tDilated()) + " s");
                    ex.setStatement(String.format(
                        "Un tripulante en una nave que viaja a v = %.1fc registra " +
                        "t₀ = %.1f s en su reloj propio. ¿Cuánto tiempo mide un " +
                        "observador en reposo (en s)?",
                        sc.beta(), sc.t0()));
                }
                default -> {
                    ex.setUnknownVariable("longitud_contraida");
                    ex.setCorrectAnswerValue(sc.LContracted());
                    ex.setAnswerUnit("m");
                    ex.setCorrectAnswerDisplay(fmtDisp(sc.LContracted()) + " m");
                    ex.setStatement(String.format(
                        "Una nave de longitud propia L₀ = %.0f m viaja a v = %.1fc. " +
                        "¿Cuál es su longitud medida por un observador en reposo (en m)?",
                        sc.L0(), sc.beta()));
                }
            }
            ex.setExplanation(buildLorentzExplanation(sc, idx));
        }
    }

    // =========================================================================
    // CONSTRUCTOR — RADIOACTIVE_DECAY
    // =========================================================================

    private void buildRadioactive(SecondBachModernPhysicsExercise ex) {
        ex.setModernPhysicsType(ModernPhysicsType.RADIOACTIVE_DECAY);
        ex.setTolerancePercent(2.0);

        RadioactiveData sc = RADIOACTIVE.get(random.nextInt(RADIOACTIVE.size()));

        switch (sc.unknownType()) {
            case "constante_lambda" -> {
                ex.setUnknownVariable("constante_lambda");
                ex.setCorrectAnswerValue(sc.lambda());
                ex.setAnswerUnit("1/s");
                ex.setCorrectAnswerDisplay(fmtDisp(sc.lambda()) + " 1/s");
                ex.setStatement(String.format(
                    "El período de semidesintegración de una muestra radiactiva es " +
                    "T½ = %.0f s. Calcula la constante radiactiva λ (en s⁻¹).",
                    sc.halfLifeS()));
            }
            case "actividad_inicial" -> {
                ex.setUnknownVariable("actividad_inicial");
                ex.setCorrectAnswerValue(sc.a0());
                ex.setAnswerUnit("Bq");
                ex.setCorrectAnswerDisplay(fmtDisp(sc.a0()) + " Bq");
                ex.setStatement(String.format(
                    "Una muestra contiene N₀ = %s núcleos radiactivos con período " +
                    "T½ = %.0f s. Calcula la actividad inicial A₀ (en Bq).",
                    fmtDisp(sc.n0()), sc.halfLifeS()));
            }
            default -> {  // masa_remanente
                ex.setUnknownVariable("masa_remanente");
                ex.setCorrectAnswerValue(sc.mRemaining());
                ex.setAnswerUnit("g");
                ex.setCorrectAnswerDisplay(fmtDisp(sc.mRemaining()) + " g");
                ex.setStatement(String.format(
                    "Una muestra de %.1f g tiene un período de semidesintegración " +
                    "T½ = %.0f s. Calcula la masa remanente tras t = %.0f s (en g).",
                    sc.m0Grams(), sc.halfLifeS(), sc.timeS()));
            }
        }
        ex.setExplanation(buildRadioactiveExplanation(sc));
    }

    // =========================================================================
    // EXPLICACIÓN — PHOTOELECTRIC_EFFECT
    // =========================================================================

    private String buildPhotoExplanation(PhotoData sc, int askedIdx) {
        var sb = new StringBuilder();

        sb.append("<strong>Balance energético de Einstein (efecto fotoeléctrico):</strong>\n\n")
          .append("\\[E_{\\text{fotón}} = h\\cdot f = W_0 + E_k \\implies E_k = hf - W_0\\]\n\n")
          .append("<em>La energía del fotón se invierte en arrancar el electrón del metal (W₀) ")
          .append("y en impartirle energía cinética. Si hf &lt; W₀, no hay efecto fotoeléctrico.</em>\n\n");

        sb.append("<strong>Conversión de unidades:</strong> ")
          .append("W₀ = ").append(sc.workEV()).append(" eV × 1,6×10⁻¹⁹ J/eV = ")
          .append(fmtK(sc.workJ())).append(" J\n\n");

        sb.append("<strong>Paso 1 — Frecuencia umbral</strong> (condición E_k = 0):\n\n")
          .append("\\[f_0 = \\frac{W_0}{h} = \\frac{").append(fmtK(sc.workJ()))
          .append("}{6{,}63\\times10^{-34}} = ").append(fmtK(sc.f0())).append("\\,\\text{Hz}\\]\n\n");

        sb.append("<strong>Paso 2 — Energía cinética máxima:</strong>\n\n")
          .append("\\[E_k = hf - W_0 = 6{,}63\\times10^{-34}\\times ").append(fmtK(sc.freq()))
          .append(" - ").append(fmtK(sc.workJ())).append("\\]\n\n")
          .append("\\[E_k = ").append(fmtK(H * sc.freq())).append(" - ")
          .append(fmtK(sc.workJ())).append(" = ").append(fmtK(sc.ekMax()))
          .append("\\,\\text{J}\\]\n\n");

        sb.append("<strong>Paso 3 — Potencial de frenado:</strong>\n\n")
          .append("\\[V_{\\text{stop}} = \\frac{E_k}{e} = \\frac{").append(fmtK(sc.ekMax()))
          .append("}{1{,}6\\times10^{-19}} = ").append(fmtK(sc.vStop()))
          .append("\\,\\text{V}\\]\n\n");

        String boxed = switch (askedIdx) {
            case 0 -> "f_0 = \\boxed{" + fmtK(sc.f0()) + "\\,\\text{Hz}}";
            case 1 -> "E_k = \\boxed{" + fmtK(sc.ekMax()) + "\\,\\text{J}}";
            default -> "V_{\\text{stop}} = \\boxed{" + fmtK(sc.vStop()) + "\\,\\text{V}}";
        };
        sb.append("∴ \\(").append(boxed).append("\\)");
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — DE BROGLIE
    // =========================================================================

    private String buildDeBroglieExplanation(DeBroglieData sc) {
        var sb = new StringBuilder();

        sb.append("<strong>Hipótesis de De Broglie — dualidad onda-corpúsculo:</strong>\n\n")
          .append("A toda partícula con momento p = mv le corresponde una longitud de onda:\n\n")
          .append("\\[\\lambda = \\frac{h}{p} = \\frac{h}{m_e\\,v}\\]\n\n");

        sb.append("<strong>Cálculo:</strong>\n\n")
          .append("\\[\\lambda = \\frac{h}{m_e\\,v} = ")
          .append("\\frac{6{,}63\\times10^{-34}}{9{,}11\\times10^{-31}\\times ")
          .append(fmtK(sc.velocity())).append("}\\]\n\n")
          .append("\\[\\lambda = \\frac{6{,}63\\times10^{-34}}{")
          .append(fmtK(sc.mass() * sc.velocity())).append("} = ")
          .append(fmtK(sc.lambda())).append("\\,\\text{m}\\]\n\n")
          .append("<em>Esta longitud de onda (del orden de angstroms, Å = 10⁻¹⁰ m) es ")
          .append("comparable a la separación interatómica en cristales, lo que explica ")
          .append("la difracción de electrones.</em>\n\n")
          .append("∴ \\(\\lambda = \\boxed{").append(fmtK(sc.lambda())).append("\\,\\text{m}}\\)");
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — LORENTZ
    // =========================================================================

    private String buildLorentzExplanation(LorentzData sc, int askedIdx) {
        var sb = new StringBuilder();

        sb.append("<strong>Transformaciones de Lorentz — postulados de la Relatividad Especial:</strong>\n\n")
          .append("\\[\\gamma = \\frac{1}{\\sqrt{1-\\beta^2}}, \\quad \\beta = \\frac{v}{c}\\]\n\n")
          .append("<ul>")
          .append("<li><strong>Dilatación temporal:</strong> \\(t = \\gamma\\,t_0\\) ")
          .append("(el reloj en movimiento va más lento).</li>")
          .append("<li><strong>Contracción de longitudes:</strong> \\(L = L_0/\\gamma\\) ")
          .append("(la regla en movimiento parece más corta).</li>")
          .append("</ul>\n\n");

        sb.append("<strong>Cálculo del factor γ:</strong>\n\n")
          .append("\\[\\beta = ").append(sc.beta()).append("c/c = ").append(sc.beta())
          .append(", \\qquad 1-\\beta^2 = 1-").append(sc.beta() * sc.beta())
          .append(" = ").append(1 - sc.beta() * sc.beta()).append("\\]\n\n")
          .append("\\[\\gamma = \\frac{1}{\\sqrt{").append(1 - sc.beta() * sc.beta())
          .append("}} = ").append(fmtK(sc.gamma())).append("\\]\n\n");

        switch (askedIdx) {
            case 0 ->
                sb.append("∴ \\(\\gamma = \\boxed{").append(fmtK(sc.gamma())).append("}\\)");
            case 1 -> {
                sb.append("<strong>Tiempo dilatado:</strong>\n\n")
                  .append("\\[t = \\gamma\\,t_0 = ").append(fmtK(sc.gamma()))
                  .append("\\times ").append(sc.t0()).append(" = ")
                  .append(fmtK(sc.tDilated())).append("\\,\\text{s}\\]\n\n")
                  .append("∴ \\(t = \\boxed{").append(fmtK(sc.tDilated())).append("\\,\\text{s}}\\)");
            }
            default -> {
                sb.append("<strong>Longitud contraída:</strong>\n\n")
                  .append("\\[L = \\frac{L_0}{\\gamma} = \\frac{").append(fmtK(sc.L0()))
                  .append("}{").append(fmtK(sc.gamma())).append("} = ")
                  .append(fmtK(sc.LContracted())).append("\\,\\text{m}\\]\n\n")
                  .append("∴ \\(L = \\boxed{").append(fmtK(sc.LContracted())).append("\\,\\text{m}}\\)");
            }
        }
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — RADIOACTIVE_DECAY
    // =========================================================================

    private String buildRadioactiveExplanation(RadioactiveData sc) {
        var sb = new StringBuilder();

        sb.append("<strong>Ley de desintegración radiactiva:</strong>\n\n")
          .append("\\[\\frac{dN}{dt} = -\\lambda N \\implies N(t) = N_0\\,e^{-\\lambda t}\\]\n\n")
          .append("Relación entre la constante λ y el período de semidesintegración T½\n")
          .append("(condición N(T½) = N₀/2):\n\n")
          .append("\\[\\frac{N_0}{2} = N_0\\,e^{-\\lambda T_{\\frac{1}{2}}} ")
          .append("\\implies \\lambda = \\frac{\\ln 2}{T_{\\frac{1}{2}}}\\]\n\n");

        sb.append("<strong>Paso 1 — Constante radiactiva:</strong>\n\n")
          .append("\\[\\lambda = \\frac{\\ln 2}{T_{\\frac{1}{2}}} = ")
          .append("\\frac{0{,}6931}{").append(fmtK(sc.halfLifeS())).append("} = ")
          .append(fmtK(sc.lambda())).append("\\,\\text{s}^{-1}\\]\n\n");

        switch (sc.unknownType()) {
            case "constante_lambda" ->
                sb.append("∴ \\(\\lambda = \\boxed{").append(fmtK(sc.lambda()))
                  .append("\\,\\text{s}^{-1}}\\)");

            case "actividad_inicial" -> {
                sb.append("<strong>Actividad inicial</strong> (número de desintegraciones por segundo):\n\n")
                  .append("\\[A_0 = \\lambda\\,N_0 = ").append(fmtK(sc.lambda()))
                  .append("\\times ").append(fmtK(sc.n0())).append(" = ")
                  .append(fmtK(sc.a0())).append("\\,\\text{Bq}\\]\n\n")
                  .append("∴ \\(A_0 = \\boxed{").append(fmtK(sc.a0())).append("\\,\\text{Bq}}\\)");
            }

            default -> {  // masa_remanente
                double nHalfLives = sc.timeS() / sc.halfLifeS();
                sb.append("<strong>Masa remanente tras t = ").append(fmtK(sc.timeS()))
                  .append(" s = ").append(fmtK(nHalfLives)).append(" T½:</strong>\n\n")
                  .append("\\[m(t) = m_0\\,e^{-\\lambda t} = m_0\\left(\\frac{1}{2}\\right)^{t/T_{1/2}}\\]\n\n")
                  .append("\\[m(t) = ").append(sc.m0Grams()).append("\\times e^{-")
                  .append(fmtK(sc.lambda())).append("\\times ").append(fmtK(sc.timeS()))
                  .append("} = ").append(sc.m0Grams()).append("\\times e^{-")
                  .append(fmtK(sc.lambda() * sc.timeS())).append("}\\]\n\n")
                  .append("\\[m(t) = ").append(sc.m0Grams()).append("\\times\\left(\\frac{1}{2}\\right)^{")
                  .append(fmtK(nHalfLives)).append("} = ")
                  .append(fmtK(sc.mRemaining())).append("\\,\\text{g}\\]\n\n")
                  .append("∴ \\(m(t) = \\boxed{").append(fmtK(sc.mRemaining())).append("\\,\\text{g}}\\)");
            }
        }
        return sb.toString();
    }

    // =========================================================================
    // FORMATEADORES
    // =========================================================================

    /**
     * Display: decimal (2 decimales) para valores normales;
     * notación científica "x.xxeN" para muy grandes o muy pequeños.
     */
    String fmtDisp(double v) {
        if (v == 0) return "0";
        double abs = Math.abs(v);
        if (abs >= 0.01 && abs < 10000) {
            return String.format(Locale.US, "%.2f", v);
        }
        int exp = (int) Math.floor(Math.log10(abs));
        double mant = v / Math.pow(10, exp);
        String s = String.format(Locale.US, "%.2f", mant);
        if (s.startsWith("10.") || s.equals("-10.00")) {
            exp++; mant = v / Math.pow(10, exp);
            s = String.format(Locale.US, "%.2f", mant);
        }
        return s + "e" + exp;
    }

    /** KaTeX: "1{,}47 \times 10^{-34}" o "1{,}25" (si el valor es normal). */
    private String fmtK(double v) {
        if (v == 0) return "0";
        double abs = Math.abs(v);
        if (abs < 1e-300) return "0";
        if (abs >= 0.01 && abs < 10000) {
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
}
