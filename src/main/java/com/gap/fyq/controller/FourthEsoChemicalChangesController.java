package com.gap.fyq.controller;

import com.gap.fyq.model.fourtheso.chemicalchanges.FourthEsoChemicalChangesExercise;
import com.gap.fyq.service.FourthEsoChemicalChangesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/eso4/bl3")
@RequiredArgsConstructor
public class FourthEsoChemicalChangesController {

    private final FourthEsoChemicalChangesService service;

    @GetMapping("")
    public String page(Model model) {
        FourthEsoChemicalChangesExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("4ESO BL3 página — type={} unknown={} id={}",
            ejercicio.getChangesType(), ejercicio.getUnknownVariable(), ejercicio.getId());
        return "eso4/bl3/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        FourthEsoChemicalChangesExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("4ESO BL3 ejercicio — type={} unknown={} id={}",
            ejercicio.getChangesType(), ejercicio.getUnknownVariable(), ejercicio.getId());
        return "eso4/bl3/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        FourthEsoChemicalChangesExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("4ESO BL3 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "eso4/bl3/ejercicio :: resultado";
    }
}
