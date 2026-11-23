package br.com.valeverde.orderhub.web.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 30) String sku,
        @NotBlank @Size(max = 150) String name,
        @NotNull Long categoryId,
        @NotBlank @Size(max = 10) String unit,
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal price,
        @NotNull @PositiveOrZero Integer stockQuantity
) {
}
