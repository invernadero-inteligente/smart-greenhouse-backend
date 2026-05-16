package com.greenhouse.smart_backend.modules.thresholds.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ThresholdZoneResponseDTO {
    private Long zoneId;
    private List<ThresholdVariableResponseDTO> variables;
}

