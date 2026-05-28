package com.greenhouse.smart_backend.modules.iot.service.impl;

import com.greenhouse.smart_backend.modules.ai.client.AIClient;
import com.greenhouse.smart_backend.modules.ai.dto.response.AIAnalysisResponseDTO;
import com.greenhouse.smart_backend.modules.ai.model.AiResult;
import com.greenhouse.smart_backend.modules.ai.repository.AiResultRepository;
import com.greenhouse.smart_backend.modules.iot.mapper.IOTMapper;
import com.greenhouse.smart_backend.modules.iot.model.Base64MultipartFile;
import com.greenhouse.smart_backend.modules.iot.mqtt.application.MqttPublisherService;
import com.greenhouse.smart_backend.modules.iot.service.CameraService;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import com.greenhouse.smart_backend.modules.zones.repository.ZoneRepository;
import com.greenhouse.smart_backend.shared.exceptions.ResourceNotFoundException;
import com.greenhouse.smart_backend.shared.exceptions.ValidationException;
import com.greenhouse.smart_backend.shared.utils.MultipartUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
@Service
public class CameraServiceImpl implements CameraService {
    private static final String PHOTO_VARIABLE = "PHOTO";
    private static final String BASE64_UNIT = "BASE64";

    private final ObjectMapper objectMapper;
    private final ZoneRepository zoneRepository;
    private final AiResultRepository aiResultRepository;
    private final AIClient aiClient;
    private final IOTMapper iotMapper;

    private final MqttPublisherService mqttPublisherService;

    @Override
    public void requestPhoto(Long zoneId) {

        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con ID: " + zoneId));

        String nodeName = zone.getName();

        mqttPublisherService.publishSensorRequest(
                nodeName,
                PHOTO_VARIABLE
        );
    }

    @Override
    @Transactional
    public boolean savePhotoIfPresent(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);

            String variableName = root.path("variable").path("name").asString(null);

            if (!PHOTO_VARIABLE.equalsIgnoreCase(variableName)) {
                return false;
            }

            String nodeName = root.path("node").path("name").asString(null);
            String unit = root.path("variable").path("unit").asString(null);
            String base64Image = root.path("variable").path("value").asString(null);

            String preview = base64Image == null
                    ? "NULL"
                    : base64Image.substring(0, Math.min(base64Image.length(), 100));

            log.info("PHOTO value length={}", base64Image != null ? base64Image.length() : 0);
            log.info("PHOTO value preview={}", preview);

            if (nodeName == null || nodeName.isBlank()) {
                throw new ValidationException("La foto llegó sin node.name");
            }

            if (!BASE64_UNIT.equalsIgnoreCase(unit)) {
                throw new ValidationException("La foto llegó con unit inválido. Esperado BASE64, recibido: " + unit);
            }

            if (base64Image == null || base64Image.isBlank()) {
                throw new ValidationException("La foto llegó sin contenido Base64");
            }

            Zone zone = zoneRepository.findByName(nodeName)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe una zona registrada con name: " + nodeName
                    ));

            Resource multipartFile =
                    MultipartUtils.base64ToResource(base64Image);
            AIAnalysisResponseDTO responseDTO = aiClient.analyzeImage(multipartFile);

            AiResult photo = iotMapper.toModel(responseDTO, base64Image, zone);

            AiResult saved = aiResultRepository.save(photo);

            log.info("Foto guardada exitosamente en ai_results. id={}, zoneId={}",
                    saved.getId(),
                    zone.getId()
            );

            return true;

        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error procesando foto recibida por MQTT", e);
            throw new ValidationException("Error procesando foto recibida por MQTT: " + e.getMessage());
        }
    }
}
