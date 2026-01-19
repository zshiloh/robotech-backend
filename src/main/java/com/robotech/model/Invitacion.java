package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_invitacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invitacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_invitacion")
    private Integer idInvitacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_club", nullable = false)
    private Club club;
    
    @Column(name = "email_destinatario", length = 100, nullable = false)
    private String emailDestinatario;
    
    @Column(name = "token", length = 64, unique = true, nullable = false)
    private String token;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false)
    private CatEstado estado;
    
    @Column(name = "fecha_envio", nullable = false, updatable = false)
    private LocalDateTime fechaEnvio;
    
    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;
    
    @PrePersist
    protected void onCreate() {
        fechaEnvio = LocalDateTime.now();
    }
}