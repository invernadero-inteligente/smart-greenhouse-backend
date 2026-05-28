package com.greenhouse.smart_backend.shared.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignRetryConfig {
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(1000, 10000, 3);
    }
}
