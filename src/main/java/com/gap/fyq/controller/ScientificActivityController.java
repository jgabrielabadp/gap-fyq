package com.gap.fyq.controller;

import com.gap.fyq.model.scientificactivity.ScientificActivityExercise;
import com.gap.fyq.service.ScientificActivityExerciseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/eso2/bl1")
@RequiredArgsConstructor
public class ScientificActivityController {

    private final ScientificActivityExerciseService service;

    // HTMX GET — carga un ejercicio nuevo y devuelve solo el fragmento tarjeta
    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        ScientificActivityExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("Ejercicio generado: id={} tipo={}", ejercicio.getId(), ejercicio.getExerciseType());
        return "eso2/bl1/ejercicio :: tarjeta";
    }

    // HTMX POST — valida la respuesta y devuelve solo el fragmento resultado
    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        ScientificActivityExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("Ejercicio id={} | respuesta='{}' | correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "eso2/bl1/ejercicio :: resultado";
    }
}
