package com.greenhouse.smart_backend.modules.zones.dto.request;

import lombok.Data;

@Data
public class ZoneUpdateRequest {
    private String name;
    private String description;
    private Boolean isActive;
}
