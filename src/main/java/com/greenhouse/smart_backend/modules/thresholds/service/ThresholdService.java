package com.greenhouse.smart_backend.modules.thresholds.service;

import com.greenhouse.smart_backend.modules.thresholds.dto.request.ThresholdCreateRequest;
import com.greenhouse.smart_backend.modules.thresholds.dto.request.ThresholdUpdateRequest;
import com.greenhouse.smart_backend.modules.thresholds.dto.response.ThresholdVariableResponseDTO;
import com.greenhouse.smart_backend.modules.thresholds.dto.response.ThresholdZoneResponseDTO;

import java.util.List;

public interface ThresholdService {
    List<ThresholdZoneResponseDTO> listThresholds(List<Long> zoneIds, List<String> variables, Boolean isActive);
    ThresholdVariableResponseDTO createThreshold(ThresholdCreateRequest request);
    void updateThreshold(Long id, ThresholdUpdateRequest request);
    void deactivateThreshold(Long id);
    void reactivateThreshold(Long id);
}

