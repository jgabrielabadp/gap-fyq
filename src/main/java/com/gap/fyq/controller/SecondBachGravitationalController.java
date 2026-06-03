package com.gap.fyq.controller;

import com.gap.fyq.model.secondbach.gravitational.SecondBachGravitationalExercise;
import com.gap.fyq.service.SecondBachGravitationalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/2bach/bl1")
@RequiredArgsConstructor
public class SecondBachGravitationalController {

    private final SecondBachGravitationalService service;

    @GetMapping("")
    public String page(Model model) {
        SecondBachGravitationalExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        return "2bach/bl1/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        SecondBachGravitationalExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        return "2bach/bl1/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        SecondBachGravitationalExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "2bach/bl1/ejercicio :: resultado";
    }
}
