package br.com.valeverde.orderhub.service;

import br.com.valeverde.orderhub.domain.model.Order;
import br.com.valeverde.orderhub.domain.model.OrderItem;
import br.com.valeverde.orderhub.domain.model.OrderStatus;
import br.com.valeverde.orderhub.domain.model.Product;
import br.com.valeverde.orderhub.domain.repository.OrderItemRepository;
import br.com.valeverde.orderhub.domain.repository.OrderRepository;
import br.com.valeverde.orderhub.exception.BusinessException;
import br.com.valeverde.orderhub.web.dto.CustomerRevenueResponse;
import br.com.valeverde.orderhub.web.dto.DailySalesResponse;
import br.com.valeverde.orderhub.web.dto.SalesReportResponse;
import br.com.valeverde.orderhub.web.dto.TopProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private static final int MAX_RANGE_DAYS = 366;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Vendas do período [from, to] (datas inclusivas), desconsiderando pedidos cancelados.
     */
    public SalesReportResponse salesReport(LocalDate from, LocalDate to) {
        validateRange(from, to);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atStartOfDay();

        List<DailySalesResponse> days = orderRepository.dailySales(start, end).stream()
                .map(row -> new DailySalesResponse(toLocalDate(row[0]), ((Number) row[1]).longValue(), (BigDecimal) row[2]))
                .toList();

        long orderCount = days.stream().mapToLong(DailySalesResponse::orderCount).sum();
        BigDecimal revenue = days.stream().map(DailySalesResponse::revenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        long cancelled = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.CANCELLED, start, end);
        BigDecimal averageTicket = orderCount == 0
                ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP);

        return new SalesReportResponse(from, to, orderCount, cancelled, revenue, averageTicket, days);
    }

    /**
     * Faturamento e recebimento por cliente no período [from, to] (datas inclusivas).
     */
    public List<CustomerRevenueResponse> revenueByCustomer(LocalDate from, LocalDate to) {
        validateRange(from, to);
        return orderRepository.revenueByCustomer(from.atStartOfDay(), to.plusDays(1).atStartOfDay()).stream()
                .map(row -> new CustomerRevenueResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        (BigDecimal) row[3],
                        (BigDecimal) row[4]))
                .toList();
    }

    /**
     * Produtos mais vendidos (em quantidade) no período [from, to], desconsiderando pedidos cancelados.
     */
    public List<TopProductResponse> topProducts(LocalDate from, LocalDate to, int limit) {
        validateRange(from, to);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        Map<Long, ProductTotals> totals = new HashMap<>();
        for (OrderItem item : orderItemRepository.findAll()) {
            Order order = item.getOrder();
            if (order.getStatus() == OrderStatus.CANCELLED) {
                continue;
            }
            if (order.getCreatedAt().isBefore(start) || !order.getCreatedAt().isBefore(end)) {
                continue;
            }
            Product product = item.getProduct();
            totals.computeIfAbsent(product.getId(), id -> new ProductTotals(product.getId(), product.getSku(), product.getName()))
                    .add(item.getQuantity(), item.getLineTotal());
        }

        return totals.values().stream()
                .sorted(Comparator.comparingLong(ProductTotals::quantity).reversed())
                .limit(limit)
                .map(t -> new TopProductResponse(t.productId, t.sku, t.name, t.quantity, t.revenue))
                .toList();
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BusinessException("Informe as datas inicial e final do período");
        }
        if (to.isBefore(from)) {
            throw new BusinessException("Data final anterior à data inicial");
        }
        if (from.plusDays(MAX_RANGE_DAYS).isBefore(to)) {
            throw new BusinessException("Período máximo do relatório é de " + MAX_RANGE_DAYS + " dias");
        }
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    private static final class ProductTotals {
        private final Long productId;
        private final String sku;
        private final String name;
        private long quantity;
        private BigDecimal revenue = BigDecimal.ZERO;

        private ProductTotals(Long productId, String sku, String name) {
            this.productId = productId;
            this.sku = sku;
            this.name = name;
        }

        void add(int qty, BigDecimal value) {
            quantity += qty;
            revenue = revenue.add(value);
        }

        long quantity() {
            return quantity;
        }
    }
}
