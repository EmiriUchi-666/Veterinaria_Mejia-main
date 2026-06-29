package com.Veterinaria.Mejia.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.Veterinaria.Mejia.dto.ProductoRentabilidadDTO;


@Service
public class ExcelExportService {

    public ByteArrayInputStream generarExcelRentabilidadProductos(List<ProductoRentabilidadDTO> datos) throws IOException {
        String[] columnas = {"Producto", "Unidades Vendidas", "Precio Compra", "Precio Venta", "Ingresos Totales", "Costo Total", "Utilidad Total", "Margen (%)"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            Sheet sheet = workbook.createSheet("Rentabilidad_Productos");

            // Estilos
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat currencyFormat = workbook.createDataFormat();
            // Formato de moneda peruana
            currencyStyle.setDataFormat(currencyFormat.getFormat("\"S/\"#,##0.00"));

            // Fila de cabecera
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < columnas.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columnas[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // Filas de datos
            int rowIdx = 1;
            for (ProductoRentabilidadDTO dto : datos) {
                Row row = sheet.createRow(rowIdx++);
                
                row.createCell(0).setCellValue(dto.getNombreProducto());
                row.createCell(1).setCellValue(dto.getUnidadesVendidas().doubleValue());

                Cell cellCompra = row.createCell(2);
                cellCompra.setCellValue(dto.getPrecioCompra().doubleValue());
                cellCompra.setCellStyle(currencyStyle);

                Cell cellVenta = row.createCell(3);
                cellVenta.setCellValue(dto.getPrecioVenta().doubleValue());
                cellVenta.setCellStyle(currencyStyle);

                Cell cellIngresos = row.createCell(4);
                cellIngresos.setCellValue(dto.getIngresosTotales().doubleValue());
                cellIngresos.setCellStyle(currencyStyle);

                Cell cellCosto = row.createCell(5);
                cellCosto.setCellValue(dto.getCostoTotal().doubleValue());
                cellCosto.setCellStyle(currencyStyle);

                Cell cellUtilidad = row.createCell(6);
                cellUtilidad.setCellValue(dto.getUtilidadTotal().doubleValue());
                cellUtilidad.setCellStyle(currencyStyle);

                // Cálculo del margen
                double costoTotal = dto.getCostoTotal().doubleValue();
                double margen = costoTotal > 0 ? (dto.getUtilidadTotal().doubleValue() / costoTotal) * 100 : 0;
                row.createCell(7).setCellValue(String.format("%.1f%%", margen));
            }

            // Auto-ajustar el ancho de las columnas
            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}