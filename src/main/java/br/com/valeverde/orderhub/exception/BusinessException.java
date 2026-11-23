package br.com.valeverde.orderhub.exception;

/**
 * Violação de regra de negócio. Mapeada para HTTP 422 (Unprocessable Entity).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
