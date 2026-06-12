package com.skyball.volley.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "User update request — all fields are optional")
public class UpdateUserDto {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "New username", example = "john_doe")
    private String username;

    @Email(message = "Email should be valid")
    @Schema(description = "New email address", example = "john@example.com")
    private String email;

}