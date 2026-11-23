package br.com.valeverde.orderhub.web.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Atualização de catálogo. SKU e estoque não são alterados por este recurso
 * (estoque possui endpoint próprio de ajuste).
 */
public record ProductUpdateRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull Long categoryId,
        @NotBlank @Size(max = 10) String unit,
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal price,
        @NotNull Boolean active
) {
}
