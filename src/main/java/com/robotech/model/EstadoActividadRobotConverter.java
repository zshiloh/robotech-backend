package com.robotech.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoActividadRobotConverter implements AttributeConverter<Robot.EstadoActividadRobot, String> {
    
    @Override
    public String convertToDatabaseColumn(Robot.EstadoActividadRobot attribute) {
        if (attribute == null) {
            return null;
        }
        
        switch (attribute) {
            case ACTIVO:
                return "Activo";
            case INACTIVO_30D:
                return "Inactivo_30d";
            default:
                throw new IllegalArgumentException("Valor desconocido: " + attribute);
        }
    }
    
    @Override
    public Robot.EstadoActividadRobot convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        
        switch (dbData) {
            case "Activo":
            case "ACTIVO":
                return Robot.EstadoActividadRobot.ACTIVO;
            case "Inactivo_30d":
            case "INACTIVO_30D":
                return Robot.EstadoActividadRobot.INACTIVO_30D;
            default:
                throw new IllegalArgumentException("Valor desconocido de estado actividad robot: " + dbData);
        }
    }
}