package com.invernadero.invernadero_inteligente_backend.shared.exceptions;

/**
 * ExcepciÃ³n lanzada cuando hay errores de validaciÃ³n
 */
public class ValidationException extends RuntimeException {

    private String fieldName;
    private String message;

    public ValidationException(String message) {
        super(message);
        this.message = message;
    }

    public ValidationException(String fieldName, String message) {
        super(String.format("Error de validaciÃ³n en %s: %s", fieldName, message));
        this.fieldName = fieldName;
        this.message = message;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

