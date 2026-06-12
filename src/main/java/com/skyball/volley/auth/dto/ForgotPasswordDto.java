package com.skyball.volley.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Forgot password request")
public class ForgotPasswordDto {

    @NotBlank
    @Email
    @Schema(description = "Email address of the account", example = "user@example.com")
    private String email;
}
