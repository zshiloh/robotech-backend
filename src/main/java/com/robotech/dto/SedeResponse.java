package com.robotech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SedeResponse {
    
    private Integer idSede;
    private String nombreSede;
    private String direccion;
}