package br.com.valeverde.orderhub.web.dto;

import java.math.BigDecimal;

public record CustomerRevenueResponse(
        Long customerId,
        String customerName,
        long orderCount,
        BigDecimal revenue,
        BigDecimal received
) {
}
