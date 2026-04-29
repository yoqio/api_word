package com.example.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @GetMapping("/{id}")
    public String getUser(@PathVariable Long id) {
        return "User: " + id;
    }
    
    @PostMapping
    public String createUser(@RequestBody UserRequest request) {
        return "Created: " + request.getName();
    }
    
    @GetMapping("/list")
    public String listUsers(@RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "10") int size) {
        return "Page: " + page + ", Size: " + size;
    }
    
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        return "Deleted: " + id;
    }
    
    static class UserRequest {
        private String name;
        private String email;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
}

