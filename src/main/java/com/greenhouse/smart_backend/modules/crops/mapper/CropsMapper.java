package com.greenhouse.smart_backend.modules.crops.mapper;

import com.greenhouse.smart_backend.modules.crops.dto.response.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CropsMapper {
    @Mapping(target = "data", expression = "java(toResponseData(cropResponseDTO))")
    CropsDataResponseDTO<CreateCropResponseDTO> toResponse(CropResponseDTO cropResponseDTO);

    @Mapping(target = "status", expression = "java(cropResponseDTO.getStatus().name())")
    CreateCropResponseDTO toResponseData(CropResponseDTO cropResponseDTO);

    default CropsDataResponseDTO<List<CropsZoneResponseDTO>> toResponse(List<CropListResponseDTO> cropList) {
        Map<Long, List<CropListResponseDTO>> grouped = cropList.stream()
                .collect(Collectors.groupingBy(CropListResponseDTO::getZoneId));

        List<CropsZoneResponseDTO> zones = grouped.entrySet().stream()
                .map(entry -> {
                    List<CropsInfoResponseDTO> info = entry.getValue().stream().map(this::toInfo).toList();
                    CropListResponseDTO first = entry.getValue().getFirst();
                    return CropsZoneResponseDTO.builder()
                            .zoneId(entry.getKey())
                            .zoneName(first.getZoneName())
                            .info(info)
                            .build();
                }).toList();
        return CropsDataResponseDTO.<List<CropsZoneResponseDTO>>builder()
                .data(zones)
                .build();
    }

    @Mapping(target = "status", expression = "java(crop.getStatus().name())")
    @Mapping(target = "sowingDate", source = "sowingDate")
    CropsInfoResponseDTO toInfo(CropListResponseDTO crop);

    default Date map(LocalDate localDate) {
        return localDate != null ? Date.valueOf(localDate) : null;
    }
}
