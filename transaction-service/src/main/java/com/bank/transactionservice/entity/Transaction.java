package com.bank.transactionservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String mode;

    @Column(length = 300)
    private String description;

    @Column(name = "balance_after")
    private Double balanceAfter;

    @Column(name = "reference_id", unique = true)
    private String referenceId;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    public Transaction() {
    }

    @PrePersist
    public void prePersist() {

        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }

        if (referenceId == null || referenceId.isBlank()) {
            referenceId = "TXN" + System.currentTimeMillis();
        }

        if (status == null || status.isBlank()) {
            status = "SUCCESS";
        }

        if (mode == null || mode.isBlank()) {
            mode = "BANKING";
        }
    }

    // ---------------- Getters ----------------

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Double getAmount() {
        return amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public String getStatus() {
        return status;
    }

    public String getMode() {
        return mode;
    }

    public String getDescription() {
        return description;
    }

    public Double getBalanceAfter() {
        return balanceAfter;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    // ---------------- Setters ----------------

    public void setId(Long id) {
        this.id = id;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBalanceAfter(Double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
}