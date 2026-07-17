package com.bank.authservice.feign;

import com.bank.authservice.dto.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "email-service",
        url = "${EMAIL_SERVICE_URL:https://email-service-lazp.onrender.com}", // fallback URL
        fallback = EmailClient.EmailClientFallback.class
)
public interface EmailClient {

    @PostMapping("/email/send")
    String sendEmail(@RequestBody EmailRequest request);

    @Component
    class EmailClientFallback implements EmailClient {
        @Override
        public String sendEmail(EmailRequest request) {
            System.out.println("Email service fallback: skipping email to " + request.getTo());
            return "EMAIL_SERVICE_UNAVAILABLE";
        }
    }
}