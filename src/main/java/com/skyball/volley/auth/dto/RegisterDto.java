package com.skyball.volley.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * User registration request DTO
 */
@Getter
@Setter
@Schema(description = "User registration request object")
public class RegisterDto {

    @NotEmpty(message = "Username is required")
    @Schema(description = "Username for the new account", example = "john_doe")
    private String username;

    @NotEmpty(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Email address for the new account", example = "john@example.com")
    private String email;

    @NotEmpty(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Schema(description = "Password for the new account (minimum 6 characters)", example = "securePassword123")
    private String password;

}
