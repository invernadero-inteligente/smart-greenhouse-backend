package com.greenhouse.smart_backend.modules.zones.controller;

import com.greenhouse.smart_backend.modules.zones.dto.request.ZoneCreateRequest;
import com.greenhouse.smart_backend.modules.zones.dto.request.ZoneUpdateRequest;
import com.greenhouse.smart_backend.modules.zones.dto.response.ZoneListResponseDTO;
import com.greenhouse.smart_backend.modules.zones.dto.response.ZoneResponseDTO;
import com.greenhouse.smart_backend.modules.zones.service.ZoneService;
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

@Tag(name = "Zone Controller", description = "Endpoints para gestión de zonas")
@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirements({
        @SecurityRequirement(name = "bearerAuth")
})
public class ZoneController {

    private final ZoneService zoneService;

    /**
     * GET /api/zones?isActive=true
     * Lista todas las zonas. Filtro opcional por estado activo/inactivo.
     * Accesible por todos los usuarios autenticados.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ZoneListResponseDTO>>> listZones(
            @RequestParam(defaultValue = "true") Boolean isActive) {

        log.info("GET /api/zones - isActive={}", isActive);
        return ResponseEntity.ok(ApiResponseDTO.success(zoneService.listZones(isActive)));
    }

    /**
     * GET /api/zones/{id}
     * Retorna el detalle de una zona.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ZoneResponseDTO>> getZoneById(@PathVariable Long id) {
        log.info("GET /api/zones/{}", id);
        return ResponseEntity.ok(ApiResponseDTO.success(zoneService.getZoneById(id)));
    }

    /**
     * POST /api/zones
     * Crea una nueva zona. Solo ADMIN.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ZoneResponseDTO>> createZone(
            @Valid @RequestBody ZoneCreateRequest request) {

        log.info("POST /api/zones - name={}", request.getName());
        ZoneResponseDTO created = zoneService.createZone(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Zona creada exitosamente", created));
    }

    /**
     * PATCH /api/zones/{id}
     * Actualiza parcialmente una zona. Solo ADMIN.
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<ZoneResponseDTO>> updateZone(
            @PathVariable Long id,
            @Valid @RequestBody ZoneUpdateRequest request) {

        log.info("PATCH /api/zones/{}", id);
        ZoneResponseDTO updated = zoneService.updateZone(id, request);
        return ResponseEntity.ok(ApiResponseDTO.success("Zona actualizada exitosamente", updated));
    }
}