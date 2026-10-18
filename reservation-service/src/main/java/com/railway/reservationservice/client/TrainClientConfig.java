package com.railway.reservationservice.client;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign configuration for train-service.
 * Adds X-Internal-Secret header for /internal/** endpoints,
 * and forwards the Authorization header (JWT) if present on the current request.
 */
@Configuration
public class TrainClientConfig {

    @Value("${internal.api.secret}")
    private String internalSecret;

    @Bean
    public RequestInterceptor trainServiceInterceptor() {
        return template -> {
            template.header("X-Internal-Secret", internalSecret);
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && !authHeader.isBlank()) {
                    template.header("Authorization", authHeader);
                }
            }
        };
    }
}
