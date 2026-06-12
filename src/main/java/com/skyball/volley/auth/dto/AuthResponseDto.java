package com.skyball.volley.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Authentication response containing token and user information")
public class AuthResponseDto {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType;

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "Username", example = "john_doe")
    private String username;

    @Schema(description = "Email", example = "john@example.com")
    private String email;

    @Schema(description = "User role", example = "PLAYER")
    private String role;

    @Schema(description = "Account creation timestamp", example = "2026-03-24T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Success message", example = "Login successful")
    private String message;
}
