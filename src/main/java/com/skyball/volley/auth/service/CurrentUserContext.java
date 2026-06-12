package com.skyball.volley.auth.service;

import com.skyball.volley.user.domain.AppUser;
import com.skyball.volley.user.persistence.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequiredArgsConstructor
public class CurrentUserContext {

    private final AppUserRepository appUserRepository;
    private AppUser cachedUser;

    public AppUser get() {
        if (cachedUser == null) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                throw new UsernameNotFoundException("No authentication context");
            }
            String username = auth.getName();
            cachedUser = appUserRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        }
        return cachedUser;
    }
}
