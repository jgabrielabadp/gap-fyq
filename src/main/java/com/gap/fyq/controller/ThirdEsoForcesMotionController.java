package com.gap.fyq.controller;

import com.gap.fyq.model.thirdeso.forcesmotion.ThirdEsoForcesMotionExercise;
import com.gap.fyq.service.ThirdEsoForcesMotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/eso3/bl4")
@RequiredArgsConstructor
public class ThirdEsoForcesMotionController {

    private final ThirdEsoForcesMotionService service;

    @GetMapping("")
    public String page(Model model) {
        ThirdEsoForcesMotionExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("3ESO BL4 página — tipo={} incógnita={} id={}",
            ejercicio.getDynamicsType(), ejercicio.getUnknownVariable(), ejercicio.getId());
        return "eso3/bl4/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        ThirdEsoForcesMotionExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("3ESO BL4 ejercicio — tipo={} incógnita={} id={}",
            ejercicio.getDynamicsType(), ejercicio.getUnknownVariable(), ejercicio.getId());
        return "eso3/bl4/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        ThirdEsoForcesMotionExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("3ESO BL4 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "eso3/bl4/ejercicio :: resultado";
    }
}
