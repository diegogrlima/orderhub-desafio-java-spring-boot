package br.com.valeverde.orderhub.web.dto;

import jakarta.validation.constraints.*;

import java.util.List;

public record CreateOrderRequest(
        @NotNull Long customerId,
        @Size(max = 30) String couponCode,
        @NotNull @Min(1) @Max(6) Integer installments,
        @Size(max = 500) String notes,
        @NotEmpty List<OrderItemRequest> items
) {
}
