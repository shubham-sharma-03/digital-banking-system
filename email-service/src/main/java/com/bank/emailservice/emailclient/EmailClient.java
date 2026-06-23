package com.bank.emailservice.emailclient;

import com.bank.emailservice.dto.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "email-service",
        url = "http://localhost:8086"
)
public interface EmailClient {

    @PostMapping("/email/send")
    String sendEmail(
            @RequestBody EmailRequest request
    );
}