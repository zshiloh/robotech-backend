package com.robotech.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.robotech.dto.ClubHistorialResponse;
import com.robotech.dto.RankingResponse;
import com.robotech.dto.TorneoResultadoResponse;
import com.robotech.exception.BusinessException;
import com.robotech.exception.ResourceNotFoundException;
import com.robotech.model.Categoria;
import com.robotech.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportePdfService {
    
    @Autowired
    private RankingService rankingService;
    
    @Autowired
    private TorneoService torneoService;
    
    @Autowired
    private ClubService clubService;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    /**
     * Generar reporte PDF de ranking de competidores
     */
    public byte[] generarRankingPdf(Integer categoriaId) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            Categoria categoria = categoriaRepository.findById(categoriaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", categoriaId));
            
            Paragraph titulo = new Paragraph("RANKING DE COMPETIDORES")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(titulo);
            
            Paragraph fecha = new Paragraph("Fecha: " + java.time.LocalDate.now())
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(fecha);
            
            document.add(new Paragraph("\n"));
            
            Paragraph cat = new Paragraph("Categoría: " + categoria.getNombreCategoria())
                    .setFontSize(14)
                    .setBold();
            document.add(cat);
            
            Paragraph torneo = new Paragraph("Torneo: " + categoria.getTorneo().getNombreTorneo())
                    .setFontSize(12);
            document.add(torneo);
            
            document.add(new Paragraph("\n"));
            
            List<RankingResponse> ranking = rankingService.obtenerRankingIndividual(categoriaId, null);
            
            if (ranking.isEmpty()) {
                document.add(new Paragraph("No hay datos de ranking disponibles."));
            } else {
                float[] columnWidths = {1, 4, 3, 3, 2, 2, 2};
                Table table = new Table(UnitValue.createPercentArray(columnWidths));
                table.setWidth(UnitValue.createPercentValue(100));
                
                table.addHeaderCell(new Cell().add(new Paragraph("#").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Competidor").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Apodo").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Club").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("V").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("D").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Pts").setBold()));
                
                for (RankingResponse r : ranking) {
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(r.getPosicion()))));
                    table.addCell(new Cell().add(new Paragraph(r.getNombreCompetidor())));
                    table.addCell(new Cell().add(new Paragraph(r.getApodo() != null ? r.getApodo() : "-")));
                    table.addCell(new Cell().add(new Paragraph(r.getNombreClub())));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(r.getVictorias()))));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(r.getDerrotas()))));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(r.getPuntosTotales()))));
                }
                
                document.add(table);
            }
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new BusinessException("Error al generar PDF de ranking: " + e.getMessage());
        }
    }
    
    /**
     * Generar reporte PDF de historial de torneos
     */
    public byte[] generarHistorialTorneosPdf() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            Paragraph titulo = new Paragraph("HISTORIAL DE TORNEOS FINALIZADOS")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(titulo);
            
            Paragraph fecha = new Paragraph("Fecha: " + java.time.LocalDate.now())
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(fecha);
            
            document.add(new Paragraph("\n"));
            
            List<TorneoResultadoResponse> torneos = torneoService.obtenerTorneosFinalizados()
                    .stream()
                    .map(t -> torneoService.obtenerResultadosTorneo(t.getIdTorneo()))
                    .toList();
            
            if (torneos.isEmpty()) {
                document.add(new Paragraph("No hay torneos finalizados."));
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                
                for (TorneoResultadoResponse torneo : torneos) {
                    
                    Paragraph nombreTorneo = new Paragraph(torneo.getNombreTorneo())
                            .setFontSize(16)
                            .setBold();
                    document.add(nombreTorneo);
                    
                    String fechas = torneo.getFechaInicio().format(formatter) + 
                                   " - " + torneo.getFechaFin().format(formatter);
                    document.add(new Paragraph("Fechas: " + fechas).setFontSize(10));
                    document.add(new Paragraph("Sede: " + torneo.getNombreSede()).setFontSize(10));
                    
                    document.add(new Paragraph("\n"));
                    
                    for (TorneoResultadoResponse.CategoriaResultadoResponse cat : torneo.getResultadosPorCategoria()) {
                        Paragraph categoria = new Paragraph("Categoría: " + cat.getNombreCategoria())
                                .setFontSize(12)
                                .setBold();
                        document.add(categoria);
                        
                        if (!cat.getPodio().isEmpty()) {
                            Table tablaPodio = new Table(UnitValue.createPercentArray(new float[]{1, 4, 4, 3, 2}));
                            tablaPodio.setWidth(UnitValue.createPercentValue(80));
                            
                            tablaPodio.addHeaderCell(new Cell().add(new Paragraph("Pos").setBold()));
                            tablaPodio.addHeaderCell(new Cell().add(new Paragraph("Competidor").setBold()));
                            tablaPodio.addHeaderCell(new Cell().add(new Paragraph("Robot").setBold()));
                            tablaPodio.addHeaderCell(new Cell().add(new Paragraph("Club").setBold()));
                            tablaPodio.addHeaderCell(new Cell().add(new Paragraph("Pts").setBold()));
                            
                            for (TorneoResultadoResponse.ParticipanteResponse p : cat.getPodio()) {
                                String emoji = p.getPosicion() == 1 ? "1o" : (p.getPosicion() == 2 ? "2o" : "3o");
                                tablaPodio.addCell(new Cell().add(new Paragraph(emoji)));
                                tablaPodio.addCell(new Cell().add(new Paragraph(p.getNombreCompetidor())));
                                tablaPodio.addCell(new Cell().add(new Paragraph(p.getNombreRobot())));
                                tablaPodio.addCell(new Cell().add(new Paragraph(p.getNombreClub())));
                                tablaPodio.addCell(new Cell().add(new Paragraph(String.valueOf(p.getPuntosTotales()))));
                            }
                            
                            document.add(tablaPodio);
                        }
                        
                        document.add(new Paragraph("\n"));
                    }
                    
                    document.add(new Paragraph("\n"));
                }
            }
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new BusinessException("Error al generar PDF de historial de torneos: " + e.getMessage());
        }
    }
    
    /**
     * Generar reporte PDF de club
     */
    public byte[] generarReporteClubPdf(Integer clubId, Integer categoriaId) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            ClubHistorialResponse historial = clubService.obtenerHistorialClub(clubId, categoriaId);
            
            Paragraph titulo = new Paragraph("REPORTE DE CLUB")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(titulo);
            
            Paragraph fecha = new Paragraph("Fecha: " + java.time.LocalDate.now())
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(fecha);
            
            document.add(new Paragraph("\n"));
            
            Paragraph nombreClub = new Paragraph("Club: " + historial.getNombreClub())
                    .setFontSize(16)
                    .setBold();
            document.add(nombreClub);
            
            Paragraph categoria = new Paragraph("Categoría: " + historial.getNombreCategoria())
                    .setFontSize(14);
            document.add(categoria);
            
            document.add(new Paragraph("\n"));
            
            Paragraph stats = new Paragraph("Estadísticas:")
                    .setFontSize(14)
                    .setBold();
            document.add(stats);
            
            document.add(new Paragraph("Puntos históricos totales: " + historial.getPuntosHistoricosTotales()));
            document.add(new Paragraph("Competidores activos: " + historial.getCompetidoresActivos()));
            
            document.add(new Paragraph("\n"));
            
            Paragraph miembros = new Paragraph("Historial de Miembros:")
                    .setFontSize(14)
                    .setBold();
            document.add(miembros);
            
            float[] columnWidths = {4, 4, 2, 4, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            
            table.addHeaderCell(new Cell().add(new Paragraph("Competidor").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Robot").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Puntos").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Periodo").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Estado").setBold()));
            
            for (ClubHistorialResponse.MiembroHistoricoResponse m : historial.getHistorial()) {
                table.addCell(new Cell().add(new Paragraph(m.getNombreCompetidor())));
                table.addCell(new Cell().add(new Paragraph(m.getNombreRobot())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(m.getPuntosAportados()))));
                table.addCell(new Cell().add(new Paragraph(m.getPeriodo())));
                table.addCell(new Cell().add(new Paragraph(m.getActivo() ? "Activo" : "Inactivo")));
            }
            
            document.add(table);
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new BusinessException("Error al generar PDF de club: " + e.getMessage());
        }
    }
}