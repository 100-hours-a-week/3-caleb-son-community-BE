package com.ktb.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PrivacyController {
    
    @GetMapping("/privacy")
    public String privacyPolicy(Model model) {
        return "privacy";
    }
}
