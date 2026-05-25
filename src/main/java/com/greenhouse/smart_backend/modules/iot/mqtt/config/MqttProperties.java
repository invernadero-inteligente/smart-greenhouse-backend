package com.greenhouse.smart_backend.modules.iot.mqtt.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {

    private String brokerUrl;
    private String username;
    private String password;
    private String clientId;
    private Topics topics = new Topics();

    @Getter
    @Setter
    public static class Topics {
        private String sensors;
        private String actuators;
        private String info;
    }
}