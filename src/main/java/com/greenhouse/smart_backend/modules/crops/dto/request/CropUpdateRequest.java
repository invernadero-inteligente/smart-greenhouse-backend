package com.greenhouse.smart_backend.modules.crops.dto.request;

import com.greenhouse.smart_backend.modules.crops.model.CropStatus;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CropUpdateRequest {
    private String name;
    private String variety;

    @Positive(message = "La cantidad de plantas debe ser mayor a cero")
    private Integer plantCount;

    private Long zoneId;
    private LocalDate sowingDate;
    private CropStatus status;
    private CropConditionsRequest conditions;
}