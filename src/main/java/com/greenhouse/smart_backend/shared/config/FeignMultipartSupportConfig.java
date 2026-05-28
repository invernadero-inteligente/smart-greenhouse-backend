package com.greenhouse.smart_backend.shared.config;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignMultipartSupportConfig {
    private final ObjectProvider<FeignHttpMessageConverters> messageConverters;

    public FeignMultipartSupportConfig(ObjectProvider<FeignHttpMessageConverters> messageConverters) {
        this.messageConverters = messageConverters;
    }

    @Bean
    public Encoder feignFormEncoder() {
        return new SpringFormEncoder(
                new SpringEncoder(messageConverters)
        );
    }
}
