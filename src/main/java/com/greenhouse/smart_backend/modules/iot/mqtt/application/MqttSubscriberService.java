package com.greenhouse.smart_backend.modules.iot.mqtt.application;

import com.greenhouse.smart_backend.modules.iot.mqtt.config.MqttProperties;
import com.greenhouse.smart_backend.modules.iot.service.SensorService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class MqttSubscriberService implements MqttCallbackExtended {

    private final MqttConnectionService mqttConnectionService;
    private final MqttProperties mqttProperties;

    private final SensorService sensorService;

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

        log.info("Suscrito a tópico info: {}", infoTopic);
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("Conexión MQTT completada. reconnect={}, serverURI={}", reconnect, serverURI);

        if (reconnect) {
            try {
                subscribeToTopics();
            } catch (MqttException e) {
                log.error("Error resuscribiendo tópicos MQTT: {}", e.getMessage());
            }
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.error("Conexión MQTT perdida: {}", cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);

        log.info("========== MENSAJE MQTT RECIBIDO ==========");
        log.info("Tópico: {}", topic);
        log.info("Payload: {}", payload);
        log.info("===========================================");

        if (topic.equals(mqttProperties.getTopics().getInfo())) {
            handleSensorInfo(payload);
        }
    }

    private void handleSensorInfo(String payload) {
        log.info("Procesando información recibida desde IoT: {}", payload);
        sensorService.saveSensorSubscriber(payload);
    }

    private void handleSensorReading(String payload) {
        log.info("Procesando lectura de sensor: {}", payload);
    }

    private void handleActuatorEvent(String payload) {
        log.info("Procesando evento de actuador: {}", payload);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.info("Mensaje MQTT entregado correctamente");
    }
}