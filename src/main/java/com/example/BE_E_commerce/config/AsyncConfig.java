package com.example.BE_E_commerce.config;


import org.springframework.context.annotation. Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // Email sẽ được gửi bất đồng bộ (không block request)
}