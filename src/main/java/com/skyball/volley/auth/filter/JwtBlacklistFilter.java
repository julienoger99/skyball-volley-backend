package com.skyball.volley.auth.filter;

import com.skyball.volley.auth.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtBlacklistFilter extends OncePerRequestFilter {

    private final TokenBlacklistService tokenBlacklistService;
    private final JwtDecoder jwtDecoder;

    public JwtBlacklistFilter(@Lazy TokenBlacklistService tokenBlacklistService, @Autowired JwtDecoder jwtDecoder) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                Jwt jwt = jwtDecoder.decode(authHeader.substring(7));
                if (tokenBlacklistService.isBlacklisted(jwt.getId())) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            } catch (Exception _) {
                // laisser Spring Security gérer les tokens invalides/expirés
            }
        }
        chain.doFilter(request, response);
    }
}
