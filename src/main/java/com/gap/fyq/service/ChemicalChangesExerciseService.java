package com.gap.fyq.service;

import com.gap.fyq.model.changes.ChemicalChangesExercise;
import com.gap.fyq.model.changes.ChemicalChangesVariant;
import com.gap.fyq.repository.ChemicalChangesExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ChemicalChangesExerciseService {

    private final ChemicalChangesExerciseRepository repository;
    private final Random random = new Random();

    // ── Opciones fijas para PHYSICAL_VS_CHEMICAL ──────────────────────────
    private static final String PHYS = "Cambio físico";
    private static final String CHEM = "Cambio químico";

    // ── Registros internos ─────────────────────────────────────────────────
    private record QuestionDef(
            String statement,
            String opt0, String opt1, String opt2, String opt3,
            int correct,
            String explanation) {}

    private record PhenomenonDef(
            String phenomenon,
            boolean isChemical,
            String explanation) {}

    // ── Banco PHYSICAL_VS_CHEMICAL (8 fenómenos) ───────────────────────────
    private static final List<PhenomenonDef> PHENOMENA = List.of(

        new PhenomenonDef(
            "combustión de la madera",
            true,
            "La combustión de la madera es un cambio químico. La madera (celulosa, lignina) reacciona " +
            "con el oxígeno del aire y produce sustancias completamente nuevas: dióxido de carbono " +
            "(CO₂) y vapor de agua (H₂O), invisibles, y cenizas (óxidos minerales). No es posible " +
            "recuperar la madera original. La aparición de nuevas sustancias, el calor y la luz " +
            "emitidos son evidencias de reacción química."
        ),

        new PhenomenonDef(
            "fusión del hielo",
            false,
            "La fusión del hielo es un cambio físico. El agua (H₂O) pasa del estado sólido al " +
            "líquido al absorber energía, pero su composición química no varía: sigue siendo H₂O. " +
            "No se forman nuevas sustancias y el proceso es fácilmente reversible: al enfriar, el " +
            "agua vuelve a congelarse. Los cambios de estado son siempre cambios físicos."
        ),

        new PhenomenonDef(
            "digestión de los alimentos",
            true,
            "La digestión es un cambio químico. Mediante enzimas y ácidos, las moléculas complejas " +
            "de los alimentos (proteínas, grasas, carbohidratos) se rompen formando moléculas más " +
            "pequeñas y distintas: aminoácidos, ácidos grasos, glucosa. Las sustancias iniciales " +
            "desaparecen y aparecen nuevas, lo que es la definición de reacción química."
        ),

        new PhenomenonDef(
            "dilatación de una barra de hierro al calentarla",
            false,
            "La dilatación es un cambio físico. Al calentarse, los átomos de hierro vibran con " +
            "mayor amplitud y la barra aumenta ligeramente de longitud, pero la composición química " +
            "(hierro puro, Fe) no cambia en absoluto. Al enfriarse, la barra recupera sus " +
            "dimensiones originales. No se forman nuevas sustancias."
        ),

        new PhenomenonDef(
            "fotosíntesis de una planta",
            true,
            "La fotosíntesis es un cambio químico. Las plantas transforman dióxido de carbono (CO₂) " +
            "y agua (H₂O) en glucosa (C₆H₁₂O₆) y oxígeno (O₂) usando la energía de la luz solar. " +
            "Las sustancias de partida (CO₂ y H₂O) se convierten en sustancias completamente " +
            "distintas, por lo que es una reacción química de enorme importancia biológica."
        ),

        new PhenomenonDef(
            "rotura de un cristal de vidrio",
            false,
            "Romper un cristal es un cambio físico. Los trozos de vidrio resultantes tienen la misma " +
            "composición química que el cristal original (dióxido de silicio y otros óxidos). " +
            "Solo ha cambiado la forma y el tamaño de los fragmentos. No se han producido nuevas " +
            "sustancias ni ha habido reacción química."
        ),

        new PhenomenonDef(
            "oxidación del hierro (formación de óxido de hierro)",
            true,
            "La oxidación del hierro es un cambio químico. El hierro (Fe) reacciona con el oxígeno " +
            "del aire y la humedad para producir óxido de hierro (Fe₂O₃), vulgarmente conocido como " +
            "herrumbre o moho. Esta nueva sustancia, de color rojizo-anaranjado, tiene propiedades " +
            "completamente distintas al hierro metálico. No es posible recuperar el hierro original " +
            "sin una reacción química inversa."
        ),

        new PhenomenonDef(
            "ebullición del agua",
            false,
            "La ebullición del agua es un cambio físico. El agua líquida (H₂O) se convierte en " +
            "vapor de agua (también H₂O) al alcanzar los 100 °C. Solo cambia el estado de " +
            "agregación: la composición química es idéntica. Al condensarse, el vapor vuelve a ser " +
            "agua líquida. No hay nuevas sustancias ni reacción química."
        )
    );

    // ── Banco REACTANTS_PRODUCTS (7 reacciones) ────────────────────────────
    private static final List<QuestionDef> REACTIONS = List.of(

        new QuestionDef(
            "Al quemar una vela, la parafina reacciona con el oxígeno del aire. " +
            "¿Cuáles son los reactivos de esta reacción?",
            "Parafina y oxígeno",
            "Dióxido de carbono y vapor de agua",
            "Parafina y vapor de agua",
            "Luz y calor",
            0,
            "Los reactivos son las sustancias que se consumen al inicio de la reacción. En la " +
            "combustión de la vela, la parafina (el combustible) y el oxígeno del aire (el " +
            "comburente) son los reactivos. El dióxido de carbono, el vapor de agua, la luz y el " +
            "calor son los productos o efectos de la reacción, no sus materias primas."
        ),

        new QuestionDef(
            "En la fotosíntesis, las plantas producen glucosa y oxígeno. " +
            "¿Cuáles son los productos de esta reacción?",
            "Dióxido de carbono y agua",
            "Glucosa y clorofila",
            "Glucosa y oxígeno",
            "Agua y oxígeno",
            2,
            "Los productos son las sustancias que se forman al final de la reacción. En la " +
            "fotosíntesis, las plantas usan CO₂ y H₂O (reactivos) para sintetizar glucosa " +
            "(C₆H₁₂O₆) y liberar oxígeno (O₂) al aire. La clorofila es el catalizador (facilita " +
            "la reacción pero no se consume), no un producto."
        ),

        new QuestionDef(
            "El vinagre (ácido acético) reacciona con el bicarbonato sódico y produce " +
            "acetato de sodio, agua y dióxido de carbono. ¿Cuáles son los reactivos?",
            "Acetato de sodio, agua y CO₂",
            "Ácido acético y bicarbonato sódico",
            "Bicarbonato sódico y CO₂",
            "Agua y CO₂",
            1,
            "Los reactivos son los ingredientes que se mezclan antes de que comience la reacción. " +
            "En este caso, el ácido acético (vinagre) y el bicarbonato sódico son las sustancias de " +
            "partida que se transforman. El acetato de sodio, el agua y el CO₂ (que produce las " +
            "burbujas características) son los productos de la reacción."
        ),

        new QuestionDef(
            "En la respiración celular, las células queman glucosa con oxígeno para " +
            "obtener energía. ¿Cuáles son los productos de esta reacción?",
            "Glucosa y oxígeno",
            "ATP y glucosa",
            "Dióxido de carbono y agua",
            "Nitrógeno y vapor de agua",
            2,
            "La respiración celular es la reacción inversa a la fotosíntesis. La glucosa (C₆H₁₂O₆) " +
            "y el oxígeno (O₂) son los reactivos. Al oxidarse la glucosa, se liberan dióxido de " +
            "carbono (CO₂), agua (H₂O) y energía en forma de ATP. El CO₂ exhalado al respirar y " +
            "el vapor de agua son los productos visibles de esta reacción en nuestro cuerpo."
        ),

        new QuestionDef(
            "Al encender una cerilla, el fósforo rojo reacciona con el oxígeno y produce " +
            "pentóxido de difósforo (P₄O₁₀). ¿Cuál es el único producto de esta reacción?",
            "Fósforo rojo y oxígeno",
            "Pentóxido de difósforo (P₄O₁₀)",
            "Dióxido de carbono",
            "Agua y cenizas",
            1,
            "En la combustión del fósforo rojo, los reactivos son el fósforo (P₄) y el oxígeno (O₂). " +
            "El único producto es el pentóxido de difósforo (P₄O₁₀), un polvo blanco que aparece " +
            "como humo al encenderse la cerilla. Esta reacción ilustra bien la distinción: lo que " +
            "entra (reactivos) frente a lo que se forma (producto)."
        ),

        new QuestionDef(
            "El zinc reacciona con el ácido clorhídrico produciendo cloruro de zinc y " +
            "gas hidrógeno. ¿Cuáles son los reactivos?",
            "Cloruro de zinc e hidrógeno",
            "Zinc y cloruro de zinc",
            "Zinc y ácido clorhídrico",
            "Ácido clorhídrico e hidrógeno",
            2,
            "El zinc (Zn) metálico y el ácido clorhídrico (HCl) son las sustancias que se " +
            "combinan al inicio de la reacción: son los reactivos. Como resultado se forman cloruro " +
            "de zinc (ZnCl₂), que queda disuelto, y gas hidrógeno (H₂), que burbujea. Esta reacción " +
            "es un ejemplo clásico de metal reaccionando con un ácido."
        ),

        new QuestionDef(
            "En la electrólisis del agua se aplica corriente eléctrica y el agua se " +
            "descompone. ¿Cuáles son los productos?",
            "Hidrógeno y nitrógeno",
            "Oxígeno e hidrógeno",
            "Agua y corriente eléctrica",
            "Hidrógeno y dióxido de carbono",
            1,
            "En la electrólisis del agua (H₂O) se usa energía eléctrica para romper sus enlaces y " +
            "obtener los dos elementos que la componen: gas hidrógeno (H₂) en el cátodo (polo " +
            "negativo) y gas oxígeno (O₂) en el ánodo (polo positivo). Es la reacción inversa a la " +
            "formación del agua. El reactivo es el agua y los productos son H₂ y O₂."
        )
    );

    // ── Banco CHEMICAL_SOCIETY (6 preguntas) ──────────────────────────────
    private static final List<QuestionDef> SOCIETY = List.of(

        new QuestionDef(
            "¿Cuál de estas afirmaciones sobre el uso seguro de productos del hogar es correcta?",
            "Mezclar lejía con amoniaco produce una mezcla más limpiadora y segura",
            "Los productos de limpieza deben almacenarse en recipientes de bebida para ahorrar espacio",
            "Se deben leer las etiquetas de los productos químicos antes de usarlos, " +
                "especialmente los pictogramas de peligro",
            "Los productos con olor agradable no pueden ser tóxicos",
            2,
            "Leer la etiqueta y los pictogramas de peligro (GHS/SGA) es imprescindible antes de " +
            "usar cualquier producto químico. Mezclar lejía (hipoclorito sódico) con amoniaco " +
            "genera gases cloramínicos muy tóxicos que pueden causar daño pulmonar grave. Nunca se " +
            "debe trasvasar un producto a un envase sin etiquetar (riesgo de ingestión accidental). " +
            "El olor agradable no indica ausencia de toxicidad."
        ),

        new QuestionDef(
            "¿Qué indica el pictograma de una calavera con tibias cruzadas (GHS06) en un producto?",
            "Que el producto es inflamable",
            "Que el producto es corrosivo y daña la piel",
            "Que el producto es tóxico o muy tóxico: puede causar daño grave o muerte",
            "Que el producto protege el medio ambiente",
            2,
            "El pictograma GHS06 (calavera con tibias) es uno de los símbolos del Sistema " +
            "Globalmente Armonizado (SGA). Indica que la sustancia es tóxica o muy tóxica: puede " +
            "causar efectos graves para la salud (incluso la muerte) por inhalación, ingestión o " +
            "contacto con la piel. El símbolo de llama indica inflamabilidad; el de tubo de ensayo " +
            "con corrosión indica corrosividad; el árbol con pez indica peligro medioambiental."
        ),

        new QuestionDef(
            "Desde el punto de vista del reciclaje, ¿en qué contenedor se deben depositar " +
            "las latas de refresco?",
            "Contenedor amarillo (envases ligeros)",
            "Contenedor verde (vidrio)",
            "Contenedor azul (papel y cartón)",
            "Contenedor gris o marrón (resto o fracción orgánica)",
            0,
            "Las latas de refresco son envases metálicos (aluminio o acero). Se depositan en el " +
            "contenedor amarillo, destinado a envases ligeros: latas, bricks, botellas y envases de " +
            "plástico. El aluminio reciclado ahorra hasta un 95 % de la energía necesaria para " +
            "producirlo desde mineral. El vidrio va al verde; el papel y cartón, al azul; los " +
            "restos no reciclables, al gris o marrón."
        ),

        new QuestionDef(
            "¿Cuál es el principal impacto ambiental de emitir grandes cantidades de " +
            "dióxido de carbono (CO₂) a la atmósfera?",
            "Destrucción de la capa de ozono",
            "Lluvia ácida",
            "Efecto invernadero reforzado y calentamiento global",
            "Eutrofización de ríos y lagos",
            2,
            "El CO₂ es el principal gas de efecto invernadero de origen humano. Al aumentar su " +
            "concentración en la atmósfera, retiene más calor solar y eleva la temperatura media del " +
            "planeta (calentamiento global), provocando cambios climáticos, deshielo de polos y " +
            "subida del nivel del mar. La destrucción de la capa de ozono se asocia principalmente " +
            "a los CFC; la lluvia ácida, al SO₂ y los NOₓ; la eutrofización, a los nitratos."
        ),

        new QuestionDef(
            "¿Cuál de las siguientes afirmaciones sobre los medicamentos es correcta?",
            "Tomar el doble de dosis acelera la recuperación sin riesgo",
            "Los medicamentos caducados se deben tirar a la basura doméstica",
            "Todos los medicamentos son seguros si se toman según la prescripción médica " +
                "o el prospecto",
            "Los medicamentos naturales o de herboristería no tienen ningún efecto secundario",
            2,
            "Los medicamentos son sustancias químicas con efectos biológicos demostrados. " +
            "Utilizados según la prescripción médica o el prospecto son seguros y eficaces, pero " +
            "superar la dosis puede causar toxicidad. Los medicamentos caducados deben depositarse " +
            "en los puntos SIGRE (farmacias), no en la basura ni por el desagüe. Los productos " +
            "naturales también pueden tener efectos secundarios e interacciones medicamentosas."
        ),

        new QuestionDef(
            "La industria química produce materiales imprescindibles en la vida moderna. " +
            "¿Cuál de los siguientes NO es un producto de la industria química?",
            "Plásticos y polímeros sintéticos",
            "Fertilizantes para la agricultura",
            "Madera sin tratar extraída del bosque",
            "Medicamentos y principios activos farmacéuticos",
            2,
            "La madera sin tratar que se extrae directamente del bosque es un material natural que " +
            "no requiere transformación química industrial. Los plásticos (polietileno, PVC, nylon) " +
            "son polímeros sintetizados industrialmente; los fertilizantes (nitratos, fosfatos) se " +
            "fabrican en procesos como Haber-Bosch; y los medicamentos son elaborados mediante " +
            "síntesis química o biotecnología. La industria química mejora nuestra calidad de vida " +
            "pero también debe gestionar sus residuos responsablemente."
        )
    );

    // ── API pública ────────────────────────────────────────────────────────

    public ChemicalChangesExercise generateAndSave() {
        ChemicalChangesExercise e = new ChemicalChangesExercise();
        e.setCourse("2ESO");
        e.setBlock("BL3");

        // Distribución: 38 % PHYSICAL_VS_CHEMICAL, 33 % REACTANTS_PRODUCTS, 29 % CHEMICAL_SOCIETY
        int roll = random.nextInt(21);
        if (roll < 8) {
            buildPhysicalVsChemical(e);
        } else if (roll < 15) {
            buildReactantsProducts(e);
        } else {
            buildChemicalSociety(e);
        }
        return repository.save(e);
    }

    public ChemicalChangesExercise findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ejercicio BL3 no encontrado: " + id));
    }

    // ── Constructores internos ─────────────────────────────────────────────

    private void buildPhysicalVsChemical(ChemicalChangesExercise e) {
        PhenomenonDef p = PHENOMENA.get(random.nextInt(PHENOMENA.size()));
        e.setVariant(ChemicalChangesVariant.PHYSICAL_VS_CHEMICAL);
        e.setStatement("Clasifica el siguiente fenómeno cotidiano: " + p.phenomenon());
        e.setOption0(PHYS);
        e.setOption1(CHEM);
        // Opciones de relleno para mantener la rejilla de 4 opciones
        e.setOption2("No es un fenómeno natural");
        e.setOption3("No se puede clasificar sin más datos");
        e.setCorrectIndex(p.isChemical() ? 1 : 0);
        e.setExplanation(p.explanation());
    }

    private void buildReactantsProducts(ChemicalChangesExercise e) {
        QuestionDef q = REACTIONS.get(random.nextInt(REACTIONS.size()));
        e.setVariant(ChemicalChangesVariant.REACTANTS_PRODUCTS);
        e.setStatement(q.statement());
        e.setOption0(q.opt0());
        e.setOption1(q.opt1());
        e.setOption2(q.opt2());
        e.setOption3(q.opt3());
        e.setCorrectIndex(q.correct());
        e.setExplanation(q.explanation());
    }

    private void buildChemicalSociety(ChemicalChangesExercise e) {
        QuestionDef q = SOCIETY.get(random.nextInt(SOCIETY.size()));
        e.setVariant(ChemicalChangesVariant.CHEMICAL_SOCIETY);
        e.setStatement(q.statement());
        e.setOption0(q.opt0());
        e.setOption1(q.opt1());
        e.setOption2(q.opt2());
        e.setOption3(q.opt3());
        e.setCorrectIndex(q.correct());
        e.setExplanation(q.explanation());
    }
}
