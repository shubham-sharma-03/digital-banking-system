package com.bank.accountservice.repository;

import com.bank.accountservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountNumber(String accountNumber);

    List<Transaction> findByAccountNumberAndTransactionType(
            String accountNumber,
            String transactionType
    );

    List<Transaction> findByAccountNumberIn(
            List<String> accountNumbers
    );
}