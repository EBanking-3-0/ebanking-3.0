package com.ebanking.account.exception;

public class InsufficientBalance extends Exception {
    public InsufficientBalance(String message) {
        super(message);
    }
}