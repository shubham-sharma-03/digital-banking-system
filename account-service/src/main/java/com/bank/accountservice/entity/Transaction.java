package com.bank.accountservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "balance_after")
    private Double balanceAfter;

    @Column(name = "reference_id", length = 50)
    private String referenceId;


    @Column(name = "transaction_type")
    private String transactionType;

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    @PrePersist
    public void prePersist() {

        if (this.transactionDate == null) {
            this.transactionDate = LocalDateTime.now();
        }

        if (this.referenceId == null) {
            this.referenceId =
                    "TXN" + System.currentTimeMillis();
        }
    }
}