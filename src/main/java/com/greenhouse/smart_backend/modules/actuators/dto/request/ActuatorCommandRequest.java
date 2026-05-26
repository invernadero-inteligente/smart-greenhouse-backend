package com.greenhouse.smart_backend.modules.actuators.dto.request;

import com.greenhouse.smart_backend.shared.enums.ActuatorAction;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActuatorCommandRequest {

    @NotNull(message = "La acción del actuador es obligatoria")
    private ActuatorAction action;
}

