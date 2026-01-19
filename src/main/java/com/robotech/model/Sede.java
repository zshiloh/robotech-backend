package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_sede")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sede {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sede")
    private Integer idSede;
    
    @Column(name = "nombre_sede", length = 100, nullable = false)
    private String nombreSede;
    
    @Column(name = "direccion", length = 200)
    private String direccion;
}