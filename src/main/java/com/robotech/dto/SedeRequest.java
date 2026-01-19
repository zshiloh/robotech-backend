package com.robotech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SedeRequest {
    
    @NotBlank(message = "El nombre de la sede es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombreSede;
    
    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String direccion;
}