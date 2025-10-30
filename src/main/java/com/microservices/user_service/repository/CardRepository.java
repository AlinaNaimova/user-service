package com.microservices.user_service.repository;

import com.microservices.user_service.model.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByUserId(Long userId);
    Optional<Card> findByNumber(String number);

    @Query("SELECT c FROM Card c JOIN c.user u WHERE u.email = :email")
    List<Card> findCardsByUserEmail(@Param("email") String email);

    @Query(value = "SELECT * FROM card_info WHERE number = :number", nativeQuery = true)
    Optional<Card> findByNumberNative(@Param("number") String number);

    Page<Card> findAll(Pageable pageable);
}
