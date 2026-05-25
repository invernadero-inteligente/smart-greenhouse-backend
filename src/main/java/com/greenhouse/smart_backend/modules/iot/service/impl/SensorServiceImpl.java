package com.greenhouse.smart_backend.modules.iot.service.impl;

import com.greenhouse.smart_backend.modules.iot.mapper.IOTMapper;
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

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {
    private final SensorReadingMongoRepository sensorReadingMongoRepository;
    private final ObjectMapper objectMapper;
    private final IOTMapper iotMapper;

    @Transactional
    @Override
    public void saveSensorSubscriber(String payload) {
        try {
            SensorPayloadDTO data = objectMapper.readValue(payload, SensorPayloadDTO.class);

            SensorReadingDocument document = iotMapper.toDocument(data);

            SensorReadingDocument response = sensorReadingMongoRepository.save(document);
            log.info("Lectura de sensor guardada exitosamente con ID: {}", response.getId());
        } catch (Exception e) {
            log.error("Error al guardar la lectura del sensor: {}", e.getMessage());
            throw new ValidationException("Error al guardar la lectura del sensor");
        }
    }
}
