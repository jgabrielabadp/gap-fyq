package com.gap.fyq.controller;

import com.gap.fyq.model.fourtheso.scientificactivity.FourthEsoScientificActivityExercise;
import com.gap.fyq.service.FourthEsoScientificActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/eso4/bl1")
@RequiredArgsConstructor
public class FourthEsoScientificActivityController {

    private final FourthEsoScientificActivityService service;

    @GetMapping("")
    public String page(Model model) {
        FourthEsoScientificActivityExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("4ESO BL1 página — type={} mode={} id={}",
            ejercicio.getActivityType(), ejercicio.getExerciseMode(), ejercicio.getId());
        return "eso4/bl1/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        FourthEsoScientificActivityExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("4ESO BL1 ejercicio — type={} mode={} id={}",
            ejercicio.getActivityType(), ejercicio.getExerciseMode(), ejercicio.getId());
        return "eso4/bl1/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        FourthEsoScientificActivityExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("4ESO BL1 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "eso4/bl1/ejercicio :: resultado";
    }
}
