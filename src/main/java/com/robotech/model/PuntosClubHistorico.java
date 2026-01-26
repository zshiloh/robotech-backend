package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_puntos_club_historico",
       indexes = {
           @Index(name = "idx_club_categoria", columnList = "id_club, id_categoria"),
           @Index(name = "idx_usuario_categoria", columnList = "id_usuario, id_categoria"),
           @Index(name = "idx_activo", columnList = "fecha_fin")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PuntosClubHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historico")
    private Integer idHistorico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_club", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @Column(name = "puntos_ganados_aqui", nullable = false)
    @Builder.Default
    private Integer puntosGanadosAqui = 0;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    public boolean isActivo() {
        return this.fechaFin == null;
    }

    public void cerrar() {
        this.fechaFin = LocalDateTime.now();
    }

    public void sumarPuntos(Integer puntos) {
        if (puntos != null && puntos > 0) {
            this.puntosGanadosAqui += puntos;
        }
    }
}