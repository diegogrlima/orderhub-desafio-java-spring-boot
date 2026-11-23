package br.com.valeverde.orderhub.support;

import br.com.valeverde.orderhub.config.BusinessProperties;

import java.math.BigDecimal;

public final class TestProperties {

    private TestProperties() {
    }

    /** Mesmos valores do application.yml. */
    public static BusinessProperties defaults() {
        return new BusinessProperties(
                new BigDecimal("500.00"),
                new BigDecimal("49.90"),
                new BigDecimal("2.00"),
                new BigDecimal("1.00"),
                6,
                "America/Sao_Paulo");
    }
}
