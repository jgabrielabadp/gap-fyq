package com.gap.fyq.controller;

import com.gap.fyq.model.secondbach.structurelink.SecondBachStructureLinkExercise;
import com.gap.fyq.service.SecondBachStructureLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/2bach-q/bl1")
@RequiredArgsConstructor
public class SecondBachStructureLinkController {

    private final SecondBachStructureLinkService service;

    @GetMapping("")
    public String page(Model model) {
        SecondBachStructureLinkExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        return "2bach-q/bl1/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        SecondBachStructureLinkExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        return "2bach-q/bl1/ejercicio :: tarjeta";
    }

    /** Validación para CONFIG_TEXT, QUANTUM_MCQ y PERIODIC_MCQ (un único campo). */
    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        SecondBachStructureLinkExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "2bach-q/bl1/ejercicio :: resultado";
    }

    /** Validación para GEOMETRY_MULTI (cuatro campos independientes). */
    @PostMapping("/ejercicio/{id}/comprobar-geometria")
    public String comprobarGeometria(@PathVariable Long id,
                                     @RequestParam String hibridacion,
                                     @RequestParam String paresEnlazantes,
                                     @RequestParam String geometria,
                                     @RequestParam String polaridad,
                                     Model model) {
        SecondBachStructureLinkExercise ejercicio = service.findById(id);
        String composite = hibridacion + "|" + paresEnlazantes + "|" + geometria + "|" + polaridad;
        boolean correcto = ejercicio.validateAnswer(composite);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "2bach-q/bl1/ejercicio :: resultado";
    }
}
