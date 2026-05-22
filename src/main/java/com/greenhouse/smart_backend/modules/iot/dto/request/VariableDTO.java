package com.greenhouse.smart_backend.modules.iot.dto.request;

import lombok.Data;

@Data
public class VariableDTO {
    private String name;
    private String unit;
    private String value;

}
