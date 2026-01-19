package com.robotech.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_enfrentamiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enfrentamiento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_enfrentamiento")
    private Integer idEnfrentamiento;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fase", nullable = false)
    private CatFase fase;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_competidor_1")
    private Usuario competidor1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_competidor_2")
    private Usuario competidor2;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ganador")
    private Usuario ganador;
    
    @Column(name = "puntos_otorgados")
    private Integer puntosOtorgados;
    
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
    
    @Column(name = "estado_combate", length = 20, nullable = false)
    private String estadoCombate = "Pendiente";
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_jurado_registro")
    private Usuario juradoRegistro;
    
    @Column(name = "fecha_registro_resultado")
    private LocalDateTime fechaRegistroResultado;
}