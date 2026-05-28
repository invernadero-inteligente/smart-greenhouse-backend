package com.greenhouse.smart_backend.modules.alerts.service;

import com.greenhouse.smart_backend.modules.alerts.dto.request.AlertCreateRequest;
import com.greenhouse.smart_backend.modules.alerts.model.AlertOrigin;
import com.greenhouse.smart_backend.modules.alerts.model.AlertSeverity;
import com.greenhouse.smart_backend.modules.alerts.model.AlertStatus;
import com.greenhouse.smart_backend.modules.alerts.repository.AlertRepository;
import com.greenhouse.smart_backend.modules.crops.model.Crop;
import com.greenhouse.smart_backend.modules.crops.model.CropStatus;
import com.greenhouse.smart_backend.modules.crops.repository.CropRepository;
import com.greenhouse.smart_backend.modules.iot.document.SensorReadingDocument;
import com.greenhouse.smart_backend.modules.iot.service.SensorReadingEvaluationService;
import com.greenhouse.smart_backend.modules.thresholds.model.ThresholdConfig;
import com.greenhouse.smart_backend.modules.thresholds.repository.ThresholdConfigRepository;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import com.greenhouse.smart_backend.modules.zones.repository.ZoneRepository;
import com.greenhouse.smart_backend.shared.enums.ReadingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomaticAlertService {

    private static final int ALERT_COOLDOWN_MINUTES = 10;

    private final AlertService alertService;
    private final AlertRepository alertRepository;
    private final CropRepository cropRepository;
    private final ThresholdConfigRepository thresholdConfigRepository;
    private final ZoneRepository zoneRepository;

    @Transactional
    public void evaluateAndCreateAlert(SensorReadingDocument reading) {
        if (reading == null || reading.getNodeName() == null || reading.getVariableName() == null) {
            return;
        }

        String zoneName = reading.getNodeName().trim();
        String variableName = normalizeIdentifier(reading.getVariableName());

        if (zoneName.isBlank() || variableName.isBlank()) {
            return;
        }

        Zone zone = zoneRepository.findByName(zoneName).orElse(null);
        if (zone == null || !zone.isActive()) {
            log.debug("No se encontró una zona activa para la lectura MQTT: {}", zoneName);
            return;
        }

        ThresholdConfig threshold = thresholdConfigRepository.findByZoneIdAndVariableName(zone.getId(), variableName)
                .orElse(null);

        BigDecimal parsedValue = SensorReadingEvaluationService.parseValue(reading.getValue());
        ReadingStatus status = SensorReadingEvaluationService.resolveStatus(parsedValue, threshold);

        if (status != ReadingStatus.WARNING && status != ReadingStatus.CRITICAL) {
            return;
        }

        AlertSeverity severity = status == ReadingStatus.CRITICAL ? AlertSeverity.HIGH : AlertSeverity.MEDIUM;
        LocalDateTime since = LocalDateTime.now().minus(ALERT_COOLDOWN_MINUTES, ChronoUnit.MINUTES);

        boolean duplicateRecentAlert = !alertRepository
                .findAllByZoneIdAndVariableNameAndSeverityAndStatusAndCreatedAtGreaterThanEqual(
                        zone.getId(),
                        variableName,
                        severity,
                        AlertStatus.OPEN,
                        since)
                .isEmpty();

        if (duplicateRecentAlert) {
            log.debug("Se omitió alerta duplicada reciente para zona={}, variable={}, severidad={}", zone.getId(), variableName, severity);
            return;
        }

        Crop crop = resolveCrop(zone.getId());
        String unit = SensorReadingEvaluationService.resolveUnit(reading.getUnit(), threshold);
        String message = buildMessage(zone.getName(), variableName, unit, parsedValue, threshold, status);

        AlertCreateRequest request = new AlertCreateRequest();
        request.setZoneId(zone.getId());
        request.setCropId(crop != null ? crop.getId() : null);
        request.setOrigin(AlertOrigin.IOT);
        request.setVariableName(variableName);
        request.setUnit(unit);
        request.setSeverity(severity);
        request.setMessage(message);
        request.setValue(parsedValue);

        alertService.createAlert(request);
        log.info("Alerta automática creada para zona={}, variable={}, severidad={}", zone.getId(), variableName, severity);
    }

    private Crop resolveCrop(Long zoneId) {
        List<Crop> activeCrops = cropRepository.findAllByZoneIdAndStatus(zoneId, CropStatus.ACTIVE);
        if (!activeCrops.isEmpty()) {
            return activeCrops.get(0);
        }

        return cropRepository.findAllByZoneId(zoneId).stream().findFirst().orElse(null);
    }

    private String buildMessage(String zoneName, String variableName, String unit, BigDecimal value, ThresholdConfig threshold, ReadingStatus status) {
        StringBuilder message = new StringBuilder();
        message.append("Lectura ").append(status.name().toLowerCase())
                .append(" detectada en ").append(zoneName)
                .append(" para ").append(variableName);

        if (value != null) {
            message.append(": valor actual ").append(value);
            if (unit != null && !unit.isBlank() && !Objects.equals(unit, "N/A")) {
                message.append(' ').append(unit);
            }
        }

        if (threshold != null) {
            message.append(" fuera del rango [")
                    .append(threshold.getMinValue())
                    .append(", ")
                    .append(threshold.getMaxValue())
                    .append(']');
        }

        return message.toString();
    }

    private String normalizeIdentifier(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}