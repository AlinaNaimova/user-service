package com.microservices.user_service.service;

import com.microservices.user_service.dto.CardDTO;
import com.microservices.user_service.exception.DuplicateResourceException;
import com.microservices.user_service.exception.NotFoundException;
import com.microservices.user_service.mapper.CardMapper;
import com.microservices.user_service.model.Card;
import com.microservices.user_service.model.User;
import com.microservices.user_service.repository.CardRepository;
import com.microservices.user_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardService cardService;

    private User createUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private Card createCard(Long id, String number, String holder, String expirationDate, User user) {
        Card card = new Card();
        card.setId(id);
        card.setNumber(number);
        card.setHolder(holder);
        card.setExpirationDate(expirationDate);
        card.setUser(user);
        return card;
    }

    private CardDTO createCardDTO(Long id, Long userId, String number, String holder, String expirationDate) {
        return new CardDTO(id, userId, number, holder, expirationDate);
    }

    @Test
    void createCardWithValidDataExpectSaveAndReturnCardDTO() {
        User user = createUser(1L, "Kira", "kira@example.com");
        CardDTO inputDTO = createCardDTO(null, 1L, "1111222233334444", "MOLLY BING", "06/26");
        Card newCard = createCard(null, "1111222233334444", "MOLLY BING", "06/26", null);
        Card savedCard = createCard(2L, "1111222233334444", "MOLLY BING", "06/26", user);
        CardDTO expectedDTO = createCardDTO(2L, 1L, "1111222233334444", "MOLLY BING", "06/26");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.findByNumberNative("1111222233334444")).thenReturn(Optional.empty());
        when(cardMapper.toEntity(inputDTO)).thenReturn(newCard);
        when(cardRepository.save(newCard)).thenReturn(savedCard);
        when(cardMapper.toDTO(savedCard)).thenReturn(expectedDTO);

        CardDTO result = cardService.createCard(inputDTO);

        assertThat(result).isEqualTo(expectedDTO);
        verify(userRepository).findById(1L);
        verify(cardRepository).findByNumberNative("1111222233334444");
        verify(cardRepository).save(newCard);
        assertThat(newCard.getUser()).isEqualTo(user);
    }

    @Test
    void createCardWhenUserNotExistsExpectThrowNotFoundException() {
        CardDTO inputDTO = createCardDTO(null, 200L, "1111222233334444", "Holder", "12/25");
        when(userRepository.findById(200L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCard(inputDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found with id: 200");

        verify(cardRepository, never()).findByNumberNative(anyString());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void createCardWhenCardNumberExistsExpectThrowDuplicateResourceException() {
        User user = createUser(1L, "Kira", "kira@example.com");
        Card existingCard = createCard(1L, "1234567812345678", "KIRA CHANG", "12/25", user);
        CardDTO inputDTO = createCardDTO(null, 1L, "1234567812345678", "NEW HOLDER", "12/26");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.findByNumberNative("1234567812345678")).thenReturn(Optional.of(existingCard));

        assertThatThrownBy(() -> cardService.createCard(inputDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Card already exists with number: 1234567812345678");

        verify(cardMapper, never()).toEntity(any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void getCardByIdWhenCardExistsExpectReturnCardDTO() {
        User user = createUser(1L, "Kira", "kira@example.com");
        Card card = createCard(1L, "1234567812345678", "KIRA CHANG", "12/25", user);
        CardDTO expectedDTO = createCardDTO(1L, 1L, "1234567812345678", "KIRA CHANG", "12/25");

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardMapper.toDTO(card)).thenReturn(expectedDTO);

        CardDTO result = cardService.getCardById(1L);

        assertThat(result).isEqualTo(expectedDTO);
        verify(cardRepository).findById(1L);
        verify(cardMapper).toDTO(card);
    }

    @Test
    void getCardByIdWhenCardNotExistsExpectThrowNotFoundException() {
        when(cardRepository.findById(333L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardById(333L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Card not found with id: 333");

        verify(cardMapper, never()).toDTO(any());
    }

    @Test
    void getAllCardsExpectReturnPageOfCardDTO() {
        User user = createUser(1L, "Kira", "kira@example.com");
        Card card = createCard(1L, "1234567812345678", "KIRA CHANG", "12/25", user);
        CardDTO cardDTO = createCardDTO(1L, 1L, "1234567812345678", "KIRA CHANG", "12/25");

        Pageable pageable = PageRequest.of(0, 10);
        List<Card> cards = Arrays.asList(card);
        Page<Card> cardPage = new PageImpl<>(cards, pageable, cards.size());

        when(cardRepository.findAll(pageable)).thenReturn(cardPage);
        when(cardMapper.toDTO(card)).thenReturn(cardDTO);

        Page<CardDTO> resultPage = cardService.getAllCards(pageable);

        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getContent().get(0)).isEqualTo(cardDTO);
        verify(cardRepository).findAll(pageable);
    }

    @Test
    void deleteCardWhenCardExistsExpectDeleteCard() {
        when(cardRepository.existsById(1L)).thenReturn(true);

        cardService.deleteCard(1L);

        verify(cardRepository).existsById(1L);
        verify(cardRepository).deleteById(1L);
    }

    @Test
    void deleteCardWhenCardNotExistsExpectThrowNotFoundException() {
        when(cardRepository.existsById(444L)).thenReturn(false);

        assertThatThrownBy(() -> cardService.deleteCard(444L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Card not found with id: 444");

        verify(cardRepository, never()).deleteById(any());
    }
}