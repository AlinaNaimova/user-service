package com.microservices.user_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
        // Просто проверяем что контекст Spring загружается
        // Вся аутентификация теперь через Keycloak
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testWithAdminUser() {
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testWithUserRole() {
    }
}