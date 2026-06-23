package com.bank.emailservice.controller;

import com.bank.emailservice.dto.EmailRequest;
import com.bank.emailservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public String sendEmail(
            @RequestBody EmailRequest request) {

        System.out.println("EMAIL API HIT");

        emailService.sendEmail(
                request.getTo(),
                request.getSubject(),
                request.getBody()
        );

        return "Email Sent Successfully";
    }
}