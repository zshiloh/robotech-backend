package com.robotech.dto;

import com.robotech.model.Enfrentamiento;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class EnfrentamientoResponse {
    
    private Integer idEnfrentamiento;
    private String nombreCategoria;
    private String nombreFase;
    private String nombreCompetidor1;
    private String robotCompetidor1;
    private String nombreCompetidor2;
    private String robotCompetidor2;
    private String nombreGanador;
    private Integer puntosOtorgados;
    private LocalDateTime fechaHora;
    private String estadoCombate;
    
    public EnfrentamientoResponse(Enfrentamiento e) {
        this.idEnfrentamiento = e.getIdEnfrentamiento();
        this.nombreCategoria = e.getCategoria().getNombreCategoria();
        this.nombreFase = e.getFase().getNombreFase();
        
        if (e.getCompetidor1() != null) {
            this.nombreCompetidor1 = e.getCompetidor1().getNombreCompleto();
            this.robotCompetidor1 = "Robot de " + e.getCompetidor1().getNombreCompleto();
        }
        
        if (e.getCompetidor2() != null) {
            this.nombreCompetidor2 = e.getCompetidor2().getNombreCompleto();
            this.robotCompetidor2 = "Robot de " + e.getCompetidor2().getNombreCompleto();
        }
        
        if (e.getGanador() != null) {
            this.nombreGanador = e.getGanador().getNombreCompleto();
        }
        
        this.puntosOtorgados = e.getPuntosOtorgados();
        this.fechaHora = e.getFechaHora();
        this.estadoCombate = e.getEstadoCombate();
    }
}