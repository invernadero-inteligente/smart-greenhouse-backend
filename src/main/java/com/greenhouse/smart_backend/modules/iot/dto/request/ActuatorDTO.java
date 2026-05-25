package com.greenhouse.smart_backend.modules.iot.dto.request;

import com.greenhouse.smart_backend.shared.enums.ActuatorAction;
import lombok.Data;

@Data
public class ActuatorDTO {
    private String name;
    private ActuatorAction action;
}
