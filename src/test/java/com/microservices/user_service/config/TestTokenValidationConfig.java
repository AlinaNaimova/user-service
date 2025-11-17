package com.microservices.user_service.config;

import com.microservices.user_service.service.TokenValidationService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
public class TestTokenValidationConfig {

    @Bean
    @Primary
    public RestTemplate testRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    @Primary
    public TokenValidationService tokenValidationService() {
        return new TokenValidationService(testRestTemplate()) {
            @Override
            public TokenValidationService.TokenValidationResult validateToken(String token) {
                // В тестах всегда возвращаем успешную валидацию
                return TokenValidationService.TokenValidationResult.valid("admin@test.com", "ROLE_ADMIN", 1L);
            }

            @Override
            public boolean isAdmin(String token) {
                return true;
            }
        };
    }
}