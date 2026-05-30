package com.gap.fyq.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/eso2")
    public String eso2() {
        return "eso2/index";
    }

    @GetMapping("/eso3")
    public String eso3() {
        return "eso3/index";
    }

    @GetMapping("/eso4")
    public String eso4() {
        return "eso4/index";
    }

    @GetMapping("/1bach")
    public String firstBach() {
        return "1bach/index";
    }
}
