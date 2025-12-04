package com.groupfinance.transaction_service.service;

import com.groupfinance.transaction_service.dto.TransactionRequest;
import com.groupfinance.transaction_service.dto.TransactionResponse;
import com.groupfinance.transaction_service.exception.InvalidTransactionOperationException;
import com.groupfinance.transaction_service.exception.TransactionNotFoundException;
import com.groupfinance.transaction_service.model.Transaction;
import com.groupfinance.transaction_service.model.TransactionCategory;
import com.groupfinance.transaction_service.model.TransactionStatus;
import com.groupfinance.transaction_service.model.TransactionType;
import com.groupfinance.transaction_service.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final MpesaMockService mpesaMockService;
    
    // 25-second cancellation and auto-completion window
    private static final int AUTO_COMPLETION_SECONDS = 25;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, MpesaMockService mpesaMockService) {
        this.transactionRepository = transactionRepository;
        this.mpesaMockService = mpesaMockService;
    }

    @Override
    public TransactionResponse initiateTransaction(TransactionRequest request, String userId) {
        // Validate request
        validateTransactionRequest(request);
        
        // Determine transaction type
        TransactionType type = (request.getOriginalTransactionId() != null) ? 
            TransactionType.CORRECTION : TransactionType.EXPENSE;
        
        // Create transaction entity
        Transaction transaction = new Transaction(type, request.getAmount(), 
            request.getDescription(), request.getCategory(), userId);
        
        transaction.setMpesaPhoneNumber(request.getMpesaPhoneNumber());
        
        // For correction transactions, link to original
        if (type == TransactionType.CORRECTION) {
            transaction.setOriginalTransactionId(request.getOriginalTransactionId());
        }
        
        // Save to get ID first
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        try {
            // Simulate M-Pesa STK Push initiation
            String requestId = mpesaMockService.initiateStkPush(
                request.getMpesaPhoneNumber(), 
                request.getAmount(), 
                request.getDescription()
            );
            
            savedTransaction.setMpesaRequestId(requestId);
            
            // SIMULATE IMMEDIATE SUCCESS/FAILURE - NO PENDING STATE
            // For testing: Use specific phone number to simulate failure
            if (request.getMpesaPhoneNumber().equals("254700000000")) {
                savedTransaction.setStatus(TransactionStatus.FAILED);
                savedTransaction.setMpesaCallbackResult("SIMULATED_FAILURE: Insufficient funds");
            } else {
                // Schedule automatic completion after 25 seconds
                scheduleAutoCompletion(savedTransaction.getId());
            }
            
            // Save updated transaction
            savedTransaction = transactionRepository.save(savedTransaction);
            
        } catch (Exception e) {
            // If M-Pesa initiation fails, mark as failed immediately
            savedTransaction.setStatus(TransactionStatus.FAILED);
            savedTransaction.setMpesaCallbackResult("INITIATION_FAILED: " + e.getMessage());
            transactionRepository.save(savedTransaction);
            throw new InvalidTransactionOperationException("Failed to initiate M-Pesa payment: " + e.getMessage());
        }
        
        return convertToResponse(savedTransaction);
    }

    @Override
    public TransactionResponse cancelTransaction(Long transactionId, String userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
            .orElseThrow(() -> new TransactionNotFoundException(transactionId));
        
        // Validate if transaction can be cancelled
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new InvalidTransactionOperationException(
                "Cannot cancel transaction. Current status: " + transaction.getStatus());
        }
        
        // Check if within 25-second cancellation window
        long secondsElapsed = java.time.Duration.between(transaction.getCreatedAt(), LocalDateTime.now()).getSeconds();
        if (secondsElapsed > AUTO_COMPLETION_SECONDS) {
            // If beyond 25 seconds, transaction should already be completed by scheduler
            throw new InvalidTransactionOperationException(
                "Cancellation window expired. Transactions can only be cancelled within " + 
                AUTO_COMPLETION_SECONDS + " seconds. Time elapsed: " + secondsElapsed + " seconds.");
        }
        
        transaction.setStatus(TransactionStatus.CANCELLED);
        Transaction cancelledTransaction = transactionRepository.save(transaction);
        
        return convertToResponse(cancelledTransaction);
    }

    /**
     * Schedule automatic completion of pending transaction after 25 seconds
     * This ensures NO transaction stays in PENDING state
     */
    @Async
    public void scheduleAutoCompletion(Long transactionId) {
        try {
            System.out.println("Scheduling auto-completion for transaction: " + transactionId + " in " + AUTO_COMPLETION_SECONDS + " seconds");
            
            // Wait for exactly 25 seconds
            Thread.sleep(AUTO_COMPLETION_SECONDS * 1000);
            
            // Complete the transaction automatically
            completePendingTransaction(transactionId);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Auto-completion interrupted for transaction: " + transactionId);
            // If interrupted, still complete the transaction
            completePendingTransaction(transactionId);
        }
    }

    /**
     * Complete a pending transaction automatically (simulate successful payment)
     * This is the guarantee that transactions won't stay pending
     */
    private void completePendingTransaction(Long transactionId) {
        try {
            Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
            
            // Only complete if still pending (might have been cancelled)
            if (transaction.getStatus() == TransactionStatus.PENDING) {
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setMpesaReceiptNumber(mpesaMockService.generateMockReceiptNumber());
                transaction.setMpesaCallbackResult("AUTO_COMPLETED: Transaction automatically completed after " + 
                    AUTO_COMPLETION_SECONDS + " seconds");
                
                transactionRepository.save(transaction);
                
                System.out.println("✓ Transaction " + transactionId + " automatically COMPLETED after " + 
                    AUTO_COMPLETION_SECONDS + " seconds");
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: Failed to auto-complete transaction " + transactionId + ": " + e.getMessage());
            // Critical: If auto-completion fails, we need to ensure transaction doesn't stay pending
            forceCompleteTransaction(transactionId);
        }
    }
    
    /**
     * EMERGENCY: Force complete transaction if auto-completion fails
     */
    private void forceCompleteTransaction(Long transactionId) {
        try {
            Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
            if (transaction != null && transaction.getStatus() == TransactionStatus.PENDING) {
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setMpesaCallbackResult("FORCE_COMPLETED: Emergency completion after auto-completion failure");
                transactionRepository.save(transaction);
                System.out.println("✓ EMERGENCY: Transaction " + transactionId + " force-completed");
            }
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Cannot force complete transaction " + transactionId);
        }
    }

    /**
     * SAFETY NET: Scheduled task runs every 30 seconds to cleanup ANY stuck pending transactions
     * This is our final guarantee that no transaction stays in PENDING state
     */
    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    public void cleanupStuckTransactions() {
        try {
            List<Transaction> pendingTransactions = transactionRepository.findByStatus(TransactionStatus.PENDING);
            
            if (!pendingTransactions.isEmpty()) {
                System.out.println("Cleaning up " + pendingTransactions.size() + " potentially stuck transactions...");
                
                for (Transaction transaction : pendingTransactions) {
                    long secondsElapsed = java.time.Duration.between(transaction.getCreatedAt(), LocalDateTime.now()).getSeconds();
                    
                    // If transaction is older than 30 seconds, force complete it
                    if (secondsElapsed > 30) {
                        System.out.println("Force completing stuck transaction: " + transaction.getId() + " (" + secondsElapsed + " seconds old)");
                        transaction.setStatus(TransactionStatus.COMPLETED);
                        transaction.setMpesaReceiptNumber("STUCK_" + mpesaMockService.generateMockReceiptNumber());
                        transaction.setMpesaCallbackResult("STUCK_CLEANUP: Auto-completed by cleanup job after " + secondsElapsed + " seconds");
                        transactionRepository.save(transaction);
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error in cleanupStuckTransactions: " + e.getMessage());
        }
    }

    // KEEP ALL OTHER METHODS EXACTLY AS THEY WERE BEFORE
    @Override
    public TransactionResponse createCorrectionTransaction(Long originalTransactionId, TransactionRequest request, String userId) {
        // Verify original transaction exists and is completed
        Transaction originalTransaction = transactionRepository.findById(originalTransactionId)
            .orElseThrow(() -> new TransactionNotFoundException(originalTransactionId));
        
        if (originalTransaction.getStatus() != TransactionStatus.COMPLETED) {
            throw new InvalidTransactionOperationException(
                "Can only create corrections for COMPLETED transactions. Original transaction status: " + 
                originalTransaction.getStatus());
        }
        
        // Set the original transaction ID in the request
        request.setOriginalTransactionId(originalTransactionId);
        
        // Force category to CORRECTION for correction transactions
        request.setCategory(TransactionCategory.CORRECTION);
        
        // Initiate the correction transaction
        return initiateTransaction(request, userId);
    }

    @Override
    public List<TransactionResponse> getUserTransactions(String userId) {
        List<Transaction> transactions = transactionRepository.findByCreatedByUserId(userId);
        return transactions.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public TransactionResponse getTransactionById(Long transactionId, String userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
            .orElseThrow(() -> new TransactionNotFoundException(transactionId));
        return convertToResponse(transaction);
    }

    @Override
    public List<TransactionResponse> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAllByOrderByCreatedAtDesc();
        return transactions.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByStatus(TransactionStatus status) {
        List<Transaction> transactions = transactionRepository.findByStatusOrderByCreatedAtDesc(status);
        return transactions.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public TransactionResponse simulateMpesaCallback(Long transactionId, boolean success, String receiptNumber) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException(transactionId));
        
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new InvalidTransactionOperationException(
                "Can only simulate callback for PENDING transactions. Current status: " + transaction.getStatus());
        }
        
        if (success) {
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setMpesaReceiptNumber(receiptNumber != null ? receiptNumber : mpesaMockService.generateMockReceiptNumber());
            transaction.setMpesaCallbackResult("SIMULATED_SUCCESS: Payment completed successfully");
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setMpesaCallbackResult("SIMULATED_FAILURE: Payment failed");
        }
        
        Transaction updatedTransaction = transactionRepository.save(transaction);
        return convertToResponse(updatedTransaction);
    }

    // Private helper methods
    private void validateTransactionRequest(TransactionRequest request) {
        if (request.getAmount() <= 0) {
            throw new InvalidTransactionOperationException("Amount must be greater than 0");
        }
        
        if (request.getMpesaPhoneNumber() == null || !request.getMpesaPhoneNumber().matches("254\\d{9}")) {
            throw new InvalidTransactionOperationException("Valid M-Pesa phone number required (format: 254XXXXXXXXX)");
        }
    }

    private TransactionResponse convertToResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setDescription(transaction.getDescription());
        response.setCategory(transaction.getCategory());
        response.setType(transaction.getType());
        response.setStatus(transaction.getStatus());
        response.setMpesaPhoneNumber(transaction.getMpesaPhoneNumber());
        response.setMpesaReceiptNumber(transaction.getMpesaReceiptNumber());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setCompletedAt(transaction.getCompletedAt());
        response.setCreatedByUserId(transaction.getCreatedByUserId());
        response.setOriginalTransactionId(transaction.getOriginalTransactionId());
        return response;
    }
}