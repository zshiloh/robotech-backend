package com.robotech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionResponse {
    
    private Integer idInscripcion;
    private String nombreCompetidor;
    private String nombreRobot;
    private String nombreTorneo;
    private String nombreCategoria;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaAprobacion;
    private String observaciones;
}