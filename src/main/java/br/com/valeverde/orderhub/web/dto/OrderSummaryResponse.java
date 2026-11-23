package br.com.valeverde.orderhub.web.dto;

import br.com.valeverde.orderhub.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponse(
        Long id,
        Long customerId,
        String customerName,
        OrderStatus status,
        int itemCount,
        BigDecimal totalAmount,
        String invoiceNumber,
        LocalDateTime createdAt
) {
}
