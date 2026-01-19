package com.robotech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingResponse {
    
    private Integer posicion;
    private String nombreCompetidor;
    private String apodo;
    private String nombreClub;
    private Integer puntosTotales;
    private Integer victorias;
    private Integer derrotas;
    private Boolean esUsuarioActual;
}