package com.greenhouse.smart_backend.modules.iot.dto.request;

import lombok.Data;

@Data
public class SensorPayloadDTO {
    private NodeDTO node;
    private VariableDTO variable;
}
