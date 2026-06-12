package com.skyball.volley.club.dto;

import com.skyball.volley.club.domain.Club;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "Club information response")
public class ClubResponseDto {

    @Schema(description = "Club ID", example = "1")
    private Long id;

    @Schema(description = "Club name", example = "Skyball Volley Club")
    private String name;

    @Schema(description = "City", example = "Paris")
    private String city;

    @Schema(description = "Logo URL", example = "https://example.com/logo.png")
    private String logoUrl;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Website URL", example = "https://skyball-volley.fr")
    private String websiteUrl;

    @Schema(description = "Creation date", example = "2026-01-15")
    private LocalDate createdAt;

    public static ClubResponseDto from(Club club) {
        return ClubResponseDto.builder()
                .id(club.getId())
                .name(club.getName())
                .city(club.getCity())
                .logoUrl(club.getLogoUrl())
                .description(club.getDescription())
                .websiteUrl(club.getWebsiteUrl())
                .createdAt(club.getCreatedAt())
                .build();
    }
}
