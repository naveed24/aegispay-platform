package com.aegispay.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    RouteLocator routes(
            RouteLocatorBuilder builder,
            @Value("${services.payment:http://localhost:8081}") String payment,
            @Value("${services.merchant:http://localhost:8082}") String merchant) {
        return builder.routes()
                .route("payment-service", r -> r.path("/api/payments/**").uri(payment))
                .route("merchant-service", r -> r.path("/api/merchants/**").uri(merchant))
                .build();
    }
}
