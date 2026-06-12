package com.skyball.volley.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Role update request")
public class UpdateMemberRoleDto {

    @NotNull(message = "Role is required")
    @Schema(description = "New role", example = "MANAGER")
    private MembershipRole role;
}
