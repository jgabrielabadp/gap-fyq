package com.gap.fyq.controller;

import com.gap.fyq.model.secondbach.kineticsthermal.SecondBachKineticsThermalExercise;
import com.gap.fyq.service.SecondBachKineticsThermalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/2bach-q/bl2")
@RequiredArgsConstructor
public class SecondBachKineticsThermalController {

    private final SecondBachKineticsThermalService service;

    @GetMapping("")
    public String page(Model model) {
        SecondBachKineticsThermalExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        return "2bach-q/bl2/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        SecondBachKineticsThermalExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        return "2bach-q/bl2/ejercicio :: tarjeta";
    }

    /** Validación para modos numéricos y de órdenes (un único campo de texto). */
    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        SecondBachKineticsThermalExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "2bach-q/bl2/ejercicio :: resultado";
    }
}
