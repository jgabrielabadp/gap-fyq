package com.gap.fyq.controller;

import com.gap.fyq.model.secondbach.transfer.SecondBachTransferExercise;
import com.gap.fyq.service.SecondBachTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/2bach-q/bl4")
@RequiredArgsConstructor
public class SecondBachTransferController {

    private final SecondBachTransferService service;

    @GetMapping
    public String page() {
        return "2bach-q/bl4/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        SecondBachTransferExercise ex = service.generateAndSave();
        model.addAttribute("ex", ex);
        return "2bach-q/bl4/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        SecondBachTransferExercise ex = service.findById(id);
        boolean correcto = ex.validateAnswer(respuesta);
        model.addAttribute("ex", ex);
        model.addAttribute("correcto", correcto);
        model.addAttribute("respuestaAlumno", respuesta.trim());
        return "2bach-q/bl4/ejercicio :: resultado";
    }
}
