package br.com.valeverde.orderhub.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SalesReportResponse(
        LocalDate from,
        LocalDate to,
        long orderCount,
        long cancelledCount,
        BigDecimal grossRevenue,
        BigDecimal averageTicket,
        List<DailySalesResponse> days
) {
}
