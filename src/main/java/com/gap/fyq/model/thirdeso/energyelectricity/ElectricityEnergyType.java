package com.gap.fyq.model.thirdeso.energyelectricity;

public enum ElectricityEnergyType {

    /** Calor absorbido o cedido: Q = m · ce · ΔT */
    HEAT_CALCULATION,

    /** Ley de Ohm: V = I · R (incógnita variable) */
    OHM_LAW,

    /** Consumo eléctrico y coste: E (kWh) = P (kW) · t (h), coste = E · tarifa */
    ELECTRIC_COST
}
