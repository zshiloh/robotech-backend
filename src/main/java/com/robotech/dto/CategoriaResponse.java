package com.robotech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaResponse {
    
    private Integer idCategoria;
    private String nombreCategoria;
    private String descripcion;
    private String reglasEspecificas;
    private Integer cupoMinimo;
    private Integer cupoMaximo;
    private String nombreCategoriaPeso;
    private String nombreTorneo;
    private Integer idEstado;
    private String estado;
    private Boolean tieneEnfrentamientos;
    private Integer inscritosActuales;
}