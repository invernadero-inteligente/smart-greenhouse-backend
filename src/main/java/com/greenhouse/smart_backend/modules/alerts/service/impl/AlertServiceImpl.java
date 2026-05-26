package com.greenhouse.smart_backend.modules.alerts.service.impl;

import com.greenhouse.smart_backend.modules.alerts.dto.request.AlertCreateRequest;
import com.greenhouse.smart_backend.modules.alerts.dto.response.AlertResponseDTO;
import com.greenhouse.smart_backend.modules.alerts.model.Alert;
import com.greenhouse.smart_backend.modules.alerts.model.AlertStatus;
import com.greenhouse.smart_backend.modules.alerts.repository.AlertRepository;
import com.greenhouse.smart_backend.modules.alerts.repository.AlertSpecification;
import com.greenhouse.smart_backend.modules.alerts.service.AlertService;
import com.greenhouse.smart_backend.modules.crops.model.Crop;
import com.greenhouse.smart_backend.modules.crops.repository.CropRepository;
import com.greenhouse.smart_backend.modules.users.model.User;
import com.greenhouse.smart_backend.modules.users.repository.UserRepository;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import com.greenhouse.smart_backend.modules.zones.repository.ZoneRepository;
import com.greenhouse.smart_backend.shared.exceptions.ResourceNotFoundException;
import com.greenhouse.smart_backend.shared.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final ZoneRepository zoneRepository;
    private final CropRepository cropRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AlertResponseDTO> listAlerts(
            AlertStatus status, Long zoneId, Long cropId,
            LocalDateTime from, LocalDateTime to) {

    return alertRepository
            .findAll(AlertSpecification.withFilters(status, zoneId, cropId, from, to))
            .stream()
            .map(this::toResponseDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlertResponseDTO getAlertById(Long id) {
        return toResponseDTO(findOrThrow(id));
    }

    @Override
    @Transactional
    public AlertResponseDTO createAlert(AlertCreateRequest request) {
        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Zona no encontrada con id: " + request.getZoneId()));

        Crop crop = null;
        if (request.getCropId() != null) {
            crop = cropRepository.findById(request.getCropId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Cultivo no encontrado con id: " + request.getCropId()));
        }

        Alert alert = Alert.builder()
                .zone(zone)
                .crop(crop)
                .origin(request.getOrigin())
                .variableName(request.getVariableName())
                .unit(request.getUnit())
                .severity(request.getSeverity())
                .message(request.getMessage())
                .value(request.getValue())
                .status(AlertStatus.OPEN)
                .build();

        alert = alertRepository.save(alert);
        log.info("Alerta creada con id: {}", alert.getId());
        return toResponseDTO(alert);
    }

    @Override
    @Transactional
    public void attendAlert(Long id, Long userId) {
        Alert alert = findOrThrow(id);

        if (alert.getStatus() == AlertStatus.ATTENDED) {
            throw new ValidationException("status",
                    "La alerta ya fue atendida anteriormente");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id: " + userId));

        alert.setStatus(AlertStatus.ATTENDED);
        alert.setAttendedBy(user);
        alert.setAttendedAt(LocalDateTime.now());

        alertRepository.save(alert);
        log.info("Alerta {} marcada como atendida por usuario {}", id, userId);
    }

    // ── Métodos privados ──────────────────────────────────────────────────────

    private Alert findOrThrow(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alerta no encontrada con id: " + id));
    }

    private AlertResponseDTO toResponseDTO(Alert alert) {
        return AlertResponseDTO.builder()
                .id(alert.getId())
                .zoneId(alert.getZone() != null ? alert.getZone().getId() : null)
                .zoneName(alert.getZone() != null ? alert.getZone().getName() : null)
                .cropId(alert.getCrop() != null ? alert.getCrop().getId() : null)
                .cropName(alert.getCrop() != null ? alert.getCrop().getName() : null)
                .origin(alert.getOrigin())
                .variableName(alert.getVariableName())
                .unit(alert.getUnit())
                .severity(alert.getSeverity())
                .message(alert.getMessage())
                .value(alert.getValue())
                .status(alert.getStatus())
                .attendedById(alert.getAttendedBy() != null
                        ? alert.getAttendedBy().getId() : null)
                .attendedByName(alert.getAttendedBy() != null
                        ? alert.getAttendedBy().getFullName() : null)
                .attendedAt(alert.getAttendedAt())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
