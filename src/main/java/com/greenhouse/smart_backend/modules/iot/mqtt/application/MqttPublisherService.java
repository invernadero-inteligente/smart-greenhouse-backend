package com.greenhouse.smart_backend.modules.iot.mqtt.application;

import com.greenhouse.smart_backend.modules.iot.mqtt.config.MqttProperties;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MqttPublisherService {

    private final MqttClient mqttClient;
    private final MqttProperties mqttProperties;
    private final ObjectMapper objectMapper;

    public void publishSensorRequest(String nodeName, String variableName) {
        try {
            Map<String, Object> payload = Map.of(
                    "node", Map.of(
                            "name", nodeName
                    ),
                    "variable", Map.of(
                            "name", variableName
                    )
            );

            publish(mqttProperties.getTopics().getSensors(), payload);

        } catch (Exception e) {
            throw new RuntimeException("Error publicando solicitud de sensor por MQTT", e);
        }
    }

    public void publishActuatorCommand(String nodeName, String actuatorName, String action) {
        try {
            Map<String, Object> payload = Map.of(
                    "node", Map.of(
                            "name", nodeName
                    ),
                    "actuator", Map.of(
                            "name", actuatorName,
                            "action", action
                    )
            );

            publish(mqttProperties.getTopics().getActuators(), payload);

        } catch (Exception e) {
            throw new RuntimeException("Error publicando comando de actuador por MQTT", e);
        }
    }

    private void publish(String topic, Object payload) throws Exception {
        if (!mqttClient.isConnected()) {
            throw new IllegalStateException("El cliente MQTT no está conectado");
        }

        String json = objectMapper.writeValueAsString(payload);

        MqttMessage message = new MqttMessage(json.getBytes(StandardCharsets.UTF_8));
        message.setQos(1);
        message.setRetained(false);

        mqttClient.publish(topic, message);

        System.out.println("Mensaje publicado en tópico: " + topic);
        System.out.println(json);
    }
}