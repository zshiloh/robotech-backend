package com.robotech.security;

public class SecurityConstants {
    
    public static final String JWT_SECRET = "TuClaveSecretaMuyLargaYSeguraParaJWTDeRobotechSistema2025";
    public static final long JWT_EXPIRATION = 86400000;
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";
    
    public static final String[] PUBLIC_URLS = {
        "/api/auth/**",
        "/api/public/**"
    };
    
    public static final String ROLE_USUARIO = "ROLE_USUARIO";
    public static final String ROLE_COMPETIDOR = "ROLE_COMPETIDOR";
    public static final String ROLE_REPRESENTANTE = "ROLE_REPRESENTANTE";
    public static final String ROLE_JURADO = "ROLE_JURADO";
    public static final String ROLE_ORGANIZADOR = "ROLE_ORGANIZADOR";
    public static final String ROLE_ADMINISTRADOR = "ROLE_ADMINISTRADOR";
    
    private SecurityConstants() {
    	
    }
}