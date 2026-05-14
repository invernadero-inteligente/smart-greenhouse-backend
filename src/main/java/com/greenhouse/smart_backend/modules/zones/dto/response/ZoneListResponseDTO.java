package com.greenhouse.smart_backend.modules.zones.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ZoneListResponseDTO {
    private Long id;
    private String name;
    private String description;
    private boolean isActive;
}
