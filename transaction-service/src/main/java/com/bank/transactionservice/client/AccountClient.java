package com.bank.transactionservice.client;

import com.bank.transactionservice.dto.AccountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "account-service")
public interface AccountClient {

    @GetMapping("/accounts/email/{email}")
    AccountResponse getAccountByEmail(@PathVariable("email") String email);

    // ADD THIS: Get all accounts for a user
    @GetMapping("/accounts/user/{email}")
    List<AccountResponse> getAccountsByEmail(@PathVariable("email") String email);
}