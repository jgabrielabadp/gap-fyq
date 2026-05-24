package com.gap.fyq.controller;

import com.gap.fyq.model.matter.MatterConceptualExercise;
import com.gap.fyq.service.MatterConceptualExerciseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/eso2/bl2/conceptual")
@RequiredArgsConstructor
public class MatterConceptualController {

    private final MatterConceptualExerciseService service;

    // Página completa — GET /eso2/bl2/conceptual
    @GetMapping("")
    public String page(Model model) {
        MatterConceptualExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("BL2C página — variante={} id={}", ejercicio.getVariant(), ejercicio.getId());
        return "eso2/bl2/conceptual-page";
    }

    // HTMX GET — nuevo ejercicio, devuelve fragmento tarjeta
    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        MatterConceptualExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("BL2C ejercicio — variante={} id={}", ejercicio.getVariant(), ejercicio.getId());
        return "eso2/bl2/conceptual :: tarjeta";
    }

    // HTMX POST — valida respuesta, devuelve fragmento resultado
    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        MatterConceptualExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("BL2C id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "eso2/bl2/conceptual :: resultado";
    }
}
