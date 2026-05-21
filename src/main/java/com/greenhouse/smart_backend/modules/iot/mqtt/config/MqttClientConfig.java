package com.greenhouse.smart_backend.modules.iot.mqtt.config;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttClientConfig {

    @Bean
    public MqttClient mqttClient(MqttProperties properties) throws Exception {
        return new MqttClient(
                properties.getBrokerUrl(),
                properties.getClientId(),
                new MemoryPersistence()
        );
    }
}