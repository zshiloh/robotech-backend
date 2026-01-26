package com.robotech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubHistorialResponse {
    
    private Integer idClub;
    private String nombreClub;
    private String nombreCategoria;
    private Integer puntosHistoricosTotales;
    private Integer competidoresActivos;
    private List<MiembroHistoricoResponse> historial;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MiembroHistoricoResponse {
        private String nombreCompetidor;
        private String nombreRobot;
        private Integer puntosAportados;
        private LocalDateTime fechaInicio;
        private LocalDateTime fechaFin;
        private Boolean activo;
        private String periodo;
    }
}