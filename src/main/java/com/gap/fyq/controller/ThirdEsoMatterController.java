package com.gap.fyq.controller;

import com.gap.fyq.model.thirdeso.matter.ThirdEsoMatterExercise;
import com.gap.fyq.service.ThirdEsoMatterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/eso3/bl2")
@RequiredArgsConstructor
public class ThirdEsoMatterController {

    private final ThirdEsoMatterService service;

    @GetMapping("")
    public String page(Model model) {
        ThirdEsoMatterExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("3ESO BL2 página — tipo={} incógnita={} id={}",
            ejercicio.getMatterType(), ejercicio.getUnknownVariable(), ejercicio.getId());
        return "eso3/bl2/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        ThirdEsoMatterExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("3ESO BL2 ejercicio — tipo={} incógnita={} id={}",
            ejercicio.getMatterType(), ejercicio.getUnknownVariable(), ejercicio.getId());
        return "eso3/bl2/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        ThirdEsoMatterExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("3ESO BL2 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "eso3/bl2/ejercicio :: resultado";
    }
}
