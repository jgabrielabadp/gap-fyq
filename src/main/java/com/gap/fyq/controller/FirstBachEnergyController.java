package com.gap.fyq.controller;

import com.gap.fyq.model.firstbach.energy.FirstBachEnergyExercise;
import com.gap.fyq.service.FirstBachEnergyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/1bach/bl8")
@RequiredArgsConstructor
public class FirstBachEnergyController {

    private final FirstBachEnergyService service;

    @GetMapping("")
    public String page(Model model) {
        FirstBachEnergyExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("1BACH BL8 página — type={} var={} id={}",
            ejercicio.getEnergyType(), ejercicio.getUnknownVariable(), ejercicio.getId());
        return "1bach/bl8/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        FirstBachEnergyExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("1BACH BL8 ejercicio — type={} var={} id={}",
            ejercicio.getEnergyType(), ejercicio.getUnknownVariable(), ejercicio.getId());
        return "1bach/bl8/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        FirstBachEnergyExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("1BACH BL8 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "1bach/bl8/ejercicio :: resultado";
    }
}
