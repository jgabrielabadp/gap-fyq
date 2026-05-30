package com.gap.fyq.model.fourtheso.forcesmotion;

public enum FourthEsoDynamicsType {

    /** MRUV vertical: caída libre o lanzamiento hacia arriba (g = 9,8 m/s²).
     *  Incógnitas: tiempo de caída, altura máxima o velocidad de impacto. */
    VERTICAL_MOTION,

    /** MCU: dados rpm y radio, calcular ω (rad/s), v (m/s) o a_c (m/s²). */
    CIRCULAR_MOTION,

    /** Dinámica en plano horizontal con rozamiento: F_r = μ·m·g y 2ª Ley de Newton.
     *  Incógnitas: fuerza de rozamiento o aceleración resultante. */
    FRICTION_DYNAMICS
}
