package com.greenhouse.smart_backend.modules.reports.builder;

import com.greenhouse.smart_backend.modules.inventory.model.InventoryItem;
import com.greenhouse.smart_backend.modules.inventory.repository.InventoryRepository;
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
public class InventoryReportBuilder extends ReportPdfBuilder {

    private final InventoryRepository inventoryRepository;

    @Override
    public byte[] build(LocalDateTime from, LocalDateTime to, Long zoneId) {
        List<InventoryItem> items = inventoryRepository.findAll();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = createDocument(out);
        doc.open();

        try {
            addHeader(doc, "Reporte de Inventario", from, to);

            // Resumen
            long lowStockCount = items.stream()
                    .filter(i -> i.getQuantity().compareTo(i.getMinStock()) <= 0)
                    .count();

            addSectionTitle(doc, "Resumen");
            PdfPTable summary = new PdfPTable(2);
            summary.setWidthPercentage(50);
            summary.addCell(headerCell("Total items"));
            summary.addCell(headerCell("Items con stock bajo"));
            summary.addCell(dataCell(String.valueOf(items.size()), false, Element.ALIGN_CENTER));
            summary.addCell(dataCell(String.valueOf(lowStockCount), false, Element.ALIGN_CENTER));
            doc.add(summary);
            doc.add(Chunk.NEWLINE);

            // Detalle agrupado por categoría
            Map<String, List<InventoryItem>> byCategory = items.stream()
                    .collect(Collectors.groupingBy(i ->
                            i.getCategory() != null ? i.getCategory().name() : "SIN CATEGORÍA"));

            for (Map.Entry<String, List<InventoryItem>> entry : byCategory.entrySet()) {
                addSectionTitle(doc, "Categoría: " + entry.getKey());

                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{3f, 1.5f, 1.5f, 1.5f, 1.5f});

                table.addCell(headerCell("Nombre"));
                table.addCell(headerCell("Cantidad"));
                table.addCell(headerCell("Unidad"));
                table.addCell(headerCell("Stock mínimo"));
                table.addCell(headerCell("Estado"));

                List<InventoryItem> catItems = entry.getValue();
                for (int i = 0; i < catItems.size(); i++) {
                    InventoryItem item = catItems.get(i);
                    boolean alt = i % 2 == 1;
                    boolean lowStock = item.getQuantity().compareTo(item.getMinStock()) <= 0;

                    table.addCell(dataCell(safe(item.getName()), alt));
                    table.addCell(dataCell(item.getQuantity().toPlainString(), alt, Element.ALIGN_CENTER));
                    table.addCell(dataCell(safe(item.getUnit()), alt, Element.ALIGN_CENTER));
                    table.addCell(dataCell(item.getMinStock().toPlainString(), alt, Element.ALIGN_CENTER));
                    table.addCell(stockStatusCell(lowStock, alt));
                }

                doc.add(table);
                doc.add(Chunk.NEWLINE);
            }

            if (items.isEmpty()) {
                doc.add(new Paragraph("No hay items registrados en el inventario.",
                        new Font(Font.HELVETICA, 10, Font.ITALIC, COLOR_TEXT_LIGHT)));
            }

        } catch (DocumentException e) {
            log.error("Error generando reporte de inventario: {}", e.getMessage());
        } finally {
            doc.close();
        }

        return out.toByteArray();
    }

    private PdfPCell stockStatusCell(boolean lowStock, boolean alt) {
        String text  = lowStock ? "STOCK BAJO" : "OK";
        Color  color = lowStock ? new Color(183, 28, 28) : new Color(27, 94, 32);
        Font   font  = new Font(Font.HELVETICA, 9, Font.BOLD, color);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(alt ? COLOR_ROW_ALT : COLOR_WHITE);
        cell.setPadding(6);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
}