package com.greenhouse.smart_backend.modules.thresholds.mapper;

import com.greenhouse.smart_backend.modules.thresholds.dto.response.ThresholdVariableResponseDTO;
import com.greenhouse.smart_backend.modules.thresholds.model.ThresholdConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ThresholdsMapper {

    @Mapping(target = "name", source = "variableName")
    ThresholdVariableResponseDTO toVariableDTO(ThresholdConfig config);
}

