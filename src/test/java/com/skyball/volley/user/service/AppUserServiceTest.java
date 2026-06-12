package com.skyball.volley.user.service;

import com.skyball.volley.auth.exception.UserAlreadyExistsException;
import com.skyball.volley.user.domain.AppUser;
import com.skyball.volley.user.domain.Role;
import com.skyball.volley.user.dto.UpdateUserDto;
import com.skyball.volley.user.dto.UserResponseDto;
import com.skyball.volley.user.exception.UserNotFoundException;
import com.skyball.volley.user.persistence.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock private AppUserRepository appUserRepository;

    @InjectMocks private AppUserService appUserService;

    private static final Long USER_ID = 1L;

    private AppUser user() {
        return AppUser.builder()
                .id(USER_ID).username("alice").email("alice@test.com")
                .role(Role.PLAYER).password("encoded").build();
    }

    // ── getAllUsers ────────────────────────────────────────────────────────────

    @Test
    void getAllUsers_returnsAllUsers() {
        when(appUserRepository.findAll()).thenReturn(List.of(user()));

        List<UserResponseDto> result = appUserService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("alice");
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    void getUserById_found_returnsDto() {
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user()));

        UserResponseDto result = appUserService.getUserById(USER_ID);

        assertThat(result.getUsername()).isEqualTo("alice");
    }

    @Test
    void getUserById_notFound_throws() {
        when(appUserRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ── getUserByUsername ─────────────────────────────────────────────────────

    @Test
    void getUserByUsername_found_returnsDto() {
        when(appUserRepository.findByUsername("alice")).thenReturn(Optional.of(user()));

        UserResponseDto result = appUserService.getUserByUsername("alice");

        assertThat(result.getUsername()).isEqualTo("alice");
    }

    @Test
    void getUserByUsername_notFound_throws() {
        when(appUserRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.getUserByUsername("unknown"))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ── loadUserByUsername ────────────────────────────────────────────────────

    @Test
    void loadUserByUsername_found_returnsUserDetails() {
        when(appUserRepository.findByUsername("alice")).thenReturn(Optional.of(user()));

        UserDetails details = appUserService.loadUserByUsername("alice");

        assertThat(details.getUsername()).isEqualTo("alice");
    }

    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(appUserRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // ── updateUser ────────────────────────────────────────────────────────────

    @Test
    void updateUser_usernameConflict_throws() {
        AppUser conflict = AppUser.builder().id(2L).username("bob").email("bob@test.com").role(Role.PLAYER).build();
        UpdateUserDto dto = new UpdateUserDto();
        dto.setUsername("bob");

        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(appUserRepository.findByUsername("bob")).thenReturn(Optional.of(conflict));

        assertThatThrownBy(() -> appUserService.updateUser(USER_ID, dto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void updateUser_emailConflict_throws() {
        AppUser conflict = AppUser.builder().id(2L).username("bob").email("bob@test.com").role(Role.PLAYER).build();
        UpdateUserDto dto = new UpdateUserDto();
        dto.setEmail("bob@test.com");

        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(appUserRepository.findByEmail("bob@test.com")).thenReturn(Optional.of(conflict));

        assertThatThrownBy(() -> appUserService.updateUser(USER_ID, dto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void updateUser_success_updatesAndReturns() {
        AppUser existing = user();
        UpdateUserDto dto = new UpdateUserDto();
        dto.setUsername("alice_new");

        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(existing));
        when(appUserRepository.findByUsername("alice_new")).thenReturn(Optional.empty());
        when(appUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDto result = appUserService.updateUser(USER_ID, dto);

        assertThat(result.getUsername()).isEqualTo("alice_new");
        verify(appUserRepository).save(existing);
    }

    // ── deleteUser ────────────────────────────────────────────────────────────

    @Test
    void deleteUser_notFound_throws() {
        when(appUserRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> appUserService.deleteUser(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deleteUser_found_deletesById() {
        when(appUserRepository.existsById(USER_ID)).thenReturn(true);

        appUserService.deleteUser(USER_ID);

        verify(appUserRepository).deleteById(USER_ID);
    }
}
