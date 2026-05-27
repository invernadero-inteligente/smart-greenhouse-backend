package com.greenhouse.smart_backend.modules.reports.builder;

import com.greenhouse.smart_backend.modules.alerts.model.Alert;
import com.greenhouse.smart_backend.modules.alerts.model.AlertStatus;
import com.greenhouse.smart_backend.modules.alerts.repository.AlertRepository;
import com.greenhouse.smart_backend.modules.alerts.repository.AlertSpecification;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertReportBuilder extends ReportPdfBuilder {

    private final AlertRepository alertRepository;

    @Override
    public byte[] build(LocalDateTime from, LocalDateTime to, Long zoneId) {
        List<Alert> alerts = alertRepository.findAll(
            AlertSpecification.withFilters(null, zoneId, null, from, to));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = createDocument(out);
        doc.open();

        try {
            addHeader(doc, "Reporte de Alertas", from, to);

            // Resumen
            long totalOpen     = alerts.stream().filter(a -> a.getStatus() == AlertStatus.OPEN).count();
            long totalAttended = alerts.stream().filter(a -> a.getStatus() == AlertStatus.ATTENDED).count();

            addSectionTitle(doc, "Resumen");
            PdfPTable summary = new PdfPTable(3);
            summary.setWidthPercentage(60);
            summary.addCell(headerCell("Total alertas"));
            summary.addCell(headerCell("Abiertas"));
            summary.addCell(headerCell("Atendidas"));
            summary.addCell(dataCell(String.valueOf(alerts.size()), false, Element.ALIGN_CENTER));
            summary.addCell(dataCell(String.valueOf(totalOpen), false, Element.ALIGN_CENTER));
            summary.addCell(dataCell(String.valueOf(totalAttended), false, Element.ALIGN_CENTER));
            doc.add(summary);
            doc.add(Chunk.NEWLINE);

            // Detalle agrupado por zona
            Map<String, List<Alert>> byZone = alerts.stream()
                    .collect(Collectors.groupingBy(a ->
                            a.getZone() != null ? a.getZone().getName() : "Sin zona"));

            for (Map.Entry<String, List<Alert>> entry : byZone.entrySet()) {
                addSectionTitle(doc, "Zona: " + entry.getKey());

                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1.5f, 1f, 1f, 1f, 3f, 1.2f});

                table.addCell(headerCell("Fecha"));
                table.addCell(headerCell("Variable"));
                table.addCell(headerCell("Valor"));
                table.addCell(headerCell("Severidad"));
                table.addCell(headerCell("Mensaje"));
                table.addCell(headerCell("Estado"));

                List<Alert> zoneAlerts = entry.getValue();
                for (int i = 0; i < zoneAlerts.size(); i++) {
                    Alert a = zoneAlerts.get(i);
                    boolean alt = i % 2 == 1;

                    table.addCell(dataCell(
                            a.getCreatedAt() != null ? a.getCreatedAt().format(DT_FORMAT) : "-", alt));
                    table.addCell(dataCell(safe(a.getVariableName()), alt));
                    table.addCell(dataCell(
                            a.getValue() != null ? a.getValue().toPlainString() + " " + safe(a.getUnit()) : "-",
                            alt, Element.ALIGN_CENTER));
                    table.addCell(severityCell(a.getSeverity() != null ? a.getSeverity().name() : "-", alt));
                    table.addCell(dataCell(safe(a.getMessage()), alt));
                    table.addCell(statusCell(a.getStatus() != null ? a.getStatus().name() : "-", alt));
                }

                doc.add(table);
                doc.add(Chunk.NEWLINE);
            }

            if (alerts.isEmpty()) {
                doc.add(new Paragraph("No se encontraron alertas en el período seleccionado.",
                        new Font(Font.HELVETICA, 10, Font.ITALIC, COLOR_TEXT_LIGHT)));
            }

        } catch (DocumentException e) {
            log.error("Error generando reporte de alertas: {}", e.getMessage());
        } finally {
            doc.close();
        }

        return out.toByteArray();
    }

    private PdfPCell severityCell(String severity, boolean alt) {
        Color color = switch (severity) {
            case "HIGH"   -> new Color(198, 40, 40);
            case "MEDIUM" -> new Color(230, 81, 0);
            case "LOW"    -> new Color(51, 105, 30);
            default       -> new Color(69, 69, 69);
        };
        Font font = new Font(Font.HELVETICA, 9, Font.BOLD, color);
        PdfPCell cell = new PdfPCell(new Phrase(severity, font));
        cell.setBackgroundColor(alt ? COLOR_ROW_ALT : COLOR_WHITE);
        cell.setPadding(6);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private PdfPCell statusCell(String status, boolean alt) {
        Color color = "OPEN".equals(status)
                ? new Color(183, 28, 28)
                : new Color(27, 94, 32);
        Font font = new Font(Font.HELVETICA, 9, Font.BOLD, color);
        PdfPCell cell = new PdfPCell(new Phrase(status, font));
        cell.setBackgroundColor(alt ? COLOR_ROW_ALT : COLOR_WHITE);
        cell.setPadding(6);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
}