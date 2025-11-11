package com.microservices.user_service.controller;

import com.microservices.user_service.dto.CardDTO;
import com.microservices.user_service.service.CardService;
import com.microservices.user_service.service.UserSecurity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/card_info")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;
    private final UserSecurity userSecurity;

    @PostMapping
    public ResponseEntity<CardDTO> createCard(@Valid @RequestBody CardDTO cardDTO,
                                              Authentication authentication) {
        if (!userSecurity.checkCardAccess(authentication, cardDTO.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        CardDTO created = cardService.createCard(cardDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDTO> getCardById(@PathVariable Long id,
                                               Authentication authentication) {
        CardDTO card = cardService.getCardById(id);
        if (!userSecurity.checkCardAccess(authentication, card.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(card);
    }

    @GetMapping
    public ResponseEntity<Page<CardDTO>> getAllCards(Pageable pageable,
                                                     Authentication authentication) {
        if (!userSecurity.checkUserId(authentication, null)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Page<CardDTO> cards = cardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id,
                                           Authentication authentication) {
        CardDTO card = cardService.getCardById(id);
        if (!userSecurity.checkCardAccess(authentication, card.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}
