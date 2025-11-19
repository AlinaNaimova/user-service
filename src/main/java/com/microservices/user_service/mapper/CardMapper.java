package com.microservices.user_service.mapper;

import com.microservices.user_service.dto.CardDTO;
import com.microservices.user_service.model.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardMapper {

    @Mapping(target = "userId", source = "user.id")
    CardDTO toDTO(Card card);

    @Mapping(target = "user", ignore = true)
    Card toEntity(CardDTO cardDTO);

    List<CardDTO> toDTOList(List<Card> cards);
}