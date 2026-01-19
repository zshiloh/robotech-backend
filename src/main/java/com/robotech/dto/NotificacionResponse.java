package com.robotech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionResponse {
    
    private Integer idNotificacion;
    private String tipo;
    private String titulo;
    private String mensaje;
    private Boolean leida;
    private LocalDateTime fechaCreacion;
}