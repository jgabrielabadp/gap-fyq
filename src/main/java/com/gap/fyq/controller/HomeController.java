package com.gap.fyq.controller;

import com.gap.fyq.service.ScientificActivityExerciseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ScientificActivityExerciseService service;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("ejercicio", service.generateAndSave());
        log.debug("Página de inicio — ejercicio generado");
        return "index";
    }
}
