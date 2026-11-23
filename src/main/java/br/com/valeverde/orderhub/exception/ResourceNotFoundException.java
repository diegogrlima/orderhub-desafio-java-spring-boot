package br.com.valeverde.orderhub.exception;

/**
 * Recurso inexistente. Mapeada para HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " não encontrado(a): " + id);
    }
}
