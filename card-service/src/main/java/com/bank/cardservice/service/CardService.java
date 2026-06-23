package com.bank.cardservice.service;

import com.bank.cardservice.dto.CardRequest;
import com.bank.cardservice.dto.CardResponse;
import com.bank.cardservice.entity.Card;
import com.bank.cardservice.entity.CardTransaction;
import com.bank.cardservice.repository.CardRepository;
import com.bank.cardservice.repository.CardTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CardService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardTransactionRepository cardTransactionRepository;

    public Card createCard(Card card) {
        String accountNumber = card.getAccountNumber();
        if (cardRepository.findByAccountNumber(accountNumber).size() >= 3) {
            throw new RuntimeException("Maximum 3 cards allowed");
        }
        return cardRepository.save(card);
    }

    public Card save(Card card) {

        if (card.getExpiryDate() == null) {

            long expiry =
                    System.currentTimeMillis()
                            + (5L * 365 * 24 * 60 * 60 * 1000);

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

    public List<Card> getAllCards() {
        List<Card> cards = cardRepository.findAll();
        System.out.println("=== ALL CARDS IN DB: " + cards.size() + " ===");
        for (Card c : cards) {
            System.out.println("  - ID:" + c.getId() + " | ACC:" + c.getAccountNumber() + " | CARD:" + c.getCardNumber());
        }
        return cards;
    }

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

    // ONLY ONE toResponseWithUsage method - public, with null check
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

    public Card getCardById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card Not Found"));
    }

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

    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }

    public String blockCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card Not Found"));
        card.setBlocked(true);
        cardRepository.save(card);
        return "Card Blocked Successfully";
    }

    public String unblockCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card Not Found"));
        card.setBlocked(false);
        cardRepository.save(card);
        return "Card Unblocked Successfully";
    }

    public void sendMail(String to, String subject, String body) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(body);
        mailSender.send(mail);
    }

    public Card applyCard(CardRequest request) {

        if (cardRepository.findByAccountNumber(
                request.getAccountNumber()).size() >= 4) {
            throw new RuntimeException("Maximum 4 cards allowed");
        }

        Card card = new Card();

        card.setAccountNumber(request.getAccountNumber());
        card.setCardHolderName(request.getCardHolderName());
        card.setCardType(request.getCardType());
        card.setCardNumber(request.getCardNumber());

        card.setLimitAmount(150000.0);
        card.setUsedAmount(0.0);

        // EXPIRY DATE = TODAY + 5 YEARS
        long expiry =
                System.currentTimeMillis()
                        + (5L * 365 * 24 * 60 * 60 * 1000);

        card.setExpiryDate(expiry);

        card.setBlocked(false);
        card.setEmail(request.getEmail());

        return cardRepository.save(card);
    }

    public List<CardTransaction> getCardTransactions(String cardNumber) {
        try {
            return cardTransactionRepository.findByCardNumber(cardNumber);
        } catch (Exception e) {
            System.err.println("Warning: Could not fetch transactions: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}