package com.gap.fyq.controller;

import com.gap.fyq.model.motionforces.ForcesAndMotionExercise;
import com.gap.fyq.service.ForcesAndMotionExerciseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/eso2/bl4")
@RequiredArgsConstructor
public class ForcesAndMotionController {

    private final ForcesAndMotionExerciseService service;

    @GetMapping("")
    public String page(Model model) {
        ForcesAndMotionExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("BL4 página — subTopic={} modo={} id={}",
            ejercicio.getSubTopic(), ejercicio.getExerciseMode(), ejercicio.getId());
        return "eso2/bl4/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        ForcesAndMotionExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("BL4 ejercicio — subTopic={} modo={} id={}",
            ejercicio.getSubTopic(), ejercicio.getExerciseMode(), ejercicio.getId());
        return "eso2/bl4/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        ForcesAndMotionExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("BL4 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "eso2/bl4/ejercicio :: resultado";
    }
}
