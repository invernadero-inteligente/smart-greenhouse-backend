package com.greenhouse.smart_backend.modules.crops.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CropConditionsResponse {
    private BigDecimal temperatureMin;
    private BigDecimal temperatureMax;
    private BigDecimal airHumidityMin;
    private BigDecimal airHumidityMax;
    private BigDecimal soilMoistureMin;
    private BigDecimal soilMoistureMax;
    private BigDecimal phMin;
    private BigDecimal phMax;
}
