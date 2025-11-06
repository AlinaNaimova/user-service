package com.microservices.user_service.service;

import com.microservices.user_service.dto.CardDTO;
import com.microservices.user_service.exception.DuplicateResourceException;
import com.microservices.user_service.exception.NotFoundException;
import com.microservices.user_service.mapper.CardMapper;
import com.microservices.user_service.model.Card;
import com.microservices.user_service.model.User;
import com.microservices.user_service.repository.CardRepository;
import com.microservices.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;

    @Transactional
    public CardDTO createCard(CardDTO cardDTO) {
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
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Card", id));
        return cardMapper.toDTO(card);
    }

    @Transactional(readOnly = true)
    public Page<CardDTO> getAllCards(Pageable pageable) {
        Page<Card> cardsPage = cardRepository.findAll(pageable);
        return cardsPage.map(cardMapper::toDTO);
    }

    @Transactional
    public void deleteCard(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new NotFoundException("Card", id);
        }
        cardRepository.deleteById(id);
    }
}