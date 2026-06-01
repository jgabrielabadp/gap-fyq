package com.gap.fyq.controller;

import com.gap.fyq.model.firstbach.thermochemistry.FirstBachThermochemistryExercise;
import com.gap.fyq.service.FirstBachThermochemistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/1bach/bl4")
@RequiredArgsConstructor
public class FirstBachThermochemistryController {

    private final FirstBachThermochemistryService service;

    @GetMapping("")
    public String page(Model model) {
        FirstBachThermochemistryExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("1BACH BL4 página — type={} mode={} id={}",
            ejercicio.getThermochemistryType(), ejercicio.getExerciseMode(), ejercicio.getId());
        return "1bach/bl4/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        FirstBachThermochemistryExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("1BACH BL4 ejercicio — type={} mode={} id={}",
            ejercicio.getThermochemistryType(), ejercicio.getExerciseMode(), ejercicio.getId());
        return "1bach/bl4/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(
            @PathVariable Long id,
            @RequestParam String respuesta,
            @RequestParam(required = false, defaultValue = "-1") String espontaneidad,
            Model model) {

        FirstBachThermochemistryExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);

        if ("GIBBS_COMBINED".equals(ejercicio.getExerciseMode())) {
            boolean correctoEsp = ejercicio.validateSpontaneity(espontaneidad);
            model.addAttribute("correctoGibbs", correcto);
            model.addAttribute("correctoEspontaneidad", correctoEsp);
            correcto = correcto && correctoEsp;
        }

        log.debug("1BACH BL4 id={} respuesta='{}' espontaneidad='{}' correcto={}",
            id, respuesta, espontaneidad, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "1bach/bl4/ejercicio :: resultado";
    }
}
