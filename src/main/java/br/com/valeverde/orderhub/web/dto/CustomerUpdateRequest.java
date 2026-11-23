package br.com.valeverde.orderhub.web.dto;

import br.com.valeverde.orderhub.domain.model.CustomerTier;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Atualização cadastral. O CNPJ não pode ser alterado.
 */
public record CustomerUpdateRequest(
        @NotBlank @Size(max = 150) String legalName,
        @Size(max = 150) String tradeName,
        @NotBlank @Email @Size(max = 120) String email,
        @Size(max = 20) String phone,
        @NotBlank @Size(max = 80) String city,
        @NotBlank @Pattern(regexp = "[A-Z]{2}", message = "deve conter a sigla da UF com 2 letras maiúsculas") String state,
        @NotNull CustomerTier tier,
        @NotNull @PositiveOrZero BigDecimal creditLimit,
        @Min(1) @Max(31) Integer billingDay
) {
}
