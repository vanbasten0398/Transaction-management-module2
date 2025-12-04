package com.groupfinance.transaction_service.service;

import com.groupfinance.transaction_service.dto.TransactionRequest;
import com.groupfinance.transaction_service.dto.TransactionResponse;
import com.groupfinance.transaction_service.model.TransactionStatus;

import java.util.List;

public interface TransactionService {
    
    // Initiate a new transaction
    TransactionResponse initiateTransaction(TransactionRequest request, String userId);
    
    // Cancel a pending transaction
    TransactionResponse cancelTransaction(Long transactionId, String userId);
    
    // Create a correction transaction for a previous error
    TransactionResponse createCorrectionTransaction(Long originalTransactionId, TransactionRequest request, String userId);
    
    // Get all transactions for a user
    List<TransactionResponse> getUserTransactions(String userId);
    
    // Get a specific transaction by ID
    TransactionResponse getTransactionById(Long transactionId, String userId);
    
    // Get all transactions (for dashboard - will be filtered by group in integration)
    List<TransactionResponse> getAllTransactions();
    
    // Get transactions by status
    List<TransactionResponse> getTransactionsByStatus(TransactionStatus status);
    
    // Simulate M-Pesa callback (for testing)
    TransactionResponse simulateMpesaCallback(Long transactionId, boolean success, String receiptNumber);
}