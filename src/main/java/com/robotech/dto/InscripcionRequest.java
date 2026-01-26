package com.robotech.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionRequest {
    
    @NotNull(message = "El ID de la categoría es obligatorio")
    private Integer idCategoria;
}