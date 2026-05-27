package com.greenhouse.smart_backend.modules.reports.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class ReportRequestDTO {

    @NotNull(message = "El tipo de reporte es obligatorio")
    private ReportType type;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @NotNull(message = "La fecha de fin es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;

    /** Filtro opcional por zona (aplica a reportes de alertas y producción) */
    private Long zoneId;

    public enum ReportType {
        ALERTS,      // Historial de alertas
        INVENTORY,   // Estado del inventario
        PRODUCTION   // Estado de cultivos (producción)
    }
}
