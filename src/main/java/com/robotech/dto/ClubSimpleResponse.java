package com.robotech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubSimpleResponse {
    
    private Integer idClub;
    private String nombreClub;
    private String descripcion;
    private String logoUrl;
    private Integer competidoresActivos;
    private Boolean aceptaSolicitudes;
}