package com.skyball.volley.team.dto;

import com.skyball.volley.team.domain.Team;
import com.skyball.volley.user.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "Team information response")
public class TeamResponseDto {

    @Schema(description = "Team ID", example = "1")
    private Long id;

    @Schema(description = "Team name", example = "Équipe A")
    private String name;

    @Schema(description = "Category", example = "SENIOR")
    private String category;

    @Schema(description = "Gender", example = "MALE")
    private String gender;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "Club ID", example = "1")
    private Long clubId;

    @Schema(description = "Club name", example = "Skyball Volley Club")
    private String clubName;

    @Schema(description = "Team members")
    private List<UserResponseDto> members;

    public static TeamResponseDto from(Team team) {
        return TeamResponseDto.builder()
                .id(team.getId())
                .name(team.getName())
                .category(team.getCategory().name())
                .gender(team.getGender().name())
                .logoUrl(team.getLogoUrl())
                .clubId(team.getClub() != null ? team.getClub().getId() : null)
                .clubName(team.getClub() != null ? team.getClub().getName() : null)
                .members(team.getTeamMemberships().stream()
                        .map(tm -> UserResponseDto.fromBasic(tm.getUser()))
                        .toList())
                .build();
    }

    public static TeamResponseDto fromWithoutMembers(Team team) {
        return TeamResponseDto.builder()
                .id(team.getId())
                .name(team.getName())
                .category(team.getCategory().name())
                .gender(team.getGender().name())
                .logoUrl(team.getLogoUrl())
                .clubId(team.getClub() != null ? team.getClub().getId() : null)
                .clubName(team.getClub() != null ? team.getClub().getName() : null)
                .build();
    }
}
