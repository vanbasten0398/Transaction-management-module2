package com.groupfinance.transaction_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    // M-Pesa Integration Fields
    private String mpesaPhoneNumber;
    private String mpesaReceiptNumber;
    private String mpesaRequestId;
    private String mpesaCallbackResult;

    // Timestamps
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    // User who initiated the transaction
    @Column(nullable = false)
    private String createdByUserId;

    // For correction transactions - links to original transaction
    private Long originalTransactionId;

    // Constructors
    public Transaction() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Transaction(TransactionType type, Double amount, String description, 
                      TransactionCategory category, String createdByUserId) {
        this();
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.createdByUserId = createdByUserId;
    }

    // Getters and Setters (Required for JPA)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
        this.updatedAt = LocalDateTime.now();
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public TransactionCategory getCategory() {
        return category;
    }

    public void setCategory(TransactionCategory category) {
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        
        if (status == TransactionStatus.COMPLETED || status == TransactionStatus.FAILED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public String getMpesaPhoneNumber() {
        return mpesaPhoneNumber;
    }

    public void setMpesaPhoneNumber(String mpesaPhoneNumber) {
        this.mpesaPhoneNumber = mpesaPhoneNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public String getMpesaReceiptNumber() {
        return mpesaReceiptNumber;
    }

    public void setMpesaReceiptNumber(String mpesaReceiptNumber) {
        this.mpesaReceiptNumber = mpesaReceiptNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public String getMpesaRequestId() {
        return mpesaRequestId;
    }

    public void setMpesaRequestId(String mpesaRequestId) {
        this.mpesaRequestId = mpesaRequestId;
        this.updatedAt = LocalDateTime.now();
    }

    public String getMpesaCallbackResult() {
        return mpesaCallbackResult;
    }

    public void setMpesaCallbackResult(String mpesaCallbackResult) {
        this.mpesaCallbackResult = mpesaCallbackResult;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(String createdByUserId) {
        this.createdByUserId = createdByUserId;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getOriginalTransactionId() {
        return originalTransactionId;
    }

    public void setOriginalTransactionId(Long originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
        this.updatedAt = LocalDateTime.now();
    }
}