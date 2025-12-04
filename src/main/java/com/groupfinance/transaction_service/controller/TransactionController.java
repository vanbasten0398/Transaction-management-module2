package com.groupfinance.transaction_service.controller;

import com.groupfinance.transaction_service.dto.ApiResponse;
import com.groupfinance.transaction_service.dto.TransactionRequest;
import com.groupfinance.transaction_service.dto.TransactionResponse;
import com.groupfinance.transaction_service.model.TransactionStatus;
import com.groupfinance.transaction_service.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Initiate a new transaction
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            @RequestHeader("X-User-Id") String userId) {
        
        TransactionResponse response = transactionService.initiateTransaction(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Transaction initiated successfully", response));
    }

    /**
     * Get all transactions for the current user
     */
    @GetMapping("/my-transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getUserTransactions(
            @RequestHeader("X-User-Id") String userId) {
        
        List<TransactionResponse> transactions = transactionService.getUserTransactions(userId);
        return ResponseEntity.ok(ApiResponse.success("User transactions retrieved successfully", transactions));
    }

    /**
     * Get all transactions (for dashboard - will be filtered by group later)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions() {
        List<TransactionResponse> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(ApiResponse.success("All transactions retrieved successfully", transactions));
    }

    /**
     * Get a specific transaction by ID
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @PathVariable Long transactionId,
            @RequestHeader("X-User-Id") String userId) {
        
        TransactionResponse transaction = transactionService.getTransactionById(transactionId, userId);
        return ResponseEntity.ok(ApiResponse.success("Transaction retrieved successfully", transaction));
    }

    /**
     * Cancel a pending transaction
     */
    @PutMapping("/{transactionId}/cancel")
    public ResponseEntity<ApiResponse<TransactionResponse>> cancelTransaction(
            @PathVariable Long transactionId,
            @RequestHeader("X-User-Id") String userId) {
        
        TransactionResponse response = transactionService.cancelTransaction(transactionId, userId);
        return ResponseEntity.ok(ApiResponse.success("Transaction cancelled successfully", response));
    }

    /**
     * Create a correction transaction for a previous error
     */
    @PostMapping("/{originalTransactionId}/correction")
    public ResponseEntity<ApiResponse<TransactionResponse>> createCorrectionTransaction(
            @PathVariable Long originalTransactionId,
            @Valid @RequestBody TransactionRequest request,
            @RequestHeader("X-User-Id") String userId) {
        
        TransactionResponse response = transactionService.createCorrectionTransaction(originalTransactionId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Correction transaction created successfully", response));
    }

    /**
     * Get transactions by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByStatus(
            @PathVariable TransactionStatus status) {
        
        List<TransactionResponse> transactions = transactionService.getTransactionsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved by status", transactions));
    }

    /**
     * ADMIN ENDPOINT: Simulate M-Pesa callback (for testing only)
     * In production, this would be called by M-Pesa directly
     */
    @PostMapping("/{transactionId}/simulate-callback")
    public ResponseEntity<ApiResponse<TransactionResponse>> simulateMpesaCallback(
            @PathVariable Long transactionId,
            @RequestParam boolean success,
            @RequestParam(required = false) String receiptNumber) {
        
        TransactionResponse response = transactionService.simulateMpesaCallback(transactionId, success, receiptNumber);
        String message = success ? "M-Pesa success callback simulated" : "M-Pesa failure callback simulated";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Transaction Service is running", null));
    }
}