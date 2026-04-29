package com.apidoc.controller;

import com.apidoc.generator.DocumentGenerator;
import com.apidoc.model.ApiDocumentation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    
    private final DocumentGenerator documentGenerator;
    
    public WebController(DocumentGenerator documentGenerator) {
        this.documentGenerator = documentGenerator;
    }
    
    @GetMapping("/")
    public String index(Model model) {
        ApiDocumentation documentation = documentGenerator.generateDocumentation();
        model.addAttribute("doc", documentation);
        return "index";
    }
}
