package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_ranking_club",
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_categoria", "id_club"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingClub {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ranking_club")
    private Integer idRankingClub;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_club", nullable = false)
    private Club club;
    
    @Column(name = "posicion_club", nullable = false)
    private Integer posicionClub;
    
    @Column(name = "puntos_totales_club", nullable = false)
    private Integer puntosTotalesClub = 0;
    
    @Column(name = "victorias_totales", nullable = false)
    private Integer victoriaTotales = 0;
    
    @Column(name = "derrotas_totales", nullable = false)
    private Integer derrotasTotales;
    
    @Column(name = "competidores_activos", nullable = false)
    private Integer competidoresActivos = 0;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}