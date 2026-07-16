package com.bank.cardservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardTransactionRequest {
    private String merchant;
    private String category;
    private BigDecimal amount;
    private LocalDate transactionDate;
}