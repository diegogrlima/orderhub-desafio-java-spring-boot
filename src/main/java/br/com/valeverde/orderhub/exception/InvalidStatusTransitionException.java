package br.com.valeverde.orderhub.exception;

public class InvalidStatusTransitionException extends BusinessException {

    public InvalidStatusTransitionException(String entity, Object current, String action) {
        super("Não é possível " + action + " " + entity + " com status " + current);
    }
}
