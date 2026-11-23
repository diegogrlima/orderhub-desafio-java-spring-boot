package br.com.valeverde.orderhub.service;

import java.math.BigDecimal;

public record CreditSummary(
        Long customerId,
        BigDecimal creditLimit,
        BigDecimal openInstallments,
        BigDecimal pendingOrders
) {

    public BigDecimal exposure() {
        return openInstallments.add(pendingOrders);
    }

    public BigDecimal availableCredit() {
        return creditLimit.subtract(exposure());
    }
}
