package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cat_rol")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatRol {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;
    
    @Column(name = "nombre_rol", length = 50, nullable = false)
    private String nombreRol;
    
    @Column(name = "descripcion", length = 255)
    private String descripcion;
}