package com.bank.accountservice.client;

import com.bank.accountservice.dto.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "email-service")
public interface EmailClient {

    @PostMapping("/email/send")
    String sendEmail(@RequestBody EmailRequest request);
}
