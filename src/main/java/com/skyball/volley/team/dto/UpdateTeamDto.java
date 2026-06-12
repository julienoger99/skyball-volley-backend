package com.skyball.volley.team.dto;

import com.skyball.volley.team.domain.TeamCategory;
import com.skyball.volley.team.domain.TeamGender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Team update request — all fields are optional")
public class UpdateTeamDto {

    @Size(min = 1, message = "Team name cannot be empty")
    @Schema(description = "Team name", example = "Équipe A")
    private String name;

    @Schema(description = "Category", example = "SENIOR")
    private TeamCategory category;

    @Schema(description = "Gender", example = "MALE")
    private TeamGender gender;

    @Schema(description = "Logo URL")
    private String logoUrl;
}
