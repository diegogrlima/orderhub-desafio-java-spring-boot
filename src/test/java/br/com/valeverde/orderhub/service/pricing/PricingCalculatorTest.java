package br.com.valeverde.orderhub.service.pricing;

import br.com.valeverde.orderhub.domain.model.CustomerTier;
import br.com.valeverde.orderhub.support.TestProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PricingCalculatorTest {

    private final PricingCalculator calculator = new PricingCalculator(TestProperties.defaults());

    @Test
    @DisplayName("Cliente STANDARD abaixo do mínimo paga frete")
    void standardCustomerBelowThresholdPaysShipping() {
        PricingResult result = calculator.calculate(new BigDecimal("300.00"), CustomerTier.STANDARD, null);

        assertThat(result.tierDiscount()).isEqualByComparingTo("0.00");
        assertThat(result.shippingFee()).isEqualByComparingTo("49.90");
        assertThat(result.total()).isEqualByComparingTo("349.90");
    }

    @Test
    @DisplayName("Cliente GOLD recebe 5% de desconto e frete grátis acima do mínimo")
    void goldCustomerGetsFivePercentDiscount() {
        PricingResult result = calculator.calculate(new BigDecimal("1000.00"), CustomerTier.GOLD, null);

        assertThat(result.tierDiscount()).isEqualByComparingTo("50.00");
        assertThat(result.shippingFee()).isEqualByComparingTo("0.00");
        assertThat(result.total()).isEqualByComparingTo("950.00");
    }

    @Test
    @DisplayName("Cliente PLATINUM recebe 8% de desconto")
    void platinumCustomerGetsEightPercentDiscount() {
        PricingResult result = calculator.calculate(new BigDecimal("2500.00"), CustomerTier.PLATINUM, null);

        assertThat(result.tierDiscount()).isEqualByComparingTo("200.00");
        assertThat(result.total()).isEqualByComparingTo("2300.00");
    }

    @Test
    @DisplayName("Pedido com mercadorias exatamente no valor mínimo tem frete grátis")
    void orderExactlyAtThresholdHasFreeShipping() {
        PricingResult result = calculator.calculate(new BigDecimal("500.00"), CustomerTier.STANDARD, null);

        assertThat(result.shippingFee()).isEqualByComparingTo("0.00");
        assertThat(result.total()).isEqualByComparingTo("500.00");
    }
}
