package com.bank.transactionservice.client;

import com.bank.transactionservice.dto.AccountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "account-service", url = "http://localhost:8081")
public interface AccountClient {

    @GetMapping("/accounts/by-email")
    List<AccountResponse> getAccountsByEmail(@RequestParam("email") String email);
}