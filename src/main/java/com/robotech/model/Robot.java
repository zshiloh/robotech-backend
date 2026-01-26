package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_robot")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Robot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_robot")
    private Integer idRobot;
    
    @Column(name = "nombre_robot", length = 100, unique = true, nullable = false)
    private String nombreRobot;
    
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "peso", nullable = false)
    private Integer peso; // En gramos
    
    @Column(name = "dimensiones", length = 50)
    private String dimensiones; // Formato: "25x25x20" (ancho x largo x alto)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria_peso", nullable = false)
    private CatCategoriaPeso categoriaPeso;
    
    @Column(name = "victorias", nullable = false)
    private Integer victorias = 0;
    
    @Column(name = "derrotas", nullable = false)
    private Integer derrotas = 0;
    
    @Column(name = "puntos_totales", nullable = false)
    private Integer puntosTotales = 0;
    
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", unique = true, nullable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_club")
    private Club club;
    
    @Convert(converter = EstadoRobotConverter.class)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoRobot estado = EstadoRobot.ACTIVO;
    
    @Column(name = "fecha_ultimo_enfrentamiento")
    private LocalDateTime fechaUltimoEnfrentamiento;
    
    @Column(name = "razon_inactividad", length = 100)
    private String razonInactividad;
    
    @Convert(converter = EstadoActividadRobotConverter.class)
    @Column(name = "estado_actividad", nullable = false)
    private EstadoActividadRobot estadoActividad = EstadoActividadRobot.ACTIVO;
    
    public enum EstadoRobot {
        ACTIVO("Activo"),
        INACTIVO("Inactivo"),
        INACTIVO_AUTO("Inactivo_Auto");
        
        private final String descripcion;
        
        EstadoRobot(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    public enum EstadoActividadRobot {
        ACTIVO("Activo"),
        INACTIVO_30D("Inactivo_30d");
        
        private final String descripcion;
        
        EstadoActividadRobot(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
}