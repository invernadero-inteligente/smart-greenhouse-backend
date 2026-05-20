package com.greenhouse.smart_backend.modules.iot.service.impl;

import com.greenhouse.smart_backend.modules.iot.document.ActuatorEventDocument;
import com.greenhouse.smart_backend.modules.iot.mqtt.application.MqttPublisherService;
import com.greenhouse.smart_backend.modules.iot.repository.ActuatorEventMongoRepository;
import com.greenhouse.smart_backend.modules.iot.service.ActuatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActuatorServiceImpl implements ActuatorService {
    private final MqttPublisherService mqttPublisherService;
    private final ActuatorEventMongoRepository actuatorEventMongoRepository;

    @Override
    public void saveActuatorPublisher(String nodeName, String actuatorName, String action) {
        log.info("Publicando comando de actuador: node={}, actuator={}, action={}", nodeName, actuatorName, action);
        mqttPublisherService.publishActuatorCommand(nodeName, actuatorName, action);

        ActuatorEventDocument document = ActuatorEventDocument.builder()
                .nodeName(nodeName)
                .actuatorName(actuatorName)
                .action(action)
                .build();

        ActuatorEventDocument saved = actuatorEventMongoRepository.save(document);
        log.info("ActuatorEvent almacenado con ID {}", saved.getId());
    }
}
