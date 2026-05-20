package com.greenhouse.smart_backend.modules.iot.service;

public interface ActuatorService {
    void saveActuatorPublisher(String nodeName, String actuatorName, String action);
}
