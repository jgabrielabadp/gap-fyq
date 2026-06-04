package com.gap.fyq.controller;

import com.gap.fyq.model.secondbach.waves.SecondBachWavesExercise;
import com.gap.fyq.service.SecondBachWavesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/2bach/bl3")
@RequiredArgsConstructor
public class SecondBachWavesController {

    private final SecondBachWavesService service;

    @GetMapping("")
    public String page(Model model) {
        SecondBachWavesExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        return "2bach/bl3/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        SecondBachWavesExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        return "2bach/bl3/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        SecondBachWavesExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "2bach/bl3/ejercicio :: resultado";
    }
}
