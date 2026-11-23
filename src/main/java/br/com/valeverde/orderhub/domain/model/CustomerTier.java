package br.com.valeverde.orderhub.domain.model;

import java.math.BigDecimal;

/**
 * Categoria comercial do cliente. Define o desconto aplicado sobre o subtotal do pedido.
 */
public enum CustomerTier {
    STANDARD(new BigDecimal("0.00")),
    SILVER(new BigDecimal("3.00")),
    GOLD(new BigDecimal("5.00")),
    PLATINUM(new BigDecimal("8.00"));

    private final BigDecimal discountPercent;

    CustomerTier(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }
}
