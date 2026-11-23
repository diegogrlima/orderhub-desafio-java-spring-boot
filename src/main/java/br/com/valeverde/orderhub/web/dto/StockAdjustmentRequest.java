package br.com.valeverde.orderhub.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Ajuste manual de estoque (inventário, avaria, recebimento). {@code delta} pode ser negativo.
 */
public record StockAdjustmentRequest(
        @NotNull Integer delta,
        @NotBlank @Size(max = 255) String reason
) {
}
