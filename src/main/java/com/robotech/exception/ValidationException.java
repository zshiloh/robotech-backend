package com.robotech.exception;

public class ValidationException extends RuntimeException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String campo, String mensaje) {
        super(String.format("Error en campo '%s': %s", campo, mensaje));
    }
}