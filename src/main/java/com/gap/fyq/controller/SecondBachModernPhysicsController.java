package com.gap.fyq.controller;

import com.gap.fyq.model.secondbach.modernphysics.SecondBachModernPhysicsExercise;
import com.gap.fyq.service.SecondBachModernPhysicsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/2bach/bl5")
@RequiredArgsConstructor
public class SecondBachModernPhysicsController {

    private final SecondBachModernPhysicsService service;

    @GetMapping("")
    public String page(Model model) {
        SecondBachModernPhysicsExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        return "2bach/bl5/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        SecondBachModernPhysicsExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        return "2bach/bl5/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        SecondBachModernPhysicsExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "2bach/bl5/ejercicio :: resultado";
    }
}
