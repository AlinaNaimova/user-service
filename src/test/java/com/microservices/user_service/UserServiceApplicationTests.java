package com.microservices.user_service;

import com.microservices.user_service.service.TokenValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class UserServiceApplicationTests {

    @MockitoBean
    private TokenValidationService tokenValidationService;

    @Test
    void contextLoads() {
        when(tokenValidationService.validateToken(anyString()))
                .thenReturn(TokenValidationService.TokenValidationResult.valid("test@test.com", "ROLE_ADMIN", 1L));
        when(tokenValidationService.isAdmin(anyString())).thenReturn(true);
    }
}