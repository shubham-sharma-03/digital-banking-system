package com.bank.loanservice.service;
import com.bank.loanservice.dto.LoanRequest;
import com.bank.loanservice.entity.Loan;
import com.bank.loanservice.entity.LoanStatus;
import com.bank.loanservice.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;

    public Loan applyLoan(LoanRequest request) {

        double emi = calculateEMI(
                request.getLoanAmount(),
                10.5,
                request.getTenureMonths());

        Loan loan = Loan.builder()
                .customerId(request.getCustomerId())
                .loanAmount(request.getLoanAmount())
                .loanType(request.getLoanType())
                .interestRate(10.5)
                .tenureMonths(request.getTenureMonths())
                .emiAmount(emi)
                .status(LoanStatus.APPROVED)
                .applicationDate(LocalDate.now())
                .approvalDate(LocalDate.now())
                .nextDueDate(LocalDate.now().plusMonths(1))
                .build();

        return loanRepository.save(loan);


    }

    public Loan approveLoan(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow();

        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovalDate(LocalDate.now());

        return loanRepository.save(loan);
    }

    public List<Loan> getCustomerLoans(Long customerId) {

        return loanRepository.findByCustomerId(customerId);
    }

    private double calculateEMI(
            double principal,
            double annualRate,
            int tenureMonths) {

        double monthlyRate = annualRate / 12 / 100;

        return (principal * monthlyRate *
                Math.pow(1 + monthlyRate, tenureMonths))
                /
                (Math.pow(1 + monthlyRate, tenureMonths) - 1);
    }



}