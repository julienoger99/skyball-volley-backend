package com.skyball.volley.match.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Full match score submission — replaces all existing sets")
public class BulkMatchSetsDto {

    @NotEmpty(message = "At least one set is required")
    @Valid
    @Schema(description = "All sets played, in order")
    private List<CreateMatchSetDto> sets;
}
