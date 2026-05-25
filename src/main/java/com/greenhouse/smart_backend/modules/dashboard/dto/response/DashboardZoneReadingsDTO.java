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
public class DashboardZoneReadingsDTO {
    private Long zoneId;
    private String zoneName;
    private String description;
    private boolean isActive;
    private boolean online;
    private Instant lastReadingAt;
    private List<DashboardReadingDTO> readings;
}

