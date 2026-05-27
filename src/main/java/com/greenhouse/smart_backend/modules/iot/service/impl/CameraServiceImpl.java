package com.greenhouse.smart_backend.modules.iot.service.impl;

import com.greenhouse.smart_backend.modules.iot.mqtt.application.MqttPublisherService;
import com.greenhouse.smart_backend.modules.iot.service.CameraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CameraServiceImpl implements CameraService {
    private static final String CAMERA_REQUEST_NODE = "CLIENT";
    private static final String PHOTO_VARIABLE = "PHOTO";

    private final MqttPublisherService mqttPublisherService;

    @Override
    public void requestPhoto() {
        log.info("Solicitando foto a IoT mediante MQTT");

        mqttPublisherService.publishSensorRequest(
                CAMERA_REQUEST_NODE,
                PHOTO_VARIABLE
        );
    }
}
