package com.robotech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditarPerfilUsuarioRequest {
    
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombreCompleto;
    
    @Size(min = 2, max = 50, message = "El apodo debe tener entre 2 y 50 caracteres")
    @Pattern(
        regexp = "^[a-zA-Z0-9_]+$", 
        message = "El apodo solo puede contener letras, números y guiones bajos"
    )
    private String apodoPersonal;
    
    @Size(min = 5, max = 200, message = "La pregunta debe tener entre 5 y 200 caracteres")
    private String preguntaSeguridad;
    
    @Size(min = 2, max = 100, message = "La respuesta debe tener entre 2 y 100 caracteres")
    private String respuestaSeguridad;
}