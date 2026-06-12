package com.skyball.volley.team.dto;

import com.skyball.volley.team.domain.TeamCategory;
import com.skyball.volley.team.domain.TeamGender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Team creation request")
public class CreateTeamDto {

    @NotEmpty(message = "Team name is required")
    @Schema(description = "Team name", example = "Équipe A")
    private String name;

    @NotNull(message = "Category is required")
    @Schema(description = "Category", example = "SENIOR")
    private TeamCategory category;

    @NotNull(message = "Gender is required")
    @Schema(description = "Gender", example = "MALE")
    private TeamGender gender;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "Club ID — omit to create an independent team", example = "1")
    private Long clubId;
}
