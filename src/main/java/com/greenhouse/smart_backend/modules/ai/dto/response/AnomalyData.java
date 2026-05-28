package com.greenhouse.smart_backend.modules.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyData {
    @JsonProperty("cantidad")
    private Integer amount;

    @JsonProperty("detectada")
    private boolean detected;
}
