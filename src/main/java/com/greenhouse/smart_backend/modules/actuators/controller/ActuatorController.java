package com.greenhouse.smart_backend.modules.actuators.controller;

import com.greenhouse.smart_backend.modules.actuators.dto.request.ActuatorCommandRequest;
import com.greenhouse.smart_backend.modules.actuators.dto.request.ActuatorCreateRequest;
import com.greenhouse.smart_backend.modules.actuators.dto.request.ActuatorUpdateRequest;
import com.greenhouse.smart_backend.modules.actuators.dto.response.ActuatorListResponseDTO;
import com.greenhouse.smart_backend.modules.actuators.dto.response.ActuatorResponseDTO;
import com.greenhouse.smart_backend.modules.actuators.service.ActuatorManagementService;
import com.greenhouse.smart_backend.shared.responses.ApiResponseDTO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Actuator Controller", description = "Endpoints para gestión de actuadores")
@RestController
@RequestMapping("/api/actuators")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirements({
        @SecurityRequirement(name = "bearerAuth")
})
public class ActuatorController {

    private final ActuatorManagementService actuatorManagementService;

    /**
     * GET /api/actuators?zoneId=1
     * Lista todos los actuadores, con filtro opcional por zona.
     * Accesible por todos los usuarios autenticados.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ActuatorListResponseDTO>>> listActuators(
            @RequestParam(required = false) Long zoneId) {

        log.info("GET /api/actuators - zoneId={}", zoneId);
        return ResponseEntity.ok(ApiResponseDTO.success(actuatorManagementService.listActuators(zoneId)));
    }

    /**
     * GET /api/actuators/{id}
     * Retorna el detalle de un actuador.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ActuatorResponseDTO>> getActuatorById(@PathVariable Long id) {
        log.info("GET /api/actuators/{}", id);
        return ResponseEntity.ok(ApiResponseDTO.success(actuatorManagementService.getActuatorById(id)));
    }

    /**
     * POST /api/actuators
     * Crea un nuevo actuador. Solo ADMIN.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ActuatorResponseDTO>> createActuator(
            @Valid @RequestBody ActuatorCreateRequest request) {

        log.info("POST /api/actuators - zoneId={}, name={}", request.getZoneId(), request.getName());
        ActuatorResponseDTO created = actuatorManagementService.createActuator(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Actuador creado exitosamente", created));
    }

    /**
     * PATCH /api/actuators/{id}
     * Actualiza parcialmente un actuador. Solo ADMIN.
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ActuatorResponseDTO>> updateActuator(
            @PathVariable Long id,
            @Valid @RequestBody ActuatorUpdateRequest request) {

        log.info("PATCH /api/actuators/{}", id);
        ActuatorResponseDTO updated = actuatorManagementService.updateActuator(id, request);
        return ResponseEntity.ok(ApiResponseDTO.success("Actuador actualizado exitosamente", updated));
    }

    /**
     * POST /api/actuators/{id}/command
     * Ejecuta un comando ON/OFF sobre un actuador existente. ADMIN y OPERATOR.
     */
    @PostMapping("/{id}/command")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponseDTO<ActuatorResponseDTO>> executeCommand(
            @PathVariable Long id,
            @Valid @RequestBody ActuatorCommandRequest request) {

        log.info("POST /api/actuators/{}/command - action={}", id, request.getAction());
        ActuatorResponseDTO updated = actuatorManagementService.executeCommand(id, request);
        return ResponseEntity.ok(ApiResponseDTO.success("Comando ejecutado exitosamente", updated));
    }

    /**
     * DELETE /api/actuators/{id}
     * Elimina un actuador. Solo ADMIN.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteActuator(@PathVariable Long id) {

        log.info("DELETE /api/actuators/{}", id);
        actuatorManagementService.deleteActuator(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Actuador eliminado exitosamente", null));
    }
}

