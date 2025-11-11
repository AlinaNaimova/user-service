package com.microservices.user_service.service;

import com.microservices.user_service.config.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserSecurity {

    public boolean checkUserId(Authentication authentication, Long userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        if (userId == null) {
            return "ROLE_ADMIN".equals(principal.getRole());
        }

        return principal.getUserId().equals(userId) ||
                "ROLE_ADMIN".equals(principal.getRole());
    }

    public boolean checkCardAccess(Authentication authentication, Long userId) {
        return checkUserId(authentication, userId);
    }
}