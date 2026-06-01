package com.gap.fyq.controller;

import com.gap.fyq.model.firstbach.organicchemistry.FirstBachOrganicChemistryExercise;
import com.gap.fyq.service.FirstBachOrganicChemistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/1bach/bl5")
@RequiredArgsConstructor
public class FirstBachOrganicChemistryController {

    private final FirstBachOrganicChemistryService service;

    @GetMapping("")
    public String page(Model model) {
        FirstBachOrganicChemistryExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("1BACH BL5 página — type={} id={}",
            ejercicio.getOrganicChemistryType(), ejercicio.getId());
        return "1bach/bl5/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        FirstBachOrganicChemistryExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("1BACH BL5 ejercicio — type={} id={}",
            ejercicio.getOrganicChemistryType(), ejercicio.getId());
        return "1bach/bl5/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        FirstBachOrganicChemistryExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("1BACH BL5 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "1bach/bl5/ejercicio :: resultado";
    }
}
