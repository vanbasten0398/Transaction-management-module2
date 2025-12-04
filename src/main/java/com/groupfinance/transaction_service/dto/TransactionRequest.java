package com.groupfinance.transaction_service.dto;

import com.groupfinance.transaction_service.model.TransactionCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TransactionRequest {
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;
    
    @NotBlank(message = "MPesa phone number is required")
    private String mpesaPhoneNumber;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Category is required")
    private TransactionCategory category;
    
    // For correction transactions - optional
    private Long originalTransactionId;

    // Default constructor (required for JSON parsing)
    public TransactionRequest() {}

    // Constructor
    public TransactionRequest(Double amount, String mpesaPhoneNumber, String description, TransactionCategory category) {
        this.amount = amount;
        this.mpesaPhoneNumber = mpesaPhoneNumber;
        this.description = description;
        this.category = category;
    }

    // Getters and Setters
    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getMpesaPhoneNumber() {
        return mpesaPhoneNumber;
    }

    public void setMpesaPhoneNumber(String mpesaPhoneNumber) {
        this.mpesaPhoneNumber = mpesaPhoneNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TransactionCategory getCategory() {
        return category;
    }

    public void setCategory(TransactionCategory category) {
        this.category = category;
    }

    public Long getOriginalTransactionId() {
        return originalTransactionId;
    }

    public void setOriginalTransactionId(Long originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }
}