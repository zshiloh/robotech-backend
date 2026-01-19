package com.robotech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String mensaje;
    
    public ErrorResponse(Integer status, String error, String mensaje) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
    }
}