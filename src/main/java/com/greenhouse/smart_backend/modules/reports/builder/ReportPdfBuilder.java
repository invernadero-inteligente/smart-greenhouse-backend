package com.greenhouse.smart_backend.modules.reports.builder;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Clase base para construir reportes PDF con formato corporativo.
 * Proporciona métodos comunes para agregar encabezados, secciones y tablas.
 */
public abstract class ReportPdfBuilder {

    protected static final DateTimeFormatter DT_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    protected static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Colores corporativos
    protected static final Color COLOR_HEADER     = new Color(27, 94, 32);   // verde oscuro
    protected static final Color COLOR_SUBHEADER  = new Color(46, 125, 50);  // verde medio
    protected static final Color COLOR_ROW_ALT    = new Color(232, 245, 233); // verde muy claro
    protected static final Color COLOR_WHITE      = Color.WHITE;
    protected static final Color COLOR_TEXT_LIGHT = new Color(117, 117, 117);

    // Fuentes
    protected static final Font FONT_TITLE = new Font(Font.HELVETICA, 18, Font.BOLD, Color.WHITE);
    protected static final Font FONT_SUBTITLE = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(69, 69, 69));
    protected static final Font FONT_SECTION = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(27, 94, 32));
    protected static final Font FONT_TABLE_HEADER = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    protected static final Font FONT_TABLE_BODY = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(33, 33, 33));
    protected static final Font FONT_TABLE_BODY_BOLD = new Font(Font.HELVETICA, 9, Font.BOLD, new Color(33, 33, 33));
    protected static final Font FONT_META = new Font(Font.HELVETICA, 9, Font.NORMAL, COLOR_TEXT_LIGHT);

    /**
     * Genera el PDF y lo retorna como array de bytes.
     */
    public abstract byte[] build(LocalDateTime from, LocalDateTime to, Long zoneId);

    /**
     * Crea el encabezado estándar del reporte con título, sistema y período.
     */
    protected void addHeader(Document doc, String reportTitle,
                              LocalDateTime from, LocalDateTime to) throws DocumentException {
        // Banner de título
        PdfPTable banner = new PdfPTable(1);
        banner.setWidthPercentage(100);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBackgroundColor(COLOR_HEADER);
        titleCell.setPadding(16);
        titleCell.setBorder(Rectangle.NO_BORDER);

        Paragraph systemName = new Paragraph("Sistema de Gestión de Invernadero Inteligente",
                new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(165, 214, 167)));
        systemName.setAlignment(Element.ALIGN_CENTER);

        Paragraph title = new Paragraph(reportTitle, FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(4);

        titleCell.addElement(systemName);
        titleCell.addElement(title);
        banner.addCell(titleCell);
        doc.add(banner);

        // Línea de metadatos
        doc.add(Chunk.NEWLINE);
        PdfPTable meta = new PdfPTable(3);
        meta.setWidthPercentage(100);
        meta.setWidths(new float[]{1, 1, 1});

        meta.addCell(metaCell("Generado el", LocalDateTime.now().format(DT_FORMAT)));
        meta.addCell(metaCell("Desde", from.format(DT_FORMAT)));
        meta.addCell(metaCell("Hasta", to.format(DT_FORMAT)));
        doc.add(meta);
        doc.add(Chunk.NEWLINE);
    }

    private PdfPCell metaCell(String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(new Color(200, 230, 201));
        cell.setPadding(8);
        cell.addElement(new Paragraph(label, FONT_META));
        cell.addElement(new Paragraph(value, FONT_TABLE_BODY_BOLD));
        return cell;
    }

    /**
     * Crea una celda de encabezado de tabla con fondo verde.
     */
    protected PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_TABLE_HEADER));
        cell.setBackgroundColor(COLOR_SUBHEADER);
        cell.setPadding(7);
        cell.setBorderColor(new Color(200, 230, 201));
        cell.setBorderWidth(0.5f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }   

    /**
     * Crea una celda de datos alternando color de fila.
     */
    protected PdfPCell dataCell(String text, boolean alternate) {
        return dataCell(text, alternate, Element.ALIGN_LEFT);
    }

    protected PdfPCell dataCell(String text, boolean alternate, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", FONT_TABLE_BODY));
        cell.setBackgroundColor(alternate ? COLOR_ROW_ALT : COLOR_WHITE);
        cell.setPadding(6);
        cell.setBorderColor(new Color(220, 237, 220));
        cell.setBorderWidth(0.5f);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    /**
     * Agrega un título de sección al documento.
     */
    protected void addSectionTitle(Document doc, String title) throws DocumentException {
        Paragraph p = new Paragraph(title, FONT_SECTION);
        p.setSpacingBefore(12);
        p.setSpacingAfter(6);
        doc.add(p);
    }

    /**
     * Crea el documento base con márgenes estándar.
     */
    protected Document createDocument(ByteArrayOutputStream out) {
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, out);
        return doc;
    }

    protected String safe(String value) {
        return value != null ? value : "-";
    }
}
