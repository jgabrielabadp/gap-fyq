package com.gap.fyq.controller;

import com.gap.fyq.model.thirdeso.scientificactivity.ThirdEsoScientificActivityExercise;
import com.gap.fyq.service.ThirdEsoScientificActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/eso3/bl1")
@RequiredArgsConstructor
public class ThirdEsoScientificActivityController {

    private final ThirdEsoScientificActivityService service;

    @GetMapping("")
    public String page(Model model) {
        ThirdEsoScientificActivityExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("3ESO BL1 página — activityType={} modo={} id={}",
            ejercicio.getActivityType(), ejercicio.getExerciseMode(), ejercicio.getId());
        return "eso3/bl1/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        ThirdEsoScientificActivityExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("3ESO BL1 ejercicio — activityType={} modo={} id={}",
            ejercicio.getActivityType(), ejercicio.getExerciseMode(), ejercicio.getId());
        return "eso3/bl1/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        ThirdEsoScientificActivityExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("3ESO BL1 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "eso3/bl1/ejercicio :: resultado";
    }
}
