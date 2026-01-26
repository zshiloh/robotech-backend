package com.robotech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TorneoResponse {
    
    private Integer idTorneo;
    private String nombreTorneo;
    private String descripcion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String nombreSede;
    private Integer idEstado;
    private String estado;
    private Integer totalCategorias;
}