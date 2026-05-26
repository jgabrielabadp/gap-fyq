package com.gap.fyq.model.energy;

public enum EnergyType {

    /** Conversión entre Julios (J), Calorías (cal) y kilojulios (kJ) / kilovatios-hora (kWh). */
    ENERGY_UNITS,

    /** Cálculo del Trabajo (W = F·d) o la Potencia (P = W/t), con incógnita rotativa. */
    WORK_AND_POWER,

    /** Cálculo directo de Energía Cinética (Ec = ½mv²) o Potencial (Ep = mgh). */
    KINETIC_POTENTIAL,

    /** Preguntas tipo test sobre fuentes de energía, sostenibilidad e impacto ambiental. */
    SUSTAINABILITY_TEST
}
