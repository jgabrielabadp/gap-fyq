package com.gap.fyq.controller;

import com.gap.fyq.model.thirdeso.chemicalchanges.ThirdEsoChemicalChangesExercise;
import com.gap.fyq.service.ThirdEsoChemicalChangesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/eso3/bl3")
@RequiredArgsConstructor
public class ThirdEsoChemicalChangesController {

    private final ThirdEsoChemicalChangesService service;

    @GetMapping("")
    public String page(Model model) {
        ThirdEsoChemicalChangesExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("3ESO BL3 página — tipo={} id={}", ejercicio.getChangeType(), ejercicio.getId());
        return "eso3/bl3/page";
    }

    @GetMapping("/ejercicio")
    public String ejercicio(Model model) {
        ThirdEsoChemicalChangesExercise ejercicio = service.generateAndSave();
        model.addAttribute("ejercicio", ejercicio);
        log.debug("3ESO BL3 ejercicio — tipo={} id={}", ejercicio.getChangeType(), ejercicio.getId());
        return "eso3/bl3/ejercicio :: tarjeta";
    }

    @PostMapping("/ejercicio/{id}/comprobar")
    public String comprobar(@PathVariable Long id,
                            @RequestParam String respuesta,
                            Model model) {
        ThirdEsoChemicalChangesExercise ejercicio = service.findById(id);
        boolean correcto = ejercicio.validateAnswer(respuesta);
        log.debug("3ESO BL3 id={} respuesta='{}' correcto={}", id, respuesta, correcto);
        model.addAttribute("correcto", correcto);
        model.addAttribute("ejercicio", ejercicio);
        return "eso3/bl3/ejercicio :: resultado";
    }
}
