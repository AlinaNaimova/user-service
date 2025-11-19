package com.microservices.user_service.repository;

import com.microservices.user_service.model.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId")
    Page<Card> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT * FROM card_info WHERE number = :number", nativeQuery = true)
    Optional<Card> findByNumberNative(@Param("number") String number);

    @Query("SELECT c FROM Card c JOIN FETCH c.user WHERE c.id = :id")
    Optional<Card> findByIdWithUser(@Param("id") Long id);
}
