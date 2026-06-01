package com.gap.fyq.controller;

import com.gap.fyq.model.firstbach.kinematics.FirstBachKinematicsExercise;
import com.gap.fyq.service.FirstBachKinematicsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/1bach/bl6")
@RequiredArgsConstructor
public class FirstBachKinematicsController {

    private final FirstBachKinematicsService service;

    @GetMapping("")
    public String page(Model model) {
        FirstBachKinematicsExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("1BACH BL6 página — type={} var={} id={}",
            ejercicio.getKinematicsType(), ejercicio.getUnknownVariable(), ejercicio.getId());
        return "1bach/bl6/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        FirstBachKinematicsExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("1BACH BL6 ejercicio — type={} var={} id={}",
            ejercicio.getKinematicsType(), ejercicio.getUnknownVariable(), ejercicio.getId());
        return "1bach/bl6/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        FirstBachKinematicsExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("1BACH BL6 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "1bach/bl6/ejercicio :: resultado";
    }
}
