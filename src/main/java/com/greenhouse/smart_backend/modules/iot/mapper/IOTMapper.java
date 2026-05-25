package com.greenhouse.smart_backend.modules.iot.mapper;

import com.greenhouse.smart_backend.modules.iot.document.ActuatorEventDocument;
import com.greenhouse.smart_backend.modules.iot.document.SensorReadingDocument;
import com.greenhouse.smart_backend.modules.iot.dto.request.SensorPayloadDTO;
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
}
