package com.greenhouse.smart_backend.modules.actuators.service.impl;

import com.greenhouse.smart_backend.modules.actuators.dto.request.ActuatorCommandRequest;
import com.greenhouse.smart_backend.modules.actuators.dto.request.ActuatorCreateRequest;
import com.greenhouse.smart_backend.modules.actuators.dto.request.ActuatorUpdateRequest;
import com.greenhouse.smart_backend.modules.actuators.dto.response.ActuatorListResponseDTO;
import com.greenhouse.smart_backend.modules.actuators.dto.response.ActuatorResponseDTO;
import com.greenhouse.smart_backend.modules.actuators.model.Actuator;
import com.greenhouse.smart_backend.modules.actuators.repository.ActuatorJPARepository;
import com.greenhouse.smart_backend.modules.actuators.service.ActuatorManagementService;
import com.greenhouse.smart_backend.modules.iot.service.ActuatorService;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import com.greenhouse.smart_backend.modules.zones.repository.ZoneRepository;
import com.greenhouse.smart_backend.shared.enums.ActuatorAction;
import com.greenhouse.smart_backend.shared.exceptions.ResourceNotFoundException;
import com.greenhouse.smart_backend.shared.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActuatorManagementServiceImpl implements ActuatorManagementService {

    private final ActuatorJPARepository actuatorJPARepository;
    private final ZoneRepository zoneRepository;
    private final ActuatorService actuatorService;

    @Override
    @Transactional(readOnly = true)
    public List<ActuatorListResponseDTO> listActuators(Long zoneId) {
        if (zoneId != null) {
            findZoneOrThrow(zoneId);
        }

        return actuatorJPARepository.findAll().stream()
                .filter(actuator -> zoneId == null || actuator.getZone().getId().equals(zoneId))
                .sorted(Comparator
                        .comparing((Actuator actuator) -> actuator.getZone().getName(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Actuator::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toListDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ActuatorResponseDTO getActuatorById(Long id) {
        return toResponseDTO(findActuatorOrThrow(id));
    }

    @Override
    @Transactional
    public ActuatorResponseDTO createActuator(ActuatorCreateRequest request) {
        Zone zone = findZoneOrThrow(request.getZoneId());
        String name = normalizeName(request.getName());
        validateUniqueActuator(zone.getId(), name, null);

        Actuator actuator = Actuator.builder()
                .zone(zone)
                .name(name)
                .currentAction(resolveActionName(request.getCurrentAction()))
                .build();

        actuator = actuatorJPARepository.save(actuator);
        log.info("Actuador creado con id: {}", actuator.getId());
        return toResponseDTO(actuator);
    }

    @Override
    @Transactional
    public ActuatorResponseDTO updateActuator(Long id, ActuatorUpdateRequest request) {
        Actuator actuator = findActuatorOrThrow(id);

        Zone targetZone = request.getZoneId() != null
                ? findZoneOrThrow(request.getZoneId())
                : actuator.getZone();
        String targetName = request.getName() != null ? normalizeName(request.getName()) : actuator.getName();

        validateUniqueActuator(targetZone.getId(), targetName, actuator.getId());

        actuator.setZone(targetZone);
        actuator.setName(targetName);
        if (request.getCurrentAction() != null) {
            actuator.setCurrentAction(request.getCurrentAction().name());
        }

        actuator = actuatorJPARepository.save(actuator);
        log.info("Actuador actualizado con id: {}", actuator.getId());
        return toResponseDTO(actuator);
    }

    @Override
    @Transactional
    public void deleteActuator(Long id) {
        Actuator actuator = findActuatorOrThrow(id);
        actuatorJPARepository.delete(actuator);
        log.info("Actuador eliminado con id: {}", id);
    }

    @Override
    @Transactional
    public ActuatorResponseDTO executeCommand(Long id, ActuatorCommandRequest request) {
        Actuator actuator = findActuatorOrThrow(id);
        actuatorService.saveActuatorPublisher(
                actuator.getZone().getId(),
                actuator.getName(),
                request.getAction().name());

        return toResponseDTO(findActuatorOrThrow(id));
    }

    private Actuator findActuatorOrThrow(Long id) {
        return actuatorJPARepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actuador no encontrado con id: " + id));
    }

    private Zone findZoneOrThrow(Long id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zona no encontrada con id: " + id));
    }

    private void validateUniqueActuator(Long zoneId, String name, Long currentId) {
        actuatorJPARepository.findByZoneIdAndName(zoneId, name)
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ValidationException("name",
                            "Ya existe un actuador con nombre: " + name + " en la zona ID: " + zoneId);
                });
    }

    private String normalizeName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new ValidationException("name", "El nombre del actuador no puede estar vacío");
        }
        return name.trim();
    }

    private String resolveActionName(ActuatorAction action) {
        return action != null ? action.name() : ActuatorAction.OFF.name();
    }

    private ActuatorListResponseDTO toListDTO(Actuator actuator) {
        return ActuatorListResponseDTO.builder()
                .id(actuator.getId())
                .zoneId(actuator.getZone().getId())
                .zoneName(actuator.getZone().getName())
                .name(actuator.getName())
                .currentAction(toAction(actuator.getCurrentAction()))
                .updatedAt(actuator.getUpdatedAt())
                .build();
    }

    private ActuatorResponseDTO toResponseDTO(Actuator actuator) {
        return ActuatorResponseDTO.builder()
                .id(actuator.getId())
                .zoneId(actuator.getZone().getId())
                .zoneName(actuator.getZone().getName())
                .name(actuator.getName())
                .currentAction(toAction(actuator.getCurrentAction()))
                .createdAt(actuator.getCreatedAt())
                .updatedAt(actuator.getUpdatedAt())
                .build();
    }

    private ActuatorAction toAction(String currentAction) {
        if (!StringUtils.hasText(currentAction)) {
            return null;
        }

        try {
            return ActuatorAction.valueOf(currentAction.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("Acción de actuador inválida encontrada en BD: {}", currentAction);
            return null;
        }
    }
}




