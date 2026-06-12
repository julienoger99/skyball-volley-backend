package com.skyball.volley.auth.service;

import com.skyball.volley.auth.domain.EmailVerificationToken;
import com.skyball.volley.auth.exception.InvalidVerificationTokenException;
import com.skyball.volley.auth.persistence.EmailVerificationTokenRepository;
import com.skyball.volley.user.domain.AppUser;
import com.skyball.volley.user.exception.UserNotFoundException;
import com.skyball.volley.user.persistence.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final int TOKEN_EXPIRY_HOURS = 24;

    private final EmailVerificationTokenRepository tokenRepository;
    private final AppUserRepository appUserRepository;
    private final EmailService emailService;

    @Transactional
    public void createAndSendToken(AppUser user) {
        doCreateAndSendToken(user);
    }

    @Transactional
    public void verify(String token) {
        EmailVerificationToken evt = tokenRepository.findById(token)
                .orElseThrow(() -> new InvalidVerificationTokenException("Invalid verification token"));

        if (evt.isUsed()) {
            throw new InvalidVerificationTokenException("This verification link has already been used");
        }
        if (evt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidVerificationTokenException("Verification link has expired — please request a new one");
        }

        evt.setUsed(true);
        evt.getUser().setEmailVerified(true);
        appUserRepository.save(evt.getUser());
    }

    @Transactional
    public void resend(String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (user.isEmailVerified()) {
            throw new InvalidVerificationTokenException("This account is already verified");
        }

        doCreateAndSendToken(user);
    }

    private void doCreateAndSendToken(AppUser user) {
        tokenRepository.invalidateAllForUser(user.getId());

        String token = UUID.randomUUID().toString();
        tokenRepository.save(EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS))
                .build());

        emailService.sendVerificationEmail(user.getEmail(), token);
    }
}
