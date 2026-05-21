package com.greenhouse.smart_backend.modules.iot.service.impl;

import com.greenhouse.smart_backend.modules.actuators.model.Actuator;
import com.greenhouse.smart_backend.modules.actuators.repository.ActuatorJPARepository;
import com.greenhouse.smart_backend.modules.iot.document.ActuatorEventDocument;
import com.greenhouse.smart_backend.modules.iot.mapper.IOTMapper;
import com.greenhouse.smart_backend.modules.iot.mqtt.application.MqttPublisherService;
import com.greenhouse.smart_backend.modules.iot.repository.ActuatorEventMongoRepository;
import com.greenhouse.smart_backend.modules.iot.service.ActuatorService;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import com.greenhouse.smart_backend.modules.zones.repository.ZoneRepository;
import com.greenhouse.smart_backend.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActuatorServiceImpl implements ActuatorService {
    private final MqttPublisherService mqttPublisherService;
    private final ActuatorEventMongoRepository actuatorEventMongoRepository;
    private final ZoneRepository zoneRepository;
    private final IOTMapper iotMapper;
    private final ActuatorJPARepository actuatorJPARepository;

    @Transactional
    @Override
    public void saveActuatorPublisher(Long nodeId, String actuatorName, String action) {
        log.info("Publicando comando de actuador: node={}, actuator={}, action={}", nodeId, actuatorName, action);
        Zone zone = zoneRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con ID: " + nodeId));
        String nodeName = zone.getName();
        persistActuatorMongo(nodeName, actuatorName, action);
        persistActuatorJPA(nodeId, actuatorName, action);

        mqttPublisherService.publishActuatorCommand(nodeName, actuatorName, action);
    }

    private void persistActuatorMongo(String nodeName, String actuatorName, String action) {
        ActuatorEventDocument document = iotMapper.toDocument(nodeName, actuatorName, action);
        ActuatorEventDocument saved = actuatorEventMongoRepository.save(document);
        log.info("ActuatorEvent almacenado con ID {}", saved.getId());
    }

    private void persistActuatorJPA(Long nodeId, String actuatorName, String action) {
        Actuator actuator = actuatorJPARepository.findByZoneIdAndName(nodeId, actuatorName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Actuador no encontrado con nombre: " + actuatorName + " en zona ID: " + nodeId));
        actuator.setCurrentAction(action);
        actuator.setUpdatedAt(LocalDateTime.now());

        actuatorJPARepository.save(actuator);
        log.info("Actuador {} de la zona {} actualizado con acción: {}", actuatorName, actuator.getZone().getName(), action);
    }
}
