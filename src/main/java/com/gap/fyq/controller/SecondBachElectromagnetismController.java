package com.gap.fyq.controller;

import com.gap.fyq.model.secondbach.electromagnetism.SecondBachElectromagnetismExercise;
import com.gap.fyq.service.SecondBachElectromagnetismService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/2bach/bl2")
@RequiredArgsConstructor
public class SecondBachElectromagnetismController {

    private final SecondBachElectromagnetismService service;

    @GetMapping("")
    public String page(Model model) {
        SecondBachElectromagnetismExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        return "2bach/bl2/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        SecondBachElectromagnetismExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        return "2bach/bl2/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        SecondBachElectromagnetismExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "2bach/bl2/ejercicio :: resultado";
    }
}
