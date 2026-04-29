package com.apidoc.controller;

import com.apidoc.generator.DocumentGenerator;
import com.apidoc.model.ApiDocumentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doc")
public class ApiDocController {
    
    private final DocumentGenerator documentGenerator;
    
    public ApiDocController(DocumentGenerator documentGenerator) {
        this.documentGenerator = documentGenerator;
    }
    
    @GetMapping("/generate")
    public ResponseEntity<ApiDocumentation> generateDocs() {
        ApiDocumentation documentation = documentGenerator.generateDocumentation();
        return ResponseEntity.ok(documentation);
    }
    
    @GetMapping("/view")
    public ResponseEntity<ApiDocumentation> viewDocs() {
        ApiDocumentation documentation = documentGenerator.generateDocumentation();
        return ResponseEntity.ok(documentation);
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshDocs() {
        documentGenerator.generateDocumentation();
        return ResponseEntity.ok("文档已刷新");
    }
}
