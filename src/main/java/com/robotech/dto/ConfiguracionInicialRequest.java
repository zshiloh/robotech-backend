package com.robotech.dto;

import lombok.Data;

@Data
public class ConfiguracionInicialRequest {
	
    private String passwordNueva;
    private String confirmarPassword;
    private String nombreCompleto;
    private String telefono;
    private String apodoPersonal;
}