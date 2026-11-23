package br.com.valeverde.orderhub.web.dto;

import java.math.BigDecimal;

public record CreditSummaryResponse(
        Long customerId,
        BigDecimal creditLimit,
        BigDecimal openInstallments,
        BigDecimal pendingOrders,
        BigDecimal exposure,
        BigDecimal availableCredit
) {
}
