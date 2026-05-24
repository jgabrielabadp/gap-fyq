package com.gap.fyq.controller;

import com.gap.fyq.model.matter.MatterQuantitativeExercise;
import com.gap.fyq.service.MatterQuantitativeExerciseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/eso2/bl2")
@RequiredArgsConstructor
public class MatterQuantitativeController {

    private final MatterQuantitativeExerciseService service;

    @GetMapping("")
    public String page(Model model) {
        MatterQuantitativeExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("BL2 página — ejercicio generado: id={}", ejercicio.getId());
        return "eso2/bl2/page";
    }

    // HTMX GET — genera ejercicio nuevo y devuelve fragmento tarjeta
    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        MatterQuantitativeExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("BL2 ejercicio generado: id={} tipo={} ley={} incógnita={}",
            ejercicio.getId(), ejercicio.getExerciseType(),
            ejercicio.getGasLaw(), ejercicio.getUnknownVariable());
        return "eso2/bl2/ejercicio :: tarjeta";
    }

    // HTMX POST — valida respuesta y devuelve fragmento resultado
    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        MatterQuantitativeExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("BL2 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "eso2/bl2/ejercicio :: resultado";
    }
}
