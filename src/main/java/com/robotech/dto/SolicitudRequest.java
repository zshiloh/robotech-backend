package com.robotech.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRequest {
    
    @Size(max = 500, message = "El mensaje no puede exceder 500 caracteres")
    private String mensajeOpcional;
}