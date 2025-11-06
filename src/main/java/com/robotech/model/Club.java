package com.robotech.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "club")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_club")
    private Integer idClub;

    @Column(name = "nombre_club", nullable = false, unique = true, length = 150)
    private String nombreClub;

    @Column(name = "representante", nullable = false, length = 150)
    private String representante;

    @Column(name = "correo_representante", nullable = false, length = 100)
    private String correoRepresentante;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "direccion", length = 250)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoClub estado = EstadoClub.Pendiente;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_validacion")
    private LocalDateTime fechaValidacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_validador")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario usuarioValidador;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoClub.Pendiente;
        }
    }

    public enum EstadoClub {
        Pendiente,
        Activo,
        Rechazado,
        Inactivo
    }
}