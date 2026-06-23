package com.bank.transactionservice.repository;

import com.bank.transactionservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountNumber(String accountNumber);

    List<Transaction> findByAccountNumberAndTransactionType(
            String accountNumber,
            String transactionType
    );

    List<Transaction> findByAccountNumberIn(
            List<String> accountNumbers
    );
}