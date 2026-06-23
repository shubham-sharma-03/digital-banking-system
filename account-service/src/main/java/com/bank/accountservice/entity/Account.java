package com.bank.accountservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "accounts",
        uniqueConstraints = @UniqueConstraint(columnNames = "email", name = "uk_email"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @Column(name = "account_number", length = 25, nullable = false, updatable = false)
    private String accountNumber;  // e.g., "ACC000000000001"

    @Column(nullable = false)
    private String accountHolderName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String accountType;  // SAVINGS or CURRENT

    @Column(nullable = false)
    private Double balance;

    @Column
    private String branch;

    @Column
    private String ifscCode;

    @PrePersist
    public void prePersist() {
        if (this.branch == null || this.branch.isEmpty()) {
            this.branch = "Mumbai Main";
        }
        if (this.balance == null) {
            this.balance = 0.0;


            if (this.ifscCode == null || this.ifscCode.isEmpty()) {
                this.ifscCode = "MYBK0001234";
            }
        }

    }
}