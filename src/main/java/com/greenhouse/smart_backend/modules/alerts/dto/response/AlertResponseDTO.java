package com.greenhouse.smart_backend.modules.alerts.dto.response;

import com.greenhouse.smart_backend.modules.alerts.model.AlertOrigin;
import com.greenhouse.smart_backend.modules.alerts.model.AlertSeverity;
import com.greenhouse.smart_backend.modules.alerts.model.AlertStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AlertResponseDTO {
    private Long id;
    private Long zoneId;
    private String zoneName;
    private Long cropId;
    private String cropName;
    private AlertOrigin origin;
    private String variableName;
    private String unit;
    private AlertSeverity severity;
    private String message;
    private BigDecimal value;
    private AlertStatus status;
    private Long attendedById;
    private String attendedByName;
    private LocalDateTime attendedAt;
    private LocalDateTime createdAt;
}
