package com.bank.cardservice.repository;

import com.bank.cardservice.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByAccountNumber(String accountNumber);

    @Query(value =
            "SELECT * FROM cards WHERE account_number = :accountNumber",
            nativeQuery = true)
    List<Card> findCardsNative(@Param("accountNumber") String accountNumber);
}