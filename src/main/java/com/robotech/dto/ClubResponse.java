package com.robotech.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubResponse {

    private Integer idClub;
    private String nombreClub;
    private String descripcion;
    private String logoUrl;
    private String nombreRepresentante;
    private String emailRepresentante;
    private Integer idRepresentante;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaValidacion;
    private String motivoRechazo;
    private Boolean aceptaSolicitudes;
    private Integer limiteCompetidores;
    private Integer competidoresActivos;
    private String estadoActividad;
    private List<MiembroDTO> miembros;
    
    public ClubResponse(Integer idClub, String nombreClub, String descripcion, 
                        String logoUrl, String nombreRepresentante, String emailRepresentante,
                        Integer idRepresentante, String estado, LocalDateTime fechaCreacion,
                        LocalDateTime fechaValidacion, String motivoRechazo, 
                        Boolean aceptaSolicitudes, Integer limiteCompetidores,
                        Integer competidoresActivos, String estadoActividad) {
        this.idClub = idClub;
        this.nombreClub = nombreClub;
        this.descripcion = descripcion;
        this.logoUrl = logoUrl;
        this.nombreRepresentante = nombreRepresentante;
        this.emailRepresentante = emailRepresentante;
        this.idRepresentante = idRepresentante;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaValidacion = fechaValidacion;
        this.motivoRechazo = motivoRechazo;
        this.aceptaSolicitudes = aceptaSolicitudes;
        this.limiteCompetidores = limiteCompetidores;
        this.competidoresActivos = competidoresActivos;
        this.estadoActividad = estadoActividad;
        this.miembros = null;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MiembroDTO {
        private Integer idUsuario;
        private String nombreCompleto;
        private String email;
        private Integer idRobot;
        private String nombreRobot;
    }
}