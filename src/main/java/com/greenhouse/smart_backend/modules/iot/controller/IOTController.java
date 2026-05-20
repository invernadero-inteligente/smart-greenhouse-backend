package com.greenhouse.smart_backend.modules.iot.controller;

import com.greenhouse.smart_backend.modules.iot.dto.request.ActuatorDTO;
import com.greenhouse.smart_backend.modules.iot.service.ActuatorService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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
public class IOTController {
    private final ActuatorService actuatorService;

    @PostMapping("/actuator/event/{zoneId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> actuatorEvent(@PathVariable String zoneId, @RequestBody ActuatorDTO actuatorDTO) {
        log.info("Actuator event request recibido por zoneId={}", zoneId);

        actuatorService.saveActuatorPublisher(zoneId, actuatorDTO.getName(), actuatorDTO.getAction().toString());
        return ResponseEntity.noContent().build();
    }
}
