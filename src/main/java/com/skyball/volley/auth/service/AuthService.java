package com.skyball.volley.auth.service;

import com.skyball.volley.auth.dto.AuthResponseDto;
import com.skyball.volley.auth.dto.LoginDto;
import com.skyball.volley.auth.dto.RegisterDto;
import com.skyball.volley.auth.dto.RegisterResponseDto;
import com.skyball.volley.auth.exception.EmailNotVerifiedException;
import com.skyball.volley.auth.exception.UserAlreadyExistsException;
import com.skyball.volley.user.domain.AppUser;
import com.skyball.volley.user.domain.Role;
import com.skyball.volley.user.persistence.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final EmailVerificationService emailVerificationService;

    @Value("${security.jwt.issuer}")
    private String jwtIssuer;

    @Value("${security.jwt.expiration-seconds}")
    private long jwtExpirationSeconds;

    @Transactional
    public RegisterResponseDto register(RegisterDto registerDto) {
        if (appUserRepository.findByUsername(registerDto.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (appUserRepository.findByEmail(registerDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        AppUser appUser = AppUser.builder()
                .username(registerDto.getUsername())
                .email(registerDto.getEmail())
                .role(Role.PLAYER)
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .build();

        appUserRepository.save(appUser);
        emailVerificationService.createAndSendToken(appUser);

        return RegisterResponseDto.builder()
                .message("Registration successful. Please check your email to verify your account.")
                .build();
    }

    public AuthResponseDto login(LoginDto loginDto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
        );

        AppUser appUser = (AppUser) auth.getPrincipal();

        if (!appUser.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        return buildResponse(appUser, "Login successful");
    }

    private AuthResponseDto buildResponse(AppUser appUser, String message) {
        return AuthResponseDto.builder()
                .token(createJwtToken(appUser))
                .tokenType("Bearer")
                .userId(appUser.getId())
                .username(appUser.getUsername())
                .email(appUser.getEmail())
                .role(appUser.getRole().toString())
                .createdAt(appUser.getCreatedAt())
                .message(message)
                .build();
    }

    private String createJwtToken(AppUser appUser) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(jwtExpirationSeconds))
                .subject(appUser.getUsername())
                .claim("role", appUser.getRole().asAuthority())
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)
        ).getTokenValue();
    }
}
