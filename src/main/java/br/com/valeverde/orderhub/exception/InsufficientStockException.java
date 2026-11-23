package br.com.valeverde.orderhub.exception;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String sku, int available, int requested) {
        super("Estoque insuficiente para o produto " + sku + ": disponível " + available + ", solicitado " + requested);
    }
}
