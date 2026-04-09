package com.example.cardtransferrest.domain;

import lombok.Getter;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Getter
public class BankAccount {
    private final String cardNumber;
    private final String validTill;
    private final String cvv;
    private long balance;
    private final Lock lock = new ReentrantLock();

    public BankAccount(String cardNumber, String validTill, String cvv, long balance) {
        this.cardNumber = cardNumber;
        this.validTill = validTill;
        this.cvv = cvv;
        this.balance = balance;
    }

    public void withdraw(long value) {
        if (this.balance < value) {
            throw new IllegalArgumentException("Недостаточно средств");
        }
        this.balance -= value;
    }

    public void deposit(long value) {
        this.balance += value;
    }
}
