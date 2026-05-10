package com.invernadero.invernadero_inteligente_backend.shared.utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilidad para construir respuestas estandarizadas de la API
 */
public class ResponseUtil {

    private ResponseUtil() {
        // Constructor privado para evitar instanciaciÃ³n
    }

    public static Map<String, Object> success(Object data) {
        return buildResponse(true, 200, "OperaciÃ³n realizada exitosamente", data);
    }

    public static Map<String, Object> success(String message, Object data) {
        return buildResponse(true, 200, message, data);
    }

    public static Map<String, Object> error(int status, String message) {
        return buildResponse(false, status, message, null);
    }

    public static Map<String, Object> error(int status, String message, Object errors) {
        Map<String, Object> response = buildResponse(false, status, message, null);
        response.put("errors", errors);
        return response;
    }

    public static Map<String, Object> notFound(String message) {
        return buildResponse(false, 404, message, null);
    }

    public static Map<String, Object> unauthorized(String message) {
        return buildResponse(false, 401, message, null);
    }

    public static Map<String, Object> forbidden(String message) {
        return buildResponse(false, 403, message, null);
    }

    private static Map<String, Object> buildResponse(boolean success, int status, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("status", status);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }
}

