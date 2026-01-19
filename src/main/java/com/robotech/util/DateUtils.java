package com.robotech.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateUtils {
    
    /**
     * Calcula el trimestre actual en formato "YYYY-QX"
     * Ejemplo: "2025-Q1", "2025-Q2", etc.
     */
    public static String calcularTrimestreActual() {
        LocalDate ahora = LocalDate.now();
        int mes = ahora.getMonthValue();
        int trimestre = (mes - 1) / 3 + 1;
        return ahora.getYear() + "-Q" + trimestre;
    }
    
    /**
     * Verifica si dos fechas están en el mismo trimestre
     */
    public static boolean mismoPeriodoTrimestral(String trimestre1, String trimestre2) {
        return trimestre1 != null && trimestre1.equals(trimestre2);
    }
    
    /**
     * Calcula la diferencia en días entre dos fechas
     */
    public static long diasEntre(LocalDateTime fecha1, LocalDateTime fecha2) {
        if (fecha1 == null || fecha2 == null) {
            return 0;
        }
        return Math.abs(java.time.Duration.between(fecha1, fecha2).toDays());
    }
    
    /**
     * Verifica si han pasado X días desde una fecha
     */
    public static boolean hanPasadoDias(LocalDateTime fechaPasada, int dias) {
        if (fechaPasada == null) {
            return false;
        }
        LocalDateTime limite = fechaPasada.plusDays(dias);
        return LocalDateTime.now().isAfter(limite);
    }
    
    private DateUtils() {
        // Clase de utilidades, no instanciar
    }
}