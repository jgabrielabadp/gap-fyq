package com.gap.fyq.service;

import com.gap.fyq.model.scientificactivity.ExerciseType;
import com.gap.fyq.model.scientificactivity.Magnitude;
import com.gap.fyq.model.scientificactivity.ScientificActivityExercise;
import com.gap.fyq.repository.ScientificActivityExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import static com.gap.fyq.model.scientificactivity.Magnitude.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScientificActivityExerciseService {

    private final ScientificActivityExerciseRepository repository;
    private final Random random = new Random();

    private static final String COURSE = "2ESO";
    private static final String BLOCK  = "BL1";

    // -------------------------------------------------------------------------
    // Definiciones de conversión de unidades
    // -------------------------------------------------------------------------

    private record ConversionDef(
        Magnitude magnitude,
        String sourceUnit,
        String targetUnit,
        double factor,
        String conversionReference,
        int[] sampleValues
    ) {}

    private static final List<ConversionDef> CONVERSIONS = List.of(
        new ConversionDef(LONGITUD, "mm", "m",   0.001,    "1 m = 1000 mm",
            new int[]{100, 150, 200, 250, 350, 500, 750, 800, 1200, 1500, 2500, 5000}),
        new ConversionDef(LONGITUD, "cm", "m",   0.01,     "1 m = 100 cm",
            new int[]{10, 25, 50, 75, 100, 125, 150, 200, 250, 500}),
        new ConversionDef(LONGITUD, "km", "m",   1000.0,   "1 km = 1000 m",
            new int[]{1, 2, 3, 5, 10, 15, 25, 50, 100}),
        new ConversionDef(LONGITUD, "m",  "km",  0.001,    "1 km = 1000 m",
            new int[]{500, 1000, 1500, 2000, 5000, 10000, 25000}),
        new ConversionDef(MASA,     "g",  "kg",  0.001,    "1 kg = 1000 g",
            new int[]{100, 250, 500, 750, 1000, 1500, 2000, 2500, 5000}),
        new ConversionDef(MASA,     "mg", "g",   0.001,    "1 g = 1000 mg",
            new int[]{100, 250, 500, 750, 1000, 1500, 2000, 5000}),
        new ConversionDef(MASA,     "kg", "g",   1000.0,   "1 kg = 1000 g",
            new int[]{1, 2, 3, 5, 10, 15, 25, 50}),
        new ConversionDef(MASA,     "t",  "kg",  1000.0,   "1 t = 1000 kg",
            new int[]{1, 2, 3, 5, 10, 25, 50, 100}),
        new ConversionDef(TIEMPO,   "min","s",   60.0,     "1 min = 60 s",
            new int[]{1, 2, 5, 10, 15, 20, 30, 45, 60}),
        new ConversionDef(TIEMPO,   "h",  "min", 60.0,     "1 h = 60 min",
            new int[]{1, 2, 3, 6, 12, 24}),
        new ConversionDef(TIEMPO,   "h",  "s",   3600.0,   "1 h = 3600 s",
            new int[]{1, 2, 3, 6, 12, 24}),
        new ConversionDef(TIEMPO,   "s",  "min", 1.0 / 60, "1 min = 60 s",
            new int[]{60, 120, 180, 240, 300, 600, 1200, 3600})
    );

    // -------------------------------------------------------------------------
    // Definiciones de notación científica
    // -------------------------------------------------------------------------

    private record SciNotationDef(
        double value,
        String valueDisplay,
        double mantissa,
        int exponent,
        Magnitude magnitude,
        String unit
    ) {}

    private static final List<SciNotationDef> SCI_NOTATION_DEFS = List.of(
        new SciNotationDef(125000,      "125 000",    1.25, 5,  LONGITUD, "m"),
        new SciNotationDef(45000,       "45 000",     4.5,  4,  LONGITUD, "m"),
        new SciNotationDef(0.00035,     "0,00035",    3.5,  -4, LONGITUD, "m"),
        new SciNotationDef(0.0000012,   "0,0000012",  1.2,  -6, MASA,     "kg"),
        new SciNotationDef(6700000,     "6 700 000",  6.7,  6,  MASA,     "g"),
        new SciNotationDef(0.0045,      "0,0045",     4.5,  -3, MASA,     "kg"),
        new SciNotationDef(86400,       "86 400",     8.64, 4,  TIEMPO,   "s"),
        new SciNotationDef(0.00025,     "0,00025",    2.5,  -4, LONGITUD, "m"),
        new SciNotationDef(1250000,     "1 250 000",  1.25, 6,  LONGITUD, "m"),
        new SciNotationDef(0.000075,    "0,000075",   7.5,  -5, MASA,     "g"),
        new SciNotationDef(300000,      "300 000",    3.0,  5,  TIEMPO,   "s"),
        new SciNotationDef(0.00000082,  "0,00000082", 8.2,  -7, MASA,     "kg")
    );

    // -------------------------------------------------------------------------
    // API pública
    // -------------------------------------------------------------------------

    public ScientificActivityExercise generateAndSave() {
        ScientificActivityExercise ex = random.nextInt(10) < 7
            ? buildConversionExercise()
            : buildScientificNotationExercise();
        log.debug("Guardando ejercicio BL1: tipo={}", ex.getExerciseType());
        return repository.save(ex);
    }

    public ScientificActivityExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ejercicio no encontrado: " + id));
    }

    public boolean validateAnswer(Long exerciseId, String input) {
        return findById(exerciseId).validateAnswer(input);
    }

    // -------------------------------------------------------------------------
    // Constructores internos
    // -------------------------------------------------------------------------

    private ScientificActivityExercise buildConversionExercise() {
        ConversionDef def = CONVERSIONS.get(random.nextInt(CONVERSIONS.size()));
        int srcValue = def.sampleValues()[random.nextInt(def.sampleValues().length)];
        double result = srcValue * def.factor();
        String resultDisplay = formatDisplay(result);

        ScientificActivityExercise ex = new ScientificActivityExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);
        ex.setStatement(String.format("Expresa %d %s en %s.", srcValue, def.sourceUnit(), def.targetUnit()));
        ex.setMagnitude(def.magnitude());
        ex.setValue(srcValue);
        ex.setSourceUnit(def.sourceUnit());
        ex.setTargetUnit(def.targetUnit());
        ex.setCorrectAnswerValue(result);
        ex.setCorrectAnswerDisplay(resultDisplay + " " + def.targetUnit());
        ex.setExerciseType(ExerciseType.UNIT_CONVERSION);
        ex.setExplanation(buildConversionExplanation(srcValue, def, result, resultDisplay));
        return ex;
    }

    private ScientificActivityExercise buildScientificNotationExercise() {
        SciNotationDef def = SCI_NOTATION_DEFS.get(random.nextInt(SCI_NOTATION_DEFS.size()));
        String mantissaDisplay = formatDisplay(def.mantissa());
        String latexMantissa   = mantissaDisplay.replace(",", "{,}");

        ScientificActivityExercise ex = new ScientificActivityExercise();
        ex.setCourse(COURSE);
        ex.setBlock(BLOCK);
        ex.setStatement(String.format("Expresa %s %s en notación científica.", def.valueDisplay(), def.unit()));
        ex.setMagnitude(def.magnitude());
        ex.setValue(def.value());
        ex.setSourceUnit(def.unit());
        ex.setTargetUnit(def.unit());
        ex.setCorrectAnswerValue(def.value());
        // correctAnswerDisplay usa KaTeX para que se renderice en la vista de resultado
        ex.setCorrectAnswerDisplay(String.format("\\(%s \\times 10^{%d}\\) %s", latexMantissa, def.exponent(), def.unit()));
        ex.setExerciseType(ExerciseType.SCIENTIFIC_NOTATION);
        ex.setExplanation(buildSciNotationExplanation(def, mantissaDisplay, latexMantissa));
        return ex;
    }

    // -------------------------------------------------------------------------
    // Generadores de explicación (KaTeX)
    // -------------------------------------------------------------------------

    private String buildConversionExplanation(int srcValue, ConversionDef def,
                                              double result, String resultDisplay) {
        // Paso 2 usa \[...\] (display math) para mostrar la fracción del factor de conversión centrada
        String latexStep = String.format(
            "\\[%d \\text{ %s} \\times %s = %s \\text{ %s}\\]",
            srcValue, def.sourceUnit(),
            buildLatexFraction(def),
            resultDisplay.replace(",", "{,}"), def.targetUnit()
        );
        return String.format(
            "Paso 1: Identificamos la relación entre unidades.\n" +
            "        %s\n\n" +
            "Paso 2: Aplicamos el factor de conversión.\n" +
            "%s\n" +
            "∴  %d %s = %s %s",
            def.conversionReference(),
            latexStep,
            srcValue, def.sourceUnit(), resultDisplay, def.targetUnit()
        );
    }

    private String buildLatexFraction(ConversionDef def) {
        if (def.factor() < 1) {
            long denom = Math.round(1.0 / def.factor());
            return String.format("\\dfrac{1 \\text{ %s}}{%d \\text{ %s}}",
                def.targetUnit(), denom, def.sourceUnit());
        }
        long numer = Math.round(def.factor());
        return String.format("\\dfrac{%d \\text{ %s}}{1 \\text{ %s}}",
            numer, def.targetUnit(), def.sourceUnit());
    }

    private String buildSciNotationExplanation(SciNotationDef def, String mantissaDisplay, String latexMantissa) {
        String latexAnswer = String.format("\\(%s \\times 10^{%d}\\)", latexMantissa, def.exponent());
        String direction   = def.exponent() >= 0 ? "hacia la izquierda" : "hacia la derecha";
        int absExp         = Math.abs(def.exponent());
        return String.format(
            "Paso 1: Localizamos la primera cifra significativa.\n" +
            "        Primer dígito no nulo: %d\n\n" +
            "Paso 2: Colocamos la coma decimal tras esa primera cifra.\n" +
            "        Mantisa: \\(%s\\)\n\n" +
            "Paso 3: Contamos los lugares que se desplaza la coma.\n" +
            "        La coma se mueve %d lugar%s %s → exponente %+d\n\n" +
            "∴  %s %s = %s %s",
            (int) def.mantissa(),
            latexMantissa,
            absExp, absExp == 1 ? "" : "es", direction, def.exponent(),
            def.valueDisplay(), def.unit(), latexAnswer, def.unit()
        );
    }

    // -------------------------------------------------------------------------
    // Utilidad numérica
    // -------------------------------------------------------------------------

    private String formatDisplay(double value) {
        return new BigDecimal(Double.toString(value))
            .stripTrailingZeros()
            .toPlainString()
            .replace(".", ",");
    }
}
