package com.bank.cardservice.controller;

import com.bank.cardservice.dto.CardRequest;
import com.bank.cardservice.dto.CardResponse;
import com.bank.cardservice.dto.PinRequest;
import com.bank.cardservice.entity.Card;
import com.bank.cardservice.entity.CardTransaction;
import com.bank.cardservice.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "*")
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
    public ResponseEntity<?> applyCard(@RequestBody CardRequest request) {
        try {
            return ResponseEntity.ok(service.applyCard(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/block/{id}")
    public ResponseEntity<?> blockCard(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(Collections.singletonMap("message", service.blockCard(id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", e.getMessage()));
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

    @GetMapping("/{cardId}/transactions")
    public ResponseEntity<?> getTransactions(
            @PathVariable Long cardId) {

        Card card = service.getCardById(cardId);

        System.out.println(
                "CARD NUMBER = "
                        + card.getCardNumber()
        );

        List<CardTransaction> transactions =
                service.getCardTransactions(
                        card.getCardNumber());

        System.out.println(
                "TXN COUNT = "
                        + transactions.size()
        );

        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/{id}/pin")
    public ResponseEntity<?> changePin(
            @PathVariable Long id,
            @RequestBody PinRequest request) {

        try {

            String result =
                    service.changePin(id, request.getPin());

            return ResponseEntity.ok(
                    Collections.singletonMap(
                            "message",
                            result
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(
                    Collections.singletonMap(
                            "error",
                            e.getMessage()
                    )
            );
        }
    }

}
