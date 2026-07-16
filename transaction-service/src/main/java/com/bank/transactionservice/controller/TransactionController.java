package com.bank.transactionservice.controller;

import com.bank.transactionservice.entity.Transaction;
import com.bank.transactionservice.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
            @RequestBody Transaction transaction) {

        return ResponseEntity.ok(
                transactionService.save(transaction)
        );
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<Transaction>> getTransactions(
            @PathVariable String accountNumber) {

        return ResponseEntity.ok(
                transactionService.getTransactionsByAccountNumber(accountNumber)
        );
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<Transaction>> getTransactionsByUser(@PathVariable String email) {
        return ResponseEntity.ok(transactionService.getAllTransactionsForUser(email));
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAll() {

        return ResponseEntity.ok(
                transactionService.getAllTransactions()
        );
    }
}