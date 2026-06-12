package com.skyball.volley.auth.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> STRICT_PATHS = Set.of("/api/v1/auth/login");
    private static final Set<String> GENERAL_PATHS = Set.of(
            "/api/v1/auth/register",
            "/api/v1/auth/resend-verification",
            "/api/v1/auth/forgot-password"
    );

    private final Cache<String, Bucket> strictBuckets = Caffeine.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .build();
    private final Cache<String, Bucket> generalBuckets = Caffeine.newBuilder()
            .expireAfterAccess(2, TimeUnit.HOURS)
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String ip = extractIp(request);

        Bucket bucket = null;
        if (STRICT_PATHS.contains(path)) {
            bucket = strictBuckets.get(ip, k -> loginBucket());
        } else if (GENERAL_PATHS.contains(path)) {
            bucket = generalBuckets.get(ip, k -> generalBucket());
        }

        if (bucket != null && !bucket.tryConsume(1)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":429,\"message\":\"Too many requests, please try again later\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private Bucket loginBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(5)
                        .refillGreedy(5, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    private Bucket generalBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(3)
                        .refillGreedy(3, Duration.ofHours(1))
                        .build())
                .build();
    }

    private String extractIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return xff != null ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }
}
