package com.microservices.user_service.controller;

import com.microservices.user_service.dto.CardDTO;
import com.microservices.user_service.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/card_info")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @PostMapping
    public ResponseEntity<CardDTO> createCard(@Valid @RequestBody CardDTO cardDTO) {
        CardDTO created = cardService.createCard(cardDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDTO> getCardById(@PathVariable Long id) {
        CardDTO card = cardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<CardDTO>> getMyCards(Pageable pageable) {
        Page<CardDTO> cards = cardService.getMyCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardDTO>> getAllCards(Pageable pageable) {
        Page<CardDTO> cards = cardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}