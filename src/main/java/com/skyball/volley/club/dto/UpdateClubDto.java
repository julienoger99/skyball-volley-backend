package com.skyball.volley.club.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Club update request — all fields are optional")
public class UpdateClubDto {

    @Size(min = 1, message = "Club name cannot be empty")
    @Schema(description = "Club name", example = "Skyball Volley Club")
    private String name;

    @Size(min = 1, message = "City cannot be empty")
    @Schema(description = "City", example = "Paris")
    private String city;

    @Schema(description = "Logo URL", example = "https://example.com/logo.png")
    private String logoUrl;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Website URL", example = "https://skyball-volley.fr")
    private String websiteUrl;
}
