package com.greenhouse.smart_backend.modules.iot.mqtt.application;

import com.greenhouse.smart_backend.modules.iot.mqtt.config.MqttProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MqttConnectionService {

    private final MqttClient mqttClient;
    private final MqttProperties mqttProperties;

    @PostConstruct
    public void connect() {
        try {
            if (mqttClient.isConnected()) {
                return;
            }

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(30);

            if (mqttProperties.getUsername() != null && !mqttProperties.getUsername().isBlank()) {
                options.setUserName(mqttProperties.getUsername());
            }

            if (mqttProperties.getPassword() != null && !mqttProperties.getPassword().isBlank()) {
                options.setPassword(mqttProperties.getPassword().toCharArray());
            }

            mqttClient.connect(options);

            System.out.println("Conectado correctamente a EMQX: " + mqttProperties.getBrokerUrl());

        } catch (MqttException e) {
            throw new RuntimeException("No se pudo conectar a EMQX", e);
        }
    }

    public MqttClient getClient() {
        return mqttClient;
    }

    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    @PreDestroy
    public void disconnect() {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
                System.out.println("Cliente MQTT desconectado");
            }
        } catch (MqttException e) {
            System.err.println("Error desconectando cliente MQTT: " + e.getMessage());
        }
    }
}