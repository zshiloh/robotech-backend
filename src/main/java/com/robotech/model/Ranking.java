package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_ranking",
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_categoria", "id_inscripcion"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ranking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ranking")
    private Integer idRanking;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_inscripcion", nullable = false)
    private Inscripcion inscripcion;
    
    @Column(name = "posicion", nullable = false)
    private Integer posicion;
    
    @Column(name = "puntos_totales", nullable = false)
    private Integer puntosTotales = 0;
    
    @Column(name = "victorias", nullable = false)
    private Integer victorias = 0;
    
    @Column(name = "derrotas", nullable = false)
    private Integer derrotas = 0;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}