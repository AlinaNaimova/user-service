package com.microservices.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microservices.user_service.dto.UserDTO;
import com.microservices.user_service.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = "classpath:sql_scripts/insert-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:sql_scripts/insert-cards.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:sql_scripts/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserControllerIntegrationTest extends AbstractIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        objectMapper.registerModule(new JavaTimeModule());
        userDTO = new UserDTO();
        userDTO.setName("Tom");
        userDTO.setSurname("Ripley");
        userDTO.setEmail("tom.ripley@example.com");
        userDTO.setBirthDate(LocalDate.of(1990, 1, 1));
    }

    @Test
    void createUserShouldReturnCreatedUser() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(userDTO.getName())))
                .andExpect(jsonPath("$.surname", is(userDTO.getSurname())))
                .andExpect(jsonPath("$.email", is(userDTO.getEmail())))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void createUserWithInvalidDataShouldReturnBadRequest() throws Exception {
        UserDTO invalidUser = new UserDTO();
        invalidUser.setName("");
        invalidUser.setEmail("invalid-email");
        invalidUser.setBirthDate(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserByIdShouldReturnUser() throws Exception {
        Long userId = 1L;

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.intValue())))
                .andExpect(jsonPath("$.name", is("Test")))
                .andExpect(jsonPath("$.surname", is("User")))
                .andExpect(jsonPath("$.email", is("test.user@example.com")));
    }

    @Test
    void getUserByIdWithNonExistingIdShouldReturnNotFound() throws Exception {
        Long nonExistingId = 999L;

        mockMvc.perform(get("/api/users/{id}", nonExistingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserWithCardsByIdShouldReturnUserWithCards() throws Exception {
        Long userId = 1L;

        mockMvc.perform(get("/api/users/{id}/with-cards", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.intValue())))
                .andExpect(jsonPath("$.name", is("Test")))
                .andExpect(jsonPath("$.cards", notNullValue()))
                .andExpect(jsonPath("$.cards.length()", is(2)));
    }

    @Test
    void getAllUsersShouldReturnPaginatedUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content.length()", is(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void getUserByEmailShouldReturnUser() throws Exception {
        String email = "test.user@example.com";

        mockMvc.perform(get("/api/users/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(email)));
    }

    @Test
    void getUserByEmailWithNonExistingEmailShouldReturnNotFound() throws Exception {
        String nonExistingEmail = "nonexisting@example.com";

        mockMvc.perform(get("/api/users/email/{email}", nonExistingEmail))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserShouldReturnUpdatedUser() throws Exception {
        Long userId = 1L;
        UserDTO updatedUser = new UserDTO();
        updatedUser.setName("Updated");
        updatedUser.setSurname("Name");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setBirthDate(LocalDate.of(1995, 5, 5));

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated")))
                .andExpect(jsonPath("$.surname", is("Name")))
                .andExpect(jsonPath("$.email", is("updated@example.com")));
    }

    @Test
    void deleteUserShouldReturnNoContent() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUserWithDuplicateEmailShouldReturnConflict() throws Exception {
        UserDTO duplicateUser = new UserDTO();
        duplicateUser.setName("Another");
        duplicateUser.setSurname("User");
        duplicateUser.setEmail("test.user@example.com");
        duplicateUser.setBirthDate(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isConflict()); // Меняем с 400 на 409
    }
}