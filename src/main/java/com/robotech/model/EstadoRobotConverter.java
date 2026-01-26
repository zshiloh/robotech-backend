package com.robotech.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoRobotConverter implements AttributeConverter<Robot.EstadoRobot, String> {
    
    @Override
    public String convertToDatabaseColumn(Robot.EstadoRobot attribute) {
        if (attribute == null) {
            return null;
        }
        
        switch (attribute) {
            case ACTIVO:
                return "Activo";
            case INACTIVO:
                return "Inactivo";
            case INACTIVO_AUTO:
                return "Inactivo_Auto";
            default:
                throw new IllegalArgumentException("Valor desconocido: " + attribute);
        }
    }
    
    @Override
    public Robot.EstadoRobot convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        
        switch (dbData) {
            case "Activo":
            case "ACTIVO":
                return Robot.EstadoRobot.ACTIVO;
            case "Inactivo":
            case "INACTIVO":
                return Robot.EstadoRobot.INACTIVO;
            case "Inactivo_Auto":
            case "INACTIVO_AUTO":
                return Robot.EstadoRobot.INACTIVO_AUTO;
            default:
                throw new IllegalArgumentException("Valor desconocido de estado robot: " + dbData);
        }
    }
}