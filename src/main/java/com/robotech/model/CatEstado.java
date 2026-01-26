package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cat_estado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatEstado {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Integer idEstado;
    
    @Column(name = "descripcion", length = 45, nullable = false)
    private String descripcion;
}