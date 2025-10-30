package com.microservices.user_service.service;

import com.microservices.user_service.dto.CardDTO;
import com.microservices.user_service.dto.UserDTO;
import com.microservices.user_service.dto.UserDTOWithCards;
import com.microservices.user_service.exception.DuplicateResourceException;
import com.microservices.user_service.exception.NotFoundException;
import com.microservices.user_service.mapper.UserMapper;
import com.microservices.user_service.model.User;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User createUser(Long id, String name, String surname, String email, LocalDate birthDate) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setBirthDate(birthDate);
        return user;
    }

    private UserDTO createUserDTO(Long id, String name, String surname, String email, LocalDate birthDate) {
        return new UserDTO(id, name, surname, birthDate, email);
    }

    private CardDTO createCardDTO(Long id, Long userId, String number, String holder, String expirationDate) {
        CardDTO card = new CardDTO();
        card.setId(id);
        card.setUserId(userId);
        card.setNumber(number);
        card.setHolder(holder);
        card.setExpirationDate(expirationDate);
        return card;
    }

    private UserDTOWithCards createUserDTOWithCards(Long id, String name, String surname, String email,
                                                    LocalDate birthDate, List<CardDTO> cards) {
        UserDTOWithCards dto = new UserDTOWithCards();
        dto.setId(id);
        dto.setName(name);
        dto.setSurname(surname);
        dto.setEmail(email);
        dto.setBirthDate(birthDate);
        dto.setCards(cards != null ? cards : Collections.emptyList());
        return dto;
    }

    @Test
    void getByIdWhenUserExistsExpectReturnUserDTO() {
        User user = createUser(1L, "Kira", "Chang", "kira.chang@example.com",
                LocalDate.of(1990, 1, 1));
        UserDTO userDTO = createUserDTO(1L, "Kira", "Chang", "kira.chang@example.com",
                LocalDate.of(1990, 1, 1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO testUser = userService.getById(1L);

        assertThat(testUser).isNotNull();
        assertThat(testUser.getId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
        verify(userMapper).toDTO(user);
    }

    @Test
    void getByIdWhenUserNotExistsExpectThrowNotFoundException() {
        when(userRepository.findById(134L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(134L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found with id: 134");

        verify(userRepository).findById(134L);
        verify(userMapper, never()).toDTO(any());
    }

    @Test
    void getUserWithCardsByIdWhenUserExistsWithCardsExpectReturnUserDTOWithCards() {
        User user = createUser(1L, "Kira", "Chang", "kira.chang@example.com",
                LocalDate.of(1990, 1, 1));

        CardDTO card1 = createCardDTO(1L, 1L, "1234567812345678", "KIRA CHANG", "12/25");
        CardDTO card2 = createCardDTO(2L, 1L, "8765432187654321", "KIRA CHANG", "06/24");
        List<CardDTO> cards = Arrays.asList(card1, card2);

        UserDTOWithCards userDTOWithCards = createUserDTOWithCards(1L, "Kira", "Chang",
                "kira.chang@example.com", LocalDate.of(1990, 1, 1), cards);

        when(userRepository.findByIdWithCards(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDTOWithCards(user)).thenReturn(userDTOWithCards);

        UserDTOWithCards testUser = userService.getUserWithCardsById(1L);

        assertThat(testUser).isNotNull();
        assertThat(testUser.getId()).isEqualTo(1L);
        assertThat(testUser.getCards()).hasSize(2);
        assertThat(testUser.getCards().get(0).getNumber()).isEqualTo("1234567812345678");
        assertThat(testUser.getCards().get(0).getExpirationDate()).isEqualTo("12/25");
        verify(userRepository).findByIdWithCards(1L);
        verify(userMapper).toDTOWithCards(user);
    }


    @Test
    void getByEmailWhenUserExistsExpectReturnUserDTO() {
        User user = createUser(1L, "Kira", "Chang", "kira.chang@example.com",
                LocalDate.of(1990, 1, 1));
        UserDTO userDTO = createUserDTO(1L, "Kira", "Chang", "kira.chang@example.com",
                LocalDate.of(1990, 1, 1));

        when(userRepository.findByEmail("kira.chang@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.getByEmail("kira.chang@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("kira.chang@example.com");
        verify(userRepository).findByEmail("kira.chang@example.com");
        verify(userMapper).toDTO(user);
    }


    @Test
    void createWithValidUserExpectSaveAndReturnUserDTO() {
        UserDTO createUserDTO = createUserDTO(null, "Molly", "Bing", "molly.bing@example.com",
                LocalDate.of(1990, 5, 15));
        User newUser = createUser(null, "Molly", "Bing", "molly.bing@example.com",
                LocalDate.of(1990, 5, 15));
        User savedUser = createUser(2L, "Molly", "Bing", "molly.bing@example.com",
                LocalDate.of(1990, 5, 15));
        UserDTO expectedDTO = createUserDTO(2L, "Molly", "Bing", "molly.bing@example.com",
                LocalDate.of(1990, 5, 15));

        when(userRepository.findByEmailNative("molly.bing@example.com")).thenReturn(Optional.empty());
        when(userMapper.toEntity(createUserDTO)).thenReturn(newUser);
        when(userRepository.save(newUser)).thenReturn(savedUser);
        when(userMapper.toDTO(savedUser)).thenReturn(expectedDTO);

        UserDTO testUser = userService.create(createUserDTO);

        assertThat(testUser).isNotNull();
        assertThat(testUser.getId()).isEqualTo(2L);
        verify(userRepository).findByEmailNative("molly.bing@example.com");
        verify(userRepository).save(newUser);
        verify(userMapper).toDTO(savedUser);
    }

    @Test
    void createWithDuplicateEmailExpectThrowDuplicateResourceException() {
        UserDTO createUserDTO = createUserDTO(null, "Kira", "Chang", "kira.chang@example.com",
                LocalDate.of(1990, 1, 1));
        User existingUser = createUser(1L, "Kira", "Chang", "kira.chang@example.com",
                LocalDate.of(1990, 1, 1));

        when(userRepository.findByEmailNative("kira.chang@example.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.create(createUserDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("User already exists with email: kira.chang@example.com");

        verify(userRepository).findByEmailNative("kira.chang@example.com");
        verify(userMapper, never()).toEntity(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateWhenUserExistsExpectUpdateAndReturnUserDTO() {
        User existingUser = createUser(1L, "Kira", "Chang", "kira.chang@example.com",
                LocalDate.of(1990, 1, 1));
        UserDTO updateData = createUserDTO(null, "Kira Updated", "Chang Updated", "kira.chang@example.com",
                LocalDate.of(1995, 6, 15));
        UserDTO expectedResult = createUserDTO(1L, "Kira Updated", "Chang Updated", "kira.chang@example.com",
                LocalDate.of(1995, 6, 15));

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toDTO(existingUser)).thenReturn(expectedResult);

        UserDTO testUser = userService.update(1L, updateData);

        assertThat(testUser).isNotNull();
        assertThat(testUser.getName()).isEqualTo("Kira Updated");
        assertThat(testUser.getEmail()).isEqualTo("kira.chang@example.com");
        verify(userRepository).findById(1L);
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateWhenUserExistsAndEmailChangedToTakenExpectThrowDuplicateResourceException() {
        User existingUser = createUser(1L, "Kira", "Chang", "kira.chang@example.com",
                LocalDate.of(1990, 1, 1));
        UserDTO updateData = createUserDTO(null, "Kira", "Chang", "taken@example.com",
                LocalDate.of(1990, 1, 1));
        User conflictingUser = createUser(2L, "Other", "User", "taken@example.com",
                LocalDate.of(1990, 1, 1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(conflictingUser));

        assertThatThrownBy(() -> userService.update(1L, updateData))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("User already exists with email: taken@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateWhenUserNotExistsExpectThrowNotFoundException() {
        UserDTO updateData = createUserDTO(null, "Kira", "Chang", "kira@example.com",
                LocalDate.of(1990, 1, 1));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(999L, updateData))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository, never()).save(any());
        verify(userRepository, never()).findByEmail(anyString());
    }


    @Test
    void deleteByIdWhenUserExistsExpectDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteById(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteByIdWhenUserNotExistsExpectThrowNotFoundException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void getAllUsersWhenUsersExistExpectReturnPageOfUserDTO() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = createUser(1L, "Kira", "Chang", "kira.chang@example.com",
                LocalDate.of(1990, 1, 1));
        UserDTO userDTO = createUserDTO(1L, "Kira", "Chang", "kira.chang@example.com",
                LocalDate.of(1990, 1, 1));
        List<User> users = Arrays.asList(user);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        Page<UserDTO> testPage = userService.getAllUsers(pageable);

        assertThat(testPage).isNotNull();
        assertThat(testPage.getTotalElements()).isEqualTo(1);
        assertThat(testPage.getContent().get(0)).isEqualTo(userDTO);
        verify(userRepository).findAll(pageable);
    }
}