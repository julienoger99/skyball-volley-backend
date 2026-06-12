package com.skyball.volley.auth.persistence;

import com.skyball.volley.auth.domain.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    boolean existsByJti(String jti);

    void deleteByExpiresAtBefore(Instant threshold);
}