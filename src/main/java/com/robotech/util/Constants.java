package com.robotech.util;

public class Constants {

    // Límites de club
    public static final int MAX_COMPETIDORES_POR_CLUB = 6;
    public static final int MAX_INVITACIONES_PENDIENTES = 16;

    // Cambios de club
    public static final int MAX_CAMBIOS_CLUB_TRIMESTRE = 5;
    public static final int DIAS_ESPERA_ENTRE_CAMBIOS = 7;

    // Sistema de inactividad
    public static final int DIAS_INACTIVIDAD_COMPETIDOR = 30;
    public static final int DIAS_INACTIVIDAD_REPRESENTANTE_ALERTA = 7;
    public static final int DIAS_INACTIVIDAD_REPRESENTANTE_CRITICA = 30;
 
    // Sistema de inactividad de usuarios (para admin)
    public static final int DIAS_INACTIVIDAD_USUARIO = 30;

    // Intentos de login
    public static final int MAX_INTENTOS_LOGIN = 5;
    public static final int MINUTOS_BLOQUEO_LOGIN = 15;

    // IDs de Estados (cat_estado)
    public static final Integer ESTADO_PENDIENTE = 1;
    public static final Integer ESTADO_VALIDADO = 2;
    public static final Integer ESTADO_RECHAZADO = 3;
    public static final Integer ESTADO_ELIMINADO = 4;
    public static final Integer ESTADO_ELIMINADO_AUTO = 5;
    public static final Integer ESTADO_ACEPTADA = 6;
    public static final Integer ESTADO_RECHAZADA = 7;
    public static final Integer ESTADO_CONFIGURACION = 8;
    public static final Integer ESTADO_EN_CURSO = 9;
    public static final Integer ESTADO_FINALIZADO = 10;
    public static final Integer ESTADO_ACTIVA = 11;
    public static final Integer ESTADO_INACTIVA = 12;
    public static final Integer ESTADO_CERRADA = 13;
    public static final Integer ESTADO_CANCELADA = 14;
    public static final Integer ESTADO_FINALIZADA = 15;
    public static final Integer ESTADO_SIN_RECLAMAR = 16;

    // IDs de Roles (cat_rol)
    public static final Integer ROL_USUARIO = 1;
    public static final Integer ROL_COMPETIDOR = 2;
    public static final Integer ROL_REPRESENTANTE = 3;
    public static final Integer ROL_JURADO = 4;
    public static final Integer ROL_ORGANIZADOR = 5;
    public static final Integer ROL_ADMINISTRADOR = 6;

    // Tipos de notificaciones
    public static final String NOTIF_NUEVO_CLUB = "NUEVO_CLUB";
    public static final String NOTIF_CLUB_VALIDADO = "CLUB_VALIDADO";
    public static final String NOTIF_CLUB_RECHAZADO = "CLUB_RECHAZADO";
    public static final String NOTIF_CLUB_DISPONIBLE = "CLUB_DISPONIBLE";
    public static final String NOTIF_CLUB_PENDIENTE = "CLUB_PENDIENTE";
    public static final String NOTIF_INVITACION_CLUB = "INVITACION_CLUB";
    public static final String NOTIF_INVITACION_ACEPTADA = "INVITACION_ACEPTADA";
    public static final String NOTIF_SOLICITUD_RECIBIDA = "SOLICITUD_RECIBIDA";
    public static final String NOTIF_SOLICITUD_ACEPTADA = "SOLICITUD_ACEPTADA";
    public static final String NOTIF_SOLICITUD_RECHAZADA = "SOLICITUD_RECHAZADA";
    public static final String NOTIF_EXPULSADO_CLUB = "EXPULSADO_CLUB";
    public static final String NOTIF_COMPETIDOR_INACTIVO_30D = "COMPETIDOR_INACTIVO_30D";
    public static final String NOTIF_REPRESENTANTE_INACTIVO_7D = "REPRESENTANTE_INACTIVO_7D";
    public static final String NOTIF_CLUB_MARCADO_INACTIVO = "CLUB_MARCADO_INACTIVO";
    public static final String NOTIF_CLUB_REACTIVADO = "CLUB_REACTIVADO";
    public static final String NOTIF_RESULTADO_REGISTRADO = "RESULTADO_REGISTRADO";
    public static final String NOTIF_INSCRIPCION_TORNEO = "INSCRIPCION_TORNEO";
    public static final String NOTIF_PASSWORD_CAMBIADA = "PASSWORD_CAMBIADA";
    public static final String NOTIF_TORNEO_INICIADO = "TORNEO_INICIADO";
    public static final String NOTIF_TORNEO_LISTO_FINALIZAR = "TORNEO_LISTO_FINALIZAR";
    public static final String NOTIF_TORNEO_FINALIZADO = "TORNEO_FINALIZADO";
    public static final String NOTIF_CATEGORIA_FINALIZADA = "CATEGORIA_FINALIZADA";

    // Validaciones
    public static final int MIN_NOMBRE_ROBOT = 2;
    public static final int MAX_NOMBRE_ROBOT = 100;
    public static final int MIN_APODO = 2;
    public static final int MAX_APODO = 50;
    public static final int MAX_DESCRIPCION_CLUB = 500;
    public static final int MAX_NOMBRE_CLUB = 100;
    public static final int MIN_NOMBRE_COMPLETO = 3;
    public static final int MAX_NOMBRE_COMPLETO = 100;

    // Preguntas de seguridad predefinidas
    public static final String[] PREGUNTAS_SEGURIDAD = {
        "¿Nombre de tu primera mascota?",
        "¿Ciudad donde naciste?",
        "¿Nombre de tu mejor amigo/a de la infancia?",
        "¿Nombre de tu escuela primaria?",
        "¿Marca de tu primer auto?",
        "¿Apodo de tu infancia?",
        "¿Nombre de tu personaje favorito?",
        "¿Comida favorita?"
    };

    private Constants() {
    }
}