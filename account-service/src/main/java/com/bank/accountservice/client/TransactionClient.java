package com.bank.accountservice.client;

import com.bank.accountservice.dto.TransactionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "transaction-service",
        url = "http://localhost:8084")
public interface TransactionClient {

    @PostMapping("/transactions")
    void createTransaction(@RequestBody TransactionRequest request);

    @GetMapping("/transactions/account/{accountNumber}")
    List<TransactionRequest> getTransactionsByAccountNumber(@PathVariable String accountNumber);
}