package br.com.valeverde.orderhub.web.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CouponRequest(
        @NotBlank @Size(max = 30) @Pattern(regexp = "[A-Z0-9]+", message = "use apenas letras maiúsculas e números") String code,
        @Size(max = 255) String description,
        @NotNull @DecimalMin("0.01") @DecimalMax("50.00") BigDecimal discountPercent,
        @NotNull LocalDate validFrom,
        @NotNull LocalDate validUntil,
        @NotNull @Positive Integer maxUses,
        @PositiveOrZero BigDecimal minOrderAmount
) {
}
