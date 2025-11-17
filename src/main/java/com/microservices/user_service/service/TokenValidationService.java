package com.microservices.user_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenValidationService {

    private final RestTemplate restTemplate;
    private final String AUTH_SERVICE_URL = "http://localhost:8080/api/auth/validate-token"; // или через service discovery

    public TokenValidationResult validateToken(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    AUTH_SERVICE_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                boolean isValid = (Boolean) body.get("valid");

                if (isValid) {
                    return TokenValidationResult.valid(
                            (String) body.get("email"),
                            (String) body.get("role"),
                            ((Number) body.get("userId")).longValue()
                    );
                }
            }

            return TokenValidationResult.invalid("Token validation failed");

        } catch (Exception e) {
            return TokenValidationResult.invalid("Authentication service unavailable: " + e.getMessage());
        }
    }

    public boolean isAdmin(String token) {
        TokenValidationResult result = validateToken(token);
        return result.isValid() && "ROLE_ADMIN".equals(result.getRole());
    }

    // Внутренний класс для результата валидации
    public static class TokenValidationResult {
        private final boolean valid;
        private final String email;
        private final String role;
        private final Long userId;
        private final String error;

        private TokenValidationResult(boolean valid, String email, String role, Long userId, String error) {
            this.valid = valid;
            this.email = email;
            this.role = role;
            this.userId = userId;
            this.error = error;
        }

        public static TokenValidationResult valid(String email, String role, Long userId) {
            return new TokenValidationResult(true, email, role, userId, null);
        }

        public static TokenValidationResult invalid(String error) {
            return new TokenValidationResult(false, null, null, null, error);
        }

        // Getters
        public boolean isValid() { return valid; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public Long getUserId() { return userId; }
        public String getError() { return error; }
    }
}