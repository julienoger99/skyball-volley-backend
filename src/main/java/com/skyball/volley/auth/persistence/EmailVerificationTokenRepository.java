package com.skyball.volley.auth.persistence;

import com.skyball.volley.auth.domain.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, String> {

    @Modifying
    @Query("UPDATE EmailVerificationToken t SET t.used = true WHERE t.user.id = :userId AND t.used = false")
    void invalidateAllForUser(Long userId);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :before OR t.used = true")
    void deleteExpiredOrUsed(LocalDateTime before);
}
