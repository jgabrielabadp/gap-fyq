package com.gap.fyq.controller;

import com.gap.fyq.model.secondbach.chemicalequilibrium.SecondBachChemicalEquilibriumExercise;
import com.gap.fyq.service.SecondBachChemicalEquilibriumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/2bach-q/bl3")
@RequiredArgsConstructor
public class SecondBachChemicalEquilibriumController {

    private final SecondBachChemicalEquilibriumService service;

    @GetMapping("")
    public String page(Model model) {
        SecondBachChemicalEquilibriumExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        return "2bach-q/bl3/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        SecondBachChemicalEquilibriumExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        return "2bach-q/bl3/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        SecondBachChemicalEquilibriumExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "2bach-q/bl3/ejercicio :: resultado";
    }
}
