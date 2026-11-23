package br.com.valeverde.orderhub.web.dto;

import java.time.LocalDate;

public record OverdueJobResponse(LocalDate referenceDate, int installmentsMarked, int invoicesMarked) {
}
