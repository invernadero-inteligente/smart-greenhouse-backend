package com.greenhouse.smart_backend.modules.iot.service;

public interface ActuatorService {
    void saveActuatorPublisher(Long nodeId, String actuatorName, String action);
}
