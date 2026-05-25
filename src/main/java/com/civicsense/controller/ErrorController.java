package com.civicsense.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    // Handles access-denied errors (403)
    @GetMapping("/403")
    public String accessDenied() {
        return "403"; // Returns the 403.html template
    }
}
