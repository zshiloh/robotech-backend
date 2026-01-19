package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_categoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;
    
    @Column(name = "nombre_categoria", length = 50, nullable = false)
    private String nombreCategoria;
    
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "reglas_especificas", columnDefinition = "JSON")
    private String reglasEspecificas;
    
    @Column(name = "cupo_minimo", nullable = false)
    private Integer cupoMinimo = 4;
    
    @Column(name = "cupo_maximo", nullable = false)
    private Integer cupoMaximo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria_peso")
    private CatCategoriaPeso categoriaPeso;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_torneo", nullable = false)
    private Torneo torneo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false)
    private CatEstado estado;
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
    
    @Column(name = "tiene_enfrentamientos", nullable = false)
    private Boolean tieneEnfrentamientos = false;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}