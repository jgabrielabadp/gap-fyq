package com.gap.fyq.model.fourtheso.matter;

public enum FourthEsoMatterType {

    /** El alumno escribe la configuración electrónica completa de un elemento (Z 1-36)
     *  usando la notación estándar (p.ej. 1s2 2s2 2p6). Se normaliza el input. */
    ELECTRONIC_CONFIGURATION,

    /** Cálculo numérico de la masa atómica media ponderada a partir de 2 o 3 isótopos
     *  con sus masas y porcentajes de abundancia (suman 100 %). */
    ISOTOPE_MASS_CALCULATION,

    /** Opción múltiple: identificar el tipo de enlace (iónico, covalente molecular,
     *  red covalente o metálico) a partir de propiedades físicas descritas. */
    CHEMICAL_BOND_PROPERTIES
}
