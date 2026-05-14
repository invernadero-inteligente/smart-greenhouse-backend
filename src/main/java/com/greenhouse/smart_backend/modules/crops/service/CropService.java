package com.greenhouse.smart_backend.modules.crops.service;

import com.greenhouse.smart_backend.modules.crops.dto.request.CropCreateRequest;
import com.greenhouse.smart_backend.modules.crops.dto.request.CropUpdateRequest;
import com.greenhouse.smart_backend.modules.crops.dto.response.CropListResponseDTO;
import com.greenhouse.smart_backend.modules.crops.dto.response.CropResponseDTO;
import com.greenhouse.smart_backend.modules.crops.model.CropStatus;

import java.util.List;

public interface CropService {
    List<CropListResponseDTO> listCrops(CropStatus status, Long zoneId);
    CropResponseDTO getCropById(Long id);
    CropResponseDTO createCrop(CropCreateRequest request);
    CropResponseDTO updateCrop(Long id, CropUpdateRequest request);
}