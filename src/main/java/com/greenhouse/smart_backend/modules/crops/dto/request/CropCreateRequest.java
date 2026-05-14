package com.greenhouse.smart_backend.modules.crops.dto.request;

import com.greenhouse.smart_backend.modules.crops.model.CropStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CropCreateRequest {

    @NotBlank(message = "El nombre del cultivo es obligatorio")
    private String name;

    private String variety;

    @Positive(message = "La cantidad de plantas debe ser mayor a cero")
    private Integer plantCount;

    @NotNull(message = "La zona es obligatoria")
    private Long zoneId;

    private LocalDate sowingDate;

    @NotNull(message = "El estado es obligatorio")
    private CropStatus status;

    private CropConditionsRequest conditions;
}
