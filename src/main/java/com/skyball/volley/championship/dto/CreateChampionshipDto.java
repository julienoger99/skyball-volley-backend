package com.skyball.volley.championship.dto;

import com.skyball.volley.team.domain.TeamCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Championship creation request")
public class CreateChampionshipDto {

    @NotBlank(message = "Name is required")
    @Schema(description = "Championship name", example = "Championnat Régional N2")
    private String name;

    @NotBlank(message = "Season is required")
    @Pattern(regexp = "\\d{4}-\\d{4}", message = "Season must follow the format YYYY-YYYY")
    @Schema(description = "Season", example = "2025-2026")
    private String season;

    @Schema(description = "Category (omit for open competitions)", example = "SENIOR")
    private TeamCategory category;
}
