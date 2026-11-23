package br.com.valeverde.orderhub.web.mapper;

import br.com.valeverde.orderhub.domain.model.Order;
import br.com.valeverde.orderhub.domain.model.OrderItem;
import br.com.valeverde.orderhub.domain.model.Product;
import br.com.valeverde.orderhub.web.dto.OrderItemResponse;
import br.com.valeverde.orderhub.web.dto.OrderResponse;
import br.com.valeverde.orderhub.web.dto.OrderSummaryResponse;

import java.math.BigDecimal;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse(Order o) {
        return new OrderResponse(
                o.getId(),
                o.getCustomer().getId(),
                o.getCustomer().getLegalName(),
                o.getStatus(),
                o.getItems().stream().map(OrderMapper::toItemResponse).toList(),
                o.getSubtotal(),
                o.getTierDiscount(),
                o.getCoupon() == null ? null : o.getCoupon().getCode(),
                o.getCouponDiscount(),
                o.getShippingFee(),
                o.getTotalAmount(),
                o.getInstallments(),
                o.getNotes(),
                o.getInvoice() == null ? null : o.getInvoice().getNumber(),
                o.getCreatedAt(),
                o.getConfirmedAt(),
                o.getShippedAt(),
                o.getDeliveredAt(),
                o.getCancelledAt(),
                o.getCancellationReason());
    }

    public static OrderSummaryResponse toSummary(Order o) {
        return new OrderSummaryResponse(
                o.getId(),
                o.getCustomer().getId(),
                o.getCustomer().getLegalName(),
                o.getStatus(),
                o.getItems().size(),
                o.getTotalAmount(),
                o.getInvoice() == null ? null : o.getInvoice().getNumber(),
                o.getCreatedAt());
    }

    private static OrderItemResponse toItemResponse(OrderItem item) {
        Product product = item.getProduct();
        BigDecimal unitPrice = product.getPrice();
        return new OrderItemResponse(
                item.getId(),
                product.getId(),
                product.getSku(),
                product.getName(),
                item.getQuantity(),
                unitPrice,
                unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
    }
}
