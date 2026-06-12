package com.skyball.volley.auth.service;

import com.skyball.volley.auth.domain.BlacklistedToken;
import com.skyball.volley.auth.persistence.BlacklistedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public void blacklist(String jti, Instant expiresAt) {
        blacklistedTokenRepository.save(new BlacklistedToken(null, jti, expiresAt));
    }

    public boolean isBlacklisted(String jti) {
        return blacklistedTokenRepository.existsByJti(jti);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void purgeExpiredTokens() {
        blacklistedTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}