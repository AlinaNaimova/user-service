package com.microservices.user_service.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDTOWithCards extends UserDTO {
    private static final long serialVersionUID = 2L;
    private List<CardDTO> cards = new ArrayList<>();
}
