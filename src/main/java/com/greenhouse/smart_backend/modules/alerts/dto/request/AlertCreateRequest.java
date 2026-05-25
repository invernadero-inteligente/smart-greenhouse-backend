package com.greenhouse.smart_backend.modules.alerts.dto.request;

import com.greenhouse.smart_backend.modules.alerts.model.AlertOrigin;
import com.greenhouse.smart_backend.modules.alerts.model.AlertSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AlertCreateRequest {

    @NotNull(message = "La zona es obligatoria")
    private Long zoneId;

    private Long cropId;

    @NotNull(message = "El origen es obligatorio")
    private AlertOrigin origin;

    @NotBlank(message = "El nombre de la variable es obligatorio")
    private String variableName;

    @NotBlank(message = "La unidad es obligatoria")
    private String unit;

    @NotNull(message = "La severidad es obligatoria")
    private AlertSeverity severity;

    @NotBlank(message = "El mensaje es obligatorio")
    private String message;

    private BigDecimal value;
}
