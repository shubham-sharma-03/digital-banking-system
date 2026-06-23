package com.bank.accountservice.repository;

import com.bank.accountservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    // PK is now String (account_number), so JpaRepository<Account, String>

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByEmail(String email);

    boolean existsByAccountNumber(String accountNumber);
}