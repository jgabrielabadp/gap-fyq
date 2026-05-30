package com.gap.fyq.model.firstbach.scientificactivity;

public enum FirstBachActivityType {

    /** Análisis de 4-5 medidas experimentales: cálculo encadenado del valor medio,
     *  el error absoluto medio (Ea = Σ|xᵢ−x̄|/n) y el error relativo (Er = Ea/x̄ × 100 %). */
    EXPERIMENTAL_ERRORS,

    /** Análisis dimensional: dada la ecuación de una magnitud física, obtener su
     *  ecuación de dimensiones usando M (masa), L (longitud), T (tiempo), I (intensidad). */
    DIMENSIONAL_ANALYSIS,

    /** Gráfica experimental: dada una tabla de 4 pares de datos perfectamente lineales,
     *  calcular la pendiente m = Δy/Δx e identificar sus unidades en el SI. */
    GRAPH_SLOPE_ANALYSIS
}
