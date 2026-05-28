package com.greenhouse.smart_backend.modules.iot.service.impl;

import com.greenhouse.smart_backend.modules.iot.mapper.IOTMapper;
import com.greenhouse.smart_backend.modules.alerts.service.AutomaticAlertService;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import com.greenhouse.smart_backend.modules.iot.document.SensorReadingDocument;
import com.greenhouse.smart_backend.modules.iot.dto.request.SensorPayloadDTO;
import com.greenhouse.smart_backend.modules.iot.repository.SensorReadingMongoRepository;
import com.greenhouse.smart_backend.modules.iot.service.SensorService;
import com.greenhouse.smart_backend.shared.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {
    private final SensorReadingMongoRepository sensorReadingMongoRepository;
    private final ObjectMapper objectMapper;
    private final IOTMapper iotMapper;
    private final AutomaticAlertService automaticAlertService;

    @Transactional
    @Override
    public void saveSensorSubscriber(String payload) {
        log.info("========== PROCESANDO LECTURA MQTT ==========");
        log.info("Payload recibido: {}", payload);

        try {
            SensorPayloadDTO data = objectMapper.readValue(payload, SensorPayloadDTO.class);
            log.info("Payload parseado exitosamente");
            log.info("  - Node Name: {}", data.getNode() != null ? data.getNode().getName() : "NULL");
            log.info("  - Variable Name: {}", data.getVariable() != null ? data.getVariable().getName() : "NULL");
            log.info("  - Variable Value: {}", data.getVariable() != null ? data.getVariable().getValue() : "NULL");
            log.info("  - Variable Unit: {}", data.getVariable() != null ? data.getVariable().getUnit() : "NULL");

            SensorReadingDocument document = iotMapper.toDocument(data);
            log.info("DTO mapeado a documento Mongo:");
            log.info("  - Document ID: {}", document.getId());
            log.info("  - Document NodeName: {}", document.getNodeName());
            log.info("  - Document VariableName: {}", document.getVariableName());
            log.info("  - Document Value: {}", document.getValue());
            log.info("  - Document Timestamp: {}", document.getTimestamp());

            SensorReadingDocument response = sensorReadingMongoRepository.save(document);
            log.info("✓ Lectura de sensor guardada exitosamente en Mongo con ID: {}", response.getId());

            try {
                automaticAlertService.evaluateAndCreateAlert(response);
            } catch (Exception alertException) {
                log.warn("No se pudo generar la alerta automática para la lectura {}: {}", response.getId(), alertException.getMessage());
            }

            log.info("===========================================");
        } catch (Exception e) {
            log.error("✗ ERROR al guardar la lectura del sensor", e);
            log.error("  Mensaje de error: {}", e.getMessage());
            log.error("  Clase de error: {}", e.getClass().getName());
            log.error("===========================================");
            throw new ValidationException("Error al guardar la lectura del sensor: " + e.getMessage());
        }
    }
}
