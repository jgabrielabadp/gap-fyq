package com.gap.fyq.controller;

import com.gap.fyq.model.fourtheso.energy.FourthEsoEnergyExercise;
import com.gap.fyq.service.FourthEsoEnergyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/eso4/bl5")
@RequiredArgsConstructor
public class FourthEsoEnergyController {

    private final FourthEsoEnergyService service;

    @GetMapping("")
    public String page(Model model) {
        FourthEsoEnergyExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("4ESO BL5 página — type={} unknown={} id={}",
            ejercicio.getEnergyType(), ejercicio.getUnknownVariable(), ejercicio.getId());
        return "eso4/bl5/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        FourthEsoEnergyExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("4ESO BL5 ejercicio — type={} unknown={} id={}",
            ejercicio.getEnergyType(), ejercicio.getUnknownVariable(), ejercicio.getId());
        return "eso4/bl5/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        FourthEsoEnergyExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("4ESO BL5 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "eso4/bl5/ejercicio :: resultado";
    }
}
