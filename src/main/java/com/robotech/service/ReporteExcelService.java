package com.robotech.service;

import com.robotech.exception.BusinessException;
import com.robotech.exception.ResourceNotFoundException;
import com.robotech.model.Categoria;
import com.robotech.model.Inscripcion;
import com.robotech.model.Robot;
import com.robotech.model.Torneo;
import com.robotech.repository.CategoriaRepository;
import com.robotech.repository.InscripcionRepository;
import com.robotech.repository.RobotRepository;
import com.robotech.repository.TorneoRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReporteExcelService {
    
    @Autowired
    private InscripcionRepository inscripcionRepository;
    
    @Autowired
    private TorneoRepository torneoRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    /**
     * Generar reporte Excel de inscripciones por torneo
     */
    public byte[] generarInscripcionesExcel(Integer torneoId) {
        try {
            Torneo torneo = torneoRepository.findById(torneoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Torneo", "id", torneoId));
            
            Workbook workbook = new XSSFWorkbook();
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            List<Categoria> categorias = categoriaRepository.findByTorneo_IdTorneo(torneoId);
            
            for (Categoria categoria : categorias) {
                Sheet sheet = workbook.createSheet(categoria.getNombreCategoria());
                
                Row titleRow = sheet.createRow(0);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("INSCRIPCIONES - " + torneo.getNombreTorneo());
                CellStyle titleStyle = workbook.createCellStyle();
                Font titleFont = workbook.createFont();
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 16);
                titleStyle.setFont(titleFont);
                titleCell.setCellStyle(titleStyle);
                
                Row infoRow1 = sheet.createRow(1);
                infoRow1.createCell(0).setCellValue("Categoría: " + categoria.getNombreCategoria());
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                Row infoRow2 = sheet.createRow(2);
                infoRow2.createCell(0).setCellValue("Fecha: " + torneo.getFechaInicio().format(formatter) + 
                                                   " - " + torneo.getFechaFin().format(formatter));
                
                sheet.createRow(3);
                
                Row headerRow = sheet.createRow(4);
                String[] headers = {"#", "Competidor", "Robot", "Club", "Estado", "Fecha Inscripción", "Puntos"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }
                
                List<Inscripcion> inscripciones = inscripcionRepository.findByCategoria_IdCategoria(categoria.getIdCategoria());
                
                int rowNum = 5;
                int count = 1;
                for (Inscripcion inscripcion : inscripciones) {
                    Robot robot = robotRepository.findByUsuario_IdUsuario(inscripcion.getUsuario().getIdUsuario())
                            .orElse(null);
                    
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(count++);
                    row.createCell(1).setCellValue(inscripcion.getUsuario().getNombreCompleto());
                    row.createCell(2).setCellValue(robot != null ? robot.getNombreRobot() : "Sin robot");
                    row.createCell(3).setCellValue(robot != null && robot.getClub() != null ? 
                                                   robot.getClub().getNombreClub() : "Sin club");
                    row.createCell(4).setCellValue(inscripcion.getEstado());
                    row.createCell(5).setCellValue(inscripcion.getFechaSolicitud().format(formatter));
                    row.createCell(6).setCellValue(inscripcion.getPuntajeAcumulado());
                }
                
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            workbook.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new BusinessException("Error al generar Excel de inscripciones: " + e.getMessage());
        }
    }
}