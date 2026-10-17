package com.onedao.authentication.controller;

import com.onedao.authentication.entity.AppUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @GetMapping
    public Map<String, String> dashboard(
            Authentication authentication) {

        AppUser user = (AppUser) authentication.getPrincipal();

        return Map.of(
                "message", "Welcome " + user.getUsername()
        );
    }
}
