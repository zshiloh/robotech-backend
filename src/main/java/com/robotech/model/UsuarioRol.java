package com.robotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_usuario_rol")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRol {
    
    @EmbeddedId
    private UsuarioRolId id;
    
    @Column(name = "fecha_asignacion", nullable = false, updatable = false)
    private LocalDateTime fechaAsignacion;
    
    @PrePersist
    protected void onCreate() {
        fechaAsignacion = LocalDateTime.now();
    }
    
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioRolId implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        @Column(name = "id_usuario")
        private Integer idUsuario;
        
        @Column(name = "id_rol")
        private Integer idRol;
    }
}