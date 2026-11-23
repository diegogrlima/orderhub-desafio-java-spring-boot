package br.com.valeverde.orderhub.web.dto;

import br.com.valeverde.orderhub.domain.model.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoiceResponse(
        Long id,
        String number,
        Long orderId,
        Long customerId,
        String customerName,
        LocalDate issueDate,
        BigDecimal totalAmount,
        InvoiceStatus status,
        List<InstallmentResponse> installments
) {
}
