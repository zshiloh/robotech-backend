package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_club")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Club {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_club")
    private Integer idClub;
    
    @Column(name = "nombre_club", length = 100, unique = true, nullable = false)
    private String nombreClub;
    
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "logo_url", length = 255)
    private String logoUrl;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_representante", nullable = false)
    private Usuario representante;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false)
    private CatEstado estado;
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_validacion")
    private LocalDateTime fechaValidacion;
    
    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;
    
    @Column(name = "cambios_pendientes_validacion", nullable = false)
    private Boolean cambiosPendientesValidacion = false;
    
    @Column(name = "acepta_solicitudes", nullable = false)
    private Boolean aceptaSolicitudes = true;
    
    @Column(name = "limite_competidores", nullable = false)
    private Integer limiteCompetidores = 6;
    
    @Column(name = "ultimo_torneo_participado")
    private LocalDateTime ultimoTorneoParticipado;
    
    @Convert(converter = EstadoActividadConverter.class)
    @Column(name = "estado_actividad", nullable = false)
    private EstadoActividad estadoActividad = EstadoActividad.ACTIVO;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
    
    public enum EstadoActividad {
        ACTIVO("Activo"),
        INACTIVO_7D("Inactivo_7d"),
        INACTIVO("Inactivo");
        
        private final String descripcion;
        
        EstadoActividad(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
}