package com.greenhouse.smart_backend.modules.reports.service.impl;

import com.greenhouse.smart_backend.modules.reports.builder.AlertReportBuilder;
import com.greenhouse.smart_backend.modules.reports.builder.InventoryReportBuilder;
import com.greenhouse.smart_backend.modules.reports.builder.ProductionReportBuilder;
import com.greenhouse.smart_backend.modules.reports.dto.ReportRequestDTO;
import com.greenhouse.smart_backend.modules.reports.service.ReportService;
import com.greenhouse.smart_backend.shared.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final AlertReportBuilder alertReportBuilder;
    private final InventoryReportBuilder inventoryReportBuilder;
    private final ProductionReportBuilder productionReportBuilder;

    @Override
    @Transactional(readOnly = true)
    public byte[] generateReport(ReportRequestDTO request) {
        if (request.getFrom().isAfter(request.getTo())) {
            throw new ValidationException("from",
                    "La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        log.info("Generando reporte tipo={} from={} to={} zoneId={}",
                request.getType(), request.getFrom(), request.getTo(), request.getZoneId());

        try {
            return switch (request.getType()) {
                case ALERTS     -> alertReportBuilder.build(
                        request.getFrom(), request.getTo(), request.getZoneId());
                case INVENTORY  -> inventoryReportBuilder.build(
                        request.getFrom(), request.getTo(), request.getZoneId());
                case PRODUCTION -> productionReportBuilder.build(
                        request.getFrom(), request.getTo(), request.getZoneId());
            };
        } catch (Exception e) {
            log.error("Error generando reporte: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public String getFilename(ReportRequestDTO.ReportType type) {
        String date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        return switch (type) {
            case ALERTS     -> "reporte_alertas_" + date + ".pdf";
            case INVENTORY  -> "reporte_inventario_" + date + ".pdf";
            case PRODUCTION -> "reporte_produccion_" + date + ".pdf";
        };
    }
}
