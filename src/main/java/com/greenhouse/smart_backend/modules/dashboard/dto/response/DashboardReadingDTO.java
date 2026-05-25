package com.greenhouse.smart_backend.modules.dashboard.dto.response;

import com.greenhouse.smart_backend.shared.enums.ReadingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardReadingDTO {
    private String variable;
    private String value;
    private String unit;
    private ReadingStatus status;
    private Instant timestamp;
    private boolean online;
}

