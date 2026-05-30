package com.gap.fyq.controller;

import com.gap.fyq.model.fourtheso.matter.FourthEsoMatterExercise;
import com.gap.fyq.service.FourthEsoMatterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/eso4/bl2")
@RequiredArgsConstructor
public class FourthEsoMatterController {

    private final FourthEsoMatterService service;

    @GetMapping("")
    public String page(Model model) {
        FourthEsoMatterExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("4ESO BL2 página — type={} mode={} id={}",
            ejercicio.getMatterType(), ejercicio.getExerciseMode(), ejercicio.getId());
        return "eso4/bl2/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        FourthEsoMatterExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("4ESO BL2 ejercicio — type={} mode={} id={}",
            ejercicio.getMatterType(), ejercicio.getExerciseMode(), ejercicio.getId());
        return "eso4/bl2/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        FourthEsoMatterExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("4ESO BL2 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "eso4/bl2/ejercicio :: resultado";
    }
}
