package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_solicitud")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Solicitud {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Integer idSolicitud;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_club", nullable = false)
    private Club club;
    
    @Column(name = "mensaje_opcional", columnDefinition = "TEXT")
    private String mensajeOpcional;
    
    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false)
    private CatEstado estado;
    
    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;
    
    @PrePersist
    protected void onCreate() {
        fechaSolicitud = LocalDateTime.now();
    }
}