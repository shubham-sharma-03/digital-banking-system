package com.bank.accountservice.client;

import com.bank.accountservice.dto.CardRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "card-service")
public interface CardClient {

    @PostMapping("/api/cards")
    void createCard(
            @RequestBody CardRequest request
    );
}