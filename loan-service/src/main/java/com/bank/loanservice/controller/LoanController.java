package com.bank.loanservice.controller;

import com.bank.loanservice.dto.LoanRequest;
import com.bank.loanservice.entity.Loan;
import com.bank.loanservice.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:63342")
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/apply")
    public ResponseEntity<Loan> applyLoan(
            @RequestBody LoanRequest request) {

        return ResponseEntity.ok(
                loanService.applyLoan(request));
    }

    @PutMapping("/approve/{loanId}")
    public ResponseEntity<Loan> approveLoan(
            @PathVariable Long loanId) {

        return ResponseEntity.ok(
                loanService.approveLoan(loanId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Loan>> getLoans(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                loanService.getCustomerLoans(customerId));
    }
}