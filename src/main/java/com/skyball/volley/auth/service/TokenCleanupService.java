package com.skyball.volley.auth.service;

import com.skyball.volley.auth.persistence.EmailVerificationTokenRepository;
import com.skyball.volley.auth.persistence.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final EmailVerificationTokenRepository emailTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Scheduled(cron = "0 0 3 * * *") // every day at 3am
    @Transactional
    public void purgeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        emailTokenRepository.deleteExpiredOrUsed(now);
        passwordResetTokenRepository.deleteExpiredOrUsed(now);
        log.info("Token cleanup completed at {}", now);
    }
}
