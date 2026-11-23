package br.com.valeverde.orderhub.web.dto;

import br.com.valeverde.orderhub.domain.model.InstallmentStatus;
import br.com.valeverde.orderhub.domain.model.InvoiceStatus;
import br.com.valeverde.orderhub.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(
        Long id,
        Long installmentId,
        BigDecimal amount,
        LocalDate paymentDate,
        PaymentMethod method,
        String registeredBy,
        InstallmentStatus installmentStatus,
        InvoiceStatus invoiceStatus
) {
}
