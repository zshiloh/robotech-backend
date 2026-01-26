package com.robotech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {
    
    private Integer idUsuario;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimoInicioSesion;
    private List<String> roles;
    
    private String apodoPersonal;
    private String preguntaSeguridad;
    private String nombreClub;
    private Boolean activo;
}