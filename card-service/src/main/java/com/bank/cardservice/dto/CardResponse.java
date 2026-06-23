package com.bank.cardservice.dto;

public class CardResponse {

    private Long id;
    private String cardType;
    private Double limitAmount;
    private String cardNumber;
    private Boolean blocked;
    private String accountNumber;
    private String cardHolderName;
    private Long expiryDate;
    private String email;
    private Double usedAmount;
    private Double availableLimit;

    public CardResponse() {}

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public Double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(Double limitAmount) { this.limitAmount = limitAmount; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public Boolean getBlocked() { return blocked; }
    public void setBlocked(Boolean blocked) { this.blocked = blocked; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public Long getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Long expiryDate) { this.expiryDate = expiryDate; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Double getUsedAmount() { return usedAmount; }
    public void setUsedAmount(Double usedAmount) { this.usedAmount = usedAmount; }

    public Double getAvailableLimit() { return availableLimit; }
    public void setAvailableLimit(Double availableLimit) { this.availableLimit = availableLimit; }
}