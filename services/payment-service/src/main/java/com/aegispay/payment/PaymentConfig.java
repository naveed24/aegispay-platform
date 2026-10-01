package com.aegispay.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PaymentConfig {
    @Bean
    RestClient fraudClient(@Value("${services.fraud:http://localhost:8083}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    RestClient merchantClient(@Value("${services.merchant:http://localhost:8082}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
