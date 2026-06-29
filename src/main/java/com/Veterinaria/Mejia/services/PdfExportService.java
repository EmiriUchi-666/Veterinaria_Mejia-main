package com.Veterinaria.Mejia.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.Veterinaria.Mejia.dto.ProductoRentabilidadDTO;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

@Service
public class PdfExportService {

    public ByteArrayInputStream generarPdfRentabilidadProductos(List<ProductoRentabilidadDTO> datos, LocalDateTime inicio, LocalDateTime fin) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(out);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4.rotate())) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            document.add(new Paragraph("Reporte de Rentabilidad por Producto")
                    .setBold().setFontSize(16).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Período: " + inicio.format(formatter) + " - " + fin.format(formatter))
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

            float[] columnWidths = {3, 1, 1, 1, 1, 1, 1, 1};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Cabeceras
            String[] headers = {"Producto", "Unidades", "P. Compra", "P. Venta", "Ingresos", "Costo", "Utilidad", "Margen"};
            for (String header : headers) {
                Cell headerCell = new Cell().add(new Paragraph(header).setBold().setFontSize(9));
                headerCell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
                headerCell.setTextAlignment(TextAlignment.CENTER);
                table.addHeaderCell(headerCell);
            }

            // Datos
            for (ProductoRentabilidadDTO dto : datos) {
                table.addCell(new Cell().add(new Paragraph(dto.getNombreProducto()).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(String.format("%.0f", dto.getUnidadesVendidas())).setFontSize(8).setTextAlignment(TextAlignment.CENTER)));
                table.addCell(new Cell().add(new Paragraph(String.format("S/ %.2f", dto.getPrecioCompra())).setFontSize(8).setTextAlignment(TextAlignment.RIGHT)));
                table.addCell(new Cell().add(new Paragraph(String.format("S/ %.2f", dto.getPrecioVenta())).setFontSize(8).setTextAlignment(TextAlignment.RIGHT)));
                table.addCell(new Cell().add(new Paragraph(String.format("S/ %.2f", dto.getIngresosTotales())).setFontSize(8).setTextAlignment(TextAlignment.RIGHT)));
                table.addCell(new Cell().add(new Paragraph(String.format("S/ %.2f", dto.getCostoTotal())).setFontSize(8).setTextAlignment(TextAlignment.RIGHT)));
                table.addCell(new Cell().add(new Paragraph(String.format("S/ %.2f", dto.getUtilidadTotal())).setBold().setFontSize(8).setTextAlignment(TextAlignment.RIGHT)));
                
                BigDecimal margen = BigDecimal.ZERO;
                if (dto.getCostoTotal() != null && dto.getCostoTotal().compareTo(BigDecimal.ZERO) > 0) {
                    margen = dto.getUtilidadTotal().divide(dto.getCostoTotal(), 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100));
                }
                table.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", margen)).setFontSize(8).setTextAlignment(TextAlignment.CENTER)));
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            // Log error
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}