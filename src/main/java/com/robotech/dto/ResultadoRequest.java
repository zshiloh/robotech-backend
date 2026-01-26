package com.robotech.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoRequest {
    
    @NotNull(message = "El ID del ganador es obligatorio")
    private Integer idGanador;
    
    @NotNull(message = "Los puntos otorgados son obligatorios")
    @Min(value = 0, message = "Los puntos deben ser mayor o igual a 0")
    private Integer puntosOtorgados;
}