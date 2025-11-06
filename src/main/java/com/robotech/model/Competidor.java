package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "competidor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Competidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_competidor")
    private Integer idCompetidor;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "documento_identidad", length = 8)
    private String documentoIdentidad;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "edad")
    private Integer edad;

    @Column(name = "correo", length = 100)
    private String correo;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_club", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @Column(name = "nombre_robot", length = 100)
    private String nombreRobot;

    @Column(name = "fecha_inscripcion", nullable = false, updatable = false)
    private LocalDateTime fechaInscripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        fechaInscripcion = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}