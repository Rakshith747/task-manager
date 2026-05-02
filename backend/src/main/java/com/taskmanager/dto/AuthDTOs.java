package com.taskmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDTOs {

    @Data
    public static class SignupRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @Email(message = "Enter valid email")
        @NotBlank(message = "Email is required")
        private String email;

        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        // ADMIN or MEMBER
        private String role;
    }

    @Data
    public static class LoginRequest {
        @NotBlank
        private String email;

        @NotBlank
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String name;
        private String email;
        private String role;
        private Long userId;

        public AuthResponse(String token, String name, String email, String role, Long userId) {
            this.token = token;
            this.name = name;
            this.email = email;
            this.role = role;
            this.userId = userId;
        }
    }
}
