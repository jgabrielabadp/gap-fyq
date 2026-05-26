package com.gap.fyq.controller;

import com.gap.fyq.model.energy.EnergyExercise;
import com.gap.fyq.service.EnergyExerciseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/eso2/bl5")
@RequiredArgsConstructor
public class EnergyController {

    private final EnergyExerciseService service;

    @GetMapping("")
    public String page(Model model) {
        EnergyExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("BL5 página — energyType={} modo={} id={}",
            ejercicio.getEnergyType(), ejercicio.getExerciseMode(), ejercicio.getId());
        return "eso2/bl5/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        EnergyExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("BL5 ejercicio — energyType={} modo={} id={}",
            ejercicio.getEnergyType(), ejercicio.getExerciseMode(), ejercicio.getId());
        return "eso2/bl5/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        EnergyExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("BL5 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "eso2/bl5/ejercicio :: resultado";
    }
}
