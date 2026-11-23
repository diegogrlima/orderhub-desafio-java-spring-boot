package br.com.valeverde.orderhub.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelOrderRequest(@NotBlank @Size(max = 255) String reason) {
}
