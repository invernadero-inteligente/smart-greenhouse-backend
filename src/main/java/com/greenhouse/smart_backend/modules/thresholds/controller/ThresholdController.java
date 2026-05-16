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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ThresholdsDataResponseDTO<List<ThresholdZoneResponseDTO>>> listThresholds(
            @RequestParam List<Long> zoneId,
            @RequestParam(required = false) List<String> variables) {

        log.info("GET /api/thresholds - zoneId={}, variables={}", zoneId, variables);
        List<ThresholdZoneResponseDTO> data = thresholdService.listThresholds(zoneId, variables);
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
}


