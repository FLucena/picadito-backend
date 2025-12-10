package com.techlab.picadito.exception;

import com.techlab.picadito.dto.ErrorResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    
    private final Environment environment;
    
    public GlobalExceptionHandler(Environment environment) {
        this.environment = environment;
    }
    
    /**
     * Verifica si estamos en producción
     */
    private boolean isProduction() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if (profile.contains("prod") || profile.contains("production")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Sanitiza un mensaje de error para no exponer información sensible en producción
     */
    private String sanitizeErrorMessage(String message, String defaultMessage) {
        if (isProduction()) {
            // En producción, usar mensaje genérico
            return defaultMessage;
        }
        // En desarrollo, mostrar el mensaje completo
        return message;
    }

    private ErrorResponseDTO buildErrorResponse(int status, String error, String message, String path) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(status, error, message, path);
        errorResponse.setCorrelationId(MDC.get(CORRELATION_ID_MDC_KEY));
        return errorResponse;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        logger.warn("Resource not found: {}", ex.getMessage());
        ErrorResponseDTO error = buildErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(
            BusinessException ex, WebRequest request) {
        logger.warn("Business exception: {}", ex.getMessage());
        ErrorResponseDTO error = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Business Error",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            ValidationException ex, WebRequest request) {
        logger.warn("Validation exception: {}", ex.getMessage());
        ErrorResponseDTO error = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        logger.warn("Validation errors in request: {}", ex.getBindingResult().getAllErrors());
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponseDTO error = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "Error de validación en los campos enviados",
                request.getDescription(false).replace("uri=", "")
        );
        error.setValidationErrors(validationErrors);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        logger.warn("Illegal argument: {}", ex.getMessage());
        String message = sanitizeErrorMessage(
            ex.getMessage(),
            "El valor proporcionado no es válido"
        );
        ErrorResponseDTO error = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        logger.warn("Method argument type mismatch: {}", ex.getMessage());
        String message;
        if (isProduction()) {
            message = String.format("El parámetro '%s' tiene un valor inválido. Se esperaba un tipo válido.", ex.getName());
        } else {
            message = String.format("El parámetro '%s' tiene un valor inválido: '%s'. Se esperaba un tipo válido.",
                    ex.getName(), ex.getValue());
        }
        ErrorResponseDTO error = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponseDTO> handleOptimisticLockException(
            ObjectOptimisticLockingFailureException ex, WebRequest request) {
        logger.warn("Optimistic lock exception: {}", ex.getMessage());
        ErrorResponseDTO error = buildErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "El recurso ha sido modificado por otro usuario. Por favor, recarga la página e intenta nuevamente.",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {
        // Log completo del error para debugging
        logger.warn("Data integrity violation: {}", ex.getMessage());
        Throwable rootCause = ex.getRootCause();
        if (rootCause != null && rootCause.getMessage() != null) {
            logger.warn("Root cause: {}", rootCause.getMessage());
        }
        
        String message;
        if (isProduction()) {
            // Mensaje genérico en producción
            message = "No se puede realizar la operación debido a restricciones de integridad referencial. " +
                     "Existen registros relacionados que impiden esta acción.";
        } else {
            // Mensaje detallado en desarrollo
            String rootCauseMessage = "";
            if (rootCause != null && rootCause.getMessage() != null) {
                rootCauseMessage = rootCause.getMessage();
            }
            
            message = "No se puede eliminar el recurso debido a restricciones de integridad referencial. ";
            String lowerRootCause = rootCauseMessage.toLowerCase();
            if (lowerRootCause.contains("partido")) {
                message += "El partido tiene relaciones asociadas (participantes, reservas, equipos, calificaciones, partidos guardados o seleccionados) que impiden su eliminación.";
            } else if (lowerRootCause.contains("foreign key")) {
                message += "Existen registros relacionados que impiden la eliminación.";
            } else {
                message += "Hay datos relacionados que deben eliminarse primero.";
            }
        }
        
        ErrorResponseDTO error = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Data Integrity Error",
                message,
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        // Log completo para debugging
        logger.warn("Authentication exception: {}", ex.getMessage());
        // Siempre usar mensaje genérico para no revelar información sobre usuarios existentes
        String message = "Credenciales inválidas";
        ErrorResponseDTO error = buildErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                message,
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(
            Exception ex, WebRequest request) {
        // Log completo del error con stack trace para debugging
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        // Mensaje genérico para el usuario
        String message = "Ha ocurrido un error inesperado. Por favor, intente más tarde.";
        
        // En desarrollo, agregar más detalles si es necesario
        if (!isProduction() && ex.getMessage() != null && !ex.getMessage().isEmpty()) {
            // Solo mostrar el mensaje básico, no el stack trace completo
            logger.debug("Error details: {}", ex.getMessage());
        }
        
        ErrorResponseDTO error = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                message,
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

