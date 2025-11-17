package com.microservices.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.user_service.dto.CardDTO;
import com.microservices.user_service.integration.AbstractIntegrationTest;
import com.microservices.user_service.util.TestSecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql(scripts = "classpath:sql_scripts/insert-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:sql_scripts/insert-cards.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:sql_scripts/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CardControllerIntegrationTest extends AbstractIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private CardDTO cardDTO;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        TestSecurityUtils.mockAdminUser();

        cardDTO = new CardDTO();
        cardDTO.setNumber("1234567812345678");
        cardDTO.setHolder("Tom Ripley");
        cardDTO.setExpirationDate("12/25");
        cardDTO.setUserId(1L);
    }

    @Test
    void createCardShouldReturnCreatedCard() throws Exception {
        mockMvc.perform(post("/api/card_info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number", is(cardDTO.getNumber())))
                .andExpect(jsonPath("$.holder", is(cardDTO.getHolder())))
                .andExpect(jsonPath("$.expirationDate", is(cardDTO.getExpirationDate())))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void createCardWithInvalidDataShouldReturnBadRequest() throws Exception {
        CardDTO invalidCard = new CardDTO();
        invalidCard.setNumber("123");
        invalidCard.setExpirationDate("invalid");

        mockMvc.perform(post("/api/card_info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCard)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCardByIdShouldReturnCard() throws Exception {
        Long cardId = 1L;

        mockMvc.perform(get("/api/card_info/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(cardId.intValue())))
                .andExpect(jsonPath("$.number", is("1111222233334444")))
                .andExpect(jsonPath("$.holder", is("Test User")))
                .andExpect(jsonPath("$.expirationDate", is("12/25")));
    }

    @Test
    void getCardByIdWithNonExistingIdShouldReturnNotFound() throws Exception {
        Long nonExistingId = 999L;

        mockMvc.perform(get("/api/card_info/{id}", nonExistingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllCardsShouldReturnPaginatedCards() throws Exception {
        mockMvc.perform(get("/api/card_info")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content.length()", is(3)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void deleteCardShouldReturnNoContent() throws Exception {
        Long cardId = 1L;

        mockMvc.perform(delete("/api/card_info/{id}", cardId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/card_info/{id}", cardId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCardForNonExistingUserShouldReturnNotFound() throws Exception {
        CardDTO cardForNonExistingUser = new CardDTO();
        cardForNonExistingUser.setNumber("9999888877776666");
        cardForNonExistingUser.setHolder("Non Existing User");
        cardForNonExistingUser.setExpirationDate("12/25");
        cardForNonExistingUser.setUserId(999L);

        mockMvc.perform(post("/api/card_info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardForNonExistingUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCardWithInvalidCardNumberShouldReturnBadRequest() throws Exception {
        CardDTO invalidCard = new CardDTO();
        invalidCard.setNumber("1234-5678-1234-5678");
        invalidCard.setHolder("Tom Ripley");
        invalidCard.setExpirationDate("12/25");
        invalidCard.setUserId(1L);

        mockMvc.perform(post("/api/card_info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCard)))
                .andExpect(status().isBadRequest());
    }
}