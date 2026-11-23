package br.com.valeverde.orderhub.web.dto;

import br.com.valeverde.orderhub.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long customerId,
        String customerName,
        OrderStatus status,
        List<OrderItemResponse> items,
        BigDecimal subtotal,
        BigDecimal tierDiscount,
        String couponCode,
        BigDecimal couponDiscount,
        BigDecimal shippingFee,
        BigDecimal totalAmount,
        Integer installments,
        String notes,
        String invoiceNumber,
        LocalDateTime createdAt,
        LocalDateTime confirmedAt,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        LocalDateTime cancelledAt,
        String cancellationReason
) {
}
