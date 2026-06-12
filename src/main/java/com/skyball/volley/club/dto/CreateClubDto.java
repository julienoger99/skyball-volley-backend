package com.skyball.volley.club.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Club creation request")
public class CreateClubDto {

    @NotEmpty(message = "Club name is required")
    @Schema(description = "Club name", example = "Skyball Volley Club")
    private String name;

    @NotEmpty(message = "City is required")
    @Schema(description = "City", example = "Paris")
    private String city;

    @Schema(description = "Logo URL", example = "https://example.com/logo.png")
    private String logoUrl;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Website URL", example = "https://skyball-volley.fr")
    private String websiteUrl;
}
