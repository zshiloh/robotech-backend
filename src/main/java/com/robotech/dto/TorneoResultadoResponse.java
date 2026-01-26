package com.robotech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TorneoResultadoResponse {
    
    private Integer idTorneo;
    private String nombreTorneo;
    private String descripcion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String nombreSede;
    private String estado;

    private List<CategoriaResultadoResponse> resultadosPorCategoria;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoriaResultadoResponse {
        private String nombreCategoria;
        private List<ParticipanteResponse> podio;
        private List<ParticipanteResponse> rankingCompleto;
        private Integer totalParticipantes;
        private Integer totalEnfrentamientos;
        private String clubGanador;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipanteResponse {
        private Integer posicion;
        private String nombreCompetidor;
        private String nombreRobot;
        private String nombreClub;
        private Integer victorias;
        private Integer derrotas;
        private Integer puntosTotales;
    }
}