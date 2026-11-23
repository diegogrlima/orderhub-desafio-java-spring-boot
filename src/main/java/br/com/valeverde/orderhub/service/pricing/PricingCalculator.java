package br.com.valeverde.orderhub.service.pricing;

import br.com.valeverde.orderhub.config.BusinessProperties;
import br.com.valeverde.orderhub.domain.model.CustomerTier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calcula descontos, frete e total de um pedido.
 */
@Component
@RequiredArgsConstructor
public class PricingCalculator {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final BusinessProperties properties;

    public PricingResult calculate(BigDecimal subtotal, CustomerTier tier, BigDecimal couponPercent) {
        BigDecimal tierDiscount = percentOf(subtotal, tier.getDiscountPercent());

        BigDecimal couponDiscount = BigDecimal.ZERO.setScale(2);
        if (couponPercent != null) {
            couponDiscount = percentOf(subtotal, couponPercent);
        }

        BigDecimal merchandise = subtotal.subtract(tierDiscount).subtract(couponDiscount);

        BigDecimal shipping = merchandise.compareTo(properties.freeShippingThreshold()) >= 0
                ? BigDecimal.ZERO.setScale(2)
                : properties.shippingFee().setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = merchandise.add(shipping).setScale(2, RoundingMode.HALF_UP);
        return new PricingResult(subtotal.setScale(2, RoundingMode.HALF_UP), tierDiscount, couponDiscount, shipping, total);
    }

    private BigDecimal percentOf(BigDecimal base, BigDecimal percent) {
        return base.multiply(percent).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }
}
