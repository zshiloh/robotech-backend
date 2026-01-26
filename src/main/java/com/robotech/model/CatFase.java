package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cat_fase")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatFase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fase")
    private Integer idFase;
    
    @Column(name = "nombre_fase", length = 50, nullable = false)
    private String nombreFase;
    
    @Column(name = "orden", nullable = false)
    private Integer orden;
}