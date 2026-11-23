package br.com.valeverde.orderhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Parâmetros comerciais e financeiros configuráveis (application.yml, prefixo {@code orderhub.business}).
 */
@ConfigurationProperties(prefix = "orderhub.business")
public record BusinessProperties(
        BigDecimal freeShippingThreshold,
        BigDecimal shippingFee,
        BigDecimal lateFeePercent,
        BigDecimal monthlyInterestPercent,
        int maxInstallments,
        String timezone
) {
}
