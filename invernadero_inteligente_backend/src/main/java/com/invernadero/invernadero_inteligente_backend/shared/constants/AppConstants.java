package com.invernadero.invernadero_inteligente_backend.shared.constants;

/**
 * Constantes de la aplicaciÃ³n
 */
public class AppConstants {

    /**
     * ConfiguraciÃ³n general
     */
    public static final String APP_NAME = "Invernadero Inteligente";
    public static final String APP_VERSION = "1.0.0";
    public static final String API_PREFIX = "/api/v1";

    /**
     * JWT y seguridad
     */
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60 * 1000; // 5 horas

    /**
     * PaginaciÃ³n
     */
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * Mensajes
     */
    public static final String SUCCESS_MESSAGE = "OperaciÃ³n realizada exitosamente";
    public static final String ERROR_MESSAGE = "Error al procesar la solicitud";
    public static final String NOT_FOUND_MESSAGE = "Recurso no encontrado";
    public static final String VALIDATION_ERROR_MESSAGE = "Error de validaciÃ³n";

    /**
     * Ranges de sensores
     */
    public static final double TEMP_MIN = -50.0;
    public static final double TEMP_MAX = 60.0;
    public static final double HUMIDITY_MIN = 0.0;
    public static final double HUMIDITY_MAX = 100.0;
    public static final double PH_MIN = 0.0;
    public static final double PH_MAX = 14.0;

    private AppConstants() {
        // Constructor privado para evitar instanciaciÃ³n
    }
}

