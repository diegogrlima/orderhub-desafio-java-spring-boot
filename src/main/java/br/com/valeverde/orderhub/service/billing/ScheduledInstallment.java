package br.com.valeverde.orderhub.service.billing;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ScheduledInstallment(int number, LocalDate dueDate, BigDecimal amount) {
}
