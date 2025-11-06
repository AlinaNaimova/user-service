package com.microservices.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO implements Serializable {
    private static final long serialVersionUID = 3L;

    private Long id;

    @NotNull(message = "User ID is mandatory")
    private Long userId;

    @NotBlank(message = "Card number is mandatory")
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    private String number;

    @NotBlank(message = "Card holder is mandatory")
    private String holder;

    @Pattern(regexp = "(0[1-9]|1[0-2])/[0-9]{2}", message = "Expiration date must be in format MM/YY")
    private String expirationDate;
}