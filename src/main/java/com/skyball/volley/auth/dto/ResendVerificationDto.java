package com.skyball.volley.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to resend verification email")
public class ResendVerificationDto {

    @NotBlank
    @Email
    @Schema(description = "Email address to resend verification to", example = "user@example.com")
    private String email;
}
