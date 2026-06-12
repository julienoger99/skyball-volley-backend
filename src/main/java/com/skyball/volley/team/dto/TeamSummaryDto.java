package com.skyball.volley.team.dto;

import com.skyball.volley.team.domain.Team;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Lightweight team summary")
public class TeamSummaryDto {

    @Schema(description = "Team ID", example = "1")
    private Long id;

    @Schema(description = "Team name", example = "Équipe A")
    private String name;

    @Schema(description = "Category", example = "SENIOR")
    private String category;

    @Schema(description = "Gender", example = "MALE")
    private String gender;

    public static TeamSummaryDto from(Team team) {
        return TeamSummaryDto.builder()
                .id(team.getId())
                .name(team.getName())
                .category(team.getCategory().name())
                .gender(team.getGender().name())
                .build();
    }
}
