package com.bank.cardservice.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/accounts")
public interface AccountClient {

    @GetExchange("/{accountNumber}")
    AccountResponse getAccountByNumber(@PathVariable String accountNumber);
}