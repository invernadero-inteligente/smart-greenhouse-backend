package com.greenhouse.smart_backend.modules.iot.service;

public interface CameraService {
    void requestPhoto(Long zoneId);
    boolean savePhotoIfPresent(String payload);
}
