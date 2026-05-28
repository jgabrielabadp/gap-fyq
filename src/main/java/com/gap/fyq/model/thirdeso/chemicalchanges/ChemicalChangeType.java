package com.gap.fyq.model.thirdeso.chemicalchanges;

public enum ChemicalChangeType {

    /** Ajuste de coeficientes estequiométricos de una ecuación química. */
    EQUATION_BALANCING,

    /** Cálculo numérico basado en la conservación de la masa (Lavoisier). */
    LAVOISIER_LAW,

    /** Problema masa-a-masa con ecuación ya ajustada (estequiometría básica). */
    BASIC_STOICHIOMETRY
}
