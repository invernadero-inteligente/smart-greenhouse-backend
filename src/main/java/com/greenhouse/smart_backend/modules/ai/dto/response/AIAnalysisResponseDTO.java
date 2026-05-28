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
public class AIAnalysisResponseDTO {
    @JsonProperty("anomalia")
    private AnomalyData anomaly;
    @JsonProperty("conteo")
    private CountData count;
    @JsonProperty("imagen_anotada")
    private String image;
    @JsonProperty("cultivo")
    private String crop;
    @JsonProperty("total")
    private Long total;
}
