package br.com.valeverde.orderhub.web.dto;

import br.com.valeverde.orderhub.domain.model.CustomerTier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String legalName,
        String tradeName,
        String document,
        String email,
        String phone,
        String city,
        String state,
        CustomerTier tier,
        BigDecimal creditLimit,
        Integer billingDay,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
