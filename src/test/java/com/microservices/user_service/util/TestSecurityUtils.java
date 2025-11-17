package com.microservices.user_service.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

public class TestSecurityUtils {

    public static void mockAuthentication(String email, String role, Long userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(role))
                );
        authentication.setDetails(userId);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    public static void mockAdminUser() {
        mockAuthentication("admin@test.com", "ROLE_ADMIN", 1L);
    }

    public static void mockRegularUser(Long userId) {
        mockAuthentication("user@test.com", "ROLE_USER", userId);
    }

    public static void mockRegularUser() {
        mockAuthentication("user@test.com", "ROLE_USER", 2L);
    }

    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}