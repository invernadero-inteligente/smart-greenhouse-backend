package com.greenhouse.smart_backend.modules.crops.service.impl;

import com.greenhouse.smart_backend.modules.crops.dto.request.CropConditionsRequest;
import com.greenhouse.smart_backend.modules.crops.dto.request.CropCreateRequest;
import com.greenhouse.smart_backend.modules.crops.dto.request.CropUpdateRequest;
import com.greenhouse.smart_backend.modules.crops.dto.response.CropConditionsResponse;
import com.greenhouse.smart_backend.modules.crops.dto.response.CropListResponseDTO;
import com.greenhouse.smart_backend.modules.crops.dto.response.CropResponseDTO;
import com.greenhouse.smart_backend.modules.crops.model.Crop;
import com.greenhouse.smart_backend.modules.crops.model.CropCondition;
import com.greenhouse.smart_backend.modules.crops.model.CropStatus;
import com.greenhouse.smart_backend.modules.crops.repository.CropConditionRepository;
import com.greenhouse.smart_backend.modules.crops.repository.CropRepository;
import com.greenhouse.smart_backend.modules.crops.service.CropService;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import com.greenhouse.smart_backend.modules.zones.repository.ZoneRepository;
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
public class CropServiceImpl implements CropService {

    private final CropRepository cropRepository;
    private final CropConditionRepository cropConditionRepository;
    private final ZoneRepository zoneRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CropListResponseDTO> listCrops(CropStatus status, Long zoneId) {
        List<Crop> crops;

        if (status != null && zoneId != null) {
            crops = cropRepository.findAllByZoneIdAndStatus(zoneId, status);
        } else if (status != null) {
            crops = cropRepository.findAllByStatus(status);
        } else if (zoneId != null) {
            crops = cropRepository.findAllByZoneId(zoneId);
        } else {
            crops = cropRepository.findAll();
        }

        return crops.stream().map(this::toCropListDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CropResponseDTO getCropById(Long id) {
        Crop crop = findCropOrThrow(id);
        CropCondition conditions = cropConditionRepository.findByCropId(id).orElse(null);
        return toCropResponseDTO(crop, conditions);
    }

    @Override
    @Transactional
    public CropResponseDTO createCrop(CropCreateRequest request) {
        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Zona no encontrada con id: " + request.getZoneId()));

        Crop crop = Crop.builder()
                .zone(zone)
                .name(request.getName())
                .variety(request.getVariety())
                .plantCount(request.getPlantCount() != null ? request.getPlantCount() : 0)
                .sowingDate(request.getSowingDate())
                .status(request.getStatus())
                .build();

        crop = cropRepository.save(crop);
        log.info("Cultivo creado con id: {}", crop.getId());

        CropCondition conditions = null;
        if (request.getConditions() != null) {
            conditions = buildCropCondition(crop, request.getConditions());
            cropConditionRepository.save(conditions);
        }

        return toCropResponseDTO(crop, conditions);
    }

    @Override
    @Transactional
    public CropResponseDTO updateCrop(Long id, CropUpdateRequest request) {
        Crop crop = findCropOrThrow(id);

        if (crop.getStatus() == CropStatus.FINISHED) {
            throw new ValidationException("status",
                    "Un cultivo finalizado no puede ser modificado");
        }

        if (request.getName() != null)       crop.setName(request.getName());
        if (request.getVariety() != null)    crop.setVariety(request.getVariety());
        if (request.getPlantCount() != null) crop.setPlantCount(request.getPlantCount());
        if (request.getSowingDate() != null) crop.setSowingDate(request.getSowingDate());
        if (request.getStatus() != null)     crop.setStatus(request.getStatus());

        if (request.getZoneId() != null) {
            Zone zone = zoneRepository.findById(request.getZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Zona no encontrada con id: " + request.getZoneId()));
            crop.setZone(zone);
        }

        cropRepository.save(crop);
        log.info("Cultivo actualizado con id: {}", crop.getId());

        CropCondition conditions = cropConditionRepository.findByCropId(id).orElse(null);
        if (request.getConditions() != null) {
            if (conditions == null) {
                conditions = buildCropCondition(crop, request.getConditions());
            } else {
                updateCropCondition(conditions, request.getConditions());
            }
            cropConditionRepository.save(conditions);
        }

        return toCropResponseDTO(crop, conditions);
    }

    // ── Métodos privados ──────────────────────────────────────────────────────

    private Crop findCropOrThrow(Long id) {
        return cropRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cultivo no encontrado con id: " + id));
    }

    private CropCondition buildCropCondition(Crop crop, CropConditionsRequest req) {
        CropCondition.CropConditionBuilder builder = CropCondition.builder().crop(crop);
        if (req.getTemperature() != null) {
            builder.temperatureMin(req.getTemperature().getMin())
                   .temperatureMax(req.getTemperature().getMax());
        }
        if (req.getAirHumidity() != null) {
            builder.airHumidityMin(req.getAirHumidity().getMin())
                   .airHumidityMax(req.getAirHumidity().getMax());
        }
        if (req.getSoilMoisture() != null) {
            builder.soilMoistureMin(req.getSoilMoisture().getMin())
                   .soilMoistureMax(req.getSoilMoisture().getMax());
        }
        if (req.getPh() != null) {
            builder.phMin(req.getPh().getMin())
                   .phMax(req.getPh().getMax());
        }
        return builder.build();
    }

    private void updateCropCondition(CropCondition c, CropConditionsRequest req) {
        if (req.getTemperature() != null) {
            if (req.getTemperature().getMin() != null) c.setTemperatureMin(req.getTemperature().getMin());
            if (req.getTemperature().getMax() != null) c.setTemperatureMax(req.getTemperature().getMax());
        }
        if (req.getAirHumidity() != null) {
            if (req.getAirHumidity().getMin() != null) c.setAirHumidityMin(req.getAirHumidity().getMin());
            if (req.getAirHumidity().getMax() != null) c.setAirHumidityMax(req.getAirHumidity().getMax());
        }
        if (req.getSoilMoisture() != null) {
            if (req.getSoilMoisture().getMin() != null) c.setSoilMoistureMin(req.getSoilMoisture().getMin());
            if (req.getSoilMoisture().getMax() != null) c.setSoilMoistureMax(req.getSoilMoisture().getMax());
        }
        if (req.getPh() != null) {
            if (req.getPh().getMin() != null) c.setPhMin(req.getPh().getMin());
            if (req.getPh().getMax() != null) c.setPhMax(req.getPh().getMax());
        }
    }

    private CropListResponseDTO toCropListDTO(Crop crop) {
        return CropListResponseDTO.builder()
                .id(crop.getId())
                .name(crop.getName())
                .variety(crop.getVariety())
                .plantCount(crop.getPlantCount())
                .zoneId(crop.getZone().getId())
                .zoneName(crop.getZone().getName())
                .sowingDate(crop.getSowingDate())
                .status(crop.getStatus())
                .build();
    }

    private CropResponseDTO toCropResponseDTO(Crop crop, CropCondition conditions) {
        CropConditionsResponse conditionsResponse = null;
        if (conditions != null) {
            conditionsResponse = CropConditionsResponse.builder()
                    .temperatureMin(conditions.getTemperatureMin())
                    .temperatureMax(conditions.getTemperatureMax())
                    .airHumidityMin(conditions.getAirHumidityMin())
                    .airHumidityMax(conditions.getAirHumidityMax())
                    .soilMoistureMin(conditions.getSoilMoistureMin())
                    .soilMoistureMax(conditions.getSoilMoistureMax())
                    .phMin(conditions.getPhMin())
                    .phMax(conditions.getPhMax())
                    .build();
        }

        return CropResponseDTO.builder()
                .id(crop.getId())
                .name(crop.getName())
                .variety(crop.getVariety())
                .plantCount(crop.getPlantCount())
                .zoneId(crop.getZone().getId())
                .zoneName(crop.getZone().getName())
                .sowingDate(crop.getSowingDate())
                .status(crop.getStatus())
                .conditions(conditionsResponse)
                .createdAt(crop.getCreatedAt())
                .updatedAt(crop.getUpdatedAt())
                .build();
    }
}
