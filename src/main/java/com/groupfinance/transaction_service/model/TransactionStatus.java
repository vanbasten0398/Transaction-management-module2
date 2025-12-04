package com.groupfinance.transaction_service.model;

public enum TransactionStatus {
    PENDING,       // Transaction initiated but not confirmed
    COMPLETED,     // M-Pesa payment successful
    FAILED,        // M-Pesa payment failed
    CANCELLED      // Cancelled by user before completion
}