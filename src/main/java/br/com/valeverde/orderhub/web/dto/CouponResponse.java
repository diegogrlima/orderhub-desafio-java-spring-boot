package br.com.valeverde.orderhub.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CouponResponse(
        Long id,
        String code,
        String description,
        BigDecimal discountPercent,
        LocalDate validFrom,
        LocalDate validUntil,
        Integer maxUses,
        Integer usedCount,
        BigDecimal minOrderAmount,
        boolean active
) {
}
