package com.robotech.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarRobotRequest {
    
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombreRobot;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    @Min(value = 1, message = "El peso debe ser mayor a 0")
    @Max(value = 5000, message = "El peso no puede exceder 5000 gramos")
    private Integer peso;
    
    @Pattern(regexp = "^\\d+x\\d+x\\d+$", message = "Formato inválido. Usa: LxWxH (ej: 25x20x15)")
    private String dimensiones;
    
    @Min(value = 1, message = "ID de categoría inválido")
    @Max(value = 3, message = "ID de categoría inválido")
    private Integer idCategoriaPeso;
}