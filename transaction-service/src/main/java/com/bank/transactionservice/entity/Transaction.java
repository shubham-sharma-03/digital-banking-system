package com.bank.transactionservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "transactionType")
    private String transactionType;

    @Column(name = "description")
    private String description;

    @Column(name = "balance_after")
    private Double balanceAfter;

    @Column(name = "reference_id")
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

        if (referenceId == null || referenceId.isEmpty()) {
            referenceId = "TXN" + System.currentTimeMillis();
        }
    }

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

    public void setId(Long id) {
        this.id = id;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setTransactionType(String transactiontype) {
        this.transactionType = transactiontype;
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