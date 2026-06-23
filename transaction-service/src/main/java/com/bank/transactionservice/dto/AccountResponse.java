package com.bank.transactionservice.dto;

public class AccountResponse {

    private String accountNumber;
    private String email;

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}