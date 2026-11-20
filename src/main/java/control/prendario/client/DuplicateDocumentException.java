package control.prendario.client;

/**
 * Se lanza cuando se intenta registrar un cliente con un número de documento existente (RN-01).
 */
public class DuplicateDocumentException extends RuntimeException {
    public DuplicateDocumentException(String message) {
        super(message);
    }
}
