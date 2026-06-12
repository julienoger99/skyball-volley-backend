package com.skyball.volley.auth.service;

import com.skyball.volley.auth.domain.PasswordResetToken;
import com.skyball.volley.auth.exception.InvalidVerificationTokenException;
import com.skyball.volley.auth.persistence.PasswordResetTokenRepository;
import com.skyball.volley.user.domain.AppUser;
import com.skyball.volley.user.persistence.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int TOKEN_EXPIRY_HOURS = 1;

    private final PasswordResetTokenRepository tokenRepository;
    private final AppUserRepository appUserRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void sendResetLink(String email) {
        AppUser user = appUserRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return; // don't reveal whether an account exists
        }

        tokenRepository.invalidateAllForUser(user.getId());

        String token = UUID.randomUUID().toString();
        tokenRepository.save(PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS))
                .build());

        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = tokenRepository.findById(token)
                .orElseThrow(() -> new InvalidVerificationTokenException("Invalid reset token"));

        if (prt.isUsed()) {
            throw new InvalidVerificationTokenException("This reset link has already been used");
        }
        if (prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidVerificationTokenException("Reset link has expired — please request a new one");
        }

        prt.setUsed(true);
        prt.getUser().setPassword(passwordEncoder.encode(newPassword));
        appUserRepository.save(prt.getUser());
    }
}
