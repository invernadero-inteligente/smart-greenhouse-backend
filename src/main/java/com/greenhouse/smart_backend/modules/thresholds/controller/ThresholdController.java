package com.greenhouse.smart_backend.modules.thresholds.controller;

import com.greenhouse.smart_backend.modules.thresholds.dto.request.ThresholdCreateRequest;
import com.greenhouse.smart_backend.modules.thresholds.dto.request.ThresholdUpdateRequest;
import com.greenhouse.smart_backend.modules.thresholds.dto.response.ThresholdVariableResponseDTO;
import com.greenhouse.smart_backend.modules.thresholds.dto.response.ThresholdZoneResponseDTO;
import com.greenhouse.smart_backend.modules.thresholds.dto.response.ThresholdsDataResponseDTO;
import com.greenhouse.smart_backend.modules.thresholds.service.ThresholdService;
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

@Tag(name = "Threshold Controller", description = "Endpoints para gestión de umbrales")
@RestController
@RequestMapping("/api/thresholds")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirements({
        @SecurityRequirement(name = "bearerAuth")
})
public class ThresholdController {

    private final ThresholdService thresholdService;

        @GetMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ThresholdsDataResponseDTO<List<ThresholdZoneResponseDTO>>> listThresholds(
            @RequestParam List<Long> zoneId,
                        @RequestParam(required = false) List<String> variables,
                        @RequestParam(required = false) Boolean isActive) {

                log.info("GET /api/thresholds - zoneId={}, variables={}, isActive={}", zoneId, variables, isActive);
                List<ThresholdZoneResponseDTO> data = thresholdService.listThresholds(zoneId, variables, isActive);
        return ResponseEntity.ok(ThresholdsDataResponseDTO.<List<ThresholdZoneResponseDTO>>builder()
                .data(data)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ThresholdsDataResponseDTO<ThresholdVariableResponseDTO>> createThreshold(
            @Valid @RequestBody ThresholdCreateRequest request) {

        log.info("POST /api/thresholds - zoneId={}, variable={}", request.getZoneId(), request.getVariableName());
        ThresholdVariableResponseDTO created = thresholdService.createThreshold(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ThresholdsDataResponseDTO.<ThresholdVariableResponseDTO>builder()
                        .data(created)
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateThreshold(@PathVariable Long id,
                                                @Valid @RequestBody ThresholdUpdateRequest request) {

        log.info("PUT /api/thresholds/{}", id);
        thresholdService.updateThreshold(id, request);
        return ResponseEntity.noContent().build();
    }

        @PatchMapping("/{id}/deactivate")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Void> deactivateThreshold(@PathVariable Long id) {
                log.info("PATCH /api/thresholds/{}/deactivate", id);
                thresholdService.deactivateThreshold(id);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/{id}/reactivate")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Void> reactivateThreshold(@PathVariable Long id) {
                log.info("PATCH /api/thresholds/{}/reactivate", id);
                thresholdService.reactivateThreshold(id);
                return ResponseEntity.noContent().build();
        }
}


