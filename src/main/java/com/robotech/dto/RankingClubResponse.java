package com.robotech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingClubResponse {
    
    private Integer posicionClub;
    private String nombreClub;
    private String logoUrl;
    private Integer puntosTotalesClub;
    private Integer victoriaTotales;
    private Integer derrotasTotales;
    private Integer competidoresActivos;
    private Boolean esClubDelUsuario;
}