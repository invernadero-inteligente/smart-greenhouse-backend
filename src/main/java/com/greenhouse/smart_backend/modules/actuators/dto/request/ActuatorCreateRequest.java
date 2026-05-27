package com.greenhouse.smart_backend.modules.actuators.dto.request;

import com.greenhouse.smart_backend.shared.enums.ActuatorAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActuatorCreateRequest {

    @NotNull(message = "La zona es obligatoria")
    private Long zoneId;

    @NotBlank(message = "El nombre del actuador es obligatorio")
    @Size(max = 100, message = "El nombre del actuador no puede superar 100 caracteres")
    private String name;

    private ActuatorAction currentAction;
}

