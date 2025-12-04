package com.groupfinance.transaction_service.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class MpesaMockService {
    
    private final Random random = new Random();
    
    /**
     * Simulates sending STK Push to M-Pesa
     * In real scenario, this would call Daraja API
     */
    public String initiateStkPush(String phoneNumber, Double amount, String description) {
        // Simulate API call delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Generate a mock request ID (simulates M-Pesa response)
        String requestId = "REQ_" + System.currentTimeMillis() + "_" + random.nextInt(1000);
        
        // Log the mock request (in real app, this would be the actual API call)
        System.out.println("=== MOCK M-PESA STK PUSH ===");
        System.out.println("Phone: " + phoneNumber);
        System.out.println("Amount: " + amount);
        System.out.println("Description: " + description);
        System.out.println("Request ID: " + requestId);
        System.out.println("=== WAITING FOR USER CONFIRMATION ON PHONE ===");
        
        return requestId;
    }
    
    /**
     * Simulates various failure scenarios for testing
     * UPDATED: Only simulate failure for specific test phone number
     * This ensures most transactions proceed to auto-completion
     */
    public boolean shouldSimulateFailure(String phoneNumber, Double amount) {
        // Only simulate failure for specific test phone number
        // This ensures most transactions proceed to auto-completion
        return phoneNumber.equals("254700000000");
    }
    
    /**
     * Generates a mock M-Pesa receipt number
     */
    public String generateMockReceiptNumber() {
        return "MPE" + System.currentTimeMillis() + "A" + random.nextInt(1000);
    }
}