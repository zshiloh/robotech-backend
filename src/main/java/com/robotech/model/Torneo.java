package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_torneo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Torneo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_torneo")
    private Integer idTorneo;
    
    @Column(name = "nombre_torneo", length = 100, nullable = false)
    private String nombreTorneo;
    
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;
    
    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sede", nullable = false)
    private Sede sede;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false)
    private CatEstado estado;
}