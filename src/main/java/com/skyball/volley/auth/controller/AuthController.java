package com.skyball.volley.auth.controller;

import com.skyball.volley.auth.dto.*;
import com.skyball.volley.auth.service.AuthService;
import com.skyball.volley.auth.service.EmailVerificationService;
import com.skyball.volley.auth.service.PasswordResetService;
import com.skyball.volley.auth.service.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("${app.api.base-path}/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "201", description = "Registration successful, verification email sent",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "409", description = "Username or email already exists",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterDto registerDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerDto));
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "Email not verified",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto loginDto) {
        return ResponseEntity.ok(authService.login(loginDto));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidates the current JWT token.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "204", description = "Logged out successfully")
    @ApiResponse(responseCode = "401", description = "Invalid or missing token")
    public ResponseEntity<Void> logout(Principal principal) {
        if (principal instanceof JwtAuthenticationToken jwtAuth) {
            tokenBlacklistService.blacklist(
                    jwtAuth.getToken().getId(),
                    jwtAuth.getToken().getExpiresAt()
            );
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify email address", description = "Validates the token sent by email and marks the account as verified.")
    @ApiResponse(responseCode = "200", description = "Email verified successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<RegisterResponseDto> verify(@RequestParam String token) {
        emailVerificationService.verify(token);
        return ResponseEntity.ok(RegisterResponseDto.builder()
                .message("Email verified successfully. You can now log in.")
                .build());
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Sends a reset link to the provided email. Always returns 200 to avoid account enumeration.")
    @ApiResponse(responseCode = "200", description = "Reset email sent if account exists")
    public ResponseEntity<RegisterResponseDto> forgotPassword(@Valid @RequestBody ForgotPasswordDto dto) {
        passwordResetService.sendResetLink(dto.getEmail());
        return ResponseEntity.ok(RegisterResponseDto.builder()
                .message("If an account exists for this email, a password reset link has been sent.")
                .build());
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using token")
    @ApiResponse(responseCode = "200", description = "Password reset successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<RegisterResponseDto> resetPassword(@Valid @RequestBody ResetPasswordDto dto) {
        passwordResetService.resetPassword(dto.getToken(), dto.getNewPassword());
        return ResponseEntity.ok(RegisterResponseDto.builder()
                .message("Password reset successfully. You can now log in with your new password.")
                .build());
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email")
    @ApiResponse(responseCode = "200", description = "Verification email sent")
    @ApiResponse(responseCode = "400", description = "Account not found or already verified",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<RegisterResponseDto> resendVerification(@Valid @RequestBody ResendVerificationDto dto) {
        emailVerificationService.resend(dto.getEmail());
        return ResponseEntity.ok(RegisterResponseDto.builder()
                .message("Verification email sent. Please check your inbox.")
                .build());
    }
}
