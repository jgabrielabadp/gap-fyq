package com.gap.fyq.controller;

import com.gap.fyq.model.changes.ChemicalChangesExercise;
import com.gap.fyq.service.ChemicalChangesExerciseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/eso2/bl3")
@RequiredArgsConstructor
public class ChemicalChangesController {

    private final ChemicalChangesExerciseService service;

    // Página completa — GET /eso2/bl3
    @GetMapping("")
    public String page(Model model) {
        ChemicalChangesExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("BL3 página — variante={} id={}", ejercicio.getVariant(), ejercicio.getId());
        return "eso2/bl3/page";
    }

    // HTMX GET — nuevo ejercicio, devuelve fragmento tarjeta
    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        ChemicalChangesExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("BL3 ejercicio — variante={} id={}", ejercicio.getVariant(), ejercicio.getId());
        return "eso2/bl3/ejercicio :: tarjeta";
    }

    // HTMX POST — valida respuesta, devuelve fragmento resultado
    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        ChemicalChangesExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("BL3 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "eso2/bl3/ejercicio :: resultado";
    }
}
