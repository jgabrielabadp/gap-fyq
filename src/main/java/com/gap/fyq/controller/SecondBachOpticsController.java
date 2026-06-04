package com.gap.fyq.controller;

import com.gap.fyq.model.secondbach.optics.SecondBachOpticsExercise;
import com.gap.fyq.service.SecondBachOpticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/2bach/bl4")
@RequiredArgsConstructor
public class SecondBachOpticsController {

    private final SecondBachOpticsService service;

    @GetMapping("")
    public String page(Model model) {
        SecondBachOpticsExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        return "2bach/bl4/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        SecondBachOpticsExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        return "2bach/bl4/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        SecondBachOpticsExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "2bach/bl4/ejercicio :: resultado";
    }
}
