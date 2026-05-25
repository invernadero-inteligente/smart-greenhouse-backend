package com.greenhouse.smart_backend.modules.dashboard.service;

import com.greenhouse.smart_backend.modules.dashboard.dto.response.DashboardLatestReadingsResponseDTO;

public interface DashboardService {
    DashboardLatestReadingsResponseDTO getLatestReadings(Long zoneId);
}

