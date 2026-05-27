package com.greenhouse.smart_backend.modules.thresholds.service.impl;

import com.greenhouse.smart_backend.modules.thresholds.dto.request.ThresholdCreateRequest;
import com.greenhouse.smart_backend.modules.thresholds.dto.request.ThresholdUpdateRequest;
import com.greenhouse.smart_backend.modules.thresholds.dto.response.ThresholdVariableResponseDTO;
import com.greenhouse.smart_backend.modules.thresholds.dto.response.ThresholdZoneResponseDTO;
import com.greenhouse.smart_backend.modules.thresholds.mapper.ThresholdsMapper;
import com.greenhouse.smart_backend.modules.thresholds.model.ThresholdConfig;
import com.greenhouse.smart_backend.modules.thresholds.repository.ThresholdConfigRepository;
import com.greenhouse.smart_backend.modules.thresholds.service.ThresholdService;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import com.greenhouse.smart_backend.modules.zones.repository.ZoneRepository;
import com.greenhouse.smart_backend.shared.enums.SensorVariable;
import com.greenhouse.smart_backend.shared.exceptions.ResourceNotFoundException;
import com.greenhouse.smart_backend.shared.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThresholdServiceImpl implements ThresholdService {

    private final ThresholdConfigRepository repository;
    private final ZoneRepository zoneRepository;
    private final ThresholdsMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ThresholdZoneResponseDTO> listThresholds(List<Long> zoneIds, List<String> variables, Boolean isActive) {
        if (zoneIds == null || zoneIds.isEmpty()) {
            throw new ValidationException("zoneId", "zoneId es requerido y no puede estar vacío");
        }

        List<ThresholdConfig> rawConfigs = (variables == null || variables.isEmpty())
                ? repository.findByZoneIdIn(zoneIds)
                : repository.findByZoneIdInAndVariableNameIn(zoneIds, variables);

        List<ThresholdConfig> configs = isActive == null
                ? rawConfigs
                : rawConfigs.stream()
                .filter(config -> config.isActive() == isActive)
                .toList();

        Map<Long, List<ThresholdVariableResponseDTO>> groupedByZone = configs.stream()
                .collect(Collectors.groupingBy(c -> c.getZone().getId(),
                        Collectors.mapping(mapper::toVariableDTO, Collectors.toList())));

        return groupedByZone.entrySet().stream()
                .map(entry -> ThresholdZoneResponseDTO.builder()
                        .zoneId(entry.getKey())
                        .variables(entry.getValue())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public ThresholdVariableResponseDTO createThreshold(ThresholdCreateRequest request) {
        validateRange(request.getMinValue(), request.getMaxValue());

        String variableName = normalizeVariableName(request.getVariableName());
        if (repository.existsByZoneIdAndVariableNameAndIsActiveTrue(request.getZoneId(), variableName)) {
            throw new ValidationException("variableName", "ya existe un umbral para esta variable en la zona indicada");
        }

        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con id: " + request.getZoneId()));

        ThresholdConfig config = repository.findByZoneIdAndVariableName(request.getZoneId(), variableName)
                .map(existing -> {
                    existing.setZone(zone);
                    existing.setVariableName(variableName);
                    existing.setUnit(request.getUnit());
                    existing.setMinValue(request.getMinValue());
                    existing.setMaxValue(request.getMaxValue());
                    existing.setActive(true);
                    return existing;
                })
                .orElseGet(() -> ThresholdConfig.builder()
                        .zone(zone)
                        .variableName(variableName)
                        .unit(request.getUnit())
                        .minValue(request.getMinValue())
                        .maxValue(request.getMaxValue())
                        .isActive(true)
                        .build());

        ThresholdConfig saved = repository.save(config);
        log.info("Threshold creado id={} zoneId={} variable={}", saved.getId(), request.getZoneId(), variableName);
        return mapper.toVariableDTO(saved);
    }

    @Override
    @Transactional
    public void updateThreshold(Long id, ThresholdUpdateRequest request) {
        validateRange(request.getMinValue(), request.getMaxValue());

        ThresholdConfig config = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Threshold config no encontrado con id: " + id));

        config.setMinValue(request.getMinValue());
        config.setMaxValue(request.getMaxValue());
        repository.save(config);
        log.info("Threshold actualizado id={}", id);
    }

    @Override
    @Transactional
    public void deactivateThreshold(Long id) {
        ThresholdConfig config = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Threshold config no encontrado con id: " + id));

        config.setActive(false);
        repository.save(config);
        log.info("Threshold desactivado id={}", id);
    }

    @Override
    @Transactional
    public void reactivateThreshold(Long id) {
        ThresholdConfig config = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Threshold config no encontrado con id: " + id));

        config.setActive(true);
        repository.save(config);
        log.info("Threshold reactivado id={}", id);
    }

    private void validateRange(BigDecimal minValue, BigDecimal maxValue) {
        if (minValue.compareTo(maxValue) >= 0) {
            throw new ValidationException("minValue", "minValue debe ser menor que maxValue");
        }
    }

    private String normalizeVariableName(String variableName) {
        String normalized = variableName.trim().toUpperCase();
        try {
            SensorVariable.valueOf(normalized);
            return normalized;
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("variableName", "variableName debe ser una variable valida del enum SensorVariable");
        }
    }
}
