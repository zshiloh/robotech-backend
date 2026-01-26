package com.robotech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RobotResponse {
    
    private Integer idRobot;
    private String nombreRobot;
    private String descripcion;
    private Integer peso;
    private String dimensiones;
    private String categoriaPeso;
    private Integer victorias;
    private Integer derrotas;
    private Integer puntosTotales;
    private String nombreClub;
    private Integer idClub;
    private String estado;
    private String razonInactividad;
}