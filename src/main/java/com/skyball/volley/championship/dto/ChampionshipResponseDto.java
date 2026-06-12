package com.skyball.volley.championship.dto;

import com.skyball.volley.championship.domain.Championship;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Championship information")
public class ChampionshipResponseDto {

    @Schema(description = "Championship ID", example = "1")
    private Long id;

    @Schema(description = "Championship name", example = "Championnat Régional N2")
    private String name;

    @Schema(description = "Season", example = "2025-2026")
    private String season;

    @Schema(description = "Category (null = open)", example = "SENIOR")
    private String category;

    public static ChampionshipResponseDto from(Championship c) {
        return ChampionshipResponseDto.builder()
                .id(c.getId())
                .name(c.getName())
                .season(c.getSeason())
                .category(c.getCategory() != null ? c.getCategory().name() : null)
                .build();
    }
}
