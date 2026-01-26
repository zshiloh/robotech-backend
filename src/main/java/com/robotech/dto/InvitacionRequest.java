package com.robotech.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitacionRequest {
    
    @NotBlank(message = "El email del destinatario es obligatorio")
    @Email(message = "Email inválido")
    private String emailDestinatario;
}