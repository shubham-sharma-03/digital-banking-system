package com.bank.transactionservice.service;

import com.bank.transactionservice.client.AccountClient;
import com.bank.transactionservice.dto.AccountResponse;
import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@CrossOrigin(origins = "http://localhost:63342")
@Service
public class TransactionService {
    @Autowired
    private TransactionRepository repository;

    @Autowired
    private AccountClient accountClient;

    // ── SAVE / CREATE ──
    @Transactional
    public Transaction save(Transaction transaction) {
        if (transaction == null) {
            throw new RuntimeException("Transaction cannot be null");
        }
        if (transaction.getAmount() == null || transaction.getAmount() <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }
        if (transaction.getAccountNumber() == null || transaction.getAccountNumber().isBlank()) {
            throw new RuntimeException("Account number is required");
        }
        if (transaction.getTransactionType() == null || transaction.getTransactionType().isBlank()) {
            throw new RuntimeException("Transaction type is required");
        }

        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDateTime.now());
        }
        if (transaction.getReferenceId() == null || transaction.getReferenceId().isBlank()) {
            transaction.setReferenceId("TXN" + System.currentTimeMillis());
        }

        Transaction saved = repository.save(transaction);
        System.out.println("[TransactionService] Saved: ID=" + saved.getId()
                + ", Type=" + saved.getTransactionType()
                + ", Account=" + saved.getAccountNumber()
                + ", Amount=" + saved.getAmount());
        return saved;
    }

    // ── GET ALL ──
    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

    // ── GET BY ID ──
    public Transaction getTransactionById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction Not Found with ID: " + id));
    }

    // ── GET BY ACCOUNT NUMBER (single account) ──
    public List<Transaction> getTransactionsByAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new RuntimeException("Account number is required");
        }
        return repository.findByAccountNumber(accountNumber);
    }

    // ── GET BY ACCOUNT NUMBERS (multiple accounts) ──
    public List<Transaction> getTransactionsByAccountNumbers(List<String> accountNumbers) {
        if (accountNumbers == null || accountNumbers.isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findByAccountNumberIn(accountNumbers);
    }

    // ── UPDATE ──
    @Transactional
    public Transaction updateTransaction(Long id, Transaction transaction) {
        Transaction existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction Not Found with ID: " + id));

        if (transaction.getAmount() != null && transaction.getAmount() > 0) {
            existing.setAmount(transaction.getAmount());
        }
        if (transaction.getTransactionType() != null
                && !transaction.getTransactionType().isBlank()) {

            existing.setTransactionType(
                    transaction.getTransactionType()
            );
        }
        if (transaction.getDescription() != null) {
            existing.setDescription(transaction.getDescription());
        }
        if (transaction.getBalanceAfter() != null) {
            existing.setBalanceAfter(transaction.getBalanceAfter());
        }

        return repository.save(existing);
    }

    // ── DELETE ──
    @Transactional
    public void deleteTransaction(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Transaction Not Found with ID: " + id);
        }
        repository.deleteById(id);
    }

    // ── GET ALL TRANSACTIONS FOR USER (by email -> accounts -> transactions) ──
    public List<Transaction> getAllTransactionsForUser(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        List<AccountResponse> accounts;

        try {
            accounts = accountClient.getAccountsByEmail(email);
        } catch (Exception e) {
            System.err.println("[TransactionService] Could not reach account-service: " + e.getMessage());
            return Collections.emptyList();
        }

        if (accounts == null || accounts.isEmpty()) {
            System.out.println("[TransactionService] No accounts found for email: " + email);
            return Collections.emptyList();
        }

        List<String> accountNumbers = accounts.stream()
                .map(AccountResponse::getAccountNumber)
                .toList();

        System.out.println("[TransactionService] Found accounts: " + accountNumbers);

        return repository.findByAccountNumberIn(accountNumbers);
    }
}