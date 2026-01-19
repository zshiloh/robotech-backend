package com.robotech.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaRequest {
    
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String nombreCategoria;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    private String reglasEspecificas;
    
    @NotNull(message = "El cupo mínimo es obligatorio")
    @Min(value = 2, message = "El cupo mínimo debe ser al menos 2")
    private Integer cupoMinimo;
    
    @NotNull(message = "El cupo máximo es obligatorio")
    @Min(value = 2, message = "El cupo máximo debe ser al menos 2")
    private Integer cupoMaximo;
    
    @NotNull(message = "El ID del torneo es obligatorio")
    private Integer idTorneo;
    
    @NotNull(message = "El ID de la categoría de peso es obligatorio")
    @Min(value = 1, message = "ID de categoría de peso inválido")
    @Max(value = 3, message = "ID de categoría de peso inválido")
    private Integer idCategoriaPeso;
}