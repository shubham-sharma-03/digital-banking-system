package com.bank.accountservice.controller;

import com.bank.accountservice.dto.TransactionRequest;
import com.bank.accountservice.entity.Account;
import com.bank.accountservice.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
@CrossOrigin(origins = "*")   // allow frontend on any port during dev
public class AccountController {

    @Autowired
    private AccountService accountService;

    // ── GET ALL  (frontend: GET /accounts) ──
    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    // ── GET by Account Number ──
    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByAccountNumber(accountNumber));
    }

    // ── GET by Email  (frontend filter fallback) ──
    @GetMapping("/email/{email}")
    public ResponseEntity<Account> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(accountService.getAccountByEmail(email));
    }

    // ── GET STATEMENT ──
    @GetMapping("/{accountNumber}/statement")
    public ResponseEntity<List<TransactionRequest>> getStatement(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getStatement(accountNumber));
    }

    // ── CREATE ACCOUNT  (frontend: POST /accounts) ──
    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody Account account) {
        try {
            Account created = accountService.createAccount(account);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── UPDATE ──
    @PutMapping("/{accountNumber}")
    public ResponseEntity<?> updateAccount(@PathVariable String accountNumber,
                                           @RequestBody Account account) {
        try {
            return ResponseEntity.ok(accountService.updateAccount(accountNumber, account));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── DELETE ──
    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<?> deleteAccount(@PathVariable String accountNumber) {
        try {
            accountService.deleteAccount(accountNumber);
            return ResponseEntity.ok(Map.of("message", "Account deleted: " + accountNumber));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── DEPOSIT  (POST /accounts/{accountNumber}/deposit  body: {"amount": 5000}) ──
    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<?> deposit(@PathVariable String accountNumber,
                                     @RequestBody Map<String, Double> body) {
        try {
            Double amount = body.get("amount");
            return ResponseEntity.ok(accountService.deposit(accountNumber, amount));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── WITHDRAW  (POST /accounts/{accountNumber}/withdraw  body: {"amount": 1000}) ──
    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<?> withdraw(@PathVariable String accountNumber,
                                      @RequestBody Map<String, Double> body) {
        try {
            Double amount = body.get("amount");
            return ResponseEntity.ok(accountService.withdraw(accountNumber, amount));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── TRANSFER  (POST /accounts/transfer  body: {"fromAccountNumber":"ACC...","toAccountNumber":"ACC...","amount":500}) ──
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody Map<String, Object> body) {
        try {
            String from   = (String) body.get("fromAccountNumber");
            String to     = (String) body.get("toAccountNumber");
            Double amount = ((Number) body.get("amount")).doubleValue();
            String result = accountService.transfer(from, to, amount);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            
        }
    }
}
