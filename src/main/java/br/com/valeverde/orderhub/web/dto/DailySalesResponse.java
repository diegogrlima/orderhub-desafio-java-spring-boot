package br.com.valeverde.orderhub.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySalesResponse(LocalDate date, long orderCount, BigDecimal revenue) {
}
