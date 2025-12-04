package com.groupfinance.transaction_service.exception;

public class TransactionNotFoundException extends TransactionException {
    
    public TransactionNotFoundException(Long transactionId) {
        super("Transaction not found with ID: " + transactionId);
    }
    
    public TransactionNotFoundException(String message) {
        super(message);
    }
}