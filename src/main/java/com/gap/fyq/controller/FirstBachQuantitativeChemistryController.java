package com.gap.fyq.controller;

import com.gap.fyq.model.firstbach.quantitativechemistry.FirstBachQuantitativeChemistryExercise;
import com.gap.fyq.service.FirstBachQuantitativeChemistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/1bach/bl2")
@RequiredArgsConstructor
public class FirstBachQuantitativeChemistryController {

    private final FirstBachQuantitativeChemistryService service;

    @GetMapping("")
    public String page(Model model) {
        FirstBachQuantitativeChemistryExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("1BACH BL2 página — type={} mode={} id={}",
            ejercicio.getChemistryType(), ejercicio.getExerciseMode(), ejercicio.getId());
        return "1bach/bl2/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        FirstBachQuantitativeChemistryExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("1BACH BL2 ejercicio — type={} mode={} id={}",
            ejercicio.getChemistryType(), ejercicio.getExerciseMode(), ejercicio.getId());
        return "1bach/bl2/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        FirstBachQuantitativeChemistryExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("1BACH BL2 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "1bach/bl2/ejercicio :: resultado";
    }
}
