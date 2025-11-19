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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final SecurityUtils securityUtils; // Добавляем

    @Transactional
    public CardDTO createCard(CardDTO cardDTO) {
        if (!securityUtils.hasAccessToUser(cardDTO.getUserId())) {
            throw new AccessDeniedException("You can only create cards for yourself");
        }
        User user = userRepository.findById(cardDTO.getUserId())
                .orElseThrow(() -> new NotFoundException("User", cardDTO.getUserId()));
        if (cardRepository.findByNumberNative(cardDTO.getNumber()).isPresent()) {
            throw new DuplicateResourceException("Card", "number", cardDTO.getNumber());
        }
        Card card = cardMapper.toEntity(cardDTO);
        card.setUser(user);
        Card savedCard = cardRepository.save(card);
        return cardMapper.toDTO(savedCard);
    }

    @Transactional(readOnly = true)
    public CardDTO getCardById(Long id) {
        Card card = cardRepository.findByIdWithUser(id)
                .orElseThrow(() -> new NotFoundException("Card", id));

        if (!securityUtils.hasAccessToUser(card.getUser().getId())) {
            throw new AccessDeniedException("Access denied to this card");
        }
        return cardMapper.toDTO(card);
    }

    @Transactional(readOnly = true)
    public Page<CardDTO> getMyCards(Pageable pageable) {
        Long currentUserId = securityUtils.getCurrentUser().getId();
        return cardRepository.findByUserId(currentUserId, pageable)
                .map(cardMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<CardDTO> getAllCards(Pageable pageable) {
        if (!securityUtils.isAdmin()) {
            throw new AccessDeniedException("Admin access required");
        }
        return cardRepository.findAll(pageable)
                .map(cardMapper::toDTO);
    }

    @Transactional
    public void deleteCard(Long id) {
        Card card = cardRepository.findByIdWithUser(id)
                .orElseThrow(() -> new NotFoundException("Card", id));

        if (!securityUtils.hasAccessToUser(card.getUser().getId())) {
            throw new AccessDeniedException("You can only delete your own cards");
        }

        cardRepository.deleteById(id);
    }
}