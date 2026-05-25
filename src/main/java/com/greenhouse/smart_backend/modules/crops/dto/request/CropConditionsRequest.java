package com.greenhouse.smart_backend.modules.crops.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CropConditionsRequest {
    private RangeRequest temperature;
    private RangeRequest airHumidity;
    private RangeRequest soilMoisture;
    private RangeRequest ph;

    @Data
    public static class RangeRequest {
        private BigDecimal min;
        private BigDecimal max;
    }
}
