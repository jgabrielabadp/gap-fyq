package com.gap.fyq.service;

import com.gap.fyq.model.firstbach.dynamics.DynamicsType;
import com.gap.fyq.model.firstbach.dynamics.FirstBachDynamicsExercise;
import com.gap.fyq.repository.FirstBachDynamicsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirstBachDynamicsService {

    private final FirstBachDynamicsRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "1BACH";
    private static final String BLOCK  = "BL7";
    private static final double G_ACC  = 10.0;  // m/s² (convenio 1bach)
    private static final double G_GRAV = 6.674e-11; // N·m²/kg²
    private static final double K_COUL = 8.988e9;   // N·m²/C²

    // =========================================================================
    // DATA RECORDS
    // =========================================================================

    /**
     * Cuerpos ligados: m₁ sobre plano inclinado (α), m₂ colgante.
     * a = g(m₂ − m₁(sinα + μcosα)) / (m₁+m₂)   [pre-computada]
     * T = m₂(g − a)                               [pre-computada]
     */
    private record ConnectedBodyData(
        double alphaDeg,
        double sinA, double cosA,
        double m1, double m2, double mu,
        double a,   // m/s²
        double T    // N
    ) {}

    /** Choque inelástico o elástico unidimensional. */
    private record MomentumData(
        boolean isElastic,
        String context,       // descripción del choque
        double m1, double v1,
        double m2, double v2,
        double vf,            // velocidad final inelástico (o v1f elástico si isElastic)
        double v2f,           // velocidad final del cuerpo 2 en elástico
        String unknownVar     // "vf_inelastico" | "v1f_elastico" | "v2f_elastico"
    ) {}

    /** Ley de Newton o Coulomb. */
    private record FieldForceData(
        boolean isGravity,
        String description,
        double param1,    // m₁ (kg) o q₁ (C)
        double param2,    // m₂ (kg) o q₂ (C)
        double r,         // distancia (m)
        double force      // fuerza pre-computada (N)
    ) {}

    // =========================================================================
    // CONNECTED_BODIES — 8 escenarios (g = 10 m/s²)
    // Verificado: a=g(m₂−m₁(sinα+μcosα))/(m₁+m₂),  T=m₂(g−a)
    // =========================================================================

    private static final List<ConnectedBodyData> CONNECTED = List.of(

        // CB1: α=30°, m₁=4, m₂=6, μ=0.2  →  a=3.31, T=40.16
        new ConnectedBodyData(30.0, 0.5, 0.866025,
            4.0, 6.0, 0.2,
            computeA(4,6,0.5,0.866025,0.2), computeT(6,computeA(4,6,0.5,0.866025,0.2))),

        // CB2: α=0° (horizontal), m₁=5, m₂=3, μ=0.2  →  a=2.50, T=22.50
        new ConnectedBodyData(0.0, 0.0, 1.0,
            5.0, 3.0, 0.2,
            computeA(5,3,0,1,0.2), computeT(3,computeA(5,3,0,1,0.2))),

        // CB3: α=45°, m₁=2, m₂=4, μ=0.1  →  a=4.07, T=23.72
        new ConnectedBodyData(45.0, 0.707107, 0.707107,
            2.0, 4.0, 0.1,
            computeA(2,4,0.707107,0.707107,0.1), computeT(4,computeA(2,4,0.707107,0.707107,0.1))),

        // CB4: α=30°, m₁=5, m₂=5, μ=0 (liso)  →  a=2.50, T=37.50
        new ConnectedBodyData(30.0, 0.5, 0.866025,
            5.0, 5.0, 0.0,
            computeA(5,5,0.5,0.866025,0), computeT(5,computeA(5,5,0.5,0.866025,0))),

        // CB5: α=53°, m₁=3, m₂=6, μ=0.25  →  a=3.50, T=39.00
        new ConnectedBodyData(53.0, 0.798636, 0.601815,
            3.0, 6.0, 0.25,
            computeA(3,6,0.798636,0.601815,0.25), computeT(6,computeA(3,6,0.798636,0.601815,0.25))),

        // CB6: α=37°, m₁=4, m₂=7, μ=0.3  →  a=3.30, T=46.87
        new ConnectedBodyData(37.0, 0.601815, 0.798636,
            4.0, 7.0, 0.3,
            computeA(4,7,0.601815,0.798636,0.3), computeT(7,computeA(4,7,0.601815,0.798636,0.3))),

        // CB7: α=45°, m₁=4, m₂=8, μ=0.2  →  a=3.84, T=49.29
        new ConnectedBodyData(45.0, 0.707107, 0.707107,
            4.0, 8.0, 0.2,
            computeA(4,8,0.707107,0.707107,0.2), computeT(8,computeA(4,8,0.707107,0.707107,0.2))),

        // CB8: α=0° (horizontal), m₁=8, m₂=4, μ=0.15  →  a=2.33, T=30.67
        new ConnectedBodyData(0.0, 0.0, 1.0,
            8.0, 4.0, 0.15,
            computeA(8,4,0,1,0.15), computeT(4,computeA(8,4,0,1,0.15)))
    );

    /** a = g(m₂ − m₁(sinα + μcosα)) / (m₁+m₂) */
    private static double computeA(double m1, double m2,
                                    double sinA, double cosA, double mu) {
        return G_ACC * (m2 - m1*(sinA + mu*cosA)) / (m1 + m2);
    }
    /** T = m₂(g − a) */
    private static double computeT(double m2, double a) {
        return m2 * (G_ACC - a);
    }

    // =========================================================================
    // MOMENTUM_CONSERVATION — 8 escenarios
    // Inelástico: v_f = (m₁v₁+m₂v₂)/(m₁+m₂)
    // Elástico:   v₁'=((m₁−m₂)v₁+2m₂v₂)/(m₁+m₂),  v₂'=((m₂−m₁)v₂+2m₁v₁)/(m₁+m₂)
    // =========================================================================

    private static final List<MomentumData> MOMENTUM = List.of(

        // MC1: inelástico, m₁=4(6m/s)+m₂=2(0) → vf=4.00 m/s
        new MomentumData(false, "un bloque en movimiento choca con otro en reposo y quedan unidos",
            4.0, 6.0, 2.0, 0.0,
            (4*6+2*0.0)/(4+2), 0, "vf_inelastico"),

        // MC2: inelástico, mismo sentido, m₁=3(8)+m₂=5(2) → vf=4.25 m/s
        new MomentumData(false, "dos vehículos colisionan por alcance y quedan enganchados",
            3.0, 8.0, 5.0, 2.0,
            (3*8+5*2)/(3+5.0), 0, "vf_inelastico"),

        // MC3: inelástico, sentidos opuestos, m₁=6(5)+m₂=4(-3) → vf=1.80 m/s
        new MomentumData(false, "dos bolas de arcilla se lanzan en sentidos opuestos y se pegan",
            6.0, 5.0, 4.0, -3.0,
            (6*5+4*(-3))/(6+4.0), 0, "vf_inelastico"),

        // MC4: inelástico, m₁=2(0)+m₂=3(6) → vf=3.60 m/s
        new MomentumData(false, "un proyectil se incrusta en un bloque inicialmente en reposo",
            2.0, 0.0, 3.0, 6.0,
            (2*0.0+3*6)/(2+3.0), 0, "vf_inelastico"),

        // MC5: elástico masas iguales, m₁=m₂=2kg, v₁=4→v₁'=0, v₂'=4.00 m/s
        new MomentumData(true, "colisión elástica entre bolas de igual masa (intercambian velocidades)",
            2.0, 4.0, 2.0, 0.0,
            0.0, 4.0, "v2f_elastico"),

        // MC6: elástico m₁=4, v₁=6; m₂=2, v₂=0 → v₁'=2.00, v₂'=8.00 m/s
        new MomentumData(true, "colisión elástica entre bolas de distinta masa",
            4.0, 6.0, 2.0, 0.0,
            ((4-2)*6+2*2*0)/(4+2.0), (2*2*0+2*4*6.0)/(4+2.0), "v2f_elastico"),

        // MC7: elástico m₁=3, v₁=5; m₂=1, v₂=0 → v₁'=2.50, v₂'=7.50 m/s
        new MomentumData(true, "bola pesada choca elásticamente contra bola ligera en reposo",
            3.0, 5.0, 1.0, 0.0,
            ((3-1)*5+2*1*0)/(3+1.0), (2*3*5.0+0)/(3+1.0), "v1f_elastico"),

        // MC8: inelástico, sentidos opuestos, m₁=5(4)+m₂=3(-2) → vf=1.75 m/s
        new MomentumData(false, "dos patinadores se agarran tras el choque frontal",
            5.0, 4.0, 3.0, -2.0,
            (5*4+3*(-2))/(5+3.0), 0, "vf_inelastico")
    );

    // =========================================================================
    // FIELD_FORCES_COMPARISON — 8 escenarios
    // G = 6,674×10⁻¹¹ N·m²/kg²,  k = 8,988×10⁹ N·m²/C²
    // =========================================================================

    private static final List<FieldForceData> FIELD_FORCES = List.of(

        // FF1: Grav Tierra-Luna  →  F=1.98×10²⁰ N
        new FieldForceData(true, "entre la Tierra (m₁=5,972×10²⁴ kg) y la Luna (m₂=7,342×10²² kg) " +
            "separadas r = 3,844×10⁸ m",
            5.972e24, 7.342e22, 3.844e8,
            G_GRAV*5.972e24*7.342e22/(3.844e8*3.844e8)),

        // FF2: Grav 2 personas  →  F=3.27×10⁻⁷ N
        new FieldForceData(true, "entre dos personas de m₁=m₂=70 kg separadas r = 1 m",
            70.0, 70.0, 1.0,
            G_GRAV*70*70/1.0),

        // FF3: Coulomb p-e átomo H (radio de Bohr)  →  F≈8.24×10⁻⁸ N
        new FieldForceData(false, "entre el protón y el electrón del átomo de hidrógeno " +
            "(q₁=q₂=1,602×10⁻¹⁹ C, r=5,29×10⁻¹¹ m, radio de Bohr)",
            1.602e-19, 1.602e-19, 5.29e-11,
            K_COUL*(1.602e-19)*(1.602e-19)/(5.29e-11*5.29e-11)),

        // FF4: Grav 2 esferas 100 kg, 1 m  →  F=6.67×10⁻⁷ N
        new FieldForceData(true, "entre dos esferas metálicas de m₁=m₂=100 kg separadas r = 1 m",
            100.0, 100.0, 1.0,
            G_GRAV*100*100/1.0),

        // FF5: Coulomb 3 μC + 2 μC, 0.5 m  →  F≈0.22 N
        new FieldForceData(false, "entre dos cargas puntuales q₁=3,0×10⁻⁶ C y q₂=2,0×10⁻⁶ C " +
            "separadas r = 0,50 m",
            3e-6, 2e-6, 0.5,
            K_COUL*3e-6*2e-6/(0.5*0.5)),

        // FF6: Coulomb 2 electrones, 1 Å  →  F≈2.31×10⁻⁸ N
        new FieldForceData(false, "entre dos electrones (q=1,602×10⁻¹⁹ C) separados r = 1×10⁻¹⁰ m",
            1.602e-19, 1.602e-19, 1e-10,
            K_COUL*(1.602e-19)*(1.602e-19)/(1e-10*1e-10)),

        // FF7: Grav asteroides, masas 10²⁰ kg y 2×10²⁰ kg, r=10⁶ m  →  F=1.33×10¹⁸ N
        new FieldForceData(true, "entre dos asteroides de m₁=1,0×10²⁰ kg y m₂=2,0×10²⁰ kg " +
            "separados r = 1,0×10⁶ m",
            1e20, 2e20, 1e6,
            G_GRAV*1e20*2e20/(1e6*1e6)),

        // FF8: Coulomb 5 μC + 8 μC, 0.2 m  →  F≈8.99 N
        new FieldForceData(false, "entre dos cargas puntuales q₁=5,0×10⁻⁶ C y q₂=8,0×10⁻⁶ C " +
            "separadas r = 0,20 m",
            5e-6, 8e-6, 0.2,
            K_COUL*5e-6*8e-6/(0.2*0.2))
    );

    // =========================================================================
    // API PÚBLICA
    // =========================================================================

    public FirstBachDynamicsExercise generateAndSave() {
        FirstBachDynamicsExercise ex = new FirstBachDynamicsExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);
        ex.setExerciseMode("NUMERICAL");

        int roll = random.nextInt(3);
        if      (roll == 0) buildConnectedBodies(ex);
        else if (roll == 1) buildMomentum(ex);
        else                buildFieldForces(ex);

        log.debug("1BACH BL7 generado: type={} var={}",
            ex.getDynamicsType(), ex.getUnknownVariable());
        return repository.save(ex);
    }

    public FirstBachDynamicsExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ejercicio 1BACH BL7 no encontrado: " + id));
    }

    // =========================================================================
    // CONSTRUCTOR — CONNECTED_BODIES
    // =========================================================================

    private void buildConnectedBodies(FirstBachDynamicsExercise ex) {
        ex.setDynamicsType(DynamicsType.CONNECTED_BODIES);
        ex.setTolerancePercent(2.0);

        ConnectedBodyData sc = CONNECTED.get(random.nextInt(CONNECTED.size()));

        // Aleatoriamente: calcular aceleración o tensión
        boolean askAcceleration = random.nextBoolean();
        if (askAcceleration) {
            ex.setUnknownVariable("aceleracion");
            ex.setCorrectAnswerValue(sc.a());
            ex.setAnswerUnit("m/s²");
            ex.setCorrectAnswerDisplay(fmt2(sc.a()) + " m/s²");
        } else {
            ex.setUnknownVariable("tension");
            ex.setCorrectAnswerValue(sc.T());
            ex.setAnswerUnit("N");
            ex.setCorrectAnswerDisplay(fmt2(sc.T()) + " N");
        }

        String planoDesc = sc.alphaDeg() == 0.0
            ? "una superficie horizontal"
            : "un plano inclinado " + fmt1(sc.alphaDeg()) + "°";
        String rozDesc = sc.mu() == 0.0
            ? "Considera la superficie lisa (sin rozamiento)."
            : "El coeficiente de rozamiento cinético es μ = " + fmt2(sc.mu()) + ".";
        String askVerb = askAcceleration
            ? "Calcula la aceleración del sistema (en m/s²)."
            : "Calcula la tensión de la cuerda (en N).";

        ex.setStatement(String.format(
            "Dos masas m₁ = %s kg y m₂ = %s kg están conectadas por una cuerda inextensible " +
            "a través de una polea sin rozamiento. La masa m₁ reposa sobre %s y m₂ cuelga " +
            "libremente. %s %s (g = 10 m/s²).",
            fmt1(sc.m1()), fmt1(sc.m2()), planoDesc, rozDesc, askVerb));

        ex.setExplanation(buildConnectedExplanation(sc, askAcceleration));
    }

    // =========================================================================
    // CONSTRUCTOR — MOMENTUM_CONSERVATION
    // =========================================================================

    private void buildMomentum(FirstBachDynamicsExercise ex) {
        ex.setDynamicsType(DynamicsType.MOMENTUM_CONSERVATION);
        ex.setTolerancePercent(2.0);
        ex.setAnswerUnit("m/s");

        MomentumData sc = MOMENTUM.get(random.nextInt(MOMENTUM.size()));
        ex.setUnknownVariable(sc.unknownVar());

        double answer = switch (sc.unknownVar()) {
            case "vf_inelastico" -> sc.vf();
            case "v2f_elastico"  -> sc.v2f();
            default              -> sc.vf(); // v1f_elastico stored in vf field
        };
        ex.setCorrectAnswerValue(answer);
        ex.setCorrectAnswerDisplay(fmt2(answer) + " m/s");

        String v2str = sc.v2() == 0.0
            ? "m₂ = " + fmt1(sc.m2()) + " kg está en reposo"
            : "m₂ = " + fmt1(sc.m2()) + " kg con v₂ = " + fmt1(sc.v2()) + " m/s" +
              (sc.v2() < 0 ? " (sentido opuesto)" : "");
        String askVerb = switch (sc.unknownVar()) {
            case "vf_inelastico" -> "Calcula la velocidad del conjunto tras el impacto (en m/s).";
            case "v2f_elastico"  -> "Calcula la velocidad final de m₂ tras el choque (en m/s).";
            default              -> "Calcula la velocidad final de m₁ tras el choque (en m/s).";
        };

        ex.setStatement(String.format(
            "En un choque %s, m₁ = %s kg con v₁ = %s m/s colisiona con %s. %s",
            sc.isElastic() ? "perfectamente elástico" : "perfectamente inelástico",
            fmt1(sc.m1()), fmt1(sc.v1()), v2str, askVerb));

        ex.setExplanation(buildMomentumExplanation(sc));
    }

    // =========================================================================
    // CONSTRUCTOR — FIELD_FORCES_COMPARISON
    // =========================================================================

    private void buildFieldForces(FirstBachDynamicsExercise ex) {
        ex.setDynamicsType(DynamicsType.FIELD_FORCES_COMPARISON);
        ex.setTolerancePercent(2.0);

        FieldForceData sc = FIELD_FORCES.get(random.nextInt(FIELD_FORCES.size()));

        ex.setUnknownVariable(sc.isGravity() ? "fuerza_gravitatoria" : "fuerza_coulomb");
        ex.setCorrectAnswerValue(sc.force());
        ex.setAnswerUnit("N");
        ex.setCorrectAnswerDisplay(fmtSciDisp(sc.force()) + " N");

        String law = sc.isGravity() ? "gravitatoria (Ley de Newton)" : "eléctrica (Ley de Coulomb)";
        ex.setStatement(String.format(
            "Calcula la fuerza %s %s. " +
            "Usa G = 6,674×10⁻¹¹ N·m²/kg² o k = 8,988×10⁹ N·m²/C² según corresponda. " +
            "Expresa el resultado en N (notación científica si es necesario: ej. 1.98e20).",
            law, sc.description()));

        ex.setExplanation(buildFieldExplanation(sc));
    }

    // =========================================================================
    // EXPLICACIÓN — CONNECTED_BODIES
    // =========================================================================

    private String buildConnectedExplanation(ConnectedBodyData sc, boolean askedAcc) {
        double N   = sc.m1() * G_ACC * sc.cosA();
        double f   = sc.mu() * N;
        double Ppar = sc.m1() * G_ACC * sc.sinA();
        double net  = sc.m2() * G_ACC - Ppar - f;

        String boxed = askedAcc
            ? "a = \\boxed{" + fmtK2(sc.a()) + "\\,\\text{m/s}^2}"
            : "T = \\boxed{" + fmtK2(sc.T()) + "\\,\\text{N}}";

        var sb = new StringBuilder();

        sb.append("<strong>Sistema: m₁ (plano")
          .append(sc.alphaDeg() == 0 ? " horizontal" : " inclinado α=" + fmt1(sc.alphaDeg()) + "°")
          .append(") — cuerda — m₂ (colgante)</strong>\n\n");

        sb.append("<strong>Fuerzas sobre m₁:</strong>\n\n")
          .append("<ul>");
        if (sc.alphaDeg() > 0) {
            sb.append("<li>Componente del peso paralela al plano (hacia abajo): ")
              .append("\\(P_{1\\parallel} = m_1 g\\sin\\alpha = ")
              .append(fmtK2(sc.m1())).append("\\times 10\\times")
              .append(fmtK2(sc.sinA())).append(" = ").append(fmtK2(Ppar))
              .append("\\,\\text{N}\\)</li>");
        }
        sb.append("<li>Normal al plano: \\(N = m_1 g\\cos\\alpha = ")
          .append(fmtK2(sc.m1())).append("\\times 10\\times")
          .append(fmtK2(sc.cosA())).append(" = ").append(fmtK2(N))
          .append("\\,\\text{N}\\)</li>");
        if (sc.mu() > 0) {
            sb.append("<li>Rozamiento cinético: \\(f = \\mu N = ")
              .append(fmtK2(sc.mu())).append("\\times").append(fmtK2(N))
              .append(" = ").append(fmtK2(f)).append("\\,\\text{N}\\)</li>");
        } else {
            sb.append("<li>Sin rozamiento: f = 0 N</li>");
        }
        sb.append("<li>Tensión T (hacia arriba del plano)</li></ul>\n\n");

        sb.append("<strong>Ecuaciones de Newton</strong> ")
          .append("(sentido positivo: m₂ baja, m₁ sube el plano):\n\n");

        // Ec. [I] para m₂
        sb.append("\\[\\text{Para }m_2:\\quad m_2 g - T = m_2 a ")
          .append("\\implies ").append(fmtK2(sc.m2()*G_ACC))
          .append(" - T = ").append(fmtK2(sc.m2())).append("a \\quad[I]\\]\n\n");

        // Ec. [II] para m₁
        sb.append("\\[\\text{Para }m_1:\\quad T - P_{1\\parallel} - f = m_1 a ")
          .append("\\implies T - ").append(fmtK2(Ppar)).append(" - ").append(fmtK2(f))
          .append(" = ").append(fmtK2(sc.m1())).append("a \\quad[II]\\]\n\n");

        // Suma
        sb.append("<strong>Sumando [I]+[II]:</strong>\n\n")
          .append("\\[").append(fmtK2(sc.m2()*G_ACC))
          .append(" - ").append(fmtK2(Ppar))
          .append(" - ").append(fmtK2(f))
          .append(" = (").append(fmtK2(sc.m1())).append("+").append(fmtK2(sc.m2())).append(")\\,a\\]\n\n")
          .append("\\[").append(fmtK2(net))
          .append(" = ").append(fmtK2(sc.m1()+sc.m2())).append("a ")
          .append("\\implies a = \\frac{").append(fmtK2(net)).append("}{")
          .append(fmtK2(sc.m1()+sc.m2())).append("} = ").append(fmtK2(sc.a()))
          .append("\\,\\text{m/s}^2\\]\n\n");

        // Tensión desde [I]
        sb.append("<strong>Tensión (de [I]):</strong>\n\n")
          .append("\\[T = m_2(g-a) = ").append(fmtK2(sc.m2()))
          .append("\\times(10-").append(fmtK2(sc.a())).append(") = ")
          .append(fmtK2(sc.T())).append("\\,\\text{N}\\]\n\n");

        sb.append("∴ \\(").append(boxed).append("\\)");
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — MOMENTUM_CONSERVATION
    // =========================================================================

    private String buildMomentumExplanation(MomentumData sc) {
        var sb = new StringBuilder();

        if (!sc.isElastic()) {
            // Inelástico
            double Eci = 0.5*sc.m1()*sc.v1()*sc.v1() + 0.5*sc.m2()*sc.v2()*sc.v2();
            double Ecf = 0.5*(sc.m1()+sc.m2())*sc.vf()*sc.vf();
            sb.append("<strong>Choque perfectamente inelástico:</strong> los cuerpos quedan unidos.\n\n")
              .append("<strong>Conservación del momento lineal:</strong>\n\n")
              .append("\\[\\vec{p}_i = \\vec{p}_f \\implies m_1 v_1 + m_2 v_2 = (m_1+m_2)\\,v_f\\]\n\n")
              .append("\\[v_f = \\frac{m_1 v_1 + m_2 v_2}{m_1+m_2} = ")
              .append("\\frac{").append(fmtK2(sc.m1())).append("\\times(").append(fmtK2(sc.v1()))
              .append(")+").append(fmtK2(sc.m2())).append("\\times(").append(fmtK2(sc.v2())).append(")}")
              .append("{").append(fmtK2(sc.m1()+sc.m2())).append("} = ")
              .append(fmtK2(sc.vf())).append("\\,\\text{m/s}\\]\n\n");
            sb.append("<strong>Verificación energética</strong> (energía NO conservada):\n\n")
              .append("\\[E_{c,i} = ").append(fmtK2(Eci)).append("\\,\\text{J} \\qquad ")
              .append("E_{c,f} = ").append(fmtK2(Ecf)).append("\\,\\text{J} \\qquad ")
              .append("\\Delta E_c = ").append(fmtK2(Ecf-Eci)).append("\\,\\text{J (energía absorbida)}\\]\n\n");
            sb.append("∴ \\(v_f = \\boxed{").append(fmtK2(sc.vf())).append("\\,\\text{m/s}}\\)");

        } else {
            // Elástico
            double Eci = 0.5*sc.m1()*sc.v1()*sc.v1() + 0.5*sc.m2()*sc.v2()*sc.v2();
            double Ecf = 0.5*sc.m1()*sc.vf()*sc.vf() + 0.5*sc.m2()*sc.v2f()*sc.v2f();
            String boxed = "v1f_elastico".equals(sc.unknownVar())
                ? "v_1' = \\boxed{" + fmtK2(sc.vf()) + "\\,\\text{m/s}}"
                : "v_2' = \\boxed{" + fmtK2(sc.v2f()) + "\\,\\text{m/s}}";

            sb.append("<strong>Choque perfectamente elástico:</strong> ")
              .append("se conservan momento lineal <em>y</em> energía cinética.\n\n")
              .append("<strong>Fórmulas de resolución analítica:</strong>\n\n")
              .append("\\[v_1' = \\frac{(m_1-m_2)v_1 + 2m_2 v_2}{m_1+m_2}, \\quad")
              .append("v_2' = \\frac{(m_2-m_1)v_2 + 2m_1 v_1}{m_1+m_2}\\]\n\n")
              .append("<strong>Sustitución numérica:</strong>\n\n")
              .append("\\[v_1' = \\frac{(").append(fmtK2(sc.m1())).append("-").append(fmtK2(sc.m2()))
              .append(")\\times").append(fmtK2(sc.v1())).append(" + 2\\times").append(fmtK2(sc.m2()))
              .append("\\times").append(fmtK2(sc.v2())).append("}{")
              .append(fmtK2(sc.m1()+sc.m2())).append("} = ").append(fmtK2(sc.vf())).append("\\,\\text{m/s}\\]\n\n")
              .append("\\[v_2' = \\frac{(").append(fmtK2(sc.m2())).append("-").append(fmtK2(sc.m1()))
              .append(")\\times").append(fmtK2(sc.v2())).append(" + 2\\times").append(fmtK2(sc.m1()))
              .append("\\times").append(fmtK2(sc.v1())).append("}{")
              .append(fmtK2(sc.m1()+sc.m2())).append("} = ").append(fmtK2(sc.v2f())).append("\\,\\text{m/s}\\]\n\n")
              .append("<strong>Verificación:</strong> \\(E_{c,i} = ").append(fmtK2(Eci))
              .append("\\,\\text{J} = E_{c,f} = ").append(fmtK2(Ecf)).append("\\,\\text{J}\\) ✓\n\n")
              .append("∴ \\(").append(boxed).append("\\)");
        }
        return sb.toString();
    }

    // =========================================================================
    // EXPLICACIÓN — FIELD_FORCES_COMPARISON
    // =========================================================================

    private String buildFieldExplanation(FieldForceData sc) {
        var sb = new StringBuilder();

        if (sc.isGravity()) {
            sb.append("<strong>Ley de Gravitación Universal de Newton:</strong>\n\n")
              .append("\\[F = G \\cdot \\frac{m_1 \\cdot m_2}{r^2}\\]\n\n")
              .append("\\[G = 6{,}674 \\times 10^{-11}\\,\\text{N·m}^2/\\text{kg}^2\\]\n\n")
              .append("<strong>Sustitución:</strong>\n\n")
              .append("\\[F = \\frac{6{,}674\\times10^{-11} \\times ")
              .append(fmtKSci(sc.param1())).append(" \\times ")
              .append(fmtKSci(sc.param2())).append("}{(")
              .append(fmtKSci(sc.r())).append(")^2}\\]\n\n")
              .append("\\[F = ").append(fmtKSci(sc.force())).append("\\,\\text{N}\\]\n\n");
        } else {
            sb.append("<strong>Ley de Coulomb:</strong>\n\n")
              .append("\\[F = k \\cdot \\frac{|q_1| \\cdot |q_2|}{r^2}\\]\n\n")
              .append("\\[k = 8{,}988 \\times 10^{9}\\,\\text{N·m}^2/\\text{C}^2\\]\n\n")
              .append("<strong>Sustitución:</strong>\n\n")
              .append("\\[F = \\frac{8{,}988\\times10^{9} \\times ")
              .append(fmtKSci(sc.param1())).append(" \\times ")
              .append(fmtKSci(sc.param2())).append("}{(")
              .append(fmtKSci(sc.r())).append(")^2}\\]\n\n")
              .append("\\[F = ").append(fmtKSci(sc.force())).append("\\,\\text{N}\\]\n\n");
        }

        double grav = G_GRAV*9.11e-31*1.67e-27/(1e-10*1e-10); // tiny for context
        sb.append("<strong>Orden de magnitud:</strong> ")
          .append(sc.isGravity()
            ? "La gravitación es siempre atractiva e insignificante a escala atómica."
            : "La fuerza eléctrica puede ser atractiva o repulsiva según los signos de las cargas.")
          .append("\n\n∴ \\(F = \\boxed{").append(fmtKSci(sc.force()))
          .append("\\,\\text{N}}\\)");

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

    /** Notación científica en KaTeX: "1{,}98 \\times 10^{20}" */
    private String fmtKSci(double v) {
        if (v == 0) return "0";
        double absV = Math.abs(v);
        if (absV >= 0.01 && absV < 1000) {
            return fmtK2(v);  // regular notation
        }
        int exp = (int) Math.floor(Math.log10(absV));
        double mantissa = v / Math.pow(10, exp);
        return fmtK2(mantissa) + " \\times 10^{" + exp + "}";
    }

    /** Notación científica para correctAnswerDisplay (con × y superíndice Unicode). */
    private String fmtSciDisp(double v) {
        if (v == 0) return "0";
        double absV = Math.abs(v);
        if (absV >= 0.01 && absV < 10000) {
            return fmt2(v);
        }
        int exp = (int) Math.floor(Math.log10(absV));
        double mantissa = v / Math.pow(10, exp);
        String expStr = String.valueOf(exp);
        // Digits to superscripts
        StringBuilder supStr = new StringBuilder();
        if (exp < 0) supStr.append("⁻");
        for (char c : String.valueOf(Math.abs(exp)).toCharArray()) {
            supStr.append(switch (c) {
                case '0'->'⁰'; case '1'->'¹'; case '2'->'²'; case '3'->'³';
                case '4'->'⁴'; case '5'->'⁵'; case '6'->'⁶'; case '7'->'⁷';
                case '8'->'⁸'; case '9'->'⁹'; default -> c;
            });
        }
        return fmt2(mantissa) + " × 10" + supStr;
    }
}
