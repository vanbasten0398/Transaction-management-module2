package com.groupfinance.transaction_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync  // This enables @Async functionality
public class AsyncConfig {
    // Configuration for async processing
}