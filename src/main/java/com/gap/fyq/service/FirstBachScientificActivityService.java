package com.gap.fyq.service;

import com.gap.fyq.model.firstbach.scientificactivity.FirstBachActivityType;
import com.gap.fyq.model.firstbach.scientificactivity.FirstBachScientificActivityExercise;
import com.gap.fyq.repository.FirstBachScientificActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirstBachScientificActivityService {

    private final FirstBachScientificActivityRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "1BACH";
    private static final String BLOCK  = "BL1";

    // =========================================================================
    // EXPERIMENTAL_ERRORS — 6 datasets de medidas reales
    // Fórmulas: x̄ = Σxᵢ/n  |  Ea = Σ|xᵢ−x̄|/n  |  Er = Ea/x̄ × 100 %
    // Todos los valores verificados algebraicamente (resultados limpios a 2 d.p.)
    // =========================================================================

    private record ErrorDataSet(
        String quantityName,   // "la temperatura"
        String quantitySymbol, // "T"
        String unit,           // "°C"
        List<Double> values,
        double mean,
        double absoluteError,
        double relativeError   // en %
    ) {}

    private static final List<ErrorDataSet> ERROR_DATASETS = List.of(

        // Dataset 1: Temperatura — 5 medidas
        // Media=(24,5+25,5+25,0+24,0+26,0)/5=25,00 ; Ea=(0,5+0,5+0+1+1)/5=0,60 ; Er=2,40%
        new ErrorDataSet("la temperatura", "T", "°C",
            List.of(24.5, 25.5, 25.0, 24.0, 26.0),
            25.00, 0.60, 2.40),

        // Dataset 2: Masa — 5 medidas
        // Media=(9,8+10,2+10,0+9,6+10,4)/5=10,00 ; Ea=(0,2+0,2+0+0,4+0,4)/5=0,24 ; Er=2,40%
        new ErrorDataSet("la masa", "m", "g",
            List.of(9.8, 10.2, 10.0, 9.6, 10.4),
            10.00, 0.24, 2.40),

        // Dataset 3: Período — 5 medidas
        // Media=(2,0+2,2+2,1+1,9+1,8)/5=2,00 ; Ea=(0+0,2+0,1+0,1+0,2)/5=0,12 ; Er=6,00%
        new ErrorDataSet("el período del péndulo", "T", "s",
            List.of(2.0, 2.2, 2.1, 1.9, 1.8),
            2.00, 0.12, 6.00),

        // Dataset 4: Fuerza — 5 medidas
        // Media=(19,5+20,5+20,0+19,0+21,0)/5=20,00 ; Ea=(0,5+0,5+0+1+1)/5=0,60 ; Er=3,00%
        new ErrorDataSet("la fuerza", "F", "N",
            List.of(19.5, 20.5, 20.0, 19.0, 21.0),
            20.00, 0.60, 3.00),

        // Dataset 5: Longitud — 4 medidas
        // Media=(9,8+10,2+10,0+10,0)/4=10,00 ; Ea=(0,2+0,2+0+0)/4=0,10 ; Er=1,00%
        new ErrorDataSet("la longitud", "l", "cm",
            List.of(9.8, 10.2, 10.0, 10.0),
            10.00, 0.10, 1.00),

        // Dataset 6: Resistencia eléctrica — 4 medidas
        // Media=(9,5+10,5+10,0+10,0)/4=10,00 ; Ea=(0,5+0,5+0+0)/4=0,25 ; Er=2,50%
        new ErrorDataSet("la resistencia eléctrica", "R", "Ω",
            List.of(9.5, 10.5, 10.0, 10.0),
            10.00, 0.25, 2.50)
    );

    // =========================================================================
    // DIMENSIONAL_ANALYSIS — 12 escenarios
    // Notación: M=masa, L=longitud, T=tiempo, I=intensidad eléctrica
    // correctDimensionFormula: cadena legible y parseable (p.ej. "M·L·T⁻²")
    // =========================================================================

    private record DimScenario(
        String quantityName,
        String formula,
        String symbol,
        String correctFormula,  // display + parseable form
        String explanation
    ) {}

    private static final List<DimScenario> DIM_SCENARIOS = List.of(

        new DimScenario("Fuerza", "F = m·a", "F", "M·L·T⁻²",
            "Sustituimos las dimensiones de cada magnitud en \\(F = m \\cdot a\\):\n\n" +
            "<ul><li>\\([m] = \\text{M}\\) (masa)</li>" +
            "<li>\\([a] = \\text{L}\\cdot\\text{T}^{-2}\\) (aceleración = longitud/tiempo²)</li></ul>\n\n" +
            "\\[[F] = [m]\\cdot[a] = \\text{M}\\cdot\\text{L}\\cdot\\text{T}^{-2}\\]\n\n" +
            "∴ \\(\\boxed{[F] = \\text{M}\\cdot\\text{L}\\cdot\\text{T}^{-2}}\\)"
        ),

        new DimScenario("Presión", "P = F/S", "P", "M·L⁻¹·T⁻²",
            "Sustituimos en \\(P = F/S\\):\n\n" +
            "<ul><li>\\([F] = \\text{M}\\cdot\\text{L}\\cdot\\text{T}^{-2}\\)</li>" +
            "<li>\\([S] = \\text{L}^2\\) (área = longitud²)</li></ul>\n\n" +
            "\\[[P] = \\frac{\\text{M}\\cdot\\text{L}\\cdot\\text{T}^{-2}}{\\text{L}^2} " +
            "= \\text{M}\\cdot\\text{L}^{1-2}\\cdot\\text{T}^{-2} = \\text{M}\\cdot\\text{L}^{-1}\\cdot\\text{T}^{-2}\\]\n\n" +
            "∴ \\(\\boxed{[P] = \\text{M}\\cdot\\text{L}^{-1}\\cdot\\text{T}^{-2}}\\)"
        ),

        new DimScenario("Energía (trabajo)", "E = F·d", "E", "M·L²·T⁻²",
            "Sustituimos en \\(E = F \\cdot d\\):\n\n" +
            "<ul><li>\\([F] = \\text{M}\\cdot\\text{L}\\cdot\\text{T}^{-2}\\)</li>" +
            "<li>\\([d] = \\text{L}\\) (desplazamiento)</li></ul>\n\n" +
            "\\[[E] = \\text{M}\\cdot\\text{L}\\cdot\\text{T}^{-2}\\cdot\\text{L} " +
            "= \\text{M}\\cdot\\text{L}^2\\cdot\\text{T}^{-2}\\]\n\n" +
            "∴ \\(\\boxed{[E] = \\text{M}\\cdot\\text{L}^2\\cdot\\text{T}^{-2}}\\)"
        ),

        new DimScenario("Potencia", "P = E/t", "P", "M·L²·T⁻³",
            "Sustituimos en \\(P = E/t\\):\n\n" +
            "<ul><li>\\([E] = \\text{M}\\cdot\\text{L}^2\\cdot\\text{T}^{-2}\\)</li>" +
            "<li>\\([t] = \\text{T}\\)</li></ul>\n\n" +
            "\\[[P] = \\frac{\\text{M}\\cdot\\text{L}^2\\cdot\\text{T}^{-2}}{\\text{T}} " +
            "= \\text{M}\\cdot\\text{L}^2\\cdot\\text{T}^{-3}\\]\n\n" +
            "∴ \\(\\boxed{[P] = \\text{M}\\cdot\\text{L}^2\\cdot\\text{T}^{-3}}\\)"
        ),

        new DimScenario("Velocidad", "v = s/t", "v", "L·T⁻¹",
            "Sustituimos en \\(v = s/t\\):\n\n" +
            "<ul><li>\\([s] = \\text{L}\\) (espacio)</li>" +
            "<li>\\([t] = \\text{T}\\)</li></ul>\n\n" +
            "\\[[v] = \\frac{\\text{L}}{\\text{T}} = \\text{L}\\cdot\\text{T}^{-1}\\]\n\n" +
            "∴ \\(\\boxed{[v] = \\text{L}\\cdot\\text{T}^{-1}}\\)"
        ),

        new DimScenario("Aceleración", "a = F/m", "a", "L·T⁻²",
            "Sustituimos en \\(a = F/m\\):\n\n" +
            "<ul><li>\\([F] = \\text{M}\\cdot\\text{L}\\cdot\\text{T}^{-2}\\)</li>" +
            "<li>\\([m] = \\text{M}\\)</li></ul>\n\n" +
            "\\[[a] = \\frac{\\text{M}\\cdot\\text{L}\\cdot\\text{T}^{-2}}{\\text{M}} " +
            "= \\text{L}\\cdot\\text{T}^{-2}\\]\n\n" +
            "∴ \\(\\boxed{[a] = \\text{L}\\cdot\\text{T}^{-2}}\\)"
        ),

        new DimScenario("Cantidad de movimiento (momento lineal)", "p = m·v", "p", "M·L·T⁻¹",
            "Sustituimos en \\(p = m \\cdot v\\):\n\n" +
            "<ul><li>\\([m] = \\text{M}\\)</li>" +
            "<li>\\([v] = \\text{L}\\cdot\\text{T}^{-1}\\)</li></ul>\n\n" +
            "\\[[p] = \\text{M}\\cdot\\text{L}\\cdot\\text{T}^{-1}\\]\n\n" +
            "∴ \\(\\boxed{[p] = \\text{M}\\cdot\\text{L}\\cdot\\text{T}^{-1}}\\)"
        ),

        new DimScenario("Densidad", "ρ = m/V", "ρ", "M·L⁻³",
            "Sustituimos en \\(\\rho = m/V\\):\n\n" +
            "<ul><li>\\([m] = \\text{M}\\)</li>" +
            "<li>\\([V] = \\text{L}^3\\) (volumen)</li></ul>\n\n" +
            "\\[[\\rho] = \\frac{\\text{M}}{\\text{L}^3} = \\text{M}\\cdot\\text{L}^{-3}\\]\n\n" +
            "∴ \\(\\boxed{[\\rho] = \\text{M}\\cdot\\text{L}^{-3}}\\)"
        ),

        new DimScenario("Constante elástica (muelle)", "k = F/x", "k", "M·T⁻²",
            "Sustituimos en \\(k = F/x\\) (Ley de Hooke):\n\n" +
            "<ul><li>\\([F] = \\text{M}\\cdot\\text{L}\\cdot\\text{T}^{-2}\\)</li>" +
            "<li>\\([x] = \\text{L}\\) (elongación)</li></ul>\n\n" +
            "\\[[k] = \\frac{\\text{M}\\cdot\\text{L}\\cdot\\text{T}^{-2}}{\\text{L}} " +
            "= \\text{M}\\cdot\\text{T}^{-2}\\]\n\n" +
            "∴ \\(\\boxed{[k] = \\text{M}\\cdot\\text{T}^{-2}}\\)"
        ),

        new DimScenario("Velocidad angular", "ω = v/r", "ω", "T⁻¹",
            "Sustituimos en \\(\\omega = v/r\\):\n\n" +
            "<ul><li>\\([v] = \\text{L}\\cdot\\text{T}^{-1}\\)</li>" +
            "<li>\\([r] = \\text{L}\\) (radio)</li></ul>\n\n" +
            "\\[[\\omega] = \\frac{\\text{L}\\cdot\\text{T}^{-1}}{\\text{L}} = \\text{T}^{-1}\\]\n\n" +
            "El radián es adimensional (cociente de dos longitudes).\n\n" +
            "∴ \\(\\boxed{[\\omega] = \\text{T}^{-1}}\\)"
        ),

        new DimScenario("Viscosidad dinámica", "η = F·d/(A·v)", "η", "M·L⁻¹·T⁻¹",
            "Sustituimos en \\(\\eta = \\frac{F \\cdot d}{A \\cdot v}\\):\n\n" +
            "<ul><li>\\([F] = \\text{M}\\cdot\\text{L}\\cdot\\text{T}^{-2}\\)</li>" +
            "<li>\\([d] = \\text{L}\\)</li>" +
            "<li>\\([A] = \\text{L}^2\\)</li>" +
            "<li>\\([v] = \\text{L}\\cdot\\text{T}^{-1}\\)</li></ul>\n\n" +
            "\\[[\\eta] = \\frac{\\text{M}\\cdot\\text{L}\\cdot\\text{T}^{-2}\\cdot\\text{L}}{" +
            "\\text{L}^2\\cdot\\text{L}\\cdot\\text{T}^{-1}} = " +
            "\\frac{\\text{M}\\cdot\\text{L}^2\\cdot\\text{T}^{-2}}{\\text{L}^3\\cdot\\text{T}^{-1}} " +
            "= \\text{M}\\cdot\\text{L}^{-1}\\cdot\\text{T}^{-1}\\]\n\n" +
            "∴ \\(\\boxed{[\\eta] = \\text{M}\\cdot\\text{L}^{-1}\\cdot\\text{T}^{-1}}\\)"
        ),

        new DimScenario("Tensión superficial", "γ = F/l", "γ", "M·T⁻²",
            "Sustituimos en \\(\\gamma = F/l\\):\n\n" +
            "<ul><li>\\([F] = \\text{M}\\cdot\\text{L}\\cdot\\text{T}^{-2}\\)</li>" +
            "<li>\\([l] = \\text{L}\\) (longitud del frente)</li></ul>\n\n" +
            "\\[[\\gamma] = \\frac{\\text{M}\\cdot\\text{L}\\cdot\\text{T}^{-2}}{\\text{L}} " +
            "= \\text{M}\\cdot\\text{T}^{-2}\\]\n\n" +
            "Coincide dimensionalmente con la constante elástica k — " +
            "ambas son fuerzas por longitud.\n\n" +
            "∴ \\(\\boxed{[\\gamma] = \\text{M}\\cdot\\text{T}^{-2}}\\)"
        )
    );

    // =========================================================================
    // GRAPH_SLOPE_ANALYSIS — 8 escenarios de rectas perfectas
    // Pendiente m = Δy/Δx calculada con los puntos extremos.
    // Todos los resultados verificados a 2 d.p.
    // =========================================================================

    private record SlopeScenario(
        String xHeader,      // "t (s)"
        String yHeader,      // "v (m/s)"
        double[] xs,
        double[] ys,
        double slope,
        String slopeUnit,
        String physContext,  // "MRUV — velocidad frente al tiempo"
        String interpretation, // "aceleración del objeto"
        String explanation
    ) {}

    private static final List<SlopeScenario> SLOPE_SCENARIOS = List.of(

        // 1. v vs t (MRUV, a=2 m/s²)
        // slope=(6-0)/(3-0)=6/3=2,00
        new SlopeScenario("t (s)", "v (m/s)",
            new double[]{0, 1, 2, 3}, new double[]{0, 2, 4, 6},
            2.00, "m/s²", "MRUV — velocidad frente al tiempo", "aceleración del objeto",
            "Tomamos los puntos extremos de la tabla para máxima precisión:\n\n" +
            "\\[m = \\frac{\\Delta v}{\\Delta t} = \\frac{v_4 - v_1}{t_4 - t_1} = " +
            "\\frac{6{,}0 - 0{,}0}{3{,}0 - 0{,}0} = \\frac{6}{3} = 2{,}00\\,\\text{m/s}^2\\]\n\n" +
            "Las dimensiones son \\([v]/[t] = \\text{L}\\cdot\\text{T}^{-1}/\\text{T} = " +
            "\\text{L}\\cdot\\text{T}^{-2}\\), es decir, <strong>aceleración</strong>.\n\n" +
            "∴  Pendiente m = <strong>2,00 m/s²</strong>"
        ),

        // 2. F vs x (Ley de Hooke, k=50 N/m)
        // slope=(20-5)/(0.4-0.1)=15/0.3=50,00
        new SlopeScenario("x (m)", "F (N)",
            new double[]{0.1, 0.2, 0.3, 0.4}, new double[]{5, 10, 15, 20},
            50.00, "N/m", "Ley de Hooke — fuerza elástica frente a elongación",
            "constante elástica del muelle",
            "Tomamos los puntos extremos:\n\n" +
            "\\[m = \\frac{\\Delta F}{\\Delta x} = \\frac{20 - 5}{0{,}4 - 0{,}1} = " +
            "\\frac{15}{0{,}3} = 50{,}00\\,\\text{N/m}\\]\n\n" +
            "La pendiente representa la <strong>constante elástica k</strong> de la Ley de Hooke " +
            "\\(F = k \\cdot x\\).\n\n" +
            "∴  Pendiente m = <strong>50,00 N/m</strong>"
        ),

        // 3. m vs V (densidad ρ=800 g/L)
        // slope=(3200-800)/(4-1)=2400/3=800,00
        new SlopeScenario("V (L)", "m (g)",
            new double[]{1, 2, 3, 4}, new double[]{800, 1600, 2400, 3200},
            800.00, "g/L", "densidad — masa frente a volumen", "densidad del líquido",
            "\\[m = \\frac{\\Delta m}{\\Delta V} = \\frac{3200 - 800}{4 - 1} = " +
            "\\frac{2400}{3} = 800{,}00\\,\\text{g/L}\\]\n\n" +
            "La pendiente es la <strong>densidad ρ</strong> de la sustancia: " +
            "\\(\\rho = m/V\\).\n\n" +
            "∴  Pendiente m = <strong>800,00 g/L</strong>"
        ),

        // 4. V vs I (Ley de Ohm, R=5 Ω)
        // slope=(10.0-2.5)/(2.0-0.5)=7.5/1.5=5,00
        new SlopeScenario("I (A)", "V (V)",
            new double[]{0.5, 1.0, 1.5, 2.0}, new double[]{2.5, 5.0, 7.5, 10.0},
            5.00, "Ω", "Ley de Ohm — tensión frente a intensidad", "resistencia eléctrica",
            "\\[m = \\frac{\\Delta V}{\\Delta I} = \\frac{10{,}0 - 2{,}5}{2{,}0 - 0{,}5} = " +
            "\\frac{7{,}5}{1{,}5} = 5{,}00\\,\\text{V/A} = 5{,}00\\,\\Omega\\]\n\n" +
            "La pendiente es la <strong>resistencia eléctrica R</strong> (Ley de Ohm: V = R·I).\n\n" +
            "∴  Pendiente m = <strong>5,00 Ω</strong>"
        ),

        // 5. s vs t (MRU, v=5 m/s)
        // slope=(20-5)/(4-1)=15/3=5,00
        new SlopeScenario("t (s)", "s (m)",
            new double[]{1, 2, 3, 4}, new double[]{5, 10, 15, 20},
            5.00, "m/s", "MRU — posición frente al tiempo", "velocidad del móvil",
            "\\[m = \\frac{\\Delta s}{\\Delta t} = \\frac{20 - 5}{4 - 1} = " +
            "\\frac{15}{3} = 5{,}00\\,\\text{m/s}\\]\n\n" +
            "La pendiente es la <strong>velocidad constante v</strong> del movimiento rectilíneo " +
            "uniforme (\\(s = v \\cdot t\\)).\n\n" +
            "∴  Pendiente m = <strong>5,00 m/s</strong>"
        ),

        // 6. W vs s (F=10 N constante)
        // slope=(40-10)/(4-1)=30/3=10,00
        new SlopeScenario("s (m)", "W (J)",
            new double[]{1, 2, 3, 4}, new double[]{10, 20, 30, 40},
            10.00, "N", "trabajo — energía frente a desplazamiento",
            "fuerza constante que realiza el trabajo",
            "\\[m = \\frac{\\Delta W}{\\Delta s} = \\frac{40 - 10}{4 - 1} = " +
            "\\frac{30}{3} = 10{,}00\\,\\text{J/m} = 10{,}00\\,\\text{N}\\]\n\n" +
            "La pendiente es la <strong>fuerza constante F</strong> que realiza el trabajo " +
            "(\\(W = F \\cdot s\\)).\n\n" +
            "∴  Pendiente m = <strong>10,00 N</strong>"
        ),

        // 7. Q vs ΔT (capacidad calorífica C=250 J/°C)
        // slope=(10000-2500)/(40-10)=7500/30=250,00
        new SlopeScenario("ΔT (°C)", "Q (J)",
            new double[]{10, 20, 30, 40}, new double[]{2500, 5000, 7500, 10000},
            250.00, "J/°C", "calorimetría — calor frente a variación de temperatura",
            "capacidad calorífica del sistema",
            "\\[m = \\frac{\\Delta Q}{\\Delta(\\Delta T)} = \\frac{10000 - 2500}{40 - 10} = " +
            "\\frac{7500}{30} = 250{,}00\\,\\text{J/°C}\\]\n\n" +
            "La pendiente es la <strong>capacidad calorífica C</strong> del sistema " +
            "(\\(Q = C \\cdot \\Delta T\\)).\n\n" +
            "∴  Pendiente m = <strong>250,00 J/°C</strong>"
        ),

        // 8. F vs m (2ª Ley Newton, a=10 m/s²)
        // slope=(40-10)/(4-1)=30/3=10,00
        new SlopeScenario("m (kg)", "F (N)",
            new double[]{1, 2, 3, 4}, new double[]{10, 20, 30, 40},
            10.00, "m/s²", "2ª Ley de Newton — fuerza frente a masa",
            "aceleración del sistema",
            "\\[m = \\frac{\\Delta F}{\\Delta m} = \\frac{40 - 10}{4 - 1} = " +
            "\\frac{30}{3} = 10{,}00\\,\\text{N/kg} = 10{,}00\\,\\text{m/s}^2\\]\n\n" +
            "La pendiente es la <strong>aceleración constante a</strong> imprimida al sistema " +
            "(\\(F = m \\cdot a\\), 2ª Ley de Newton).\n\n" +
            "∴  Pendiente m = <strong>10,00 m/s²</strong>"
        )
    );

    // =========================================================================
    // API pública
    // =========================================================================

    public FirstBachScientificActivityExercise generateAndSave() {
        FirstBachScientificActivityExercise ex = new FirstBachScientificActivityExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);

        int roll = random.nextInt(9);
        if (roll < 3) {
            buildExperimentalErrors(ex);
        } else if (roll < 6) {
            buildDimensionalAnalysis(ex);
        } else {
            buildGraphSlope(ex);
        }

        log.debug("1BACH BL1 generado: type={} mode={}", ex.getActivityType(), ex.getExerciseMode());
        return repository.save(ex);
    }

    public FirstBachScientificActivityExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ejercicio 1BACH BL1 no encontrado: " + id));
    }

    // =========================================================================
    // Constructor — EXPERIMENTAL_ERRORS
    // =========================================================================

    private static final String[] ERROR_UNKNOWNS = {"valor_medio", "error_absoluto", "error_relativo"};

    private void buildExperimentalErrors(FirstBachScientificActivityExercise ex) {
        ex.setActivityType(FirstBachActivityType.EXPERIMENTAL_ERRORS);
        ex.setExerciseMode("NUMERICAL");

        ErrorDataSet ds = ERROR_DATASETS.get(random.nextInt(ERROR_DATASETS.size()));
        String unknown = ERROR_UNKNOWNS[random.nextInt(3)];
        ex.setUnknownVariable(unknown);

        // Enunciado: lista de medidas
        String measureList = buildMeasureList(ds);
        String asks = switch (unknown) {
            case "valor_medio"      -> "Calcula el valor medio (promedio) de las medidas.";
            case "error_absoluto"   -> "Calcula el error absoluto medio (Ea) de las medidas.";
            default                 -> "Calcula el error relativo porcentual (Er en %).";
        };
        ex.setStatement(String.format(
            "Se han realizado %d medidas de %s: %s. %s",
            ds.values().size(), ds.quantityName(), measureList, asks));

        switch (unknown) {
            case "valor_medio" -> {
                ex.setCorrectAnswerValue(ds.mean());
                ex.setCorrectAnswerDisplay(fmt2(ds.mean()) + " " + ds.unit());
                ex.setAnswerUnit(ds.unit());
                ex.setTolerancePercent(1.0);
            }
            case "error_absoluto" -> {
                ex.setCorrectAnswerValue(ds.absoluteError());
                ex.setCorrectAnswerDisplay(fmt2(ds.absoluteError()) + " " + ds.unit());
                ex.setAnswerUnit(ds.unit());
                ex.setTolerancePercent(3.0);
            }
            default -> {
                ex.setCorrectAnswerValue(ds.relativeError());
                ex.setCorrectAnswerDisplay(fmt2(ds.relativeError()) + " %");
                ex.setAnswerUnit("%");
                ex.setTolerancePercent(2.0);
            }
        }

        ex.setExplanation(buildErrorExplanation(ds));
    }

    private String buildMeasureList(ErrorDataSet ds) {
        var sb = new StringBuilder();
        for (int i = 0; i < ds.values().size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(ds.quantitySymbol()).append("₊").append(i + 1)
              .append(" = ").append(fmtVal(ds.values().get(i)))
              .append(" ").append(ds.unit());
        }
        // Unicode subscript numbers would be nicer but plain ASCII is safer here
        return sb.toString()
            .replace("₊1", "₁").replace("₊2", "₂").replace("₊3", "₃")
            .replace("₊4", "₄").replace("₊5", "₅");
    }

    private String buildErrorExplanation(ErrorDataSet ds) {
        List<Double> v = ds.values();
        int n = v.size();

        // Suma para la media
        StringBuilder sumTerms = new StringBuilder();
        double sum = 0;
        for (int i = 0; i < n; i++) {
            if (i > 0) sumTerms.append(" + ");
            sumTerms.append(fmtKatex(v.get(i)));
            sum += v.get(i);
        }

        // Suma de desviaciones para Ea
        StringBuilder devTerms = new StringBuilder();
        double devSum = 0;
        for (int i = 0; i < n; i++) {
            if (i > 0) devTerms.append(" + ");
            double dev = Math.abs(v.get(i) - ds.mean());
            devTerms.append(fmtKatex(dev));
            devSum += dev;
        }

        return "<strong>Paso 1 — Valor medio:</strong>\n\n" +
            "\\[\\bar{x} = \\frac{\\sum_{i=1}^{" + n + "} x_i}{n} = " +
            "\\frac{" + sumTerms + "}{" + n + "} = " +
            "\\frac{" + fmtKatex(sum) + "}{" + n + "} = " +
            fmtKatex(ds.mean()) + "\\,\\text{" + ds.unit() + "}\\]\n\n" +
            "<strong>Paso 2 — Error absoluto medio:</strong>\n\n" +
            "\\[E_a = \\frac{\\sum_{i=1}^{" + n + "} |x_i - \\bar{x}|}{n} = " +
            "\\frac{" + devTerms + "}{" + n + "} = " +
            "\\frac{" + fmtKatex(devSum) + "}{" + n + "} = " +
            fmtKatex(ds.absoluteError()) + "\\,\\text{" + ds.unit() + "}\\]\n\n" +
            "<strong>Paso 3 — Error relativo porcentual:</strong>\n\n" +
            "\\[E_r = \\frac{E_a}{\\bar{x}} \\times 100 = " +
            "\\frac{" + fmtKatex(ds.absoluteError()) + "}{" + fmtKatex(ds.mean()) + "} " +
            "\\times 100 = " + fmtKatex(ds.relativeError()) + "\\,\\%\\]\n\n" +
            "∴  \\(\\bar{x} = " + fmtKatex(ds.mean()) + "\\,\\text{" + ds.unit() + "}\\),  " +
            "\\(E_a = " + fmtKatex(ds.absoluteError()) + "\\,\\text{" + ds.unit() + "}\\),  " +
            "\\(E_r = " + fmtKatex(ds.relativeError()) + "\\,\\%\\)";
    }

    // =========================================================================
    // Constructor — DIMENSIONAL_ANALYSIS
    // =========================================================================

    private void buildDimensionalAnalysis(FirstBachScientificActivityExercise ex) {
        ex.setActivityType(FirstBachActivityType.DIMENSIONAL_ANALYSIS);
        ex.setExerciseMode("DIMENSIONAL");

        DimScenario sc = DIM_SCENARIOS.get(random.nextInt(DIM_SCENARIOS.size()));
        ex.setStatement(String.format(
            "Dada la ecuación de la %s: %s. Halla la ecuación de dimensiones de %s " +
            "usando M, L, T (p.ej. M·L^-1·T^-2 o MLT-2).",
            sc.quantityName(), sc.formula(), sc.symbol()));
        ex.setCorrectDimensionFormula(sc.correctFormula());
        ex.setExplanation(sc.explanation());
    }

    // =========================================================================
    // Constructor — GRAPH_SLOPE_ANALYSIS
    // =========================================================================

    private void buildGraphSlope(FirstBachScientificActivityExercise ex) {
        ex.setActivityType(FirstBachActivityType.GRAPH_SLOPE_ANALYSIS);
        ex.setExerciseMode("NUMERICAL");
        ex.setUnknownVariable("pendiente");
        ex.setTolerancePercent(2.0);

        SlopeScenario sc = SLOPE_SCENARIOS.get(random.nextInt(SLOPE_SCENARIOS.size()));
        ex.setStatement(String.format(
            "La tabla muestra datos experimentales de %s en un experimento de %s. " +
            "Calcula la pendiente de la recta de ajuste.",
            sc.yHeader(), sc.physContext()));
        ex.setCorrectAnswerValue(sc.slope());
        ex.setCorrectAnswerDisplay(fmt2(sc.slope()) + " " + sc.slopeUnit());
        ex.setAnswerUnit(sc.slopeUnit());
        ex.setTableHtml(buildTableHtml(sc.xHeader(), sc.yHeader(), sc.xs(), sc.ys()));
        ex.setExplanation(sc.explanation());
    }

    // =========================================================================
    // Utilidades estáticas de formato y construcción
    // =========================================================================

    /** Formatea a 2 decimales con coma española. */
    private String fmt2(double v) {
        return String.format("%.2f", v).replace(".", ",");
    }

    /** Formatea a 2 decimales con {,} para KaTeX. */
    private String fmtKatex(double v) {
        return String.format("%.2f", v).replace(".", "{,}");
    }

    /** Formatea para celdas de tabla: entero si no tiene decimales, si no 1 d.p. */
    private static String fmtVal(double v) {
        if (v == Math.floor(v) && !Double.isInfinite(v)) return String.valueOf((long) v);
        return String.format("%.1f", v).replace(".", ",");
    }

    private static String buildTableHtml(String xH, String yH, double[] xs, double[] ys) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"data-table\">")
          .append("<thead><tr><th>").append(xH).append("</th>")
          .append("<th>").append(yH).append("</th></tr></thead><tbody>");
        for (int i = 0; i < xs.length; i++) {
            sb.append("<tr><td>").append(fmtVal(xs[i])).append("</td>")
              .append("<td>").append(fmtVal(ys[i])).append("</td></tr>");
        }
        sb.append("</tbody></table>");
        return sb.toString();
    }
}
