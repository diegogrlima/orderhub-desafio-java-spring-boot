package br.com.valeverde.orderhub.exception;

import java.math.BigDecimal;

public class CreditLimitExceededException extends BusinessException {

    public CreditLimitExceededException(BigDecimal available, BigDecimal requested) {
        super("Limite de crédito insuficiente: disponível R$ " + available + ", necessário R$ " + requested);
    }
}
