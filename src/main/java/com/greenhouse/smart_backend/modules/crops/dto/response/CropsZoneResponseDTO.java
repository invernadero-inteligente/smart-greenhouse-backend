package com.greenhouse.smart_backend.modules.crops.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CropsZoneResponseDTO {
    private Long zoneId;
    private String zoneName;
    private List<CropsInfoResponseDTO> info;
}
