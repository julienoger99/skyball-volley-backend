package com.skyball.volley.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Registration response")
public class RegisterResponseDto {

    @Schema(description = "Informational message", example = "Registration successful. Please check your email to verify your account.")
    private String message;
}
