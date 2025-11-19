package com.microservices.user_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
// Simply verifying that the Spring context loads successfully
// All authentication is now handled through Keycloak
    }
}