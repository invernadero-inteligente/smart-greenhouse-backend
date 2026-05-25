package com.greenhouse.smart_backend.modules.thresholds.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ThresholdUpdateRequest {
    @NotNull
    private BigDecimal minValue;

    @NotNull
    private BigDecimal maxValue;
}

