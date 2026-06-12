package com.skyball.volley.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Reset password request")
public class ResetPasswordDto {

    @NotBlank
    @Schema(description = "Token received by email")
    private String token;

    @NotBlank
    @Size(min = 8)
    @Schema(description = "New password (minimum 8 characters)", example = "newSecurePassword123")
    private String newPassword;
}
