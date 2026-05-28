package com.greenhouse.smart_backend.modules.thresholds.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ThresholdVariableResponseDTO {
    private Long id;
    private String name;
    private String unit;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

