package com.bank.accountservice.service;

import com.bank.accountservice.client.TransactionClient;
import com.bank.accountservice.dto.TransactionRequest;
import com.bank.accountservice.client.EmailClient;
import com.bank.accountservice.entity.Account;
import com.bank.accountservice.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.bank.accountservice.client.CardClient;
import com.bank.accountservice.dto.CardRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bank.accountservice.dto.EmailRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class AccountService {

    @Autowired
    private EmailClient emailClient;

    @Autowired
    private AccountRepository repository;

    @Autowired
    private CardClient cardClient;

    @Autowired
    private TransactionClient transactionClient;

    // ── GET ALL ──
    public List<Account> getAllAccounts() {
        return repository.findAll();
    }

    // ── GET by Account Number ──
    public Account getAccountByAccountNumber(String accountNumber) {
        return repository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account Not Found: " + accountNumber));
    }

    // ── GET by Email (single account) ──
    public Account getAccountByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account Not Found for email: " + email));
    }

    // ── GET ALL ACCOUNTS BY EMAIL (NEW — for transaction-service multi-account) ──
    public List<Account> getAccountsByEmail(String email) {
        return repository.findAllByEmail(email);
    }

    // ── UPDATE by Account Number ──
    public Account updateAccount(String accountNumber, Account account) {
        Account existing = repository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account Not Found: " + accountNumber));

        if (account.getAccountHolderName() != null) existing.setAccountHolderName(account.getAccountHolderName());
        if (account.getAccountType() != null) existing.setAccountType(account.getAccountType());
        if (account.getBalance() != null) existing.setBalance(account.getBalance());
        if (account.getBranch() != null) existing.setBranch(account.getBranch());
        if (account.getIfscCode() != null) existing.setIfscCode(account.getIfscCode());

        return repository.save(existing);
    }

    // ── DELETE by Account Number ──
    public void deleteAccount(String accountNumber) {
        Account account = repository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account Not Found: " + accountNumber));
        repository.delete(account);
    }

    // ── DEPOSIT ──
    @Transactional
    public Account deposit(String accountNumber, Double amount) {
        if (amount == null || amount <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        Account account = repository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account Not Found: " + accountNumber));

        account.setBalance(account.getBalance() + amount);
        repository.save(account);

        System.out.println("Saving transaction...");

        saveTransaction(accountNumber, amount, "DEPOSIT",
                "Deposit to " + accountNumber, account.getBalance());

        sendEmail(account.getEmail(), "Amount Credited",
                "₹" + amount + " credited to account " + accountNumber +
                        ". Current Balance: ₹" + account.getBalance());

        return account;
    }

    // ── WITHDRAW ──
    @Transactional
    public Account withdraw(String accountNumber, Double amount) {
        if (amount == null || amount <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        Account account = repository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account Not Found: " + accountNumber));

        if (account.getBalance() < amount) {
            throw new RuntimeException("Insufficient Balance. Available: ₹" + account.getBalance());
        }

        account.setBalance(account.getBalance() - amount);
        repository.save(account);

        System.out.println("Saving withdrawal transaction...");

        saveTransaction(accountNumber, amount, "WITHDRAWAL",
                "Withdrawal from " + accountNumber, account.getBalance());

        sendEmail(account.getEmail(), "Amount Debited",
                "₹" + amount + " debited from account " + accountNumber +
                        ". Current Balance: ₹" + account.getBalance());

        return account;
    }

    // ── TRANSFER ──
    @Transactional
    public String transfer(String fromAccountNumber, String toAccountNumber, Double amount) {
        if (amount == null || amount <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new RuntimeException("Cannot transfer to the same account");
        }

        System.out.println("======================================");
        System.out.println("FROM = [" + fromAccountNumber + "]");
        System.out.println("TO   = [" + toAccountNumber + "]");
        System.out.println("======================================");

        System.out.println("Accounts available in database:");
        repository.findAll().forEach(a ->
                System.out.println("-> " + a.getAccountNumber())
        );

        Account from = repository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new RuntimeException("Sender Account Not Found: " + fromAccountNumber));

        Account to = repository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new RuntimeException("Receiver Account Not Found: " + toAccountNumber));

        if (from.getBalance() < amount) {
            throw new RuntimeException("Insufficient Balance. Available: ₹" + from.getBalance());
        }

        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);

        repository.save(from);
        repository.save(to);

        // Save transactions with post-transfer balances
        saveTransaction(fromAccountNumber, amount, "TRANSFER_DEBIT",
                "Transfer to " + toAccountNumber, from.getBalance());
        saveTransaction(toAccountNumber, amount, "TRANSFER_CREDIT",
                "Transfer from " + fromAccountNumber, to.getBalance());

        sendEmail(from.getEmail(), "Money Transferred",
                "₹" + amount + " transferred from " + fromAccountNumber +
                        " to " + toAccountNumber + ". Balance: ₹" + from.getBalance());

        sendEmail(to.getEmail(), "Money Received",
                "₹" + amount + " received from " + fromAccountNumber +
                        ". Balance: ₹" + to.getBalance());

        return "Money Transferred Successfully from " + fromAccountNumber + " to " + toAccountNumber;
    }

    // ── CREATE ACCOUNT ──
    @Transactional
    public Account createAccount(Account account) {

        if (account.getEmail() == null || account.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (account.getAccountHolderName() == null || account.getAccountHolderName().isBlank()) {
            throw new RuntimeException("Account Holder Name is required");
        }

        if (repository.findByEmail(account.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        if (account.getBalance() == null) {
            account.setBalance(0.0);
        }

        if (account.getBalance() < 20000) {
            throw new RuntimeException("Minimum opening balance is ₹20,000");
        }

        if (account.getBranch() == null || account.getBranch().isBlank()) {
            account.setBranch("Mumbai Main");
        }

        if (account.getIfscCode() == null || account.getIfscCode().isBlank()) {
            account.setIfscCode("MYBNK0000001");
        }

        account.setAccountNumber(generateUniqueAccountNumber());

        Account saved = repository.save(account);

        // Account opening transaction
        saveTransaction(saved.getAccountNumber(), saved.getBalance(),
                "ACCOUNT_OPENING", "Initial Deposit", saved.getBalance());

        // create card
        try {
            CardRequest card = new CardRequest();
            card.setCardHolderName(saved.getAccountHolderName());
            card.setCardType("Visa Debit");
            card.setCardNumber(generateCardNumber());
            card.setExpiryDate("12/30");
            card.setLimitAmount(100000.0);
            card.setEmail(saved.getEmail());
            card.setAccountNumber(saved.getAccountNumber());
            card.setBlocked(false);

            cardClient.createCard(card);

        } catch (Exception e) {
            System.out.println("Card Service Down: " + e.getMessage());
        }

        // email
        sendEmail(
                saved.getEmail(),
                "Welcome To MyBank",
                "Hello " + saved.getAccountHolderName()
                        + ", your account " + saved.getAccountNumber()
                        + " has been created successfully."
        );

        return saved;
    }

    // ── Helper: Generate unique account number ──
    private String generateUniqueAccountNumber() {
        List<Account> allAccounts = repository.findAll();

        long max = allAccounts.stream()
                .map(Account::getAccountNumber)
                .filter(acc -> acc != null && acc.startsWith("ACC"))
                .mapToLong(acc -> {
                    try {
                        return Long.parseLong(acc.substring(3));
                    } catch (Exception e) {
                        return 0L;
                    }
                })
                .max()
                .orElse(0L);

        long next = max + 1;
        return String.format("ACC%014d", next);
    }

    // ── Helper: Generate proper 16-digit Visa card number ──
    private String generateCardNumber() {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder("4");
        for (int i = 0; i < 15; i++) {
            sb.append(rnd.nextInt(10));
        }
        return sb.toString();
    }

    // ── Helper: Save a transaction record via transaction-service ──
    private void saveTransaction(
            String accountNumber,
            Double amount,
            String transactionType,
            String description,
            Double balanceAfter) {

        TransactionRequest req = new TransactionRequest();
        req.setAccountNumber(accountNumber);
        req.setAmount(amount);
        req.setTransactionType(transactionType);
        req.setDescription(description);
        req.setBalanceAfter(balanceAfter);
        req.setStatus("SUCCESS");
        req.setMode("IMPS");
        req.setReferenceId("TXN" + System.currentTimeMillis());
        req.setTransactionDate(LocalDateTime.now());

        try {
            transactionClient.createTransaction(req);
            System.out.println("Transaction saved successfully");
        } catch (Exception e) {
            System.out.println("Transaction save failed: " + e.getMessage());
        }
    }

    // ── Helper: Send email silently ──
    private void sendEmail(String to, String subject, String body) {
        try {
            EmailRequest req = new EmailRequest();
            req.setTo(to);
            req.setSubject(subject);
            req.setBody(body);
            emailClient.sendEmail(req);
        } catch (Exception e) {
            System.out.println("[AccountService] Email service unavailable.");
        }
    }

    // ── DEPRECATED: backward compat ──
    public Account save(Account account) {
        return createAccount(account);
    }

    public List<TransactionRequest> getStatement(String accountNumber) {
        repository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account Not Found: " + accountNumber));

        return transactionClient.getTransactionsByAccountNumber(accountNumber);
    }
}