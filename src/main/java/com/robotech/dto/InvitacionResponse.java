package com.robotech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitacionResponse {
    
    private Integer idInvitacion;
    private String nombreClub;
    private Integer idClub;
    private String emailDestinatario;
    private String token;
    private String estado;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaRespuesta;
}