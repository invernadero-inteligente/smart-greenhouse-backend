package com.greenhouse.smart_backend.modules.dashboard.service.impl;

import com.greenhouse.smart_backend.modules.dashboard.dto.response.DashboardLatestReadingsResponseDTO;
import com.greenhouse.smart_backend.modules.dashboard.dto.response.DashboardReadingDTO;
import com.greenhouse.smart_backend.modules.dashboard.dto.response.DashboardZoneReadingsDTO;
import com.greenhouse.smart_backend.modules.dashboard.service.DashboardService;
import com.greenhouse.smart_backend.modules.iot.document.SensorReadingDocument;
import com.greenhouse.smart_backend.modules.iot.repository.SensorReadingMongoRepository;
import com.greenhouse.smart_backend.modules.thresholds.model.ThresholdConfig;
import com.greenhouse.smart_backend.modules.thresholds.repository.ThresholdConfigRepository;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import com.greenhouse.smart_backend.modules.zones.repository.ZoneRepository;
import com.greenhouse.smart_backend.shared.enums.ReadingStatus;
import com.greenhouse.smart_backend.shared.exceptions.ResourceNotFoundException;
import com.greenhouse.smart_backend.shared.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private static final Duration ONLINE_WINDOW = Duration.ofMinutes(2);

    private final ZoneRepository zoneRepository;
    private final ThresholdConfigRepository thresholdConfigRepository;
    private final SensorReadingMongoRepository sensorReadingMongoRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardLatestReadingsResponseDTO getLatestReadings(Long zoneId) {
        if (zoneId != null && zoneId <= 0) {
            throw new ValidationException("zoneId", "zoneId debe ser mayor que cero");
        }

        List<Zone> zones = resolveZones(zoneId);
        Instant generatedAt = Instant.now();

        if (zones.isEmpty()) {
            return DashboardLatestReadingsResponseDTO.builder()
                    .generatedAt(generatedAt)
                    .zones(List.of())
                    .build();
        }

        List<Long> zoneIds = zones.stream().map(Zone::getId).toList();
        Map<Long, List<ThresholdConfig>> thresholdsByZoneId = thresholdConfigRepository.findByZoneIdIn(zoneIds).stream()
                .collect(Collectors.groupingBy(config -> config.getZone().getId()));

        List<DashboardZoneReadingsDTO> zoneReadings = zones.stream()
                .map(zone -> buildZoneReadings(zone, thresholdsByZoneId.getOrDefault(zone.getId(), List.of()), generatedAt))
                .toList();

        return DashboardLatestReadingsResponseDTO.builder()
                .generatedAt(generatedAt)
                .zones(zoneReadings)
                .build();
    }

    private List<Zone> resolveZones(Long zoneId) {
        if (zoneId == null) {
            return zoneRepository.findAllByIsActiveTrue();
        }

        Zone zone = zoneRepository.findById(zoneId)
                .filter(Zone::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada o inactiva con id: " + zoneId));

        return List.of(zone);
    }

    private DashboardZoneReadingsDTO buildZoneReadings(Zone zone, List<ThresholdConfig> thresholds, Instant now) {
        Map<String, ThresholdConfig> thresholdsByVariable = thresholds.stream()
                .filter(config -> config.getVariableName() != null && !config.getVariableName().isBlank())
                .collect(Collectors.toMap(
                        config -> normalizeIdentifier(config.getVariableName()),
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new));

        List<SensorReadingDocument> latestReadings = sensorReadingMongoRepository.findByNodeNameOrderByTimestampDesc(zone.getName());
        Map<String, SensorReadingDocument> latestByVariable = latestReadings.stream()
                .filter(reading -> reading.getVariableName() != null && !reading.getVariableName().isBlank())
                .sorted(Comparator.comparing(SensorReadingDocument::getTimestamp,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toMap(
                        reading -> normalizeIdentifier(reading.getVariableName()),
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new));

        List<DashboardReadingDTO> readings = latestByVariable.values().stream()
                .map(reading -> toReadingDTO(reading, thresholdsByVariable.get(normalizeIdentifier(reading.getVariableName())), now))
                .sorted(Comparator.comparing(DashboardReadingDTO::getVariable, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();

        Instant lastReadingAt = latestByVariable.values().stream()
                .map(SensorReadingDocument::getTimestamp)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return DashboardZoneReadingsDTO.builder()
                .zoneId(zone.getId())
                .zoneName(zone.getName())
                .description(zone.getDescription())
                .isActive(zone.isActive())
                .online(isOnline(lastReadingAt, now))
                .lastReadingAt(lastReadingAt)
                .readings(readings)
                .build();
    }

    private DashboardReadingDTO toReadingDTO(SensorReadingDocument reading, ThresholdConfig threshold, Instant now) {
        BigDecimal parsedValue = parseValue(reading.getValue());
        ReadingStatus status = threshold == null
                ? ReadingStatus.UNKNOWN
                : ReadingStatus.from(parsedValue, threshold.getMinValue(), threshold.getMaxValue());

        String unit = threshold != null && threshold.getUnit() != null && !threshold.getUnit().isBlank()
                ? threshold.getUnit()
                : reading.getUnit();

        return DashboardReadingDTO.builder()
                .variable(normalizeIdentifier(reading.getVariableName()))
                .value(reading.getValue())
                .unit(unit)
                .status(status)
                .timestamp(reading.getTimestamp())
                .online(isOnline(reading.getTimestamp(), now))
                .build();
    }

    private boolean isOnline(Instant timestamp, Instant now) {
        return timestamp != null && !timestamp.isBefore(now.minus(ONLINE_WINDOW));
    }

    private String normalizeIdentifier(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private BigDecimal parseValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(value.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            log.debug("No se pudo convertir el valor '{}' a número", value);
            return null;
        }
    }
}

