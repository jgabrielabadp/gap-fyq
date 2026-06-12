package com.gap.fyq.controller;

import com.gap.fyq.model.secondbach.organic.SecondBachOrganicExercise;
import com.gap.fyq.service.SecondBachOrganicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/2bach-q/bl5")
@RequiredArgsConstructor
public class SecondBachOrganicController {

    private final SecondBachOrganicService service;

    @GetMapping
    public String page(Model model) {
        SecondBachOrganicExercise ex = service.generateAndSave();
        model.addAttribute("ex", ex);
        return "2bach-q/bl5/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        SecondBachOrganicExercise ex = service.generateAndSave();
        model.addAttribute("ex", ex);
        return "2bach-q/bl5/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        SecondBachOrganicExercise ex = service.findById(id);
        boolean correcto = ex.validateAnswer(respuesta);
        model.addAttribute("ex", ex);
        model.addAttribute("correcto", correcto);
        model.addAttribute("respuestaAlumno", respuesta.trim());
        return "2bach-q/bl5/ejercicio :: resultado";
    }
}
