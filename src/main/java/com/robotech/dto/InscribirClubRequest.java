package com.robotech.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscribirClubRequest {
    
    @NotNull(message = "El ID del torneo es obligatorio")
    private Integer idTorneo;
    
    @NotEmpty(message = "Debe seleccionar al menos una categoría")
    private List<InscripcionCategoriaDto> categorias;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InscripcionCategoriaDto {
        @NotNull(message = "El ID de la categoría es obligatorio")
        private Integer idCategoria;
        
        @NotEmpty(message = "Debe seleccionar al menos un competidor")
        private List<Integer> idsCompetidores;
    }
}