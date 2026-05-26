package com.greenhouse.smart_backend.modules.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryResponseDTO {
    private Long zoneId;
    private String zoneName;
    private String variableName;
    private String unit;
    private List<HistoryPointDTO> points;
}

