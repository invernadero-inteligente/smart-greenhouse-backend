package com.greenhouse.smart_backend.modules.iot.controller;

import com.greenhouse.smart_backend.modules.iot.dto.request.ActuatorDTO;
import com.greenhouse.smart_backend.modules.iot.service.ActuatorService;
import com.greenhouse.smart_backend.modules.iot.service.CameraService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iot")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirements({
        @SecurityRequirement(name = "bearerAuth")
})
@Tag(name = "IOT Controller", description = "Endpoints para gestión de eventos de actuadores")
public class IOTController {
    private final ActuatorService actuatorService;
    private final CameraService cameraService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/actuator/event/{zoneId}")
    public ResponseEntity<Void> actuatorEvent(@PathVariable Long zoneId, @RequestBody ActuatorDTO actuatorDTO) {
        log.info("Actuator event request recibido por zoneId={}", zoneId);

        actuatorService.saveActuatorPublisher(zoneId, actuatorDTO.getName(), actuatorDTO.getAction().toString());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/camera/photo/request")
    public ResponseEntity<Void> requestPhoto() {
        log.info("Solicitud manual de captura de foto recibida");

        cameraService.requestPhoto();

        return ResponseEntity.accepted().build();
    }
}
