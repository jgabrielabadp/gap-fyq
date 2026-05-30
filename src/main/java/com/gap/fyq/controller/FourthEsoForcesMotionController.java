package com.gap.fyq.controller;

import com.gap.fyq.model.fourtheso.forcesmotion.FourthEsoForcesMotionExercise;
import com.gap.fyq.service.FourthEsoForcesMotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/eso4/bl4")
@RequiredArgsConstructor
public class FourthEsoForcesMotionController {

    private final FourthEsoForcesMotionService service;

    @GetMapping("")
    public String page(Model model) {
        FourthEsoForcesMotionExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("4ESO BL4 página — type={} unknown={} id={}",
            ejercicio.getDynamicsType(), ejercicio.getUnknownVariable(), ejercicio.getId());
        return "eso4/bl4/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        FourthEsoForcesMotionExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("4ESO BL4 ejercicio — type={} unknown={} id={}",
            ejercicio.getDynamicsType(), ejercicio.getUnknownVariable(), ejercicio.getId());
        return "eso4/bl4/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        FourthEsoForcesMotionExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("4ESO BL4 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "eso4/bl4/ejercicio :: resultado";
    }
}
