package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_notificacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Integer idNotificacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    @Column(name = "tipo", length = 50, nullable = false)
    private String tipo;
    
    @Column(name = "titulo", length = 100, nullable = false)
    private String titulo;
    
    @Column(name = "mensaje", columnDefinition = "TEXT", nullable = false)
    private String mensaje;
    
    @Column(name = "leida", nullable = false)
    private Boolean leida = false;
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}