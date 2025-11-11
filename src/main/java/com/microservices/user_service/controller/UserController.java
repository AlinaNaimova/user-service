package com.microservices.user_service.controller;

import com.microservices.user_service.config.UserPrincipal;
import com.microservices.user_service.dto.UserDTO;
import com.microservices.user_service.dto.UserDTOWithCards;
import com.microservices.user_service.service.UserSecurity;
import com.microservices.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserSecurity userSecurity;

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        UserDTO created = userService.create(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long userId = principal.getUserId();
        String email = principal.getEmail();

        UserDTO user = userService.getOrCreateUser(userId, email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me/with-cards")
    public ResponseEntity<UserDTOWithCards> getCurrentUserWithCards(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long userId = principal.getUserId();

        UserDTOWithCards user = userService.getUserWithCardsById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id,
                                               Authentication authentication) {
        if (!userSecurity.checkUserId(authentication, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        UserDTO user = userService.getById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/with-cards")
    public ResponseEntity<UserDTOWithCards> getUserWithCardsById(@PathVariable Long id,
                                                                 Authentication authentication) {
        if (!userSecurity.checkUserId(authentication, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.getUserWithCardsById(id));
    }

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable,
                                                     Authentication authentication) {
        if (!userSecurity.checkUserId(authentication, null)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Page<UserDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email,
                                                  Authentication authentication) {
        if (!userSecurity.checkUserId(authentication, null)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        UserDTO user = userService.getByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id,
                                              @Valid @RequestBody UserDTO userDTO,
                                              Authentication authentication) {
        if (!userSecurity.checkUserId(authentication, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        UserDTO updated = userService.update(id, userDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           Authentication authentication) {
        if (!userSecurity.checkUserId(authentication, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}