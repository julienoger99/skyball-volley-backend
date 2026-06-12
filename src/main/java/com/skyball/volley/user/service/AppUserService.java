package com.skyball.volley.user.service;

import com.skyball.volley.auth.exception.UserAlreadyExistsException;
import com.skyball.volley.user.domain.AppUser;
import com.skyball.volley.user.dto.UpdateUserDto;
import com.skyball.volley.user.dto.UserResponseDto;
import com.skyball.volley.user.exception.UserNotFoundException;
import com.skyball.volley.user.persistence.AppUserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return appUserRepository.findAll().stream()
                .map(UserResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        AppUser appUser = appUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return UserResponseDto.from(appUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserByUsername(String username) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        return UserResponseDto.from(appUser);
    }

    @PreAuthorize("@sec.isSelf(#id)")
    @Transactional
    public UserResponseDto updateUser(Long id, UpdateUserDto dto) {
        AppUser appUser = appUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (dto.getUsername() != null && !dto.getUsername().equals(appUser.getUsername())) {
            appUserRepository.findByUsername(dto.getUsername()).ifPresent(existing -> {
                throw new UserAlreadyExistsException("Username already exists");
            });
            appUser.setUsername(dto.getUsername());
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(appUser.getEmail())) {
            appUserRepository.findByEmail(dto.getEmail()).ifPresent(existing -> {
                throw new UserAlreadyExistsException("Email already exists");
            });
            appUser.setEmail(dto.getEmail());
        }

        appUserRepository.save(appUser);
        return UserResponseDto.from(appUser);
    }

    @PreAuthorize("@sec.isSelf(#id)")
    public void deleteUser(Long id) {
        if (!appUserRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        appUserRepository.deleteById(id);
    }
}