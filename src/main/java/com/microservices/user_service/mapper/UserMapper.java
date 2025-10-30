package com.microservices.user_service.mapper;

import com.microservices.user_service.dto.UserDTO;
import com.microservices.user_service.dto.UserDTOWithCards;
import com.microservices.user_service.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = CardMapper.class)
public interface UserMapper {

    UserDTOWithCards toDTOWithCards(User user);

    UserDTO toDTO(User user);

    User toEntity(UserDTO userDTO);
}
