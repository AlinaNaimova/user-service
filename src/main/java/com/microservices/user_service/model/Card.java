package com.microservices.user_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "card_info")
@Data
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String number;
    private String holder;

    @Column(name = "expiration_date")
    private String expirationDate;

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : "null") +
                ", number='" + number + '\'' +
                ", holder='" + holder + '\'' +
                ", expirationDate='" + expirationDate + '\'' +
                '}';
    }
}
