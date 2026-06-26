package com.bank.accountservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "loan-service", url = "http://localhost:8082")
public interface LoanClient {

    @GetMapping("/loans")
    Object getLoans();


}