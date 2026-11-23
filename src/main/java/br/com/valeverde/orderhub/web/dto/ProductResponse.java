package br.com.valeverde.orderhub.web.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        Long categoryId,
        String categoryName,
        String unit,
        BigDecimal price,
        Integer stockQuantity,
        boolean active
) {
}
