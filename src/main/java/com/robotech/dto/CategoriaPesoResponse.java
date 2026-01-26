package com.robotech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaPesoResponse {
    
    private Integer idCategoriaPeso;
    private String nombre;
    private String descripcion;
    private Integer pesoMinimo;
    private Integer pesoMaximo;
    private Integer dimensionMaxima;
    private Boolean activa;
}