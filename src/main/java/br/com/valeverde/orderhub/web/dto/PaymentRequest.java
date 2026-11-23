package br.com.valeverde.orderhub.web.dto;

import br.com.valeverde.orderhub.domain.model.PaymentMethod;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRequest(
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotNull LocalDate paymentDate,
        @NotNull PaymentMethod method
) {
}
