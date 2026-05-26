package com.greenhouse.smart_backend.modules.reports.builder;

import com.greenhouse.smart_backend.modules.crops.model.Crop;
import com.greenhouse.smart_backend.modules.crops.model.CropStatus;
import com.greenhouse.smart_backend.modules.crops.repository.CropRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
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
public class ProductionReportBuilder extends ReportPdfBuilder {

    private final CropRepository cropRepository;

    @Override
    public byte[] build(LocalDateTime from, LocalDateTime to, Long zoneId) {
        List<Crop> crops = zoneId != null
                ? cropRepository.findAllByZoneId(zoneId)
                : cropRepository.findAll();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = createDocument(out);
        doc.open();

        try {
            addHeader(doc, "Reporte de Producción", from, to);

            // Resumen por estado
            long active   = crops.stream().filter(c -> c.getStatus() == CropStatus.ACTIVE).count();
            long harvest  = crops.stream().filter(c -> c.getStatus() == CropStatus.HARVEST).count();
            long finished = crops.stream().filter(c -> c.getStatus() == CropStatus.FINISHED).count();
            int  total    = crops.stream().mapToInt(c -> c.getPlantCount() != null ? c.getPlantCount() : 0).sum();

            addSectionTitle(doc, "Resumen");
            PdfPTable summary = new PdfPTable(4);
            summary.setWidthPercentage(80);
            summary.addCell(headerCell("Activos"));
            summary.addCell(headerCell("En cosecha"));
            summary.addCell(headerCell("Finalizados"));
            summary.addCell(headerCell("Total plantas"));
            summary.addCell(dataCell(String.valueOf(active),   false, Element.ALIGN_CENTER));
            summary.addCell(dataCell(String.valueOf(harvest),  false, Element.ALIGN_CENTER));
            summary.addCell(dataCell(String.valueOf(finished), false, Element.ALIGN_CENTER));
            summary.addCell(dataCell(String.valueOf(total),    false, Element.ALIGN_CENTER));
            doc.add(summary);
            doc.add(Chunk.NEWLINE);

            // Detalle agrupado por zona
            Map<String, List<Crop>> byZone = crops.stream()
                    .collect(Collectors.groupingBy(c ->
                            c.getZone() != null ? c.getZone().getName() : "Sin zona"));

            for (Map.Entry<String, List<Crop>> entry : byZone.entrySet()) {
                addSectionTitle(doc, "Zona: " + entry.getKey());

                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{2.5f, 2f, 1.5f, 1.5f, 1.5f});

                table.addCell(headerCell("Cultivo"));
                table.addCell(headerCell("Variedad"));
                table.addCell(headerCell("Plantas"));
                table.addCell(headerCell("Fecha siembra"));
                table.addCell(headerCell("Estado"));

                List<Crop> zoneCrops = entry.getValue();
                for (int i = 0; i < zoneCrops.size(); i++) {
                    Crop c = zoneCrops.get(i);
                    boolean alt = i % 2 == 1;

                    table.addCell(dataCell(safe(c.getName()), alt));
                    table.addCell(dataCell(safe(c.getVariety()), alt));
                    table.addCell(dataCell(
                            c.getPlantCount() != null ? String.valueOf(c.getPlantCount()) : "-",
                            alt, Element.ALIGN_CENTER));
                    table.addCell(dataCell(
                            c.getSowingDate() != null ? c.getSowingDate().format(DATE_FORMAT) : "-",
                            alt, Element.ALIGN_CENTER));
                    table.addCell(cropStatusCell(c.getStatus(), alt));
                }

                doc.add(table);
                doc.add(Chunk.NEWLINE);
            }

            if (crops.isEmpty()) {
                doc.add(new Paragraph("No se encontraron cultivos para el período seleccionado.",
                        new Font(Font.HELVETICA, 10, Font.ITALIC, COLOR_TEXT_LIGHT)));
            }

        } catch (DocumentException e) {
            log.error("Error generando reporte de producción: {}", e.getMessage());
        } finally {
            doc.close();
        }

        return out.toByteArray();
    }

    private PdfPCell cropStatusCell(CropStatus status, boolean alt) {
        Color color = switch (status) {
            case ACTIVE   -> new Color(27, 94, 32);
            case HARVEST  -> new Color(230, 81, 0);
            case FINISHED -> new Color(69, 69, 69);
        };
        Font font = new Font(Font.HELVETICA, 9, Font.BOLD, color);
        PdfPCell cell = new PdfPCell(
                new Phrase(status != null ? status.name() : "-", font));
        cell.setBackgroundColor(alt ? COLOR_ROW_ALT : COLOR_WHITE);
        cell.setPadding(6);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
}