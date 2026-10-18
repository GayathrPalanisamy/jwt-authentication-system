package com.onedao.authentication.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private static final Logger logger =
            LoggerFactory.getLogger(SettingsController.class);

    @GetMapping
    public Map<String, String> getSettings() {

        logger.info("Administrator settings endpoint accessed");

        return Map.of(
                "message",
                "Administrator settings access granted"
        );
    }
}
