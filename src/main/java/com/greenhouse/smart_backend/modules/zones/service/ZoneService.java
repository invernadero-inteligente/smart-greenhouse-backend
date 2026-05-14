package com.greenhouse.smart_backend.modules.zones.service;

import com.greenhouse.smart_backend.modules.zones.dto.request.ZoneCreateRequest;
import com.greenhouse.smart_backend.modules.zones.dto.request.ZoneUpdateRequest;
import com.greenhouse.smart_backend.modules.zones.dto.response.ZoneListResponseDTO;
import com.greenhouse.smart_backend.modules.zones.dto.response.ZoneResponseDTO;

import java.util.List;

public interface ZoneService {
    List<ZoneListResponseDTO> listZones(Boolean isActive);
    ZoneResponseDTO getZoneById(Long id);
    ZoneResponseDTO createZone(ZoneCreateRequest request);
    ZoneResponseDTO updateZone(Long id, ZoneUpdateRequest request);
}
