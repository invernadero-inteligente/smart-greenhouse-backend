package com.greenhouse.smart_backend.modules.actuators.service;

import com.greenhouse.smart_backend.modules.actuators.dto.request.ActuatorCommandRequest;
import com.greenhouse.smart_backend.modules.actuators.dto.request.ActuatorCreateRequest;
import com.greenhouse.smart_backend.modules.actuators.dto.request.ActuatorUpdateRequest;
import com.greenhouse.smart_backend.modules.actuators.dto.response.ActuatorListResponseDTO;
import com.greenhouse.smart_backend.modules.actuators.dto.response.ActuatorResponseDTO;

import java.util.List;

public interface ActuatorManagementService {
    List<ActuatorListResponseDTO> listActuators(Long zoneId);
    ActuatorResponseDTO getActuatorById(Long id);
    ActuatorResponseDTO createActuator(ActuatorCreateRequest request);
    ActuatorResponseDTO updateActuator(Long id, ActuatorUpdateRequest request);
    void deleteActuator(Long id);
    ActuatorResponseDTO executeCommand(Long id, ActuatorCommandRequest request);
}

