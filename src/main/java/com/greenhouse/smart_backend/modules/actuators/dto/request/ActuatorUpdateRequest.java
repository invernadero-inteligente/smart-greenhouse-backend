package com.greenhouse.smart_backend.modules.actuators.dto.request;

import com.greenhouse.smart_backend.shared.enums.ActuatorAction;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActuatorUpdateRequest {

    private Long zoneId;

    @Size(max = 100, message = "El nombre del actuador no puede superar 100 caracteres")
    private String name;

    private ActuatorAction currentAction;
}

