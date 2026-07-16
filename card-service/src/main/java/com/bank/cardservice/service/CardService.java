package com.bank.cardservice.service;

import com.bank.cardservice.client.AccountClient;
import com.bank.cardservice.client.AccountResponse;
import com.bank.cardservice.dto.CardRequest;
import com.bank.cardservice.dto.CardResponse;
import com.bank.cardservice.dto.CardTransactionRequest;
import com.bank.cardservice.entity.Card;
import com.bank.cardservice.entity.CardTransaction;
import com.bank.cardservice.repository.CardRepository;
import com.bank.cardservice.repository.CardTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardTransactionRepository cardTransactionRepository;

    @Autowired
    private AccountClient accountClient;

    // ── CREATE CARD (called by account-service) ──
    public Card createCard(Card card) {
        String accountNumber = card.getAccountNumber();
        if (cardRepository.findByAccountNumber(accountNumber).size() >= 3) {
            throw new RuntimeException("Maximum 3 cards allowed");
        }
        return cardRepository.save(card);
    }

    // ── SAVE / UPDATE CARD ──
    public Card save(Card card) {
        if (card.getExpiryDate() == null) {
            long expiry = System.currentTimeMillis() + (5L * 365 * 24 * 60 * 60 * 1000);
            card.setExpiryDate(expiry);
        }
        if (card.getUsedAmount() == null) {
            card.setUsedAmount(0.0);
        }
        if (card.getBlocked() == null) {
            card.setBlocked(false);
        }
        return cardRepository.save(card);
    }

    // ── GET ALL CARDS ──
    public List<Card> getAllCards() {
        List<Card> cards = cardRepository.findAll();
        System.out.println("=== ALL CARDS IN DB: " + cards.size() + " ===");
        for (Card c : cards) {
            System.out.println("  - ID:" + c.getId() + " | ACC:" + c.getAccountNumber() + " | CARD:" + c.getCardNumber());
        }
        return cards;
    }

    // ── GET CARDS BY ACCOUNT NUMBER ──
    public List<CardResponse> getCardsByAccountNumber(String accountNumber) {
        System.out.println("=== QUERYING FOR ACCOUNT: '" + accountNumber + "' ===");
        List<Card> cards = cardRepository.findByAccountNumber(accountNumber);
        System.out.println("=== FOUND " + cards.size() + " RAW CARDS ===");

        List<CardResponse> responses = new ArrayList<>();
        for (Card card : cards) {
            try {
                CardResponse resp = toResponseWithUsage(card);
                responses.add(resp);
                System.out.println("  ✓ Processed card: " + card.getCardNumber() + " | Type: " + card.getCardType());
            } catch (Exception e) {
                System.err.println("  ✗ Failed to process card " + card.getId() + ": " + e.getMessage());
                CardResponse resp = new CardResponse();
                resp.setId(card.getId());
                resp.setCardType(card.getCardType());
                resp.setLimitAmount(card.getLimitAmount() != null ? card.getLimitAmount() : 0.0);
                resp.setCardNumber(card.getCardNumber());
                resp.setBlocked(card.getBlocked());
                resp.setAccountNumber(card.getAccountNumber());
                resp.setCardHolderName(card.getCardHolderName());
                resp.setExpiryDate(card.getExpiryDate());
                resp.setEmail(card.getEmail());
                resp.setUsedAmount(card.getUsedAmount() != null ? card.getUsedAmount() : 0.0);
                resp.setAvailableLimit(card.getLimitAmount() != null ? card.getLimitAmount() : 0.0);
                responses.add(resp);
            }
        }
        System.out.println("=== RETURNING " + responses.size() + " CARD RESPONSES ===");
        return responses;
    }

    // ── CONVERT CARD TO RESPONSE WITH USAGE ──
    public CardResponse toResponseWithUsage(Card card) {
        double used = 0.0;
        try {
            List<CardTransaction> txns = cardTransactionRepository.findByCardNumber(card.getCardNumber());
            if (txns != null) {
                used = txns.stream()
                        .mapToDouble(t -> t.getAmount() != null ? t.getAmount() : 0.0)
                        .sum();
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not fetch transactions for card " + card.getCardNumber() + ": " + e.getMessage());
            used = card.getUsedAmount() != null ? card.getUsedAmount() : 0.0;
        }

        double limit = card.getLimitAmount() != null ? card.getLimitAmount() : 0.0;
        double available = Math.max(0.0, limit - used);

        CardResponse resp = new CardResponse();
        resp.setId(card.getId());
        resp.setCardType(card.getCardType());
        resp.setLimitAmount(limit);
        resp.setCardNumber(card.getCardNumber());
        resp.setBlocked(card.getBlocked());
        resp.setAccountNumber(card.getAccountNumber());
        resp.setCardHolderName(card.getCardHolderName());
        resp.setExpiryDate(card.getExpiryDate());
        resp.setEmail(card.getEmail());
        resp.setUsedAmount(used);
        resp.setAvailableLimit(available);
        return resp;
    }

    // ── GET CARD BY ID ──
    public Card getCardById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card Not Found"));
    }

    // ── UPDATE CARD ──
    public Card updateCard(Long id, Card card) {
        Card existing = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card Not Found"));

        existing.setCardType(card.getCardType());
        existing.setLimitAmount(card.getLimitAmount());
        existing.setCardNumber(card.getCardNumber());
        existing.setBlocked(card.getBlocked());
        existing.setAccountNumber(card.getAccountNumber());
        existing.setCardHolderName(card.getCardHolderName());
        existing.setExpiryDate(card.getExpiryDate());
        existing.setEmail(card.getEmail());

        return cardRepository.save(existing);
    }

    // ── DELETE CARD ──
    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }

    // ── BLOCK CARD ──
    public String blockCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card Not Found"));
        card.setBlocked(true);
        cardRepository.save(card);
        return "Card Blocked Successfully";
    }

    // ── UNBLOCK CARD ──
    public String unblockCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card Not Found"));
        card.setBlocked(false);
        cardRepository.save(card);
        return "Card Unblocked Successfully";
    }

    // ── APPLY FOR NEW CARD (VALIDATES ACCOUNT FIRST) ──
    public Card applyCard(CardRequest request) {
        String accountNumber = request.getAccountNumber();
        System.out.println("Inside applyCard() for account: " + accountNumber);

        // Validate account exists via account-service
        AccountResponse account;
        try {
            account = accountClient.getAccountByNumber(accountNumber);
            System.out.println("Account validated: " + account.getAccountNumber());
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Account not found: " + accountNumber);
        } catch (Exception e) {
            throw new RuntimeException("Account service unavailable: " + e.getMessage());
        }

        // Check max cards per account
        if (cardRepository.findByAccountNumber(accountNumber).size() >= 3) {
            throw new RuntimeException("Maximum 3 cards allowed for this account");
        }

        Card card = new Card();
        card.setAccountNumber(accountNumber);

        // Use provided or auto-generate from account data
        card.setCardHolderName(
                request.getCardHolderName() != null ? request.getCardHolderName() : account.getAccountHolderName()
        );
        card.setCardType(
                request.getCardType() != null ? request.getCardType() : "VISA"
        );
        card.setCardNumber(
                request.getCardNumber() != null ? request.getCardNumber() : generateCardNumber()
        );
        card.setEmail(
                request.getEmail() != null ? request.getEmail() : account.getEmail()
        );

        card.setLimitAmount(150000.0);
        card.setUsedAmount(0.0);

        long expiry = System.currentTimeMillis() + (5L * 365 * 24 * 60 * 60 * 1000);
        card.setExpiryDate(expiry);
        card.setBlocked(false);

        System.out.println("Saving auto-generated card...");
        Card saved = cardRepository.save(card);
        System.out.println("Saved ID = " + saved.getId());

        return saved;
    }

    // Helper method to generate 16-digit Visa card number
    private String generateCardNumber() {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder("4");
        for (int i = 0; i < 15; i++) {
            sb.append(rnd.nextInt(10));
        }
        return sb.toString();
    }

    // ── GET CARD TRANSACTIONS ──
    public List<CardTransaction> getCardTransactions(String cardNumber) {
        try {
            return cardTransactionRepository.findByCardNumber(cardNumber);
        } catch (Exception e) {
            System.err.println("Warning: Could not fetch transactions: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ── CHANGE PIN ──
    public String changePin(Long cardId, String newPin) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setPin(newPin);
        cardRepository.save(card);
        return "PIN changed successfully";
    }

    // ── CREATE CARD TRANSACTION ──
    public CardTransaction createTransaction(Long cardId, CardTransactionRequest request) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        CardTransaction transaction = new CardTransaction();
        transaction.setCardId(cardId);
        transaction.setCardNumber(card.getCardNumber());
        transaction.setMerchant(request.getMerchant());
        transaction.setCategory(request.getCategory());
        transaction.setAmount(request.getAmount().doubleValue());
        transaction.setTransactionDate(request.getTransactionDate().atStartOfDay());
        transaction.setCreatedAt(LocalDateTime.now());

        return cardTransactionRepository.save(transaction);
    }
}