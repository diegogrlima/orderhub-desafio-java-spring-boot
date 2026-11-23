package br.com.valeverde.orderhub.web.dto;

import java.math.BigDecimal;

public record TopProductResponse(
        Long productId,
        String sku,
        String name,
        long quantity,
        BigDecimal revenue
) {
}
