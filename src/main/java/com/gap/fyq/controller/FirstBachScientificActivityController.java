package com.gap.fyq.controller;

import com.gap.fyq.model.firstbach.scientificactivity.FirstBachScientificActivityExercise;
import com.gap.fyq.service.FirstBachScientificActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/1bach/bl1")
@RequiredArgsConstructor
public class FirstBachScientificActivityController {

    private final FirstBachScientificActivityService service;

    @GetMapping("")
    public String page(Model model) {
        FirstBachScientificActivityExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("1BACH BL1 página — type={} mode={} id={}",
            ejercicio.getActivityType(), ejercicio.getExerciseMode(), ejercicio.getId());
        return "1bach/bl1/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        FirstBachScientificActivityExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("1BACH BL1 ejercicio — type={} mode={} id={}",
            ejercicio.getActivityType(), ejercicio.getExerciseMode(), ejercicio.getId());
        return "1bach/bl1/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        FirstBachScientificActivityExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("1BACH BL1 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "1bach/bl1/ejercicio :: resultado";
    }
}
