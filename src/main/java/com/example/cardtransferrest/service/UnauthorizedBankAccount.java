package com.example.cardtransferrest.service;

public class UnauthorizedBankAccount extends RuntimeException {
    public UnauthorizedBankAccount(String message) {
        super(message);
    }
}
