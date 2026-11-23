package br.com.valeverde.orderhub.integration;

import br.com.valeverde.orderhub.domain.model.Customer;
import br.com.valeverde.orderhub.domain.model.CustomerTier;
import br.com.valeverde.orderhub.domain.model.Product;
import br.com.valeverde.orderhub.exception.BusinessException;
import br.com.valeverde.orderhub.service.OrderService;
import br.com.valeverde.orderhub.support.AbstractIntegrationTest;
import br.com.valeverde.orderhub.web.dto.CreateOrderRequest;
import br.com.valeverde.orderhub.web.dto.OrderItemRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simula vários vendedores fechando pedidos do mesmo produto ao mesmo tempo.
 */
class StockConcurrencyIntegrationTest extends AbstractIntegrationTest {

    private static final int INITIAL_STOCK = 5;
    private static final int CONCURRENT_ORDERS = 12;

    @Autowired
    private OrderService orderService;

    @Test
    @DisplayName("Pedidos simultâneos não podem vender mais do que o estoque disponível")
    void concurrentOrdersMustNotOversell() throws Exception {
        Product product = newProduct(new BigDecimal("10.00"), INITIAL_STOCK);
        List<Customer> customers = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_ORDERS; i++) {
            customers.add(newCustomer(CustomerTier.STANDARD, new BigDecimal("100000.00"), null));
        }

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENT_ORDERS);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (Customer customer : customers) {
            futures.add(pool.submit(() -> {
                start.await();
                try {
                    orderService.create(new CreateOrderRequest(customer.getId(), null, 1, null,
                            List.of(new OrderItemRequest(product.getId(), 1))));
                    accepted.incrementAndGet();
                } catch (BusinessException e) {
                    rejected.incrementAndGet();
                }
                return null;
            }));
        }
        start.countDown();
        for (Future<?> f : futures) {
            f.get(60, TimeUnit.SECONDS);
        }
        pool.shutdown();

        Long sold = jdbc.queryForObject("SELECT COALESCE(SUM(quantity), 0) FROM order_items WHERE product_id = ?",
                Long.class, product.getId());
        int finalStock = stockOf(product);

        assertThat(sold).as("unidades vendidas").isLessThanOrEqualTo(INITIAL_STOCK);
        assertThat(finalStock + sold).as("estoque final + vendido deve fechar com o estoque inicial").isEqualTo(INITIAL_STOCK);
        assertThat(accepted.get()).isEqualTo(INITIAL_STOCK);
    }
}
