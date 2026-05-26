package com.greenhouse.smart_backend.modules.reports.controller;

import com.greenhouse.smart_backend.modules.reports.dto.ReportRequestDTO;
import com.greenhouse.smart_backend.modules.reports.service.ReportService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reports Controller", description = "Generación de reportes PDF del sistema")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirements({@SecurityRequirement(name = "bearerAuth")})
public class ReportController {

    private final ReportService reportService;

    /**
     * POST /api/reports/generate
     * Genera un reporte PDF según el tipo y rango de fechas indicados.
     * Solo ADMIN.
     *
     * Body:
     * {
     *   "type": "ALERTS" | "INVENTORY" | "PRODUCTION",
     *   "from": "2026-01-01T00:00:00",
     *   "to":   "2026-05-31T23:59:59",
     *   "zoneId": 1  (opcional)
     * }
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> generateReport(
            @Valid @RequestBody ReportRequestDTO request) {

        log.info("POST /api/reports/generate - type={}", request.getType());

        byte[] pdf = reportService.generateReport(request);
        String filename = reportService.getFilename(request.getType());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}
