package com.example.cardtransferrest.repository;

import com.example.cardtransferrest.domain.BankAccount;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class BankAccountRepository {
    private final Map<String, BankAccount> storage = new ConcurrentHashMap<>();


    public BankAccountRepository() {
        storage.put("1111222233334444", new BankAccount("1111222233334444", "12/26", "123", 10000000L));
        storage.put("5555666677778888", new BankAccount("5555666677778888", "10/26", "456", 50000L));
    }

    public Optional<BankAccount> findByCardNumber(String cardNumber) {
        return Optional.ofNullable(storage.get(cardNumber));
    }

    public Map<String, BankAccount> getAllAccounts() {
        return storage;
    }
}
