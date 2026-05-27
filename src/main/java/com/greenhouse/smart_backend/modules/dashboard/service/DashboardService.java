package com.greenhouse.smart_backend.modules.dashboard.service;

import com.greenhouse.smart_backend.modules.dashboard.dto.response.DashboardLatestReadingsResponseDTO;
import com.greenhouse.smart_backend.modules.dashboard.dto.response.HistoryResponseDTO;

import java.time.Instant;

public interface DashboardService {
    DashboardLatestReadingsResponseDTO getLatestReadings(Long zoneId);
    
    HistoryResponseDTO getHistory(Long zoneId, String variableName, Instant from, Instant to);
}

