package com.example.roombooking.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class Controller {
    
    @GetMapping("/myrole")
    public String getMyRole(Authentication authentication) {
        return "Your roles: " + authentication.getAuthorities().toString();
    }
}