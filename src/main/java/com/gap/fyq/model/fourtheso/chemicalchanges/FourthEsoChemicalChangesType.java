package com.gap.fyq.model.fourtheso.chemicalchanges;

public enum FourthEsoChemicalChangesType {

    /** Conversión entre gramos, moles y número de partículas usando la masa molar
     *  y la constante de Avogadro (N_A = 6,022 × 10²³ mol⁻¹). */
    MOL_AVOGADRO_CONVERSION,

    /** Estequiometría avanzada: ecuación de gas ideal (P·V = n·R·T)
     *  o molaridad (M = n/V). */
    ADVANCED_STOICHIOMETRY,

    /** Reactivo limitante: dadas las masas de dos reactivos de una ecuación ajustada,
     *  identificar el limitante y calcular la masa de producto formada. */
    LIMITING_REACTANT
}
