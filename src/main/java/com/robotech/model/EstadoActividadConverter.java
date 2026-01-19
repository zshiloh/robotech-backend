package com.robotech.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoActividadConverter implements AttributeConverter<Club.EstadoActividad, String> {
    
    @Override
    public String convertToDatabaseColumn(Club.EstadoActividad attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDescripcion();
    }
    
    @Override
    public Club.EstadoActividad convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        
        switch (dbData) {
            case "Activo":
            case "ACTIVO":
                return Club.EstadoActividad.ACTIVO;
            case "Inactivo":
            case "INACTIVO":
                return Club.EstadoActividad.INACTIVO;
            case "Inactivo_7d":
            case "INACTIVO_7D":
                return Club.EstadoActividad.INACTIVO_7D;
            default:
                throw new IllegalArgumentException("Valor desconocido de estado_actividad: " + dbData);
        }
    }
}