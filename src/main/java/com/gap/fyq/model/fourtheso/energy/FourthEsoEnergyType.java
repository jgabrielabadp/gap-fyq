package com.gap.fyq.model.fourtheso.energy;

public enum FourthEsoEnergyType {

    /** Balance de energía mecánica con pérdida por rozamiento:
     *  E_mec_final = E_mec_inicial − W_Fr.
     *  Incógnitas: velocidad final o altura máxima alcanzada. */
    MECHANICAL_ENERGY_LOSS,

    /** Termoquímica: dada la ecuación con su ΔH (kJ/mol) y una masa en gramos,
     *  calcular el calor transferido Q = n × |ΔH|. */
    THERMOCHEMISTRY_CALC,

    /** Ecuación de onda v = λ·f. Ondas sonoras (v ≈ 340 m/s)
     *  y luz (c = 3 × 10⁸ m/s). Incógnitas: λ, f o v. */
    WAVE_PROPERTIES
}
