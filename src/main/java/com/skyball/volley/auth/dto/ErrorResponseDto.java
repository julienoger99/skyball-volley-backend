package com.skyball.volley.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Schema(description = "Error response object")
public class ErrorResponseDto {

    @Schema(description = "Error message", example = "Error message")
    private String message;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error details or field errors", example = "field: constraint violated")
    private String details;
}
