package com.greenhouse.smart_backend.modules.crops.dto.response;

import com.greenhouse.smart_backend.modules.crops.model.CropStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CropListResponseDTO {
    private Long id;
    private String name;
    private String variety;
    private Integer plantCount;
    private BigDecimal sensorHeight;
    private Long zoneId;
    private String zoneName;
    private LocalDate sowingDate;
    private CropStatus status;
}