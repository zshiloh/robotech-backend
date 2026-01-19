package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "t_usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;
    
    @Column(name = "nombre_completo", length = 100, nullable = false)
    private String nombreCompleto;
    
    @Column(name = "apodo_personal", unique = true, length = 50)
    private String apodoPersonal;
    
    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;
    
    @Column(name = "telefono", length = 20, unique = true)
    private String telefono;
    
    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;
    
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;
    
    @Column(name = "intentos_fallidos", nullable = false)
    private Integer intentosFallidos = 0;
    
    @Column(name = "fecha_ultimo_intento")
    private LocalDateTime fechaUltimoIntento;
    
    @Column(name = "cambios_club_trimestre_actual", nullable = false)
    private Integer cambiosClubTrimestreActual = 0;
    
    @Column(name = "ultimo_trimestre_registrado", length = 10)
    private String ultimoTrimestreRegistrado;
    
    @Column(name = "cambios_club_totales_historico", nullable = false)
    private Integer cambiosClubTotalesHistorico = 0;
    
    @Column(name = "fecha_ultimo_cambio_club")
    private LocalDateTime fechaUltimoCambioClub;
    
    @Column(name = "ultimo_inicio_sesion")
    private LocalDateTime ultimoInicioSesion;
    
    @Column(name = "pregunta_seguridad", length = 200)
    private String preguntaSeguridad;

    @Column(name = "respuesta_seguridad_hash", length = 255)
    private String respuestaSeguridadHash;

    @Column(name = "token_recuperacion", length = 100)
    private String tokenRecuperacion;

    @Column(name = "token_expiracion")
    private LocalDateTime tokenExpiracion;

    @Column(name = "intentos_recuperacion")
    private Integer intentosRecuperacion = 0;

    @Column(name = "fecha_ultimo_intento_recuperacion")
    private LocalDateTime fechaUltimoIntentoRecuperacion;
    
    @Column(name = "debe_cambiar_password", nullable = false)
    private Boolean debeCambiarPassword = false;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "t_usuario_rol",
        joinColumns = @JoinColumn(name = "id_usuario"),
        inverseJoinColumns = @JoinColumn(name = "id_rol")
    )
    private Set<CatRol> roles = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }
}