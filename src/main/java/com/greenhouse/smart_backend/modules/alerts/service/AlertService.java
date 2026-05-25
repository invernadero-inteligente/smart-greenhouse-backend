package com.greenhouse.smart_backend.modules.alerts.service;

import com.greenhouse.smart_backend.modules.alerts.dto.request.AlertCreateRequest;
import com.greenhouse.smart_backend.modules.alerts.dto.response.AlertResponseDTO;
import com.greenhouse.smart_backend.modules.alerts.model.AlertStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertService {
    List<AlertResponseDTO> listAlerts(
            AlertStatus status,
            Long zoneId,
            Long cropId,
            LocalDateTime from,
            LocalDateTime to);

    AlertResponseDTO getAlertById(Long id);
    AlertResponseDTO createAlert(AlertCreateRequest request);
    void attendAlert(Long id, Long userId);
}
