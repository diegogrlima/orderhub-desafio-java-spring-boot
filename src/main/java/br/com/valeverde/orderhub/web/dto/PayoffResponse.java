package br.com.valeverde.orderhub.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayoffResponse(
        Long installmentId,
        LocalDate dueDate,
        LocalDate paymentDate,
        BigDecimal amount,
        int daysLate,
        BigDecimal lateFee,
        BigDecimal interest,
        BigDecimal total
) {
}
