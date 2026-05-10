package com.invernadero.invernadero_inteligente_backend.shared.exceptions;

/**
 * ExcepciÃ³n lanzada cuando el usuario no estÃ¡ autorizado
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}

