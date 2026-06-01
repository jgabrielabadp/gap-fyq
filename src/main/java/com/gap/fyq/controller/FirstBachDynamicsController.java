package com.gap.fyq.controller;

import com.gap.fyq.model.firstbach.dynamics.FirstBachDynamicsExercise;
import com.gap.fyq.service.FirstBachDynamicsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/1bach/bl7")
@RequiredArgsConstructor
public class FirstBachDynamicsController {

    private final FirstBachDynamicsService service;

    @GetMapping("")
    public String page(Model model) {
        FirstBachDynamicsExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("1BACH BL7 página — type={} var={} id={}",
            ejercicio.getDynamicsType(), ejercicio.getUnknownVariable(), ejercicio.getId());
        return "1bach/bl7/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        FirstBachDynamicsExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("1BACH BL7 ejercicio — type={} var={} id={}",
            ejercicio.getDynamicsType(), ejercicio.getUnknownVariable(), ejercicio.getId());
        return "1bach/bl7/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        FirstBachDynamicsExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("1BACH BL7 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "1bach/bl7/ejercicio :: resultado";
    }
}
