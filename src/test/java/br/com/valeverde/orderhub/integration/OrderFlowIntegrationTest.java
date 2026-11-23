package br.com.valeverde.orderhub.integration;

import br.com.valeverde.orderhub.domain.model.Customer;
import br.com.valeverde.orderhub.domain.model.CustomerTier;
import br.com.valeverde.orderhub.domain.model.Order;
import br.com.valeverde.orderhub.domain.model.OrderStatus;
import br.com.valeverde.orderhub.domain.model.Product;
import br.com.valeverde.orderhub.exception.InsufficientStockException;
import br.com.valeverde.orderhub.service.OrderService;
import br.com.valeverde.orderhub.support.AbstractIntegrationTest;
import br.com.valeverde.orderhub.web.dto.CreateOrderRequest;
import br.com.valeverde.orderhub.web.dto.OrderItemRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Fluxo completo de pedidos contra o banco real: criação, confirmação (faturamento) e cancelamento.
 */
class OrderFlowIntegrationTest extends AbstractIntegrationTest {

    private static final BigDecimal HIGH_LIMIT = new BigDecimal("100000.00");

    @Autowired
    private OrderService orderService;

    private CreateOrderRequest orderOf(Customer customer, Product product, int quantity, int installments) {
        return new CreateOrderRequest(customer.getId(), null, installments, "teste automatizado",
                List.of(new OrderItemRequest(product.getId(), quantity)));
    }

    @Test
    @DisplayName("Criar pedido reserva estoque e calcula totais")
    void createOrderReservesStockAndCalculatesTotals() {
        Customer customer = newCustomer(CustomerTier.STANDARD, HIGH_LIMIT, null);
        Product product = newProduct(new BigDecimal("100.00"), 50);

        Order order = orderService.create(orderOf(customer, product, 3, 1));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getSubtotal()).isEqualByComparingTo("300.00");
        assertThat(order.getShippingFee()).isEqualByComparingTo("49.90");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("349.90");
        assertThat(stockOf(product)).isEqualTo(47);
    }

    @Test
    @DisplayName("Pedido acima do estoque disponível é recusado sem alterar o estoque")
    void orderAboveStockIsRejected() {
        Customer customer = newCustomer(CustomerTier.STANDARD, HIGH_LIMIT, null);
        Product product = newProduct(new BigDecimal("10.00"), 2);

        assertThatThrownBy(() -> orderService.create(orderOf(customer, product, 3, 1)))
                .isInstanceOf(InsufficientStockException.class);
        assertThat(stockOf(product)).isEqualTo(2);
    }

    @Test
    @DisplayName("Confirmar pedido emite fatura com parcelas que somam o total")
    void confirmOrderIssuesInvoice() {
        Customer customer = newCustomer(CustomerTier.GOLD, HIGH_LIMIT, null);
        Product product = newProduct(new BigDecimal("250.00"), 20);
        Order order = orderService.create(orderOf(customer, product, 4, 2));

        orderService.confirm(order.getId());

        String invoiceStatus = jdbc.queryForObject("SELECT status FROM invoices WHERE order_id = ?", String.class, order.getId());
        BigDecimal installmentsSum = jdbc.queryForObject("""
                SELECT SUM(p.amount) FROM installments p JOIN invoices i ON i.id = p.invoice_id WHERE i.order_id = ?
                """, BigDecimal.class, order.getId());
        assertThat(invoiceStatus).isEqualTo("OPEN");
        assertThat(installmentsSum).isEqualByComparingTo(order.getTotalAmount());
    }

    @Test
    @DisplayName("Cancelar pedido pendente devolve o estoque")
    void cancelPendingOrderRestoresStock() {
        Customer customer = newCustomer(CustomerTier.STANDARD, HIGH_LIMIT, null);
        Product product = newProduct(new BigDecimal("80.00"), 30);
        Order order = orderService.create(orderOf(customer, product, 10, 1));

        orderService.cancel(order.getId(), "cliente desistiu");

        assertThat(stockOf(product)).isEqualTo(30);
    }

    @Test
    @DisplayName("Cancelar pedido confirmado devolve o estoque e cancela a fatura")
    void cancelConfirmedOrderRestoresStockAndCancelsInvoice() {
        Customer customer = newCustomer(CustomerTier.STANDARD, HIGH_LIMIT, null);
        Product product = newProduct(new BigDecimal("80.00"), 30);
        Order order = orderService.create(orderOf(customer, product, 10, 1));
        orderService.confirm(order.getId());

        orderService.cancel(order.getId(), "cliente desistiu após confirmação");

        String invoiceStatus = jdbc.queryForObject("SELECT status FROM invoices WHERE order_id = ?", String.class, order.getId());
        assertThat(invoiceStatus).isEqualTo("CANCELLED");
        assertThat(stockOf(product)).isEqualTo(30);
    }
}
