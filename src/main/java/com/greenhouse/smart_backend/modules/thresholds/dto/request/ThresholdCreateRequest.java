package com.greenhouse.smart_backend.modules.thresholds.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ThresholdCreateRequest {

    @NotNull(message = "El id de la zona es obligatorio")
    private Long zoneId;

    @NotBlank(message = "El nombre de la variable es obligatorio")
    private String variableName;

    @NotBlank(message = "La unidad es obligatoria")
    private String unit;

    @NotNull(message = "El valor minimo es obligatorio")
    private BigDecimal minValue;

    @NotNull(message = "El valor maximo es obligatorio")
    private BigDecimal maxValue;
}
