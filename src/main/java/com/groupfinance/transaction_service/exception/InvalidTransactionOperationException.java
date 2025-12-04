package com.groupfinance.transaction_service.exception;

public class InvalidTransactionOperationException extends TransactionException {
    
    public InvalidTransactionOperationException(String message) {
        super(message);
    }
}