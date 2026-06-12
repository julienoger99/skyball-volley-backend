package com.skyball.volley.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "User login request")
public class LoginDto {

    @NotEmpty(message = "Username is required")
    @Schema(description = "Username", example = "john_doe")
    private String username;

    @NotEmpty(message = "Password is required")
    @Schema(description = "Password", example = "securePassword123")
    private String password;
}
