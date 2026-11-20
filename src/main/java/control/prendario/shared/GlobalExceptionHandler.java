package control.prendario.shared;

import control.prendario.client.ClientService;
import control.prendario.client.DuplicateDocumentException;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce los errores de validación y de negocio a respuestas HTTP comprensibles.
 * Los controladores que ya capturan sus propias excepciones conservan su comportamiento.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fields.putIfAbsent(
                        jsonName(ex.getBindingResult().getTarget(), error.getField()), error.getDefaultMessage()));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Datos inválidos");
        body.put("campos", fields);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidBody(HttpMessageNotReadableException ex) {
        return error(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no tiene un formato válido");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidArgument(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DuplicateDocumentException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateDocumentException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({EntityNotFoundException.class, ClientService.ClientNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Devuelve el nombre del campo tal como se expone en JSON (@JsonProperty), para que
     * el detalle de errores use los mismos nombres que recibe y envía la API.
     */
    static String jsonName(Object target, String fieldPath) {
        if (target == null || fieldPath == null) {
            return fieldPath;
        }
        StringBuilder result = new StringBuilder();
        Class<?> type = target.getClass();
        for (String segment : fieldPath.split("\\.")) {
            Field field = findField(type, segment);
            JsonProperty property = field == null ? null : field.getAnnotation(JsonProperty.class);
            if (result.length() > 0) {
                result.append('.');
            }
            result.append(property != null && !property.value().isEmpty() ? property.value() : segment);
            type = field == null ? Object.class : field.getType();
        }
        return result.toString();
    }

    private static Field findField(Class<?> type, String name) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // continuar con la superclase
            }
        }
        return null;
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String messageText) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", messageText);
        return ResponseEntity.status(status).body(body);
    }
}
