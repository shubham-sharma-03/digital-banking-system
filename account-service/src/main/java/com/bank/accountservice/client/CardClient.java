package com.bank.accountservice.client;

import com.bank.accountservice.dto.CardRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "card-service",
        url = "http://localhost:8083"
)
public interface CardClient {

    @PostMapping("/api/cards")
    void createCard(
            @RequestBody CardRequest request
    );
}