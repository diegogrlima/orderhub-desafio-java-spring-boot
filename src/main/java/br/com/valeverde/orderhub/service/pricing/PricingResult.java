package br.com.valeverde.orderhub.service.pricing;

import java.math.BigDecimal;

public record PricingResult(
        BigDecimal subtotal,
        BigDecimal tierDiscount,
        BigDecimal couponDiscount,
        BigDecimal shippingFee,
        BigDecimal total
) {
}
