package com.bank.cardservice.client;

import lombok.Data;

@Data
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private String accountHolderName;
    private String email;
    private Double balance;
    private String accountType;
    private String branch;
    private String ifscCode;
    private String status;
}