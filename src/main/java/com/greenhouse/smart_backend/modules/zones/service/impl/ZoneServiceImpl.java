package com.greenhouse.smart_backend.modules.zones.service.impl;

import com.greenhouse.smart_backend.modules.zones.dto.request.ZoneCreateRequest;
import com.greenhouse.smart_backend.modules.zones.dto.request.ZoneUpdateRequest;
import com.greenhouse.smart_backend.modules.zones.dto.response.ZoneListResponseDTO;
import com.greenhouse.smart_backend.modules.zones.dto.response.ZoneResponseDTO;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import com.greenhouse.smart_backend.modules.zones.repository.ZoneRepository;
import com.greenhouse.smart_backend.modules.zones.service.ZoneService;
import com.greenhouse.smart_backend.shared.exceptions.ResourceNotFoundException;
import com.greenhouse.smart_backend.shared.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository zoneRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ZoneListResponseDTO> listZones(Boolean isActive) {
        List<Zone> zones = isActive != null
                ? zoneRepository.findAllByIsActive(isActive)
                : zoneRepository.findAll();

        return zones.stream().map(this::toListDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ZoneResponseDTO getZoneById(Long id) {
        return toResponseDTO(findOrThrow(id));
    }

    @Override
    @Transactional
    public ZoneResponseDTO createZone(ZoneCreateRequest request) {
        if (zoneRepository.existsByName(request.getName())) {
            throw new ValidationException("name",
                    "Ya existe una zona con el nombre: " + request.getName());
        }

        Zone zone = Zone.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(true)
                .build();

        zone = zoneRepository.save(zone);
        log.info("Zona creada con id: {}", zone.getId());
        return toResponseDTO(zone);
    }

    @Override
    @Transactional
    public ZoneResponseDTO updateZone(Long id, ZoneUpdateRequest request) {
        Zone zone = findOrThrow(id);

        if (request.getName() != null) {
            if (!request.getName().equals(zone.getName())
                    && zoneRepository.existsByName(request.getName())) {
                throw new ValidationException("name",
                        "Ya existe una zona con el nombre: " + request.getName());
            }
            zone.setName(request.getName());
        }

        if (request.getDescription() != null) zone.setDescription(request.getDescription());
        if (request.getIsActive() != null)    zone.setActive(request.getIsActive());

        zoneRepository.save(zone);
        log.info("Zona actualizada con id: {}", zone.getId());
        return toResponseDTO(zone);
    }

    // ── Métodos privados ──────────────────────────────────────────────────────

    private Zone findOrThrow(Long id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Zona no encontrada con id: " + id));
    }

    private ZoneListResponseDTO toListDTO(Zone zone) {
        return ZoneListResponseDTO.builder()
                .id(zone.getId())
                .name(zone.getName())
                .description(zone.getDescription())
                .isActive(zone.isActive())
                .build();
    }

    private ZoneResponseDTO toResponseDTO(Zone zone) {
        return ZoneResponseDTO.builder()
                .id(zone.getId())
                .name(zone.getName())
                .description(zone.getDescription())
                .isActive(zone.isActive())
                .createdAt(zone.getCreatedAt())
                .updatedAt(zone.getUpdatedAt())
                .build();
    }
}
