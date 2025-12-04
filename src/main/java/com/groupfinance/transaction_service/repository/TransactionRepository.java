package com.groupfinance.transaction_service.repository;

import com.groupfinance.transaction_service.model.Transaction;
import com.groupfinance.transaction_service.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Find all transactions by user ID
    List<Transaction> findByCreatedByUserId(String createdByUserId);
    
    // Find transactions by status
    List<Transaction> findByStatus(TransactionStatus status);
    
    // Find pending transactions (commonly used)
    List<Transaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status);
    
    // Find transactions by user ID and status
    List<Transaction> findByCreatedByUserIdAndStatus(String createdByUserId, TransactionStatus status);
    
    // Find correction transactions linked to an original transaction
    List<Transaction> findByOriginalTransactionId(Long originalTransactionId);
    
    // Find all transactions ordered by creation date (for dashboard)
    List<Transaction> findAllByOrderByCreatedAtDesc();
    
    // Custom query to check if a transaction exists and is owned by user
    @Query("SELECT t FROM Transaction t WHERE t.id = :transactionId AND t.createdByUserId = :userId")
    Optional<Transaction> findByIdAndUserId(@Param("transactionId") Long transactionId, @Param("userId") String userId);
    
    // Custom query to find transactions within a date range (for future reporting)
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findTransactionsBetweenDates(@Param("startDate") java.time.LocalDateTime startDate, 
                                                   @Param("endDate") java.time.LocalDateTime endDate);
}