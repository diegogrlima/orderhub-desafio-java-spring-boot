package br.com.valeverde.orderhub.web.dto;

import br.com.valeverde.orderhub.domain.model.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceSummaryResponse(
        Long id,
        String number,
        Long orderId,
        Long customerId,
        LocalDate issueDate,
        BigDecimal totalAmount,
        InvoiceStatus status
) {
}
