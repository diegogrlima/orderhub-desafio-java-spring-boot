package br.com.valeverde.orderhub.web.dto;

import br.com.valeverde.orderhub.domain.model.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InstallmentResponse(
        Long id,
        Integer number,
        LocalDate dueDate,
        BigDecimal amount,
        InstallmentStatus status,
        LocalDate paidAt,
        BigDecimal paidAmount,
        BigDecimal lateFee,
        BigDecimal interest
) {
}
