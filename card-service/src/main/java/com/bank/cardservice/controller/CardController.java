package com.bank.cardservice.controller;

import com.bank.cardservice.dto.CardRequest;
import com.bank.cardservice.dto.CardResponse;
import com.bank.cardservice.dto.CardTransactionRequest;
import com.bank.cardservice.entity.Card;
import com.bank.cardservice.entity.CardTransaction;
import com.bank.cardservice.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(
        origins = {"http://localhost:8081", "http://localhost:9090", "http://localhost:3000"},
        allowedHeaders = "*",
        allowCredentials = "false"
)
public class CardController {

    @Autowired
    private CardService service;

    @PostMapping
    public Card create(@RequestBody Card card) {
        return service.save(card);
    }

    @GetMapping
    public List<Card> getAllCards() {
        return service.getAllCards();
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<?> getByAccountNumber(@PathVariable String accountNumber) {
        try {
            System.out.println("=== FETCHING CARDS FOR: " + accountNumber + " ===");
            List<CardResponse> cards = service.getCardsByAccountNumber(accountNumber);
            System.out.println("=== FOUND " + cards.size() + " CARDS ===");
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            System.err.println("=== ERROR: " + e.getMessage() + " ===");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/account/{accountNumber}/count")
    public ResponseEntity<?> getCardCount(@PathVariable String accountNumber) {
        try {
            List<CardResponse> cards = service.getCardsByAccountNumber(accountNumber);
            return ResponseEntity.ok(Collections.singletonMap("count", cards.size()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.getCardById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public Card update(@PathVariable Long id, @RequestBody Card card) {
        return service.updateCard(id, card);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.deleteCard(id);
        return "Card Deleted Successfully";
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyCard(@RequestBody Map<String, Object> body) {
        String accountNumber = (String) body.get("accountNumber");

        System.out.println("========== AUTO APPLY CARD ==========");
        System.out.println("Account: " + accountNumber);
        System.out.println("Full body: " + body);

        if (accountNumber == null || accountNumber.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "accountNumber is required"));
        }

        try {
            // Auto-generate card details
            CardRequest request = new CardRequest();
            request.setAccountNumber(accountNumber);
            request.setCardHolderName(body.containsKey("cardHolderName") ? (String) body.get("cardHolderName") : "Shubham Sharma");
            request.setCardType(body.containsKey("cardType") ? (String) body.get("cardType") : "VISA");
            request.setCardNumber(body.containsKey("cardNumber") ? (String) body.get("cardNumber") : generateCardNumber());
            request.setEmail(body.containsKey("email") ? (String) body.get("email") : "shubham@gmail.com");

            Card card = service.applyCard(request);

            System.out.println("CARD AUTO-GENERATED SUCCESSFULLY");
            return ResponseEntity.ok(Map.of(
                    "message", "Card applied successfully",
                    "cardId", card.getId(),
                    "cardNumber", card.getCardNumber()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Helper to generate card number
    private String generateCardNumber() {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder("4");
        for (int i = 0; i < 15; i++) {
            sb.append(rnd.nextInt(10));
        }
        return sb.toString();
    }

    @PostMapping("/block/{id}")
    public ResponseEntity<?> blockCard(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(Collections.singletonMap("message", service.blockCard(id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/unblock/{id}")
    public ResponseEntity<?> unblockCard(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(Collections.singletonMap("message", service.unblockCard(id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/{cardId}/transactions")
    public ResponseEntity<CardTransaction> createCardTransaction(
            @PathVariable Long cardId,
            @RequestBody CardTransactionRequest request) {
        return ResponseEntity.ok(service.createTransaction(cardId, request));
    }

    @GetMapping("/{cardId}/transactions")
    public ResponseEntity<?> getTransactions(@PathVariable Long cardId) {
        Card card = service.getCardById(cardId);
        System.out.println("CARD NUMBER = " + card.getCardNumber());

        List<CardTransaction> transactions = service.getCardTransactions(card.getCardNumber());
        System.out.println("TXN COUNT = " + transactions.size());

        return ResponseEntity.ok(transactions);
    }
}