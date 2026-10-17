package com.onedao.authentication.dto;

import com.onedao.authentication.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 8, max = 50,
                message = "Username must be 8 to 50 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Pattern(
                regexp = "^[^\\s@]+_[^\\s@]+@[^\\s@]+\\.[^\\s@]+$",
                message = "Email must contain @ and _"
        )
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 12,
                message = "Password must be 8 to 12 characters")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{8,12}$",
                message = "Password needs uppercase, lowercase, number and special character"
        )
        String password,

        @NotNull(message = "User type is required")
        Role role

) {}
