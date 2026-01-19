package com.robotech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String token;
    private UsuarioResponse usuario;
    private String apodo;
    private List<String> roles;
    private Boolean debeCambiarPassword;
}