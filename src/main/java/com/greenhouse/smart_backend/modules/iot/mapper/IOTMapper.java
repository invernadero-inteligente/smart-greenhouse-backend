package com.greenhouse.smart_backend.modules.iot.mapper;

import com.greenhouse.smart_backend.modules.ai.dto.response.AIAnalysisResponseDTO;
import com.greenhouse.smart_backend.modules.ai.model.AiResult;
import com.greenhouse.smart_backend.modules.iot.document.ActuatorEventDocument;
import com.greenhouse.smart_backend.modules.iot.document.SensorReadingDocument;
import com.greenhouse.smart_backend.modules.iot.dto.request.SensorPayloadDTO;
import com.greenhouse.smart_backend.modules.zones.model.Zone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IOTMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nodeName", source = "node.name")
    @Mapping(target = "variableName", source = "variable.name")
    @Mapping(target = "value", source = "variable.value")
    @Mapping(target = "unit", source = "variable.unit")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    SensorReadingDocument toDocument(SensorPayloadDTO sensorPayloadDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    ActuatorEventDocument toDocument(String nodeName, String actuatorName, String action);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "aiImage", source = "aiAnalysisResponseDTO.image")
    @Mapping(target = "image", source = "base64Image")
    @Mapping(target = "zone", source = "zone")
    @Mapping(target = "description", expression = "java(generateDescription(aiAnalysisResponseDTO))")
    AiResult toModel(AIAnalysisResponseDTO aiAnalysisResponseDTO, String base64Image, Zone zone);

    default String generateDescription(AIAnalysisResponseDTO aiAnalysisResponseDTO) {
        if (aiAnalysisResponseDTO == null) {
            return " ";
        }
        StringBuilder sb = new StringBuilder();

        sb.append("cultivo=").append(aiAnalysisResponseDTO.getCrop())
                .append("; ");

        if (aiAnalysisResponseDTO.getAnomaly() != null) {
            sb.append("anomalia.cantidad=").append(aiAnalysisResponseDTO.getAnomaly().getAmount())
                    .append("; ");
            sb.append("anomalia.detectada=").append(aiAnalysisResponseDTO.getAnomaly().isDetected())
                    .append("; ");
        }

        if (aiAnalysisResponseDTO.getCount() != null) {
            sb.append("conteo.maduro=").append(aiAnalysisResponseDTO.getCount().getRipe())
                    .append("; ");
            sb.append("conteo.pinton=").append(aiAnalysisResponseDTO.getCount().getPinton())
                    .append("; ");
            sb.append("conteo.verde=").append(aiAnalysisResponseDTO.getCount().getUnripe())
                    .append("; ");
        }

        sb.append("total=").append(aiAnalysisResponseDTO.getTotal());

        return sb.toString();
    }
}
