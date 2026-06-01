package com.gap.fyq.service;

import com.gap.fyq.model.firstbach.energy.EnergyType;
import com.gap.fyq.model.firstbach.energy.FirstBachEnergyExercise;
import com.gap.fyq.repository.FirstBachEnergyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirstBachEnergyService {

    private final FirstBachEnergyRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "1BACH";
    private static final String BLOCK  = "BL8";
    private static final double G_ACC  = 10.0; // m/s² (convenio 1bach)

    // =========================================================================
    // DATA RECORDS
    // =========================================================================

    /**
     * Teorema trabajo-energía.
     * unknownVar: "trabajo_neto" | "distancia_frenada" | "trabajo_disipado"
     * Para "distancia_frenada": d = v0²/(2μg), vf=0.
     * Para "trabajo_disipado" con d>0: W=μmgd (arrastre conocido).
     * Para "trabajo_disipado" con d=0: W=½m(v0²-vf²) (pérdida de Ec).
     */
    private record WorkEnergyData(
        String unknownVar,
        double mass,
        double v0,
        double vf,
        double mu,
        double d,
        double answer,
        String unit
    ) {}

    /**
     * Energía en el MAS.
     * unknownVar: "energia_cinetica" | "energia_potencial"
     * Em = ½kA²,  Ep = ½kx²,  Ec = Em − Ep
     */
    private record HarmonicData(
        String unknownVar,
        double k,
        double mass,
        double A,
        double x,
        double answer
    ) {}

    /**
     * Trabajo del campo eléctrico.
     * W = q·ΔV  (ΔV = VA − VB)
     */
    private record ElectricPotentialData(
        double chargeCoulombs,
        String chargeDisplay,
        double deltaV,
        double work
    ) {}

    // =========================================================================
    // WORK_ENERGY_THEOREM — 8 escenarios (g = 10 m/s²)
    // Verificado: trabajo_neto = ½m(vf²−v0²)
    //             distancia_frenada = v0²/(2μg)
    //             trabajo_disipado = μmgd  ó  ½m(v0²−vf²)
    // =========================================================================

    private static final List<WorkEnergyData> WORK_ENERGY = List.of(

        // WE1: trabajo_neto, m=2, v0=3→vf=7  →  W=½·2·(49−9)=40 J
        new WorkEnergyData("trabajo_neto",      2.0, 3.0, 7.0,  0.0,  0.0,  40.0,  "J"),

        // WE2: trabajo_neto, m=4, v0=0→vf=10  →  W=½·4·100=200 J
        new WorkEnergyData("trabajo_neto",      4.0, 0.0, 10.0, 0.0,  0.0, 200.0,  "J"),

        // WE3: trabajo_neto, m=0,5, v0=6→vf=10  →  W=½·0,5·(100−36)=16 J
        new WorkEnergyData("trabajo_neto",      0.5, 6.0, 10.0, 0.0,  0.0,  16.0,  "J"),

        // WE4: distancia_frenada, m=1200, v0=25, μ=0,5  →  d=625/10=62,5 m
        new WorkEnergyData("distancia_frenada", 1200.0, 25.0, 0.0, 0.5, 62.5, 62.5, "m"),

        // WE5: distancia_frenada, m=5, v0=8, μ=0,4  →  d=64/8=8 m
        new WorkEnergyData("distancia_frenada", 5.0, 8.0, 0.0, 0.4, 8.0, 8.0, "m"),

        // WE6: distancia_frenada, m=500, v0=30, μ=0,6  →  d=900/12=75 m
        new WorkEnergyData("distancia_frenada", 500.0, 30.0, 0.0, 0.6, 75.0, 75.0, "m"),

        // WE7: trabajo_disipado por Ec  →  ½·3·(100−16)=126 J
        new WorkEnergyData("trabajo_disipado",  3.0, 10.0, 4.0, 0.0,  0.0, 126.0,  "J"),

        // WE8: trabajo_disipado por μmgd  →  0,25·2·10·8=40 J
        new WorkEnergyData("trabajo_disipado",  2.0,  0.0, 0.0, 0.25, 8.0,  40.0,  "J")
    );

    // =========================================================================
    // HARMONIC_OSCILLATOR_ENERGY — 8 escenarios
    // Em=½kA²,  Ep=½kx²,  Ec=Em−Ep
    // =========================================================================

    private static final List<HarmonicData> HARMONIC = List.of(

        // HO1: k=200, A=0,10, x=0,06  →  Em=1,00, Ep=0,36, Ec=0,64 J
        new HarmonicData("energia_cinetica",   200.0, 0.5,  0.10, 0.06,  0.64),

        // HO2: k=100, A=0,20, x=0 (equilibrio)  →  Em=2,00, Ep=0, Ec=2,00 J
        new HarmonicData("energia_cinetica",   100.0, 1.0,  0.20, 0.0,   2.00),

        // HO3: k=400, A=0,05, x=0,03  →  Em=0,50, Ep=0,18, Ec=0,32 J
        new HarmonicData("energia_potencial",  400.0, 2.0,  0.05, 0.03,  0.18),

        // HO4: k=800, A=0,10, x=0,08  →  Em=4,00, Ep=2,56, Ec=1,44 J
        new HarmonicData("energia_potencial",  800.0, 0.5,  0.10, 0.08,  2.56),

        // HO5: k=50, A=0,40, x=0,30  →  Em=4,00, Ep=2,25, Ec=1,75 J
        new HarmonicData("energia_cinetica",    50.0, 0.2,  0.40, 0.30,  1.75),

        // HO6: k=500, A=0,10, x=0,06  →  Em=2,50, Ep=0,90, Ec=1,60 J
        new HarmonicData("energia_cinetica",   500.0, 1.0,  0.10, 0.06,  1.60),

        // HO7: k=1000, A=0,02, x=0,01  →  Em=0,20, Ep=0,05, Ec=0,15 J
        new HarmonicData("energia_cinetica",  1000.0, 0.5,  0.02, 0.01,  0.15),

        // HO8: k=200, A=0,30, x=0,20  →  Em=9,00, Ep=4,00, Ec=5,00 J
        new HarmonicData("energia_potencial",  200.0, 2.0,  0.30, 0.20,  4.00)
    );

    // =========================================================================
    // ELECTRIC_POTENTIAL_WORK — 8 escenarios
    // W = q·ΔV  (ΔV = VA − VB)
    // =========================================================================

    private static final List<ElectricPotentialData> ELECTRIC = List.of(

        // EP1:  q= 2 μC,  ΔV=100 V   →  W= 2,00×10⁻⁴ J
        new ElectricPotentialData( 2e-6,   "2,0 μC",           100.0,   2e-4),

        // EP2:  q= 5 μC,  ΔV= 50 V   →  W= 2,50×10⁻⁴ J
        new ElectricPotentialData( 5e-6,   "5,0 μC",            50.0,  2.5e-4),

        // EP3:  q= 1,602×10⁻¹⁹ C (protón), ΔV=1000 V  →  W=1,60×10⁻¹⁶ J
        new ElectricPotentialData( 1.602e-19, "1,602×10⁻¹⁹ C", 1000.0, 1.602e-16),

        // EP4:  q=−3 μC,  ΔV=200 V   →  W=−6,00×10⁻⁴ J (trabajo resistente)
        new ElectricPotentialData(-3e-6,  "-3,0 μC",           200.0,  -6e-4),

        // EP5:  q= 4 μC,  ΔV=500 V   →  W= 2,00×10⁻³ J
        new ElectricPotentialData( 4e-6,   "4,0 μC",           500.0,   2e-3),

        // EP6:  q=10 nC,  ΔV=300 V   →  W= 3,00×10⁻⁶ J
        new ElectricPotentialData(10e-9,  "10,0 nC",           300.0,   3e-6),

        // EP7:  q= 2 nC,  ΔV=1000 V  →  W= 2,00×10⁻⁶ J
        new ElectricPotentialData( 2e-9,   "2,0 nC",          1000.0,   2e-6),

        // EP8:  q=50 μC,  ΔV=200 V   →  W= 0,01 J
        new ElectricPotentialData(50e-6,  "50,0 μC",           200.0,   0.01)
    );

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public FirstBachEnergyExercise generateAndSave() {
        FirstBachEnergyExercise ex = new FirstBachEnergyExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);
        ex.setExerciseMode("NUMERICAL");

        int roll = random.nextInt(3);
        if      (roll == 0) buildWorkEnergy(ex);
        else if (roll == 1) buildHarmonic(ex);
        else                buildElectric(ex);

        log.debug("1BACH BL8 generado: type={} var={}", ex.getEnergyType(), ex.getUnknownVariable());
        return repository.save(ex);
    }

    public FirstBachEnergyExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ejercicio 1BACH BL8 no encontrado: " + id));
    }

    // =========================================================================
    // CONSTRUCTOR — WORK_ENERGY_THEOREM
    // =========================================================================

    private void buildWorkEnergy(FirstBachEnergyExercise ex) {
        ex.setEnergyType(EnergyType.WORK_ENERGY_THEOREM);
        ex.setTolerancePercent(2.0);

        WorkEnergyData sc = WORK_ENERGY.get(random.nextInt(WORK_ENERGY.size()));
        ex.setUnknownVariable(sc.unknownVar());
        ex.setCorrectAnswerValue(sc.answer());
        ex.setAnswerUnit(sc.unit());
        ex.setCorrectAnswerDisplay(fmt2(sc.answer()) + " " + sc.unit());
        ex.setStatement(buildWorkEnergyStatement(sc));
        ex.setExplanation(buildWorkEnergyExplanation(sc));
    }

    private String buildWorkEnergyStatement(WorkEnergyData sc) {
        return switch (sc.unknownVar()) {
            case "trabajo_neto" -> String.format(
                "Un cuerpo de masa m = %s kg %s. El cuerpo alcanza una velocidad " +
                "final de vₑ = %s m/s. Calcula el trabajo neto realizado sobre " +
                "el cuerpo (en J).",
                fmt1(sc.mass()),
                sc.v0() == 0.0
                    ? "parte del reposo"
                    : "se mueve inicialmente a v₀ = " + fmt1(sc.v0()) + " m/s",
                fmt1(sc.vf()));

            case "distancia_frenada" -> String.format(
                "Un vehículo de masa m = %s kg circula a v₀ = %s m/s y frena " +
                "hasta detenerse. El coeficiente de rozamiento cinético entre " +
                "ruedas y calzada es μ = %s. Calcula la distancia de frenado (en m). " +
                "(g = 10 m/s²)",
                fmt1(sc.mass()), fmt1(sc.v0()), fmt2(sc.mu()));

            default -> // trabajo_disipado
                sc.d() > 0.0
                    ? String.format(
                        "Un bloque de masa m = %s kg es arrastrado horizontalmente " +
                        "una distancia d = %s m con un coeficiente de rozamiento " +
                        "cinético μ = %s. Calcula la energía disipada " +
                        "por el rozamiento (en J). (g = 10 m/s²)",
                        fmt1(sc.mass()), fmt1(sc.d()), fmt2(sc.mu()))
                    : String.format(
                        "Un bloque de masa m = %s kg se mueve horizontalmente a " +
                        "v₀ = %s m/s y frena por rozamiento hasta vₑ = %s m/s. " +
                        "Calcula la energía disipada por el rozamiento (en J).",
                        fmt1(sc.mass()), fmt1(sc.v0()), fmt1(sc.vf()));
        };
    }

    private String buildWorkEnergyExplanation(WorkEnergyData sc) {
        var sb = new StringBuilder();
        switch (sc.unknownVar()) {
            case "trabajo_neto" -> {
                double Ec0 = 0.5 * sc.mass() * sc.v0() * sc.v0();
                double Ecf = 0.5 * sc.mass() * sc.vf() * sc.vf();
                sb.append("<strong>Teorema trabajo–energía (fuerzas vivas):</strong>\n\n")
                  .append("\\[W_{\\text{neto}} = \\Delta E_c = E_{c,f} - E_{c,0}\\]\n\n")
                  .append("<strong>Energía cinética inicial:</strong>\n\n")
                  .append("\\[E_{c,0} = \\tfrac{1}{2}m v_0^2 = \\tfrac{1}{2}\\times")
                  .append(fmtK2(sc.mass())).append("\\times(").append(fmtK2(sc.v0()))
                  .append(")^2 = ").append(fmtK2(Ec0)).append("\\,\\text{J}\\]\n\n")
                  .append("<strong>Energía cinética final:</strong>\n\n")
                  .append("\\[E_{c,f} = \\tfrac{1}{2}m v_f^2 = \\tfrac{1}{2}\\times")
                  .append(fmtK2(sc.mass())).append("\\times(").append(fmtK2(sc.vf()))
                  .append(")^2 = ").append(fmtK2(Ecf)).append("\\,\\text{J}\\]\n\n")
                  .append("<strong>Trabajo neto:</strong>\n\n")
                  .append("\\[W_{\\text{neto}} = ").append(fmtK2(Ecf))
                  .append(" - ").append(fmtK2(Ec0))
                  .append(" = \\boxed{").append(fmtK2(sc.answer())).append("\\,\\text{J}}\\]");
            }
            case "distancia_frenada" -> {
                sb.append("<strong>Teorema trabajo–energía aplicado al frenado:</strong>\n\n")
                  .append("El único trabajo sobre el vehículo es el del rozamiento " +
                          "(fuerza opuesta al movimiento):\n\n")
                  .append("\\[W_{\\text{roz}} = -\\mu m g\\,d\\]\n\n")
                  .append("Al frenar hasta el reposo \\(\\Delta E_c = -E_{c,0}\\), por tanto:\n\n")
                  .append("\\[-\\mu m g\\,d = -\\tfrac{1}{2}m v_0^2 " +
                          "\\implies d = \\frac{v_0^2}{2\\mu g}\\]\n\n")
                  .append("<strong>Sustitución numérica:</strong>\n\n")
                  .append("\\[d = \\frac{(").append(fmtK2(sc.v0())).append(")^2}")
                  .append("{2 \\times ").append(fmtK2(sc.mu())).append(" \\times 10}")
                  .append(" = \\frac{").append(fmtK2(sc.v0() * sc.v0()))
                  .append("}{").append(fmtK2(2 * sc.mu() * G_ACC)).append("}")
                  .append(" = \\boxed{").append(fmtK2(sc.answer())).append("\\,\\text{m}}\\]");
            }
            default -> { // trabajo_disipado
                if (sc.d() > 0.0) {
                    double Ff = sc.mu() * sc.mass() * G_ACC;
                    sb.append("<strong>Trabajo realizado por la fricción cinética:</strong>\n\n")
                      .append("La fuerza de rozamiento cinético se opone siempre al " +
                              "desplazamiento:\n\n")
                      .append("\\[F_r = \\mu_c \\cdot N = \\mu_c \\cdot m g = ")
                      .append(fmtK2(sc.mu())).append("\\times").append(fmtK2(sc.mass()))
                      .append("\\times 10 = ").append(fmtK2(Ff)).append("\\,\\text{N}\\]\n\n")
                      .append("\\[W_{\\text{diss}} = F_r \\cdot d = ").append(fmtK2(Ff))
                      .append("\\times").append(fmtK2(sc.d()))
                      .append(" = \\boxed{").append(fmtK2(sc.answer())).append("\\,\\text{J}}\\]");
                } else {
                    double Ec0 = 0.5 * sc.mass() * sc.v0() * sc.v0();
                    double Ecf = 0.5 * sc.mass() * sc.vf() * sc.vf();
                    sb.append("<strong>Energía disipada = variación de energía " +
                              "cinética:</strong>\n\n")
                      .append("Por el Teorema trabajo–energía, toda la variación " +
                              "de \\(E_c\\) se convierte en calor por rozamiento:\n\n")
                      .append("\\[W_{\\text{diss}} = E_{c,0} - E_{c,f} = " +
                              "\\tfrac{1}{2}m v_0^2 - \\tfrac{1}{2}m v_f^2\\]\n\n")
                      .append("\\[W_{\\text{diss}} = \\tfrac{1}{2}\\times")
                      .append(fmtK2(sc.mass())).append("\\times\\left[(")
                      .append(fmtK2(sc.v0())).append(")^2-(")
                      .append(fmtK2(sc.vf())).append(")^2\\right]\\]\n\n")
                      .append("\\[W_{\\text{diss}} = \\tfrac{1}{2}\\times")
                      .append(fmtK2(sc.mass())).append("\\times[")
                      .append(fmtK2(sc.v0() * sc.v0())).append("-")
                      .append(fmtK2(sc.vf() * sc.vf())).append("]")
                      .append(" = \\boxed{").append(fmtK2(sc.answer())).append("\\,\\text{J}}\\]");
                }
            }
        }
        return sb.toString();
    }

    // =========================================================================
    // CONSTRUCTOR — HARMONIC_OSCILLATOR_ENERGY
    // =========================================================================

    private void buildHarmonic(FirstBachEnergyExercise ex) {
        ex.setEnergyType(EnergyType.HARMONIC_OSCILLATOR_ENERGY);
        ex.setTolerancePercent(2.0);
        ex.setAnswerUnit("J");

        HarmonicData sc = HARMONIC.get(random.nextInt(HARMONIC.size()));
        ex.setUnknownVariable(sc.unknownVar());
        ex.setCorrectAnswerValue(sc.answer());
        ex.setCorrectAnswerDisplay(fmt2(sc.answer()) + " J");

        double Em = 0.5 * sc.k() * sc.A() * sc.A();
        double Ep = 0.5 * sc.k() * sc.x() * sc.x();
        double Ec = Em - Ep;

        String askVar = "energia_cinetica".equals(sc.unknownVar())
            ? "la energía cinética (Eₕ) en esa posición"
            : "la energía potencial elástica (Eₚ) en esa posición";
        String posDesc = sc.x() == 0.0
            ? "pasa por la posición de equilibrio (x = 0)"
            : "se encuentra en la posición x = " + fmt2(sc.x()) + " m";

        ex.setStatement(String.format(
            "Un oscilador armónico simple tiene masa m = %s kg, " +
            "constante elástica k = %s N/m y amplitud A = %s m. " +
            "En un instante dado, el oscilador %s. " +
            "Calcula %s (en J).",
            fmt1(sc.mass()), fmt1(sc.k()), fmt2(sc.A()), posDesc, askVar));

        ex.setExplanation(buildHarmonicExplanation(sc, Em, Ep, Ec));
    }

    private String buildHarmonicExplanation(HarmonicData sc, double Em, double Ep, double Ec) {
        var sb = new StringBuilder();
        String xDisp  = sc.x() == 0.0 ? "0" : fmt2(sc.x());
        String xKatex = sc.x() == 0.0 ? "0" : fmtK2(sc.x());

        sb.append("<strong>Energía mecánica total del MAS:</strong>\n\n")
          .append("\\[E_m = \\tfrac{1}{2}kA^2 = \\tfrac{1}{2}\\times")
          .append(fmtK2(sc.k())).append("\\times(").append(fmtK2(sc.A()))
          .append(")^2 = ").append(fmtK2(Em)).append("\\,\\text{J}\\]\n\n");

        sb.append("<strong>Energía potencial elástica en x = ")
          .append(xDisp).append(" m:</strong>\n\n")
          .append("\\[E_p = \\tfrac{1}{2}kx^2 = \\tfrac{1}{2}\\times")
          .append(fmtK2(sc.k())).append("\\times(").append(xKatex)
          .append(")^2 = ").append(fmtK2(Ep)).append("\\,\\text{J}\\]\n\n");

        sb.append("<strong>Energía cinética en x = ")
          .append(xDisp).append(" m:</strong>\n\n")
          .append("\\[E_c = E_m - E_p = ").append(fmtK2(Em))
          .append(" - ").append(fmtK2(Ep)).append(" = ")
          .append(fmtK2(Ec)).append("\\,\\text{J}\\]\n\n");

        String boxed = "energia_cinetica".equals(sc.unknownVar())
            ? "E_c = \\boxed{" + fmtK2(sc.answer()) + "\\,\\text{J}}"
            : "E_p = \\boxed{" + fmtK2(sc.answer()) + "\\,\\text{J}}";

        sb.append("<strong>Verificación (conservación):</strong> ")
          .append("\\(E_c + E_p = ").append(fmtK2(Ec)).append(" + ").append(fmtK2(Ep))
          .append(" = ").append(fmtK2(Em)).append("\\,\\text{J} = E_m\\) ✓\n\n")
          .append("∴ \\(").append(boxed).append("\\)");

        return sb.toString();
    }

    // =========================================================================
    // CONSTRUCTOR — ELECTRIC_POTENTIAL_WORK
    // =========================================================================

    private void buildElectric(FirstBachEnergyExercise ex) {
        ex.setEnergyType(EnergyType.ELECTRIC_POTENTIAL_WORK);
        ex.setTolerancePercent(2.0);
        ex.setAnswerUnit("J");
        ex.setUnknownVariable("trabajo_electrico");

        ElectricPotentialData sc = ELECTRIC.get(random.nextInt(ELECTRIC.size()));
        ex.setCorrectAnswerValue(sc.work());
        ex.setCorrectAnswerDisplay(fmtSciDisp(sc.work()) + " J");

        ex.setStatement(String.format(
            "Una carga eléctrica puntual q = %s se desplaza entre dos " +
            "puntos A y B de un campo eléctrico, siendo la diferencia de potencial " +
            "V_A − V_B = %s V. " +
            "Calcula el trabajo realizado por el campo eléctrico (en J). " +
            "Usa notación científica si es necesario (ej: 1.60e-16).",
            sc.chargeDisplay(), fmt1(sc.deltaV())));

        ex.setExplanation(buildElectricExplanation(sc));
    }

    private String buildElectricExplanation(ElectricPotentialData sc) {
        var sb = new StringBuilder();

        sb.append("<strong>Trabajo del campo eléctrico sobre una carga puntual:</strong>\n\n")
          .append("\\[W_{AB} = q \\cdot (V_A - V_B) = q \\cdot \\Delta V\\]\n\n")
          .append("<strong>Significado físico del signo:</strong>\n\n")
          .append("<ul>")
          .append("<li>Si \\(W > 0\\): el campo <em>favorece</em> el desplazamiento "
                + "(trabajo motor, la carga gana Eₕ).</li>")
          .append("<li>Si \\(W < 0\\): el campo <em>se opone</em> al desplazamiento "
                + "(trabajo resistente, la carga pierde Eₕ).</li>")
          .append("</ul>\n\n")
          .append("<strong>Sustitución numérica:</strong>\n\n")
          .append("\\[W_{AB} = \\bigl(").append(fmtKSci(sc.chargeCoulombs()))
          .append("\\,\\text{C}\\bigr) \\times ").append(fmtK2(sc.deltaV()))
          .append("\\,\\text{V}\\]\n\n")
          .append("\\[W_{AB} = ").append(fmtKSci(sc.work())).append("\\,\\text{J}\\]\n\n");

        if (sc.chargeCoulombs() < 0) {
            sb.append("<strong>Nota sobre el signo:</strong> La carga es negativa, por lo que " +
                      "el campo eléctrico realiza trabajo resistente (\\(W < 0\\)) sobre " +
                      "ella en este desplazamiento. Para moverla de B a A sería necesario " +
                      "aportar una energía igual a \\(|W|\\).\n\n");
        }

        sb.append("∴ \\(W_{AB} = \\boxed{").append(fmtKSci(sc.work()))
          .append("\\,\\text{J}}\\)");

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

    private String fmtK2(double v) { return fmt2(v).replace(",", "{,}"); }

    /** KaTeX scientific: "1{,}60 \\times 10^{-16}" */
    private String fmtKSci(double v) {
        if (v == 0) return "0";
        double absV = Math.abs(v);
        if (absV >= 0.01 && absV < 10000) return fmtK2(v);
        int exp = (int) Math.floor(Math.log10(absV));
        double mantissa = v / Math.pow(10, exp);
        return fmtK2(mantissa) + " \\times 10^{" + exp + "}";
    }

    /** Display scientific (Unicode): "1,60 × 10⁻¹⁶" */
    private String fmtSciDisp(double v) {
        if (v == 0) return "0";
        double absV = Math.abs(v);
        if (absV >= 0.01 && absV < 10000) return fmt2(v);
        int exp = (int) Math.floor(Math.log10(absV));
        double mantissa = v / Math.pow(10, exp);
        StringBuilder sup = new StringBuilder();
        if (exp < 0) sup.append('⁻');
        for (char c : String.valueOf(Math.abs(exp)).toCharArray()) {
            sup.append(switch (c) {
                case '0' -> '⁰'; case '1' -> '¹'; case '2' -> '²';
                case '3' -> '³'; case '4' -> '⁴'; case '5' -> '⁵';
                case '6' -> '⁶'; case '7' -> '⁷'; case '8' -> '⁸';
                case '9' -> '⁹'; default -> c;
            });
        }
        return fmt2(mantissa) + " × 10" + sup;
    }
}
