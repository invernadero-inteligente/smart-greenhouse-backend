package com.invernadero.invernadero_inteligente_backend.shared.exceptions;

import com.invernadero.invernadero_inteligente_backend.shared.responses.ApiResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones de la aplicaciÃ³n
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de recursos no encontrados
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.error(404, ex.getMessage()));
    }

    /**
     * Maneja excepciones de validaciÃ³n
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleValidationException(
            ValidationException ex, WebRequest request) {
        log.warn("Error de validaciÃ³n: {} - {}", ex.getFieldName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error(400, ex.getMessage()));
    }

    /**
     * Maneja excepciones de autorizaciÃ³n
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) {
        log.warn("Usuario no autorizado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDTO.error(401, ex.getMessage()));
    }

    /**
     * Maneja métodos HTTP no soportados para un endpoint
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        log.warn("Método HTTP no permitido: {}", ex.getMethod());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponseDTO.error(405, "Método HTTP no permitido"));
    }

    /**
     * Maneja tipos de contenido no soportados
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, WebRequest request) {
        log.warn("Tipo de contenido no soportado: {}", ex.getContentType());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponseDTO.error(415, "Tipo de contenido no soportado"));
    }

    /**
     * Maneja cuerpos JSON inválidos o ilegibles
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        log.warn("Cuerpo de la petición inválido: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error(400, "El cuerpo de la solicitud no es válido"));
    }

    /**
     * Maneja excepciones de validaciÃ³n de argumentos
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Errores de validaciÃ³n: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error(400, "Error de validaciÃ³n", errors));
    }

    /**
     * Maneja excepciones genÃ©ricas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<?>> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Error no manejado: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.error(500, "Error interno del servidor"));
    }

    /**
     * Maneja excepciones de acceso denegado
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex, WebRequest request) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponseDTO.error(403, "Acceso denegado"));
    }

    /**
     * Maneja parámetros requeridos ausentes en la petición
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, WebRequest request) {
        log.warn("Parámetro faltante: {}", ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error(400, "Falta un parámetro requerido: " + ex.getParameterName()));
    }

    /**
     * Maneja rutas inexistentes
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleNoHandlerFoundException(
            NoHandlerFoundException ex, WebRequest request) {
        log.warn("Ruta no encontrada: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.error(404, "Recurso no encontrado"));
    }
}

