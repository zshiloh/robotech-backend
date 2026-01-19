package com.robotech.controller;

import com.robotech.service.ReporteExcelService;
import com.robotech.service.ReportePdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {
    
    @Autowired
    private ReportePdfService reportePdfService;
    
    @Autowired
    private ReporteExcelService reporteExcelService;
    
    /**
     * GET /api/reportes/ranking/pdf?categoriaId={id}
     * Descargar ranking de competidores en PDF
     */
    @GetMapping("/ranking/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZADOR', 'REPRESENTANTE')")
    public ResponseEntity<byte[]> descargarRankingPdf(@RequestParam("categoriaId") Integer categoriaId) {
        byte[] pdf = reportePdfService.generarRankingPdf(categoriaId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "ranking-categoria-" + categoriaId + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
    
    /**
     * GET /api/reportes/torneos/pdf
     * Descargar historial de torneos finalizados en PDF
     */
    @GetMapping("/torneos/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZADOR')")
    public ResponseEntity<byte[]> descargarHistorialTorneosPdf() {
        byte[] pdf = reportePdfService.generarHistorialTorneosPdf();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "historial-torneos.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
    
    /**
     * GET /api/reportes/club/pdf?clubId={id}&categoriaId={id}
     * Descargar reporte de club en PDF
     */
    @GetMapping("/club/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'REPRESENTANTE')")
    public ResponseEntity<byte[]> descargarReporteClubPdf(
            @RequestParam("clubId") Integer clubId,
            @RequestParam("categoriaId") Integer categoriaId) {
        
        byte[] pdf = reportePdfService.generarReporteClubPdf(clubId, categoriaId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "reporte-club-" + clubId + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
    
    /**
     * GET /api/reportes/inscripciones/excel?torneoId={id}
     * Descargar inscripciones de un torneo en Excel
     */
    @GetMapping("/inscripciones/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZADOR')")
    public ResponseEntity<byte[]> descargarInscripcionesExcel(@RequestParam("torneoId") Integer torneoId) {
        byte[] excel = reporteExcelService.generarInscripcionesExcel(torneoId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "inscripciones-torneo-" + torneoId + ".xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excel);
    }
}