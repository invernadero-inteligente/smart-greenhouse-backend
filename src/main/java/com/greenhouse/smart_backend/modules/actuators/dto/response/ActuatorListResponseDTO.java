package com.greenhouse.smart_backend.modules.actuators.dto.response;

import com.greenhouse.smart_backend.shared.enums.ActuatorAction;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActuatorListResponseDTO {

    private Long id;
    private Long zoneId;
    private String zoneName;
    private String name;
    private ActuatorAction currentAction;
    private LocalDateTime updatedAt;
}

