package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_inscripcion", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "id_categoria"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inscripcion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscripcion")
    private Integer idInscripcion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_torneo")
    private Torneo torneo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_robot")
    private Robot robot;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;
    
    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;
    
    @Column(name = "puntaje_acumulado")
    private Integer puntajeAcumulado = 0;
    
    @Column(name = "estado", length = 20, nullable = false)
    private String estado = "APROBADA";
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado")
    private CatEstado estadoCategoria;
    
    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;
    
    @Column(name = "observaciones_representante", columnDefinition = "TEXT")
    private String observacionesRepresentante;
    
    // Estados posibles
    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_APROBADA = "APROBADA";
    public static final String ESTADO_RECHAZADA = "RECHAZADA";
    
    @PrePersist
    protected void onCreate() {
        fechaSolicitud = LocalDateTime.now();
        if (estado == null) {
            estado = ESTADO_APROBADA;
        }
    }
}