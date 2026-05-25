package com.greenhouse.smart_backend.modules.alerts.controller;

import com.greenhouse.smart_backend.modules.alerts.dto.request.AlertCreateRequest;
import com.greenhouse.smart_backend.modules.alerts.dto.response.AlertResponseDTO;
import com.greenhouse.smart_backend.modules.alerts.model.AlertStatus;
import com.greenhouse.smart_backend.modules.alerts.service.AlertService;
import com.greenhouse.smart_backend.shared.responses.ApiResponseDTO;
import com.greenhouse.smart_backend.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Alerts Controller", description = "Endpoints para gestión de alertas")
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirements({@SecurityRequirement(name = "bearerAuth")})
public class AlertController {

    private final AlertService alertService;

    /**
     * GET /api/alerts
     * Lista alertas con filtros opcionales por estado, zona, cultivo y rango de fechas.
     * Accesible por todos los usuarios autenticados.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<AlertResponseDTO>>> listAlerts(
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long cropId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        log.info("GET /api/alerts - status={}, zoneId={}, cropId={}", status, zoneId, cropId);
        return ResponseEntity.ok(
                ApiResponseDTO.success(alertService.listAlerts(status, zoneId, cropId, from, to)));
    }

    /**
     * GET /api/alerts/{id}
     * Retorna el detalle completo de una alerta.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<AlertResponseDTO>> getAlertById(
            @PathVariable Long id) {

        log.info("GET /api/alerts/{}", id);
        return ResponseEntity.ok(ApiResponseDTO.success(alertService.getAlertById(id)));
    }

    /**
     * POST /api/alerts
     * Crea una alerta manualmente. Útil para pruebas y para alertas generadas
     * por lógica interna del sistema. En producción las alertas las genera
     * automáticamente el módulo IoT al recibir lecturas fuera de umbral.
     * Solo ADMIN y OPERATOR.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponseDTO<AlertResponseDTO>> createAlert(
            @Valid @RequestBody AlertCreateRequest request) {

        log.info("POST /api/alerts - zone={}, variable={}",
                request.getZoneId(), request.getVariableName());
        AlertResponseDTO created = alertService.createAlert(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Alerta registrada exitosamente", created));
    }

    /**
     * PUT /api/alerts/{id}/attend
     * Marca una alerta como ATTENDED. Registra automáticamente el usuario
     * autenticado en attended_by y la fecha actual en attended_at.
     * Solo ADMIN y OPERATOR.
     */
    @PutMapping("/{id}/attend")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<Void> attendAlert(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("PUT /api/alerts/{}/attend - userId={}", id, currentUser.getId());
        alertService.attendAlert(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
