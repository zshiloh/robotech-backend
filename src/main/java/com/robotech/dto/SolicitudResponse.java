package com.robotech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudResponse {
    
    private Integer idSolicitud;
    
    private String nombreUsuario;
    private String emailUsuario;
    private Integer idUsuario;
    
    private String nombreClub;
    private Integer idClub;
   
    private String mensajeOpcional;
    private String estado;  // "Pendiente", "Aceptada", "Rechazada"
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaSolicitud;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaRespuesta;
}