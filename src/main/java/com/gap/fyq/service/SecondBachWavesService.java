package com.gap.fyq.service;

import com.gap.fyq.model.secondbach.waves.SecondBachWavesExercise;
import com.gap.fyq.model.secondbach.waves.WavesType;
import com.gap.fyq.repository.SecondBachWavesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecondBachWavesService {

    private final SecondBachWavesRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "2BACH";
    private static final String BLOCK  = "BL3";
    private static final double TWO_PI = 2.0 * Math.PI;

    // ── Constantes físicas del bloque ─────────────────────────────────────────
    private static final double I0       = 1.0e-12; // W/m²  umbral de audición
    private static final double V_SOUND  = 340.0;   // m/s   velocidad del sonido en aire

    // =========================================================================
    // DATA RECORDS
    // =========================================================================

    /**
     * Movimiento Armónico Simple.
     * x(t) = A·cos(ωt+φ₀)  ·  ω = √(k/m)  ·  T = 2π/ω
     * vₘₐₓ = Aω  ·  E = ½kA²
     */
    private record HarmonicData(
        double m, double springK, double A,
        double omega, double T, double f, double vMax, double Etotal
    ) {}

    /**
     * Onda viajera: y(x,t) = A·sin(kx − ωt)  (todo en radianes).
     * k = 2π/λ  ·  ω = 2πf  ·  v = λf = ω/k
     * Δφ = k·Δx  ·  y₀ = A·sin(k·x₀) en t=0
     */
    private record WaveData(
        double A, double lambda, double f, double v, double T,
        double waveK, double omega,
        double deltaX, double deltaPhi,
        double x0, double y0
    ) {}

    /**
     * Nivel de intensidad sonora: β = 10·log₁₀(I/I₀), I = P/(4πr²).
     */
    private record DecibelData(double power, double r, double beta) {}

    /**
     * Efecto Doppler: f' = f₀·(v ± vᵣ) / (v ∓ vₛ).
     * scenario: "source_app" | "source_rec" | "receiver_app" | "receiver_rec"
     */
    private record DopplerData(
        String scenario,
        double f0, double vs, double vr, double fPrime
    ) {}

    // =========================================================================
    // FÁBRICAS ESTÁTICAS
    // =========================================================================

    private static HarmonicData harmonic(double m, double k, double A) {
        double omega = Math.sqrt(k / m);
        double T     = TWO_PI / omega;
        double f     = 1.0 / T;
        double vMax  = A * omega;
        double E     = 0.5 * k * A * A;
        return new HarmonicData(m, k, A, omega, T, f, vMax, E);
    }

    private static WaveData wave(double A, double lambda, double f,
                                 double deltaX, double x0) {
        double v      = lambda * f;
        double T      = 1.0 / f;
        double waveK  = TWO_PI / lambda;
        double omega  = TWO_PI * f;
        double dPhi   = waveK * deltaX;
        double y0     = A * Math.sin(waveK * x0);   // t = 0
        return new WaveData(A, lambda, f, v, T, waveK, omega, deltaX, dPhi, x0, y0);
    }

    private static DecibelData decibel(double P, double r) {
        double I    = P / (4.0 * Math.PI * r * r);
        double beta = 10.0 * Math.log10(I / I0);
        return new DecibelData(P, r, beta);
    }

    private static DopplerData dopplerSrcApp(double f0, double vs) {
        double fPrime = f0 * V_SOUND / (V_SOUND - vs);
        return new DopplerData("source_app", f0, vs, 0, fPrime);
    }

    private static DopplerData dopplerSrcRec(double f0, double vs) {
        double fPrime = f0 * V_SOUND / (V_SOUND + vs);
        return new DopplerData("source_rec", f0, vs, 0, fPrime);
    }

    private static DopplerData dopplerRecApp(double f0, double vr) {
        double fPrime = f0 * (V_SOUND + vr) / V_SOUND;
        return new DopplerData("receiver_app", f0, 0, vr, fPrime);
    }

    // =========================================================================
    // HARMONIC_OSCILLATOR — 6 escenarios
    // ω = √(k/m)  ·  T = 2π/ω  ·  vₘₐₓ = Aω  ·  E = ½kA²
    // =========================================================================

    private static final List<HarmonicData> HARMONIC = List.of(
        // HO1: m=0,5 kg, k=200 N/m, A=0,10 m → ω=20 rad/s, vmax=2,00 m/s, E=1,00 J
        harmonic(0.5,  200, 0.10),
        // HO2: m=0,2 kg, k=80  N/m, A=0,05 m → ω=20 rad/s, vmax=1,00 m/s, E=0,10 J
        harmonic(0.2,  80,  0.05),
        // HO3: m=1,0 kg, k=100 N/m, A=0,05 m → ω=10 rad/s, vmax=0,50 m/s, E=0,125 J
        harmonic(1.0,  100, 0.05),
        // HO4: m=0,4 kg, k=160 N/m, A=0,08 m → ω=20 rad/s, vmax=1,60 m/s, E=0,512 J
        harmonic(0.4,  160, 0.08),
        // HO5: m=0,8 kg, k=50  N/m, A=0,20 m → ω≈7,91 rad/s, vmax≈1,58 m/s, E=1,00 J
        harmonic(0.8,  50,  0.20),
        // HO6: m=2,0 kg, k=8   N/m, A=0,30 m → ω=2,00 rad/s, vmax=0,60 m/s, E=0,36 J
        harmonic(2.0,  8,   0.30)
    );

    // =========================================================================
    // WAVE_EQUATION — 6 escenarios
    // y(x,t) = A·sin(kx − ωt)  [todo en radianes]
    // =========================================================================

    private static final List<WaveData> WAVE = List.of(
        // WE1: A=0,10m, λ=2m, f=170Hz, v=340m/s — Δφ(Δx=0,50m)=π/2, y(0,50,0)=A
        wave(0.10, 2.0,  170,  0.50,  0.50),
        // WE2: A=0,20m, λ=4m, f=85Hz,  v=340m/s — Δφ(Δx=1,00m)=π/2, y(1,00,0)=A
        wave(0.20, 4.0,  85,   1.00,  1.00),
        // WE3: A=0,15m, λ=6m, f=50Hz,  v=300m/s — Δφ(Δx=2,00m)=2π/3, y(1,50,0)=A
        wave(0.15, 6.0,  50,   2.00,  1.50),
        // WE4: A=0,05m, λ=1m, f=440Hz, v=440m/s — Δφ(Δx=0,25m)=π/2,  y(0,25,0)=A
        wave(0.05, 1.0,  440,  0.25,  0.25),
        // WE5: A=0,30m, λ=10m,f=34Hz,  v=340m/s — Δφ(Δx=5,00m)=π,    y(2,50,0)=A
        wave(0.30, 10.0, 34,   5.00,  2.50),
        // WE6: A=0,10m, λ=0,5m,f=680Hz,v=340m/s — Δφ(Δx=0,25m)=π,    y(0,125,0)=A
        wave(0.10, 0.5,  680,  0.25,  0.125)
    );

    // =========================================================================
    // ACOUSTICS_DOPPLER — 3 nivel sonoro + 3 efecto Doppler
    // I = P/(4πr²)  ·  β = 10·log₁₀(I/I₀)  ·  I₀ = 10⁻¹² W/m²
    // f' = f₀·v/(v∓vₛ)  ó  f' = f₀·(v±vᵣ)/v
    // =========================================================================

    private static final List<DecibelData> DECIBELS = List.of(
        // AC1: P=1 W,   r=10 m → β≈89,01 dB
        decibel(1.0,   10.0),
        // AC2: P=100 W, r=20 m → β≈103,0 dB
        decibel(100.0, 20.0),
        // AC3: P=0,1 W, r=5  m → β≈85,03 dB
        decibel(0.1,   5.0)
    );

    private static final List<DopplerData> DOPPLER = List.of(
        // DP1: fuente se acerca a 34 m/s, f₀=440 Hz → f'≈488,89 Hz
        dopplerSrcApp(440, 34),
        // DP2: fuente se aleja a 40 m/s,  f₀=500 Hz → f'≈447,37 Hz
        dopplerSrcRec(500, 40),
        // DP3: receptor se acerca a 17 m/s, f₀=440 Hz → f'=462,00 Hz
        dopplerRecApp(440, 17)
    );

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public SecondBachWavesExercise generateAndSave() {
        SecondBachWavesExercise ex = new SecondBachWavesExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);
        ex.setExerciseMode("NUMERICAL");

        int roll = random.nextInt(3);
        if      (roll == 0) buildHarmonic(ex);
        else if (roll == 1) buildWave(ex);
        else                buildAcoustics(ex);

        log.debug("2BACH BL3 generado: type={} var={}",
            ex.getWavesType(), ex.getUnknownVariable());
        return repository.save(ex);
    }

    public SecondBachWavesExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ejercicio 2BACH BL3 no encontrado: " + id));
    }

    // =========================================================================
    // CONSTRUCTOR — HARMONIC_OSCILLATOR
    // =========================================================================

    private void buildHarmonic(SecondBachWavesExercise ex) {
        ex.setWavesType(WavesType.HARMONIC_OSCILLATOR);
        ex.setTolerancePercent(2.0);

        HarmonicData sc = HARMONIC.get(random.nextInt(HARMONIC.size()));

        // 5 posibles incógnitas
        String[] vars  = {"frecuencia_angular", "constante_elastica",
                          "periodo",             "velocidad_maxima",
                          "energia_mecanica"};
        double[] vals  = {sc.omega(),  sc.springK(),  sc.T(),   sc.vMax(), sc.Etotal()};
        String[] units = {"rad/s",     "N/m",          "s",      "m/s",    "J"};
        int idx = random.nextInt(5);

        ex.setUnknownVariable(vars[idx]);
        ex.setCorrectAnswerValue(vals[idx]);
        ex.setAnswerUnit(units[idx]);
        ex.setCorrectAnswerDisplay(fmtNum(vals[idx]) + " " + units[idx]);

        // Enunciado: si piden k_spring, dar m y T; si no, dar m, k, A
        String stmt;
        if (idx == 1) {  // constante_elastica
            stmt = String.format(
                "Una masa de %.1f kg unida a un muelle oscila con período T = %s s " +
                "y amplitud A = %.2f m. Calcula la constante elástica k del muelle (en N/m).",
                sc.m(), fmtNum(sc.T()), sc.A());
        } else {
            String askText = switch (vars[idx]) {
                case "frecuencia_angular" -> "Calcula la frecuencia angular ω (en rad/s).";
                case "periodo"            -> "Calcula el período T (en s).";
                case "velocidad_maxima"   -> "Calcula la velocidad máxima vₘₐₓ (en m/s).";
                default                   -> "Calcula la energía mecánica total E (en J).";
            };
            stmt = String.format(
                "Una masa de %.1f kg está unida a un muelle de constante k = %.0f N/m " +
                "y oscila con amplitud A = %.2f m. %s",
                sc.m(), sc.springK(), sc.A(), askText);
        }
        ex.setStatement(stmt);
        ex.setExplanation(buildHarmonicExplanation(sc, idx));
    }

    // =========================================================================
    // CONSTRUCTOR — WAVE_EQUATION
    // =========================================================================

    private void buildWave(SecondBachWavesExercise ex) {
        ex.setWavesType(WavesType.WAVE_EQUATION);
        ex.setTolerancePercent(2.0);

        WaveData sc = WAVE.get(random.nextInt(WAVE.size()));

        String[] vars  = {"longitud_onda", "periodo",       "velocidad_onda",
                          "desfase",        "elongacion"};
        double[] vals  = {sc.lambda(),      sc.T(),          sc.v(),
                          sc.deltaPhi(),    sc.y0()};
        String[] units = {"m",              "s",             "m/s",
                          "rad",            "m"};
        int idx = random.nextInt(5);

        ex.setUnknownVariable(vars[idx]);
        ex.setCorrectAnswerValue(vals[idx]);
        ex.setAnswerUnit(units[idx]);
        ex.setCorrectAnswerDisplay(fmtNum(vals[idx]) + " " + units[idx]);

        String askText = switch (vars[idx]) {
            case "longitud_onda"  -> "Calcula la longitud de onda λ (en m).";
            case "periodo"        -> "Calcula el período T (en s).";
            case "velocidad_onda" -> "Calcula la velocidad de propagación v (en m/s).";
            case "desfase"        ->
                String.format("Calcula el desfase Δφ (en rad) entre dos puntos " +
                              "separados Δx = %.3f m.", sc.deltaX());
            default ->
                String.format("⚠ Usa tu calculadora en modo RAD. " +
                              "Calcula la elongación y (en m) en x₀ = %.3f m y t₀ = 0 s.",
                              sc.x0());
        };

        ex.setStatement(String.format(
            "Una onda viajera sigue la ecuación y(x,t) = %.2f·sin(kx − ωt) m, " +
            "con frecuencia f = %.0f Hz y velocidad de propagación v = %.0f m/s. %s",
            sc.A(), sc.f(), sc.v(), askText));

        ex.setExplanation(buildWaveExplanation(sc, idx));
    }

    // =========================================================================
    // CONSTRUCTOR — ACOUSTICS_DOPPLER
    // =========================================================================

    private void buildAcoustics(SecondBachWavesExercise ex) {
        ex.setWavesType(WavesType.ACOUSTICS_DOPPLER);
        ex.setTolerancePercent(2.0);

        if (random.nextBoolean()) {
            // ── Nivel sonoro ──────────────────────────────────────────────────
            DecibelData sc = DECIBELS.get(random.nextInt(DECIBELS.size()));

            ex.setUnknownVariable("nivel_sonoro");
            ex.setCorrectAnswerValue(sc.beta());
            ex.setAnswerUnit("dB");
            ex.setCorrectAnswerDisplay(fmtNum(sc.beta()) + " dB");

            ex.setStatement(String.format(
                "Una fuente sonora emite una potencia acústica P = %.1f W de forma " +
                "isótropa. Calcula el nivel de intensidad sonora β (en dB) a una " +
                "distancia r = %.0f m. (I₀ = 10⁻¹² W/m².)",
                sc.power(), sc.r()));

            ex.setExplanation(buildDecibelExplanation(sc));

        } else {
            // ── Efecto Doppler ────────────────────────────────────────────────
            DopplerData sc = DOPPLER.get(random.nextInt(DOPPLER.size()));

            ex.setUnknownVariable("frecuencia_doppler");
            ex.setCorrectAnswerValue(sc.fPrime());
            ex.setAnswerUnit("Hz");
            ex.setCorrectAnswerDisplay(fmtNum(sc.fPrime()) + " Hz");

            String stmtMotion = switch (sc.scenario()) {
                case "source_app" -> String.format(
                    "Una fuente emite f₀ = %.0f Hz y se desplaza hacia el receptor " +
                    "a vₛ = %.0f m/s.", sc.f0(), sc.vs());
                case "source_rec" -> String.format(
                    "Una fuente emite f₀ = %.0f Hz y se aleja del receptor " +
                    "a vₛ = %.0f m/s.", sc.f0(), sc.vs());
                default -> String.format(
                    "Una fuente estacionaria emite f₀ = %.0f Hz. El receptor " +
                    "se acerca a la fuente a vᵣ = %.0f m/s.", sc.f0(), sc.vr());
            };

            ex.setStatement(String.format(
                "%s (Velocidad del sonido en el aire: v = 340 m/s.) " +
                "Calcula la frecuencia aparente f' percibida por el receptor (en Hz).",
                stmtMotion));

            ex.setExplanation(buildDopplerExplanation(sc));
        }
    }

    // =========================================================================
    // EXPLICACIÓN — HARMONIC_OSCILLATOR
    // =========================================================================

    private String buildHarmonicExplanation(HarmonicData sc, int askedIdx) {
        var sb = new StringBuilder();

        sb.append("<strong>Ecuaciones del MAS:</strong>\n\n")
          .append("\\[\\omega = \\sqrt{\\frac{k}{m}}, \\quad T = \\frac{2\\pi}{\\omega}, ")
          .append("\\quad v_{\\max} = A\\omega, \\quad E = \\tfrac{1}{2}kA^2\\]\n\n");

        sb.append("<strong>Datos:</strong> m = ").append(sc.m()).append(" kg, ")
          .append("k = ").append(fmt0(sc.springK())).append(" N/m, ")
          .append("A = ").append(sc.A()).append(" m\n\n");

        sb.append("<strong>Paso 1 — Frecuencia angular:</strong>\n\n")
          .append("\\[\\omega = \\sqrt{\\frac{k}{m}} = \\sqrt{\\frac{")
          .append(fmt0(sc.springK())).append("}{").append(sc.m())
          .append("}} = \\sqrt{").append(fmtK(sc.springK() / sc.m()))
          .append("} = ").append(fmtK(sc.omega())).append("\\,\\text{rad/s}\\]\n\n");

        sb.append("<strong>Paso 2 — Período:</strong>\n\n")
          .append("\\[T = \\frac{2\\pi}{\\omega} = \\frac{2\\pi}{")
          .append(fmtK(sc.omega())).append("} = ")
          .append(fmtK(sc.T())).append("\\,\\text{s}\\]\n\n");

        sb.append("<strong>Paso 3 — Velocidad máxima</strong>")
          .append(" (en el punto de equilibrio, donde la elongación es cero):\n\n")
          .append("\\[v_{\\max} = A\\omega = ").append(sc.A()).append("\\times ")
          .append(fmtK(sc.omega())).append(" = ").append(fmtK(sc.vMax()))
          .append("\\,\\text{m/s}\\]\n\n");

        sb.append("<strong>Paso 4 — Energía mecánica total</strong>")
          .append(" (constante a lo largo de la oscilación):\n\n")
          .append("\\[E = \\tfrac{1}{2}kA^2 = \\tfrac{1}{2}\\times ")
          .append(fmt0(sc.springK())).append("\\times (").append(sc.A())
          .append(")^2 = ").append(fmtK(sc.Etotal())).append("\\,\\text{J}\\]\n\n");

        String boxed = switch (askedIdx) {
            case 0 -> "\\omega = \\boxed{" + fmtK(sc.omega()) + "\\,\\text{rad/s}}";
            case 1 -> "k = \\boxed{" + fmtK(sc.springK()) + "\\,\\text{N/m}}";
            case 2 -> "T = \\boxed{" + fmtK(sc.T()) + "\\,\\text{s}}";
            case 3 -> "v_{\\max} = \\boxed{" + fmtK(sc.vMax()) + "\\,\\text{m/s}}";
            default -> "E = \\boxed{" + fmtK(sc.Etotal()) + "\\,\\text{J}}";
        };
        sb.append("∴ \\(").append(boxed).append("\\)");
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — WAVE_EQUATION
    // =========================================================================

    private String buildWaveExplanation(WaveData sc, int askedIdx) {
        var sb = new StringBuilder();

        sb.append("<strong>⚠ Importante:</strong> todos los cálculos con funciones ")
          .append("trigonométricas en esta ecuación de onda deben realizarse con la ")
          .append("<strong>calculadora en modo RAD (radianes)</strong>, no en grados.\n\n");

        sb.append("<strong>Relaciones fundamentales de la onda viajera:</strong>\n\n")
          .append("\\[y(x,t) = A\\sin(kx - \\omega t), \\quad ")
          .append("k = \\frac{2\\pi}{\\lambda}, \\quad \\omega = 2\\pi f, \\quad v = \\lambda f\\]\n\n");

        sb.append("<strong>Datos:</strong> A = ").append(sc.A()).append(" m, ")
          .append("f = ").append(fmt0(sc.f())).append(" Hz, ")
          .append("v = ").append(fmt0(sc.v())).append(" m/s\n\n");

        // Longitud de onda
        sb.append("<strong>Longitud de onda:</strong>\n\n")
          .append("\\[\\lambda = \\frac{v}{f} = \\frac{").append(fmt0(sc.v()))
          .append("}{").append(fmt0(sc.f())).append("} = ")
          .append(fmtK(sc.lambda())).append("\\,\\text{m}\\]\n\n");

        // Período
        sb.append("<strong>Período:</strong>\n\n")
          .append("\\[T = \\frac{1}{f} = \\frac{1}{").append(fmt0(sc.f()))
          .append("} = ").append(fmtK(sc.T())).append("\\,\\text{s}\\]\n\n");

        // Número de onda y frecuencia angular
        sb.append("<strong>Número de onda y frecuencia angular:</strong>\n\n")
          .append("\\[k = \\frac{2\\pi}{\\lambda} = \\frac{2\\pi}{").append(fmtK(sc.lambda()))
          .append("} = ").append(fmtK(sc.waveK())).append("\\,\\text{rad/m}, \\qquad ")
          .append("\\omega = 2\\pi f = 2\\pi \\times ").append(fmt0(sc.f()))
          .append(" = ").append(fmtK(sc.omega())).append("\\,\\text{rad/s}\\]\n\n");

        // Desfase
        sb.append("<strong>Desfase entre dos puntos separados Δx = ")
          .append(sc.deltaX()).append(" m:</strong>\n\n")
          .append("\\[\\Delta\\phi = k\\cdot\\Delta x = ").append(fmtK(sc.waveK()))
          .append("\\times ").append(sc.deltaX()).append(" = ")
          .append(fmtK(sc.deltaPhi())).append("\\,\\text{rad}\\]\n\n");

        // Elongación en (x₀, t=0)
        sb.append("<strong>Elongación en x₀ = ").append(sc.x0()).append(" m, t₀ = 0:</strong>\n\n")
          .append("\\[y(").append(sc.x0()).append(",\\,0) = A\\sin(k\\cdot x_0) = ")
          .append(fmtK(sc.A())).append("\\cdot\\sin\\!\\left(")
          .append(fmtK(sc.waveK())).append("\\times ").append(sc.x0())
          .append("\\right) = ").append(fmtK(sc.A())).append("\\cdot\\sin\\!\\left(")
          .append(fmtK(sc.waveK() * sc.x0())).append("\\,\\text{rad}\\right) = ")
          .append(fmtK(sc.y0())).append("\\,\\text{m}\\]\n\n");

        String[] labels = {"\\lambda", "T", "v", "\\Delta\\phi", "y_0"};
        String[] kvals  = {fmtK(sc.lambda()), fmtK(sc.T()), fmtK(sc.v()),
                           fmtK(sc.deltaPhi()), fmtK(sc.y0())};
        String[] uunits = {"\\text{m}", "\\text{s}", "\\text{m/s}",
                           "\\text{rad}", "\\text{m}"};
        sb.append("∴ \\(").append(labels[askedIdx]).append(" = \\boxed{")
          .append(kvals[askedIdx]).append("\\;").append(uunits[askedIdx]).append("}\\)");
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — ACOUSTICS (decibelios)
    // =========================================================================

    private String buildDecibelExplanation(DecibelData sc) {
        double I = sc.power() / (4.0 * Math.PI * sc.r() * sc.r());
        var sb = new StringBuilder();

        sb.append("<strong>Marco teórico — atenuación geométrica e intensidad sonora:</strong>\n\n")
          .append("Para una fuente isótropa, la intensidad disminuye con el cuadrado ")
          .append("de la distancia (superficie de la esfera \\(4\\pi r^2\\)):\n\n")
          .append("\\[I = \\frac{P}{4\\pi r^2}\\]\n\n")
          .append("El nivel de intensidad sonora en decibelios es:\n\n")
          .append("\\[\\beta = 10\\cdot\\log_{10}\\!\\left(\\frac{I}{I_0}\\right), ")
          .append("\\qquad I_0 = 10^{-12}\\,\\text{W/m}^2\\]\n\n");

        sb.append("<strong>Paso 1 — Intensidad a r = ").append(fmt0(sc.r())).append(" m:</strong>\n\n")
          .append("\\[I = \\frac{").append(sc.power()).append("}{4\\pi\\times(")
          .append(fmt0(sc.r())).append(")^2} = \\frac{").append(sc.power())
          .append("}{").append(fmtK(4*Math.PI*sc.r()*sc.r())).append("} = ")
          .append(fmtK(I)).append("\\,\\text{W/m}^2\\]\n\n");

        sb.append("<strong>Paso 2 — Nivel en dB:</strong>\n\n")
          .append("\\[\\beta = 10\\cdot\\log_{10}\\!\\left(\\frac{")
          .append(fmtK(I)).append("}{10^{-12}}\\right) = 10\\cdot\\log_{10}(")
          .append(fmtK(I / I0)).append(") = ").append(fmtK(sc.beta()))
          .append("\\,\\text{dB}\\]\n\n")
          .append("∴ \\(\\beta = \\boxed{").append(fmtK(sc.beta())).append("\\,\\text{dB}}\\)");
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — ACOUSTICS (Doppler)
    // =========================================================================

    private String buildDopplerExplanation(DopplerData sc) {
        var sb = new StringBuilder();

        sb.append("<strong>Efecto Doppler — fórmula general:</strong>\n\n")
          .append("\\[f' = f_0 \\cdot \\frac{v \\pm v_r}{v \\mp v_s}\\]\n\n")
          .append("<ul>")
          .append("<li>Numerador \\((+)\\) si el receptor <em>se acerca</em> a la fuente.</li>")
          .append("<li>Numerador \\((-)\\) si el receptor <em>se aleja</em> de la fuente.</li>")
          .append("<li>Denominador \\((-)\\) si la fuente <em>se acerca</em> al receptor.</li>")
          .append("<li>Denominador \\((+)\\) si la fuente <em>se aleja</em> del receptor.</li>")
          .append("</ul>\n\n")
          .append("<em>Regla mnemotécnica: el receptor que se acerca escucha más agudo ")
          .append("(f' &gt; f₀); la fuente que se aleja produce frecuencia más grave.</em>\n\n");

        switch (sc.scenario()) {
            case "source_app" -> {
                sb.append("<strong>Situación:</strong> fuente se acerca, receptor estático ")
                  .append("→ denominador \\(v - v_s\\)\n\n")
                  .append("\\[f' = f_0\\cdot\\frac{v}{v - v_s} = ").append(fmt0(sc.f0()))
                  .append("\\cdot\\frac{").append(fmt0(V_SOUND)).append("}{")
                  .append(fmt0(V_SOUND)).append(" - ").append(fmt0(sc.vs()))
                  .append("} = ").append(fmt0(sc.f0())).append("\\cdot\\frac{")
                  .append(fmt0(V_SOUND)).append("}{").append(fmtK(V_SOUND - sc.vs()))
                  .append("} = ").append(fmtK(sc.fPrime())).append("\\,\\text{Hz}\\]\n\n");
            }
            case "source_rec" -> {
                sb.append("<strong>Situación:</strong> fuente se aleja, receptor estático ")
                  .append("→ denominador \\(v + v_s\\)\n\n")
                  .append("\\[f' = f_0\\cdot\\frac{v}{v + v_s} = ").append(fmt0(sc.f0()))
                  .append("\\cdot\\frac{").append(fmt0(V_SOUND)).append("}{")
                  .append(fmt0(V_SOUND)).append(" + ").append(fmt0(sc.vs()))
                  .append("} = ").append(fmtK(sc.fPrime())).append("\\,\\text{Hz}\\]\n\n");
            }
            default -> {  // receiver_app
                sb.append("<strong>Situación:</strong> receptor se acerca, fuente estática ")
                  .append("→ numerador \\(v + v_r\\)\n\n")
                  .append("\\[f' = f_0\\cdot\\frac{v + v_r}{v} = ").append(fmt0(sc.f0()))
                  .append("\\cdot\\frac{").append(fmt0(V_SOUND)).append(" + ")
                  .append(fmt0(sc.vr())).append("}{").append(fmt0(V_SOUND))
                  .append("} = ").append(fmtK(sc.fPrime())).append("\\,\\text{Hz}\\]\n\n");
            }
        }
        sb.append("∴ \\(f' = \\boxed{").append(fmtK(sc.fPrime())).append("\\,\\text{Hz}}\\)");
        return sb.toString();
    }

    // =========================================================================
    // FORMATEADORES
    // =========================================================================

    /** Notación científica o decimal limpia con 2 sig. decimales. */
    String fmtNum(double v) {
        if (v == 0) return "0";
        double abs = Math.abs(v);
        // Para valores "normales" (0,001 ≤ |v| < 10000): mostrar como decimal
        if (abs >= 0.001 && abs < 10000) {
            String s = String.format(Locale.US, "%.4f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
            // Limitar a 2 decimales significativos después del punto
            int dot = s.indexOf('.');
            if (dot >= 0 && s.length() - dot > 3) s = String.format(Locale.US, "%.2f", v);
            return s;
        }
        // Para el resto: notación científica
        int exp = (int) Math.floor(Math.log10(abs));
        double mant = v / Math.pow(10, exp);
        String s = String.format(Locale.US, "%.2f", mant);
        if (s.startsWith("10.") || s.equals("-10.00")) {
            exp += 1; mant = v / Math.pow(10, exp);
            s = String.format(Locale.US, "%.2f", mant);
        }
        return s + "e" + exp;
    }

    /** KaTeX: "1{,}47 \times 10^{3}" o "1{,}47" (si exp=0). */
    private String fmtK(double v) {
        if (v == 0) return "0";
        double abs = Math.abs(v);
        if (abs < 1e-300) return "0";
        // Rango "normal" sin notación científica
        if (abs >= 0.01 && abs < 10000) {
            String s = String.format(Locale.US, "%.4f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
            int dot = s.indexOf('.');
            if (dot >= 0 && s.length() - dot > 3) s = String.format(Locale.US, "%.2f", v);
            return s.replace(".", "{,}");
        }
        int exp = (int) Math.floor(Math.log10(abs));
        double mant = v / Math.pow(10, exp);
        String s = String.format(Locale.US, "%.2f", mant);
        if (s.startsWith("10.") || s.equals("-10.00")) {
            exp += 1; mant = v / Math.pow(10, exp);
            s = String.format(Locale.US, "%.2f", mant);
        }
        return s.replace(".", "{,}") + " \\times 10^{" + exp + "}";
    }

    /** Para enteros (masas en N/m, frecuencias en Hz, velocidades). */
    private String fmt0(double v) {
        if (v == Math.floor(v) && !Double.isInfinite(v))
            return String.valueOf((long) v);
        return String.format(Locale.US, "%.1f", v);
    }
}
