package com.greenhouse.smart_backend.modules.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardLatestReadingsResponseDTO {
    private Instant generatedAt;
    private List<DashboardZoneReadingsDTO> zones;
}

