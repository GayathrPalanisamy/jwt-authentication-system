
package com.onedao.authentication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    @GetMapping
    public Map<String, String> getSettings() {
        return Map.of(
                "message",
                "Administrator settings access granted"
        );
    }
}

