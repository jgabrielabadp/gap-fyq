package com.gap.fyq.controller;

import com.gap.fyq.model.firstbach.chemicalreactions.FirstBachChemicalReactionsExercise;
import com.gap.fyq.service.FirstBachChemicalReactionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/1bach/bl3")
@RequiredArgsConstructor
public class FirstBachChemicalReactionsController {

    private final FirstBachChemicalReactionsService service;

    @GetMapping("")
    public String page(Model model) {
        FirstBachChemicalReactionsExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("1BACH BL3 página — type={} id={}",
            ejercicio.getReactionsType(), ejercicio.getId());
        return "1bach/bl3/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        FirstBachChemicalReactionsExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("1BACH BL3 ejercicio — type={} id={}",
            ejercicio.getReactionsType(), ejercicio.getId());
        return "1bach/bl3/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        FirstBachChemicalReactionsExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("1BACH BL3 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "1bach/bl3/ejercicio :: resultado";
    }
}
