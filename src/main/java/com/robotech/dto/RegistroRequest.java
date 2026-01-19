package com.robotech.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroRequest {
    
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombreCompleto;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
    
    @NotBlank(message = "Debes confirmar la contraseña")
    private String confirmarPassword;
    
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(
        regexp = "^(\\+51)?[9]\\d{8}$",
        message = "Formato de teléfono inválido. Debe ser +51987654321 o 987654321"
    )
    private String telefono;
    
    @Size(min = 5, max = 200, message = "La pregunta debe tener entre 5 y 200 caracteres")
    private String preguntaSeguridad;

    @Size(min = 2, max = 100, message = "La respuesta debe tener entre 2 y 100 caracteres")
    private String respuestaSeguridad;
}