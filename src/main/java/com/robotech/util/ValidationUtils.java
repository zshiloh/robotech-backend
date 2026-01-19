package com.robotech.util;

import com.robotech.exception.ValidationException;

import java.util.regex.Pattern;

public class ValidationUtils {
    
    private static final Pattern PATTERN_TELEFONO_CORTO = Pattern.compile("^9\\d{8}$");
    private static final Pattern PATTERN_TELEFONO_COMPLETO = Pattern.compile("^\\+519\\d{8}$");
    private static final Pattern PATTERN_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PATTERN_PASSWORD = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );
    private static final Pattern PATTERN_NOMBRE_COMPLETO = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$");
    private static final Pattern PATTERN_NOMBRE_ROBOT = Pattern.compile("^[a-zA-Z0-9 -]+$");
    private static final Pattern PATTERN_APODO = Pattern.compile("^[a-zA-Z0-9_]+$");
    
    /**
     * Formatea el teléfono al formato peruano +51XXXXXXXXX
     */
    public static String formatearTelefono(String telefono) {
    	
        String telefonoLimpio = telefono.trim()
            .replaceAll("[\\s\\-()]", "");
        
        if (PATTERN_TELEFONO_COMPLETO.matcher(telefonoLimpio).matches()) {
            return telefonoLimpio;
        }
        
        if (PATTERN_TELEFONO_CORTO.matcher(telefonoLimpio).matches()) {
            return "+51" + telefonoLimpio;
        }
        
        throw new ValidationException("...");
    }
    
    /**
     * Valida el formato de email
     */
    public static void validarEmail(String email) {
        if (email == null || !PATTERN_EMAIL.matcher(email).matches()) {
            throw new ValidationException("Formato de email inválido");
        }
    }
    
    /**
     * Valida el formato de contraseña ANTES de hashear
     */
    public static void validarPassword(String password) {
        if (password == null || !PATTERN_PASSWORD.matcher(password).matches()) {
            throw new ValidationException(
                "La contraseña debe tener al menos 8 caracteres, " +
                "incluyendo mayúsculas, minúsculas, números y caracteres especiales (@$!%*?&)"
            );
        }
    }
    
    /**
     * Valida el nombre completo
     */
    public static void validarNombreCompleto(String nombre) {
        if (nombre == null || nombre.length() < Constants.MIN_NOMBRE_COMPLETO || 
            nombre.length() > Constants.MAX_NOMBRE_COMPLETO) {
            throw new ValidationException("El nombre debe tener entre " + 
                Constants.MIN_NOMBRE_COMPLETO + " y " + Constants.MAX_NOMBRE_COMPLETO + " caracteres");
        }
        
        if (!PATTERN_NOMBRE_COMPLETO.matcher(nombre).matches()) {
            throw new ValidationException("El nombre solo puede contener letras y espacios");
        }
    }
    
    /**
     * Valida el nombre del robot
     */
    public static void validarNombreRobot(String nombreRobot) {
        if (nombreRobot == null || nombreRobot.length() < Constants.MIN_NOMBRE_ROBOT || 
            nombreRobot.length() > Constants.MAX_NOMBRE_ROBOT) {
            throw new ValidationException("El nombre del robot debe tener entre " + 
                Constants.MIN_NOMBRE_ROBOT + " y " + Constants.MAX_NOMBRE_ROBOT + " caracteres");
        }
        
        if (!PATTERN_NOMBRE_ROBOT.matcher(nombreRobot).matches()) {
            throw new ValidationException("El nombre del robot solo puede contener letras, números, espacios y guiones");
        }
    }
    
    /**
     * Valida el apodo
     */
    public static void validarApodo(String apodo) {
        if (apodo != null) {
            if (apodo.length() < Constants.MIN_APODO || apodo.length() > Constants.MAX_APODO) {
                throw new ValidationException("El apodo debe tener entre " + 
                    Constants.MIN_APODO + " y " + Constants.MAX_APODO + " caracteres");
            }
            
            if (!PATTERN_APODO.matcher(apodo).matches()) {
                throw new ValidationException("El apodo solo puede contener letras, números y guiones bajos (sin espacios)");
            }
        }
    }
    
    /**
     * Valida que el cupo mínimo sea PAR
     */
    public static void validarCupoMinimoPar(int cupoMinimo) {
        if (cupoMinimo % 2 != 0) {
            throw new ValidationException("El cupo mínimo debe ser un número PAR");
        }
    }
    
    private ValidationUtils() {
    }
}