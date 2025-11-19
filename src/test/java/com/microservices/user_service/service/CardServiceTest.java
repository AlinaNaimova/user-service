package com.microservices.user_service.service;

import com.microservices.user_service.dto.CardDTO;
import com.microservices.user_service.exception.DuplicateResourceException;
import com.microservices.user_service.exception.NotFoundException;
import com.microservices.user_service.mapper.CardMapper;
import com.microservices.user_service.model.Card;
import com.microservices.user_service.model.User;
import com.microservices.user_service.repository.CardRepository;
import com.microservices.user_service.repository.UserRepository;
import com.microservices.user_service.util.SecurityUtils;
import com.microservices.user_service.util.TestSecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private CardService cardService;

    @BeforeEach
    void setUp() {
        TestSecurityUtils.mockAdminUser();
    }

    @AfterEach
    void tearDown() {
        TestSecurityUtils.clearAuthentication();
    }

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

        when(securityUtils.hasAccessToUser(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.findByNumberNative("1111222233334444")).thenReturn(Optional.empty());
        when(cardMapper.toEntity(inputDTO)).thenReturn(newCard);
        when(cardRepository.save(newCard)).thenReturn(savedCard);
        when(cardMapper.toDTO(savedCard)).thenReturn(expectedDTO);

        CardDTO result = cardService.createCard(inputDTO);

        assertThat(result).isEqualTo(expectedDTO);
        verify(securityUtils).hasAccessToUser(1L);
        verify(userRepository).findById(1L);
        verify(cardRepository).findByNumberNative("1111222233334444");
        verify(cardRepository).save(newCard);
        assertThat(newCard.getUser()).isEqualTo(user);
    }

    @Test
    void createCardWhenNoAccessExpectThrowAccessDeniedException() {
        CardDTO inputDTO = createCardDTO(null, 2L, "1111222233334444", "MOLLY BING", "06/26");

        when(securityUtils.hasAccessToUser(2L)).thenReturn(false);

        assertThatThrownBy(() -> cardService.createCard(inputDTO))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You can only create cards for yourself");

        verify(userRepository, never()).findById(any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void createCardWhenUserNotExistsExpectThrowNotFoundException() {
        CardDTO inputDTO = createCardDTO(null, 200L, "1111222233334444", "Holder", "12/25");

        when(securityUtils.hasAccessToUser(200L)).thenReturn(true);
        when(userRepository.findById(200L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCard(inputDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found with id: 200");

        verify(securityUtils).hasAccessToUser(200L);
        verify(cardRepository, never()).findByNumberNative(anyString());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void createCardWhenCardNumberExistsExpectThrowDuplicateResourceException() {
        User user = createUser(1L, "Kira", "kira@example.com");
        Card existingCard = createCard(1L, "1234567812345678", "KIRA CHANG", "12/25", user);
        CardDTO inputDTO = createCardDTO(null, 1L, "1234567812345678", "NEW HOLDER", "12/26");

        when(securityUtils.hasAccessToUser(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.findByNumberNative("1234567812345678")).thenReturn(Optional.of(existingCard));

        assertThatThrownBy(() -> cardService.createCard(inputDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Card already exists with number: 1234567812345678");

        verify(securityUtils).hasAccessToUser(1L);
        verify(cardMapper, never()).toEntity(any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void getCardByIdWhenCardExistsExpectReturnCardDTO() {
        User user = createUser(1L, "Kira", "kira@example.com");
        Card card = createCard(1L, "1234567812345678", "KIRA CHANG", "12/25", user);
        CardDTO expectedDTO = createCardDTO(1L, 1L, "1234567812345678", "KIRA CHANG", "12/25");

        when(cardRepository.findByIdWithUser(1L)).thenReturn(Optional.of(card));
        when(securityUtils.hasAccessToUser(1L)).thenReturn(true);
        when(cardMapper.toDTO(card)).thenReturn(expectedDTO);

        CardDTO result = cardService.getCardById(1L);

        assertThat(result).isEqualTo(expectedDTO);
        verify(cardRepository).findByIdWithUser(1L);
        verify(securityUtils).hasAccessToUser(1L);
        verify(cardMapper).toDTO(card);
    }

    @Test
    void getCardByIdWhenNoAccessExpectThrowAccessDeniedException() {
        User otherUser = createUser(2L, "Other", "other@example.com");
        Card card = createCard(1L, "1234567812345678", "OTHER USER", "12/25", otherUser);

        when(cardRepository.findByIdWithUser(1L)).thenReturn(Optional.of(card));
        when(securityUtils.hasAccessToUser(2L)).thenReturn(false);

        assertThatThrownBy(() -> cardService.getCardById(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access denied to this card");

        verify(cardMapper, never()).toDTO(any());
    }

    @Test
    void getCardByIdWhenCardNotExistsExpectThrowNotFoundException() {
        when(cardRepository.findByIdWithUser(333L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardById(333L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Card not found with id: 333");

        verify(cardMapper, never()).toDTO(any());
    }

    @Test
    void getMyCardsShouldReturnUserCards() {
        User currentUser = createUser(1L, "Kira", "kira@example.com");
        Card card1 = createCard(1L, "1111222233334444", "KIRA CHANG", "12/25", currentUser);
        Card card2 = createCard(2L, "5555666677778888", "KIRA CHANG", "12/26", currentUser);
        CardDTO cardDTO1 = createCardDTO(1L, 1L, "1111222233334444", "KIRA CHANG", "12/25");
        CardDTO cardDTO2 = createCardDTO(2L, 1L, "5555666677778888", "KIRA CHANG", "12/26");

        Pageable pageable = PageRequest.of(0, 10);
        List<Card> cards = Arrays.asList(card1, card2);
        Page<Card> cardPage = new PageImpl<>(cards, pageable, cards.size());

        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
        when(cardRepository.findByUserId(1L, pageable)).thenReturn(cardPage);
        when(cardMapper.toDTO(card1)).thenReturn(cardDTO1);
        when(cardMapper.toDTO(card2)).thenReturn(cardDTO2);

        Page<CardDTO> result = cardService.getMyCards(pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(cardDTO1, cardDTO2);
        verify(securityUtils).getCurrentUser();
        verify(cardRepository).findByUserId(1L, pageable);
    }

    @Test
    void getAllCardsWhenAdminExpectReturnAllCards() {
        User user1 = createUser(1L, "Kira", "kira@example.com");
        User user2 = createUser(2L, "Molly", "molly@example.com");
        Card card1 = createCard(1L, "1111222233334444", "KIRA", "12/25", user1);
        Card card2 = createCard(2L, "5555666677778888", "MOLLY", "12/26", user2);
        CardDTO cardDTO1 = createCardDTO(1L, 1L, "1111222233334444", "KIRA", "12/25");
        CardDTO cardDTO2 = createCardDTO(2L, 2L, "5555666677778888", "MOLLY", "12/26");

        Pageable pageable = PageRequest.of(0, 10);
        List<Card> cards = Arrays.asList(card1, card2);
        Page<Card> cardPage = new PageImpl<>(cards, pageable, cards.size());

        when(securityUtils.isAdmin()).thenReturn(true);
        when(cardRepository.findAll(pageable)).thenReturn(cardPage);
        when(cardMapper.toDTO(card1)).thenReturn(cardDTO1);
        when(cardMapper.toDTO(card2)).thenReturn(cardDTO2);

        Page<CardDTO> result = cardService.getAllCards(pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(cardDTO1, cardDTO2);
        verify(securityUtils).isAdmin();
        verify(cardRepository).findAll(pageable);
    }

    @Test
    void getAllCardsWhenNotAdminExpectThrowAccessDeniedException() {
        Pageable pageable = PageRequest.of(0, 10);

        when(securityUtils.isAdmin()).thenReturn(false);

        assertThatThrownBy(() -> cardService.getAllCards(pageable))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Admin access required");

        verify(cardRepository, never()).findAll(any(Pageable.class));    }

    @Test
    void deleteCardWhenCardExistsExpectDeleteCard() {
        User user = createUser(1L, "Kira", "kira@example.com");
        Card card = createCard(1L, "1234567812345678", "KIRA CHANG", "12/25", user);

        when(cardRepository.findByIdWithUser(1L)).thenReturn(Optional.of(card));
        when(securityUtils.hasAccessToUser(1L)).thenReturn(true);

        cardService.deleteCard(1L);

        verify(cardRepository).findByIdWithUser(1L);
        verify(securityUtils).hasAccessToUser(1L);
        verify(cardRepository).deleteById(1L);
    }

    @Test
    void deleteCardWhenNoAccessExpectThrowAccessDeniedException() {
        User otherUser = createUser(2L, "Other", "other@example.com");
        Card card = createCard(1L, "1234567812345678", "OTHER USER", "12/25", otherUser);

        when(cardRepository.findByIdWithUser(1L)).thenReturn(Optional.of(card));
        when(securityUtils.hasAccessToUser(2L)).thenReturn(false);

        assertThatThrownBy(() -> cardService.deleteCard(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You can only delete your own cards");

        verify(cardRepository, never()).deleteById(any());
    }

    @Test
    void deleteCardWhenCardNotExistsExpectThrowNotFoundException() {
        when(cardRepository.findByIdWithUser(444L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.deleteCard(444L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Card not found with id: 444");

        verify(cardRepository, never()).deleteById(any());
    }
}