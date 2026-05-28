package com.gap.fyq.model.thirdeso.forcesmotion;

public enum DynamicsType {

    /** Cálculo de aceleración o variables cinemáticas: a = (vf - vi) / t */
    ACCELERATION_MRUV,

    /** Segunda ley de Newton: F = m · a (incógnita variable) */
    NEWTON_SECOND_LAW,

    /** Principio de Pascal / prensa hidráulica: F1/S1 = F2/S2 */
    HYDRAULIC_PRESS
}
