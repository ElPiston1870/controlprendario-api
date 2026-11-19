package control.prendario.service;

/**
 * Se lanza cuando se intenta registrar un cliente con un número de documento existente (RN-01).
 */
public class DocumentoDuplicadoException extends RuntimeException {
    public DocumentoDuplicadoException(String message) {
        super(message);
    }
}
