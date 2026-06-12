package com.skyball.volley.match.dto;

import com.skyball.volley.match.domain.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Player attendance update")
public class UpdateAttendanceDto {

    @NotNull(message = "Attendance status is required")
    @Schema(description = "Attendance status", example = "PRESENT")
    private AttendanceStatus attendanceStatus;
}
