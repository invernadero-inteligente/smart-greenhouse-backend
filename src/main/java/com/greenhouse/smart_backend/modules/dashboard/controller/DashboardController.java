package com.greenhouse.smart_backend.modules.dashboard.controller;

import com.greenhouse.smart_backend.modules.dashboard.dto.response.DashboardLatestReadingsResponseDTO;
import com.greenhouse.smart_backend.modules.dashboard.service.DashboardService;
import com.greenhouse.smart_backend.shared.responses.ApiResponseDTO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard Controller", description = "Endpoints para monitoreo en tiempo real")
@RestController
@RequestMapping("/api/readings")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirements({
        @SecurityRequirement(name = "bearerAuth")
})
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/latest")
    public ResponseEntity<ApiResponseDTO<DashboardLatestReadingsResponseDTO>> getLatestReadings(
            @RequestParam(required = false) Long zoneId) {

        log.info("GET /api/readings/latest - zoneId={}", zoneId);
        DashboardLatestReadingsResponseDTO data = dashboardService.getLatestReadings(zoneId);
        return ResponseEntity.ok(ApiResponseDTO.success("Lecturas recientes retornadas correctamente", data));
    }
}

