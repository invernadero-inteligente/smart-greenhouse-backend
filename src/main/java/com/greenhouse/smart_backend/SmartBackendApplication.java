package com.greenhouse.smart_backend;

import com.greenhouse.smart_backend.modules.iot.mqtt.config.MqttProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class SmartBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartBackendApplication.class, args);
    }
}