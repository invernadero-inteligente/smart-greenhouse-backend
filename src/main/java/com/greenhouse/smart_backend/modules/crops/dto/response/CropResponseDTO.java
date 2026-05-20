package com.greenhouse.smart_backend.modules.crops.dto.response;

import com.greenhouse.smart_backend.modules.crops.model.CropStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CropResponseDTO {
    private Long id;
    private String name;
    private String variety;
    private Integer plantCount;
    private Long zoneId;
    private String zoneName;
    private LocalDate sowingDate;
    private CropStatus status;
    private BigDecimal sensorHeight;
    private CropConditionsResponse conditions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}