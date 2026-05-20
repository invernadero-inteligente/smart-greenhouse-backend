package com.greenhouse.smart_backend.modules.iot.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VariableDTO {
    private String name;
    private String unit;
    private BigDecimal value;

}
