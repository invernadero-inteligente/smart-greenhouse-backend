package com.greenhouse.smart_backend.modules.iot.mqtt.application;

import com.greenhouse.smart_backend.modules.iot.mqtt.config.MqttProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class MqttSubscriberService implements MqttCallbackExtended {

    private final MqttConnectionService mqttConnectionService;
    private final MqttProperties mqttProperties;

    private MqttClient mqttClient;

    @PostConstruct
    public void subscribe() throws MqttException {
        this.mqttClient = mqttConnectionService.getClient();

        mqttClient.setCallback(this);

        if (!mqttConnectionService.isConnected()) {
            throw new IllegalStateException("MQTT no está conectado. No se pueden suscribir tópicos.");
        }

        subscribeToTopics();
    }

    private void subscribeToTopics() throws MqttException {
        String infoTopic = mqttProperties.getTopics().getInfo();

        mqttClient.subscribe(infoTopic, 1);

        System.out.println("Suscrito a tópico info: " + infoTopic);
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        System.out.println("Conexión MQTT completada. reconnect=" + reconnect + ", serverURI=" + serverURI);

        if (reconnect) {
            try {
                subscribeToTopics();
            } catch (MqttException e) {
                System.err.println("Error resuscribiendo tópicos MQTT: " + e.getMessage());
            }
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.err.println("Conexión MQTT perdida: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);

        System.out.println("========== MENSAJE MQTT RECIBIDO ==========");
        System.out.println("Tópico: " + topic);
        System.out.println("Payload: " + payload);
        System.out.println("===========================================");

        if (topic.equals(mqttProperties.getTopics().getInfo())) {
            handleSensorInfo(payload);
        }
    }

    private void handleSensorInfo(String payload) {
        System.out.println("Procesando información recibida desde IoT: " + payload);

        // Luego aquí guardamos en MongoDB sensor_readings.
    }

    private void handleSensorReading(String payload) {
        System.out.println("Procesando lectura de sensor: " + payload);
    }

    private void handleActuatorEvent(String payload) {
        System.out.println("Procesando evento de actuador: " + payload);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("Mensaje MQTT entregado correctamente");
    }
}