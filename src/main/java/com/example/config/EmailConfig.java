package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.resend.Resend;

/**
 * Configuración de Resend Email Service
 */
@Configuration
public class EmailConfig {

    @Value("${resend.api-key}")
    private String apiKey;

    @Bean
    public Resend resendClient() {
        return new Resend(apiKey);
    }
}
