package com.skyball.volley.match.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Set score submission")
public class CreateMatchSetDto {

    @NotNull
    @Min(value = 1, message = "Set number must be between 1 and 5")
    @Max(value = 5, message = "Set number must be between 1 and 5")
    @Schema(description = "Set number", example = "1")
    private Integer setNumber;

    @NotNull
    @Min(value = 0, message = "Points must be non-negative")
    @Schema(description = "Team points", example = "25")
    private Integer teamPoints;

    @NotNull
    @Min(value = 0, message = "Points must be non-negative")
    @Schema(description = "Opponent points", example = "18")
    private Integer opponentPoints;
}
