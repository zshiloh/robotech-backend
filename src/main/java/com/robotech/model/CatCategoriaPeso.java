package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "cat_categoria_peso")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatCategoriaPeso {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria_peso")
    private Integer idCategoriaPeso;
    
    @Column(name = "nombre", length = 100, unique = true, nullable = false)
    private String nombre;
    
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "peso_minimo", nullable = false)
    private Integer pesoMinimo; // En gramos
    
    @Column(name = "peso_maximo", nullable = false)
    private Integer pesoMaximo; // En gramos
    
    @Column(name = "dimension_maxima", nullable = false)
    private Integer dimensionMaxima; // En centímetros
    
    @Column(name = "activa", nullable = false)
    private Boolean activa = true;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}