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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AppUserRepository appUserRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtEncoder jwtEncoder;
    @Mock private EmailVerificationService emailVerificationService;

    @InjectMocks private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtIssuer", "test-issuer");
        ReflectionTestUtils.setField(authService, "jwtExpirationSeconds", 86400L);
    }

    private AppUser user() {
        return AppUser.builder()
                .id(1L).username("alice").email("alice@test.com")
                .role(Role.PLAYER).password("encoded")
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private AppUser unverifiedUser() {
        return AppUser.builder()
                .id(2L).username("bob").email("bob@test.com")
                .role(Role.PLAYER).password("encoded")
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Jwt fakeJwt() {
        return Jwt.withTokenValue("fake-token")
                .header("alg", "HS256")
                .subject("alice")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    void register_usernameAlreadyExists_throws() {
        RegisterDto dto = new RegisterDto();
        dto.setUsername("alice");
        dto.setEmail("alice@test.com");
        dto.setPassword("password");

        when(appUserRepository.findByUsername("alice")).thenReturn(Optional.of(user()));

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void register_emailAlreadyExists_throws() {
        RegisterDto dto = new RegisterDto();
        dto.setUsername("bob");
        dto.setEmail("alice@test.com");
        dto.setPassword("password");

        when(appUserRepository.findByUsername("bob")).thenReturn(Optional.empty());
        when(appUserRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user()));

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void register_success_savesUserAndSendsVerificationEmail() {
        RegisterDto dto = new RegisterDto();
        dto.setUsername("alice");
        dto.setEmail("alice@test.com");
        dto.setPassword("password");

        when(appUserRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(appUserRepository.findByEmail("alice@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(appUserRepository.save(any())).thenReturn(user());

        RegisterResponseDto result = authService.register(dto);

        assertThat(result.getMessage()).contains("Registration successful");
        verify(appUserRepository).save(any());
        verify(emailVerificationService).createAndSendToken(any());
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_success_returnsToken() {
        LoginDto dto = new LoginDto();
        dto.setUsername("alice");
        dto.setPassword("password");

        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getPrincipal()).thenReturn(user());
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(jwtEncoder.encode(any())).thenReturn(fakeJwt());

        AuthResponseDto result = authService.login(dto);

        assertThat(result.getToken()).isEqualTo("fake-token");
        assertThat(result.getUsername()).isEqualTo("alice");
    }

    @Test
    void login_emailNotVerified_throws() {
        LoginDto dto = new LoginDto();
        dto.setUsername("bob");
        dto.setPassword("password");

        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getPrincipal()).thenReturn(unverifiedUser());
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(EmailNotVerifiedException.class);
    }

    @Test
    void login_userNotFound_throwsUsernameNotFoundException() {
        LoginDto dto = new LoginDto();
        dto.setUsername("nobody");
        dto.setPassword("password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new UsernameNotFoundException("User not found"));

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
