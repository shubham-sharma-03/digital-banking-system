package com.bank.cardservice.repository;

import com.bank.cardservice.entity.CardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardTransactionRepository
        extends JpaRepository<CardTransaction, Long> {

    List<CardTransaction> findByCardNumber(String cardNumber);
}